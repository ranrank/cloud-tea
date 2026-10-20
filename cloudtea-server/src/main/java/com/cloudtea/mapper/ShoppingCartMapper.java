package com.cloudtea.mapper;

import com.cloudtea.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    /**
     * 动态条件查询
     * @param shoppingCart 购物车信息
     * @return 购物车列表
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);
    /**
     * 根据id修改商品数量
     * @param shoppingCart 购物车信息
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);
    /**
     * 新增购物车
     * @param shoppingCart 购物车信息
     */
    @Insert("insert into shopping_cart (name, user_id, drink_id, combo_id, drink_spec, number, amount, image, create_time) values (#{name}, #{userId}, #{drinkId}, #{comboId}, #{drinkSpec}, #{number}, #{amount}, #{image}, #{createTime})")
    void insert(ShoppingCart shoppingCart);
    /**
     * 根据用户id删除购物车
     * @param userId 用户id
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);
    /**
     * 根据id删除购物车中的一条记录
     * @param id 购物车id
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);
    /**
     * 批量新增购物车
     * @param shoppingCartList 购物车列表
     */
    void insertBatch(List<ShoppingCart> shoppingCartList);
}
