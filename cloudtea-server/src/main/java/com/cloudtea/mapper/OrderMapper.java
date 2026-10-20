package com.cloudtea.mapper;

import com.github.pagehelper.Page;
import com.cloudtea.dto.DrinkSalesDTO;
import com.cloudtea.dto.OrdersPageQueryDTO;
import com.cloudtea.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    /**
     * 插入订单
     * @param orders 订单
     */
    void insert(Orders orders);
    /**
     * 分页查询订单
     * @param ordersPageQueryDTO 分页查询订单DTO
     * @return 分页查询订单结果
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);
    /**
     * 根据订单ID查询订单
     * @param id 订单ID
     * @return 订单
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);
    /**
     * 根据订单号查询订单
     * @param number 订单号
     * @return 订单
     */
    @Select("select * from orders where number=#{number}")
    Orders getByNumber(String number);
    /**
     * 更新订单
     * @param orders 订单
     */
    void update(Orders orders);
    /**
     * 根据订单状态查询订单数量
     * @param status 订单状态
     * @return 订单数量
     */
    @Select("select count(*) from orders where status=#{status}")
    Integer countStatus(Integer status);
    @Select("select * from orders where status=#{status} and order_time<#{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);
    /**
     * 根据订单状态和订单时间查询订单金额总和
     * @param map 查询参数
     * @return 订单金额总和
     */

    Double sumByMap(Map map);
    /**
     * 根据动态条件统计订单数量
     * @param map 查询参数
     * @return 订单数量
     */
    Integer countByMap(Map map);
    /**
     * 查询销售排名top10
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 销售排名top10结果
     */
    List<DrinkSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);
}