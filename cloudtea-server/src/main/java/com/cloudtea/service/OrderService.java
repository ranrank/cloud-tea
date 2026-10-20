package com.cloudtea.service;

import com.cloudtea.dto.*;
import com.cloudtea.result.PageResult;
import com.cloudtea.vo.OrderPaymentVO;
import com.cloudtea.vo.OrderStatisticsVO;
import com.cloudtea.vo.OrderSubmitVO;
import com.cloudtea.vo.OrderVO;

public interface OrderService {
    /**
     * 提交订单
     *
     * @param ordersSubmitDTO 订单提交DTO
     * @return 订单提交VO
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO 订单支付DTO
     * @return 预支付交易单参数
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo 商户订单号
     * @param payMethod  付款方式
     */
    void paySuccess(String outTradeNo, Integer payMethod);

    /**
     * 分页查询订单
     *
     * @param ordersPageQueryDTO 订单分页查询DTO
     * @return 订单分页查询结果
     */
    PageResult pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 订单详情查询
     *
     * @param id 订单ID
     * @return 订单详情VO
     */
    OrderVO details(Long id);

    /**
     * 用户取消订单
     *
     * @param id 订单ID
     */
    void userCancelById(Long id) throws Exception;

    /**
     * 再来一单
     *
     * @param id 订单ID
     */
    void repetition(Long id);

    /**
     * 订单条件查询
     *
     * @param ordersPageQueryDTO 订单分页查询DTO
     * @return 订单分页查询结果
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 订单状态统计
     *
     * @return 订单状态统计VO
     */
    OrderStatisticsVO statistics();
    /**
     * 接单
     *
     * @param ordersConfirmDTO 订单确认DTO
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);
    /**
     * 拒单
     *
     * @param ordersRejectionDTO 订单拒单DTO
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;
    /**
     * 取消订单
     *
     * @param ordersCancelDTO 订单取消DTO
     */
    void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception;
    /**
     * 配送订单
     *
     * @param id 订单ID
     */
    void delivery(Long id);
    /**
     * 完成订单
     *
     * @param id 订单ID
     */
    void complete(Long id);
    /**
     * 客户催单
     *
     * @param id 订单ID
     */
    void reminder(Long id);
}

