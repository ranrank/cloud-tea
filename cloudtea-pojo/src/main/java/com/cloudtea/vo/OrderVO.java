package com.cloudtea.vo;

import com.cloudtea.entity.OrderDetail;
import com.cloudtea.entity.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO extends Orders implements Serializable {

    //订单饮品信息
    private String orderItems;

    //订单详情
    private List<OrderDetail> orderDetailList;

}
