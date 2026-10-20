package com.cloudtea.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 饮品规格
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrinkSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    //饮品id
    private Long drinkId;

    //规格组名称，如 杯型 / 温度 / 糖度
    private String specName;

    //规格值列表（JSON 数组字符串，如 ["中杯","大杯"]）
    private String specValues;

}
