package com.cloudtea.service;

import com.cloudtea.dto.ShoppingCartDTO;
import com.cloudtea.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    /**
     * 添加购物车
     * @param shoppingCartDTO 购物车信息
     */
    void add(ShoppingCartDTO shoppingCartDTO);
    /**
     * 减少购物车中某件商品的数量，数量只剩 1 时直接删掉该条记录
     * @param shoppingCartDTO 购物车信息
     */
    void sub(ShoppingCartDTO shoppingCartDTO);
    /**
     * 查询购物车列表
     * @return 购物车列表
     */
    List<ShoppingCart> showShoppingcart();
    /**
     * 清空购物车
     */
    void cleanShoppingCart();
}
