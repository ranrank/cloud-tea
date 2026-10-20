package com.cloudtea.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 组合套餐总览
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComboOverViewVO implements Serializable {
    // 已上架数量
    private Integer sold;

    // 已下架数量
    private Integer discontinued;
}
