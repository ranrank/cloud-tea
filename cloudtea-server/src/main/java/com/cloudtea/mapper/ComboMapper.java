package com.cloudtea.mapper;

import com.github.pagehelper.Page;
import com.cloudtea.annotation.AutoFill;
import com.cloudtea.dto.ComboPageQueryDTO;
import com.cloudtea.entity.Combo;
import com.cloudtea.enumeration.OperationType;
import com.cloudtea.vo.DrinkItemVO;
import com.cloudtea.vo.ComboVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ComboMapper {

    /**
     * 根据分类id查询组合套餐的数量
     * @param id
     * @return
     */
    @Select("select count(id) from combo where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);
    /**
     * 新增组合套餐
     * @param combo
     */
    @AutoFill(OperationType.INSERT)
    void insert(Combo combo);
    /**
     * 分页查询组合套餐
     * @param comboPageQueryDTO
     * @return
     */
    Page<ComboVO> pageQuery(ComboPageQueryDTO comboPageQueryDTO);
    /**
     * 根据id查询组合套餐
     * @param id
     * @return
     */
    @Select("select * from combo where id = #{id}")
    Combo getById(Long id);
    /**
     * 根据id删除组合套餐
     * @param id
     */
    @Delete("delete from combo where id = #{id}")
    void deleteById(Long id);
    /**
     * 更新组合套餐
     * @param combo
     */
    @AutoFill(OperationType.UPDATE)
    void update(Combo combo);

    /**
     * 动态条件查询组合套餐
     * @param combo
     * @return
     */
    List<Combo> list(Combo combo);

    /**
     * 根据组合套餐id查询饮品选项
     * @param comboId
     * @return
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from combo_drink sd left join drink d on sd.drink_id = d.id " +
            "where sd.combo_id = #{comboId}")
    List<DrinkItemVO> getDrinkItemByComboId(Long comboId);
    /**
     * 根据条件统计组合套餐数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
