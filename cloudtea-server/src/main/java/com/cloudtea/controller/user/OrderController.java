package com.cloudtea.controller.user;

import com.cloudtea.dto.OrdersPageQueryDTO;
import com.cloudtea.dto.OrdersPaymentDTO;
import com.cloudtea.dto.OrdersSubmitDTO;
import com.cloudtea.result.PageResult;
import com.cloudtea.result.Result;
import com.cloudtea.service.OrderService;
import com.cloudtea.vo.OrderPaymentVO;
import com.cloudtea.vo.OrderSubmitVO;
import com.cloudtea.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "订单相关接口")
@Slf4j
public class OrderController {
    /**
     * 提交订单
     * @param ordersSubmitDTO 订单提交DTO
     * @return 订单提交VO
     */
    @Autowired
    private OrderService orderService;
    @PostMapping("/submit")
    @ApiOperation("提交订单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("提交订单，订单信息：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }
    /**
     * 订单支付
     * @param ordersPaymentDTO 订单支付DTO
     * @return 预支付交易单参数
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO)throws Exception{
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }
    /**
     * 历史订单查询
     * @param ordersPageQueryDTO 订单分页查询DTO
     * @return 订单分页查询结果
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("历史订单查询：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQuery(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 订单详情查询
     * @param id 订单ID
     * @return 订单详情VO
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("订单详情查询")
    public Result<OrderVO> orderDetail(@PathVariable Long id){
        log.info("订单详情查询：{}", id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }
    /**
     * 订单取消
     * @param id 订单ID
     * @return 订单取消结果
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("订单取消")
    public Result<Void> cancel(@PathVariable Long id)throws Exception{
        log.info("订单取消：{}", id);
        orderService.userCancelById(id);
        return Result.success();
    }
    /**
     * 再来一单
     * @param id 订单ID
     * @return 订单提交VO
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id){
        orderService.repetition(id);
        return Result.success();
    }

    /**
     * 客户催单
     * @param id 订单ID
     * @return 客户催单结果
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("客户催单")
    public Result reminder(@PathVariable Long id){
        orderService.reminder(id);
        return Result.success();
    }
}
