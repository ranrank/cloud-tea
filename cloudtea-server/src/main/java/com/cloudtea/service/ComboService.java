package com.cloudtea.service;

import com.cloudtea.dto.ComboDTO;
import com.cloudtea.dto.ComboPageQueryDTO;
import com.cloudtea.entity.Combo;
import com.cloudtea.result.PageResult;
import com.cloudtea.vo.DrinkItemVO;
import com.cloudtea.vo.ComboVO;

import java.util.List;

public interface ComboService {
    /**
     * 新增组合套餐
     * @param comboDTO
     */
    void saveWithDrink(ComboDTO comboDTO);
    /**
     * 分页查询组合套餐
     * @param comboPageQueryDTO
     * @return
     */
    PageResult page(ComboPageQueryDTO comboPageQueryDTO);
    /**
     * 批量删除组合套餐
     * @param ids
     */
    void delete(List<Long> ids);
    /**
     * 根据id查询组合套餐
     * @param id
     * @return
     */
    ComboVO getById(Long id);
    /**
     * 更新组合套餐
     * @param comboDTO
     */
    void update(ComboDTO comboDTO);
    /**
     * 更新组合套餐状态
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 条件查询
     * @param combo
     * @return
     */
    List<Combo> list(Combo combo);

    /**
     * 根据id查询饮品选项
     * @param id
     * @return
     */
    List<DrinkItemVO> getDrinkItemById(Long id);
}
