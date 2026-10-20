package com.cloudtea.dto;

import com.cloudtea.entity.ComboDrink;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ComboDTO implements Serializable {

    private Long id;

    //分类id
    private Long categoryId;

    //组合套餐名称
    private String name;

    //组合套餐价格
    private BigDecimal price;

    //状态 0:停用 1:启用
    private Integer status;

    //描述信息
    private String description;

    //图片
    private String image;

    //组合套餐饮品关系
    private List<ComboDrink> comboDrinks = new ArrayList<>();

}
