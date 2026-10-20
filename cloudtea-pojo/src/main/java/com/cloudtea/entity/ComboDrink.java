package com.cloudtea.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 组合套餐饮品关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboDrink implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    //组合套餐id
    private Long comboId;

    //饮品id
    private Long drinkId;

    //饮品名称 （冗余字段）
    private String name;

    //饮品原价
    private BigDecimal price;

    //份数
    private Integer copies;
}
