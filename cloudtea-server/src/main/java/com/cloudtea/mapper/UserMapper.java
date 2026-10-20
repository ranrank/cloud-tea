package com.cloudtea.mapper;

import com.cloudtea.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);
    /**
     * 根据用户id查询用户
     * @param id 用户id
     * @return 用户
     */
    @Select("select * from user where id = #{id}")
    User getById(Long id);
    /**
     * 新增用户
     * @param user
     */
    void insert(User user);
    /**
     * 根据动态条件统计用户数量
     * @param map 查询参数
     * @return 用户数量
     */
    Integer countByMap(Map map);
}
