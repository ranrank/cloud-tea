package com.cloudtea.mapper;

import com.github.pagehelper.Page;
import com.cloudtea.annotation.AutoFill;
import com.cloudtea.dto.DrinkPageQueryDTO;
import com.cloudtea.entity.Drink;
import com.cloudtea.enumeration.OperationType;
import com.cloudtea.vo.DrinkVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DrinkMapper {

    /**
     * 根据分类id查询饮品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from drink where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 新增饮品
     */
    @AutoFill(value= OperationType.INSERT)
    void insert(Drink drink);
    /**
     * 分页查询饮品
     */
    Page<DrinkVO> pageQuery(DrinkPageQueryDTO drinkPageQueryDTO);
    /**
     * 根据id查询饮品
     */
    @Select("select * from drink where id = #{id}")
    Drink getById(Long id);
    /**
     * 根据id删除饮品
     */
    @Delete("delete from drink where id = #{id}")
    void deleteById(Long id);
    /**
     * 根据饮品id集合批量删除饮品数据
     */
    void deleteByIds(List<Long> ids);
    /**
     * 更新饮品
     */
    @AutoFill(value= OperationType.UPDATE)
    void updateById(Drink drink);
    /**
     * 根据分类id查询饮品
     */
    List<Drink> list(Drink drink);
    /**
     * 根据组合套餐id查询饮品id集合
     */
    @Select("select a.* from drink a left join combo_drink b on a.id = b.drink_id where b.combo_id = #{id}")
    List<Drink> getByComboId(Long id);
    /**
     * 根据条件统计饮品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
