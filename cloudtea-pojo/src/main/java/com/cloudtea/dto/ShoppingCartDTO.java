package com.cloudtea.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class ShoppingCartDTO implements Serializable {

    private Long drinkId;
    private Long comboId;
    private String drinkSpec;

}
