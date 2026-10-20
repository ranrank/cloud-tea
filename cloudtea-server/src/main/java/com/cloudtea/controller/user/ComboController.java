package com.cloudtea.controller.user;

import com.cloudtea.constant.StatusConstant;
import com.cloudtea.entity.Combo;
import com.cloudtea.result.Result;
import com.cloudtea.service.ComboService;
import com.cloudtea.vo.DrinkItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userComboController")
@RequestMapping("/user/combo")
@Api(tags = "C端-组合套餐浏览接口")
public class ComboController {
    @Autowired
    private ComboService comboService;

    /**
     * 条件查询
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询组合套餐")
    @Cacheable(cacheNames = "comboCache",key = "#categoryId")
    public Result<List<Combo>> list(Long categoryId) {
        Combo combo = new Combo();
        combo.setCategoryId(categoryId);
        combo.setStatus(StatusConstant.ENABLE);

        List<Combo> list = comboService.list(combo);
        return Result.success(list);
    }

    /**
     * 根据组合套餐id查询包含的饮品列表
     *
     * @param id
     * @return
     */
    @GetMapping("/drink/{id}")
    @ApiOperation("根据组合套餐id查询包含的饮品列表")
    public Result<List<DrinkItemVO>> drinkList(@PathVariable("id") Long id) {
        List<DrinkItemVO> list = comboService.getDrinkItemById(id);
        return Result.success(list);
    }
}
