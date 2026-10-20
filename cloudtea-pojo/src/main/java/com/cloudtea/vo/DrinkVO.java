package com.cloudtea.vo;

import com.cloudtea.entity.DrinkSpec;
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
public class DrinkVO implements Serializable {

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
    //更新时间
    private LocalDateTime updateTime;
    //分类名称
    private String categoryName;
    //饮品关联的规格
    private List<DrinkSpec> specs = new ArrayList<>();

    //private Integer copies;
}
