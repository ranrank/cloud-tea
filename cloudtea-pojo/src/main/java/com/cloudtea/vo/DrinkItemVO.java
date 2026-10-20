package com.cloudtea.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DrinkItemVO implements Serializable {

    //饮品名称
    private String name;

    //份数
    private Integer copies;

    //饮品图片
    private String image;

    //饮品描述
    private String description;
}
