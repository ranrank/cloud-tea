package com.cloudtea.mapper;

import com.cloudtea.entity.DrinkSpec;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DrinkSpecMapper {
    /**
     * 批量插入饮品规格数据
     */
    void insertBatch(List<DrinkSpec> specs);
    /**
     * 根据饮品id删除饮品规格数据
     */
    @Delete("delete from drink_spec where drink_id = #{id}")
    void deleteByDrinkId(Long id);
    /**
     * 根据饮品id查询饮品规格数据
     */
    @Select("select * from drink_spec where drink_id = #{id}")
    List<DrinkSpec> getByDrinkId(Long id);
}
