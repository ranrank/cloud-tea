package com.cloudtea.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.cloudtea.constant.MessageConstant;
import com.cloudtea.context.BaseContext;
import com.cloudtea.dto.*;
import com.cloudtea.entity.AddressBook;
import com.cloudtea.entity.OrderDetail;
import com.cloudtea.entity.Orders;
import com.cloudtea.entity.ShoppingCart;
import com.cloudtea.entity.User;
import com.cloudtea.exception.AddressBookBusinessException;
import com.cloudtea.exception.OrderBusinessException;
import com.cloudtea.exception.ShoppingCartBusinessException;
import com.cloudtea.mapper.AddressBookMapper;
import com.cloudtea.mapper.OrderDetailMapper;
import com.cloudtea.mapper.OrderMapper;
import com.cloudtea.mapper.ShoppingCartMapper;
import com.cloudtea.mapper.UserMapper;
import com.cloudtea.result.PageResult;
import com.cloudtea.service.OrderService;
import com.cloudtea.utils.HttpClientUtil;
import com.cloudtea.utils.WeChatPayUtil;
import com.cloudtea.vo.OrderPaymentVO;
import com.cloudtea.vo.OrderStatisticsVO;
import com.cloudtea.vo.OrderSubmitVO;
import com.cloudtea.vo.OrderVO;
import com.cloudtea.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    /**
     * 最大配送距离，单位：米。
     * 值配在 application-dev.yml 的 cloudtea.store.max-delivery-distance，想调配送范围只改配置就行。
     */
    @Value("${cloudtea.store.max-delivery-distance:50000}")
    private int maxDeliveryDistance;

    @Value("${cloudtea.store.address}")
    private String storeAddress;

    @Value("${cloudtea.baidu.ak}")
    private String ak;

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private WebSocketServer webSocketServer;

    /**
     * 订单支付
     * @param ordersPaymentDTO 订单支付DTO
     * @return 预支付交易单参数
     */
    @Transactional
    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        //根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //已支付的订单不能重复支付
        if (Orders.PAID.equals(ordersDB.getPayStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_PAID);
        }

        //当前登录用户
        User user = userMapper.getById(BaseContext.getCurrentId());

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                ordersDB.getAmount(),              //支付金额，单位 元
                "云栖茶饮订单",                      //商品描述
                user == null ? null : user.getOpenid() //微信用户的openid
        );

        if ("ORDERPAID".equals(jsonObject.getString("code"))) {
            throw new OrderBusinessException(MessageConstant.ORDER_PAID);
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //== 模拟支付：微信商户号申请不到，这里不等回调，直接按“用户已支付”处理 ==
        //真实项目这一步是微信异步通知 /notify/paySuccess 触发的，拿到商户号后把这两行交给 PayNotifyController 即可
        paySuccess(ordersPaymentDTO.getOrderNumber(), ordersPaymentDTO.getPayMethod());

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo 商户订单号
     * @param payMethod 付款方式
     */
    @Override
    public void paySuccess(String outTradeNo, Integer payMethod) {
        //根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .payMethod(payMethod)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
        log.info("订单支付成功：订单号={}，订单id={}", outTradeNo, ordersDB.getId());
        //痛过WebSocker向客户端浏览器推送消息type orderId content
        Map map=new HashMap<>();
        map.put("type",1);//1.来单提醒 2.客户催单
        map.put("orderId",ordersDB.getId());
        //堂食单带上取餐号，管理端弹窗就能直接叫号
        map.put("content",Orders.DINE_IN.equals(ordersDB.getOrderType())
                ? "堂食 "+ordersDB.getPickupNo()+"，订单号："+outTradeNo
                : "订单号："+outTradeNo);
        String json = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /**
     * 提交订单
     * @param ordersSubmitDTO 订单提交DTO
     * @return 订单提交VO
     */
    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //就餐方式，不传按外卖处理（兼容没有这个字段的老前端）
        Integer orderType = ordersSubmitDTO.getOrderType() == null ? Orders.DELIVERY : ordersSubmitDTO.getOrderType();
        boolean dineIn = Orders.DINE_IN.equals(orderType);

        //外卖要有收货地址，并校验收货地址是否超出配送范围；堂食到店取，这两步都跳过
        AddressBook addressBook = null;
        if (!dineIn) {
            addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
            if (addressBook == null) {
                throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
            }
            //校验收货地址是否超出配送范围
            String address = addressBook.getProvinceName() + addressBook.getCityName()
                    + addressBook.getDistrictName() + addressBook.getDetail();
            checkOutOfRange(address);
        }
        //查询当前用户的购物车数据
        ShoppingCart shoppingCart=new ShoppingCart();
        Long userId= BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList=shoppingCartMapper.list(shoppingCart);
        if(shoppingCartList==null||shoppingCartList.size()==0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //向订单表插入一条数据
        Orders orders=new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        //补充订单号、状态、时间、用户等字段（必须在 insert 之前设置，否则这些列是 NULL）
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setOrderType(orderType);
        orders.setUserId(userId);
        if (dineIn) {
            //堂食没有收货地址、也没有送达时间，只有取餐号
            orders.setAddressBookId(null);
            orders.setAddress(null);
            orders.setConsignee(null);
            orders.setPhone(null);
            orders.setEstimatedDeliveryTime(null);
            orders.setPickupNo(nextPickupNo());
        } else {
            orders.setPhone(addressBook.getPhone());
            orders.setConsignee(addressBook.getConsignee());
            //收货地址（OrdersSubmitDTO 里没有 address 字段，copyProperties 补不上，必须手动设置）
            orders.setAddress(addressBook.getDetail());
        }
        //插入订单
        orderMapper.insert(orders);
        //向订单明细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);
        //清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);
        //封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;


    }

    /**
     * 生成堂食取餐号，形如 A018：当天已有的堂食单数 + 1。
     * 用订单表按天计数而不是单独维护计数器，重启、多实例都不会把号发重；
     * 取消掉的堂食单也占号、不回收，和门店真实叫号习惯一致，隔天自然从 A001 重新开始。
     */
    private String nextPickupNo() {
        Map<String, Object> map = new HashMap<>();
        map.put("orderType", Orders.DINE_IN);
        LocalDate today = LocalDate.now();
        map.put("begin", LocalDateTime.of(today, LocalTime.MIN));
        map.put("end", LocalDateTime.of(today, LocalTime.MAX));
        Integer todayDineInCount = orderMapper.countByMap(map);
        return String.format("A%03d", (todayDineInCount == null ? 0 : todayDineInCount) + 1);
    }

    @Override
    public PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO) {
        //设置分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Long currentId = BaseContext.getCurrentId();
        ordersPageQueryDTO.setUserId(currentId);
        //查询订单数据
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        //封装VO返回结果
        List<OrderVO> list=new ArrayList<>();
        if(page!=null&&page.size()>0){
            for (Orders orders : page) {
                Long orderId=orders.getId();
                List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(orderId);
                OrderVO orderVO=new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(),list);
    }
    /**
     * 订单详情查询
     * @param id 订单ID
     * @return 订单详情VO
     */
    @Override
    public OrderVO details(Long id) {
        Orders orders=orderMapper.getById(id);
        List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(id);
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }
    @Transactional
    @Override
    public void userCancelById(Long id) throws Exception {
        //根据id查询订单
        Orders ordersDB=orderMapper.getById(id);
        //判断订单是否存在
        if(ordersDB==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //订单状态 1 待支付 2 待接单 3 制作中 4 配送中 5 已完成 6 已取消
        if(ordersDB.getStatus()>2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders=new Orders();
        orders.setId(ordersDB.getId());
        if(ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            //待接单说明用户已经付过款，调用微信支付退款接口
            weChatPayUtil.refund(ordersDB.getNumber(), ordersDB.getNumber(), ordersDB.getAmount(), ordersDB.getAmount());
            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }
        // 更新订单状态、取消原因、取消时间
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void repetition(Long id) {
        // 查询当前用户id
        Long userId = BaseContext.getCurrentId();

        // 根据订单id查询当前订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // 将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的饮品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        // 将购物车对象批量添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
    }
    /**
     * 订单条件查询
     * @param ordersPageQueryDTO 订单分页查询DTO
     * @return 订单分页查询结果
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //设置分页参数
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        //查询订单数据
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);
        //封装VO返回结果
        List<OrderVO> list=getOrderVOList(page);
        return new PageResult(page.getTotal(),list);
    }
    /**
     * 订单状态统计
     * @return 订单状态统计VO
     */
    @Override
    public OrderStatisticsVO statistics() {
        // 根据状态，分别查询出待接单、待配送、配送中的订单数量
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        // 将查询出的数据封装到orderStatisticsVO中响应
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        //根据id查询订单
        Orders ordersDB = orderMapper.getById(ordersConfirmDTO.getId());
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //只有状态为待接单的订单才能接单
        if (!Orders.TO_BE_CONFIRMED.equals(ordersDB.getStatus())) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        //根据id查询订单
        Orders ordersDB=orderMapper.getById(ordersRejectionDTO.getId());
        if(ordersDB==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //只有状态为待接单的订单才能拒单
        if(!ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 更新订单状态、取消原因、取消时间
        Orders orders=new Orders();
        orders.setId(ordersDB.getId());
        //已支付的订单需要退款
        if(Orders.PAID.equals(ordersDB.getPayStatus())){
            weChatPayUtil.refund(ordersDB.getNumber(), ordersDB.getNumber(), ordersDB.getAmount(), ordersDB.getAmount());
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);

    }
    /**
     * 取消订单
     * @param ordersCancelDTO 订单取消DTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        //根据id查询订单
        Orders ordersDB=orderMapper.getById(ordersCancelDTO.getId());
        if(ordersDB==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //已完成或已取消的订单不能再取消
        if(Orders.COMPLETED.equals(ordersDB.getStatus())||Orders.CANCELLED.equals(ordersDB.getStatus())){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        // 管理端取消订单需要退款，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        //已支付的订单需要退款
        if(Orders.PAID.equals(ordersDB.getPayStatus())){
            weChatPayUtil.refund(ordersDB.getNumber(), ordersDB.getNumber(), ordersDB.getAmount(), ordersDB.getAmount());
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }
    /**
     * 配送订单
     * @param id 订单ID
     */
    @Override
    public void delivery(Long id) {
       //根据id查询订单
       Orders ordersDB=orderMapper.getById(id);
       //只有状态为待配送（制作中）的订单才能配送
       if(ordersDB==null||!ordersDB.getStatus().equals(Orders.CONFIRMED)){
           throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
       }
       //堂食到店自取，没有配送环节，不允许推进到「配送中」，只能直接完成
       if(Orders.DINE_IN.equals(ordersDB.getOrderType())){
           throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
       }
       // 更新订单状态、配送时间
       Orders orders=new Orders();
       orders.setId(ordersDB.getId());
       orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
       orders.setDeliveryTime(LocalDateTime.now());
       orderMapper.update(orders);
    }

    @Override
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getById(id);

        // 校验订单是否存在：外卖要处于配送中，堂食没有配送环节、制作中即可完成
        boolean deliveryDone = ordersDB != null && ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS);
        boolean dineInDone = ordersDB != null && Orders.DINE_IN.equals(ordersDB.getOrderType())
                && ordersDB.getStatus().equals(Orders.CONFIRMED);
        if (!deliveryDone && !dineInDone) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        // 外卖这里是送达时间，堂食是取餐时间，两种方式共用这一个字段
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    @Override
    public void reminder(Long id) {
        //根据id查询订单
        Orders ordersDB=orderMapper.getById(id);
        //订单不存在
        if(ordersDB==null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Map map = new HashMap<>();
        map.put("type",2);//1.来单提醒 2.客户催单
        map.put("orderId",id);
        //堂食单带上取餐号，管理端弹窗就能直接叫号
        map.put("content",Orders.DINE_IN.equals(ordersDB.getOrderType())
                ? "堂食 "+ordersDB.getPickupNo()+"，订单号："+ordersDB.getNumber()
                : "订单号："+ordersDB.getNumber());
        // 发送消息到所有客户端（用注入的字段，不能用类名调用）
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    /**
     * 检查客户的收货地址是否超出配送范围
     *
     * @param address 用户收货地址（省市区 + 详细地址）
     */
    private void checkOutOfRange(String address) {
        Map<String, String> map = new HashMap<>();
        map.put("address", storeAddress);
        map.put("output", "json");
        map.put("ak", ak);

        //获取门店的经纬度坐标
        String storeCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        JSONObject jsonObject = JSON.parseObject(storeCoordinate);
        if (jsonObject == null || !"0".equals(jsonObject.getString("status"))) {
            throw new OrderBusinessException(MessageConstant.STORE_ADDRESS_PARSE_FAILED);
        }

        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //门店经纬度坐标
        String storeLngLat = lat + "," + lng;

        map.put("address", address);
        //获取用户收货地址的经纬度坐标
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        jsonObject = JSON.parseObject(userCoordinate);
        if (jsonObject == null || !"0".equals(jsonObject.getString("status"))) {
            throw new OrderBusinessException(MessageConstant.USER_ADDRESS_PARSE_FAILED);
        }

        //数据解析
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        lat = location.getString("lat");
        lng = location.getString("lng");
        //用户收货地址经纬度坐标
        String userLngLat = lat + "," + lng;

        map.put("origin", storeLngLat);
        map.put("destination", userLngLat);
        map.put("steps_info", "0");

        //路线规划
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);
        jsonObject = JSON.parseObject(json);
        if (jsonObject == null || !"0".equals(jsonObject.getString("status"))) {
            throw new OrderBusinessException(MessageConstant.ROUTE_PLAN_FAILED);
        }

        //数据解析
        JSONArray jsonArray = jsonObject.getJSONObject("result").getJSONArray("routes");
        Integer distance = jsonArray.getJSONObject(0).getInteger("distance");

        if (distance != null && distance > maxDeliveryDistance) {
            //配送距离超过最大配送距离
            throw new OrderBusinessException(MessageConstant.OUT_OF_DELIVERY_RANGE);
        }
    }

    private List<OrderVO> getOrderVOList(Page<Orders> page) {
        // 需要返回订单饮品信息，自定义OrderVO响应结果
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if (!CollectionUtils.isEmpty(ordersList)) {
            for (Orders orders : ordersList) {
                // 将共同字段复制到OrderVO
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                String orderItems = getOrderItemsStr(orders);

                // 将订单饮品信息封装到orderVO中，并添加到orderVOList
                orderVO.setOrderItems(orderItems);
                orderVOList.add(orderVO);
            }
        }
        return orderVOList;
    }

    /**
     * 根据订单id获取饮品信息字符串
     *
     * @param orders
     * @return
     */
    private String getOrderItemsStr(Orders orders) {
        // 查询订单饮品详情信息（订单中的饮品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // 将每一条订单饮品信息拼接为字符串（格式：云顶乌龙*3；）
        List<String> orderItemList = orderDetailList.stream().map(x -> {
            String orderItem = x.getName() + "*" + x.getNumber() + ";";
            return orderItem;
        }).collect(Collectors.toList());

        // 将该订单对应的所有饮品信息拼接在一起
        return String.join("", orderItemList);
    }
}
