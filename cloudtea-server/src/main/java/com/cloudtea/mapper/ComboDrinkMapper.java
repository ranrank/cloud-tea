package com.cloudtea.mapper;

import com.cloudtea.entity.ComboDrink;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ComboDrinkMapper {
    List<Long> getComboIdsByDrinkId(List<Long> drinkIds);
    /**
     * 根据饮品id集合批量删除组合套餐关联的饮品数据
     */
    void deleteByDrinkIds(List<Long> drinkIds);
    /**
     * 批量新增组合套餐饮品关系
     */
    void insertBatch(List<ComboDrink> comboDrinkList);
    /**
     * 根据组合套餐id删除组合套餐关联的饮品数据
     */
    @Delete("delete from combo_drink where combo_id = #{id}")
    void deleteByComboId(Long id);
    /**
     * 根据组合套餐id查询组合套餐关联的饮品数据
     */
    @Select("select * from combo_drink where combo_id = #{id}")
    List<ComboDrink> getByComboId(Long id);
}
