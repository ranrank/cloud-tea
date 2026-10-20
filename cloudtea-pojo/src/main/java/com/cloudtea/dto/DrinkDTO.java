package com.cloudtea.dto;

import com.cloudtea.entity.DrinkSpec;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class DrinkDTO implements Serializable {

    private Long id;
    //饮品名称
    private String name;
    //饮品分类id
    private Long categoryId;
    //饮品价格
    private BigDecimal price;
    //图片
    private String image;
    //描述信息
    private String description;
    //0 下架 1 上架
    private Integer status;
    //规格
    private List<DrinkSpec> specs = new ArrayList<>();

}
