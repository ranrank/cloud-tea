package com.cloudtea.service;

import com.cloudtea.dto.DrinkDTO;
import com.cloudtea.dto.DrinkPageQueryDTO;
import com.cloudtea.entity.Drink;
import com.cloudtea.result.PageResult;
import com.cloudtea.vo.DrinkVO;
import io.swagger.annotations.ApiOperation;

import java.util.List;

public interface DrinkService {
    /**
     * 新增饮品和规格
     */
    public void saveWithSpec(DrinkDTO drinkDTO);
    /**
     * 分页查询饮品
     */
    PageResult pageQuery(DrinkPageQueryDTO drinkPageQueryDTO);
    /**
     * 批量删除饮品
     */
    void deleteBatch(List<Long> ids);
    /**
     * 根据饮品id查询饮品详情
     */
    DrinkVO getByIdWithSpec(Long id);
    /**
     * 更新饮品和规格
     */
    void updateWithSpec(DrinkDTO drinkDTO);
    /**
     * 根据分类id查询饮品
     */
    List<Drink> list(Long categoryId);


    /**
     * 条件查询饮品和规格
     * @param drink
     * @return
     */
    List<DrinkVO> listWithSpec(Drink drink);


    /**
     * 饮品上架下架
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

}
