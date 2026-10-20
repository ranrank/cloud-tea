package com.cloudtea.service.impl;

import com.cloudtea.context.BaseContext;
import com.cloudtea.dto.ShoppingCartDTO;
import com.cloudtea.entity.Drink;
import com.cloudtea.entity.Combo;
import com.cloudtea.entity.ShoppingCart;
import com.cloudtea.mapper.DrinkMapper;
import com.cloudtea.mapper.ComboMapper;
import com.cloudtea.mapper.ShoppingCartMapper;
import com.cloudtea.service.DrinkService;
import com.cloudtea.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service

public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DrinkMapper drinkMapper;
    @Autowired
    private ComboMapper comboMapper;
    /**
     * 添加购物车
     * @param shoppingCartDTO 购物车信息
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        //判断当前加入到购物车中的商品是否已经存在了
        ShoppingCart shoppingCart =new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //如果存在，就更新数量
        if(list!=null&&list.size()>0){
            //更新数量
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber()+1);
            //更新数据库中的数量
            shoppingCartMapper.updateNumberById(cart);
            //更新完毕，直接返回，不再执行后面的插入
            return;
        }

        //如果不存在，就插入一条购物车数据

        //判断当前加入到购物车中的商品是否是饮品还是组合套餐
        Long drinkId=shoppingCartDTO.getDrinkId();
        if(drinkId!=null){
            //饮品
            Drink drink = drinkMapper.getById(drinkId);
            shoppingCart.setName(drink.getName());
            shoppingCart.setImage(drink.getImage());
            shoppingCart.setAmount(drink.getPrice());
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
        }else{
            //组合套餐
            Long comboId=shoppingCartDTO.getComboId();

            Combo combo = comboMapper.getById(comboId);
            shoppingCart.setName(combo.getName());
            shoppingCart.setImage(combo.getImage());
            shoppingCart.setAmount(combo.getPrice());
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
        }
        //插入购物车数据
        shoppingCartMapper.insert(shoppingCart);

    }

    /**
     * 减少购物车中某件商品的数量
     * @param shoppingCartDTO 购物车信息
     */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        //按和 add 相同的方式定位到购物车里这一条记录（同饮品 + 同规格才算同一条）
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.isEmpty()) {
            return;
        }
        ShoppingCart cart = list.get(0);
        //只剩一份的时候再减就把这条记录直接删掉，避免出现数量为 0 的脏数据
        if (cart.getNumber() != null && cart.getNumber() == 1) {
            shoppingCartMapper.deleteById(cart.getId());
        } else {
            cart.setNumber(cart.getNumber() - 1);
            shoppingCartMapper.updateNumberById(cart);
        }
    }

    /**
     * 查询购物车列表
     * @return 购物车列表
     */
    @Override
    public List<ShoppingCart> showShoppingcart() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart=ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }
    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCart() {
        //获取当前登录用户的id
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }
}
