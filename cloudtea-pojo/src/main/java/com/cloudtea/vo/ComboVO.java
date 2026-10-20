package com.cloudtea.vo;

import com.cloudtea.entity.ComboDrink;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboVO implements Serializable {

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

    //更新时间
    private LocalDateTime updateTime;

    //分类名称
    private String categoryName;

    //组合套餐和饮品的关联关系
    private List<ComboDrink> comboDrinks = new ArrayList<>();
}
