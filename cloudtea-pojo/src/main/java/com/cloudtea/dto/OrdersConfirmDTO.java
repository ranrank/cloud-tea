package com.cloudtea.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrdersConfirmDTO implements Serializable {

    private Long id;
    //订单状态 1待付款 2待接单 3 制作中 4 配送中 5 已完成 6 已取消 7 退款
    private Integer status;

}
