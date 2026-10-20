package com.cloudtea.controller.admin;

import com.cloudtea.dto.OrdersCancelDTO;
import com.cloudtea.dto.OrdersConfirmDTO;
import com.cloudtea.dto.OrdersPageQueryDTO;
import com.cloudtea.dto.OrdersRejectionDTO;
import com.cloudtea.result.PageResult;
import com.cloudtea.result.Result;
import com.cloudtea.service.OrderService;
import com.cloudtea.vo.OrderStatisticsVO;
import com.cloudtea.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "订单相关接口")
@Slf4j
public class OrderController{
    @Autowired
    private OrderService orderService;

    /**
     * 订单条件查询
     * @param ordersPageQueryDTO 订单查询参数
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("订单条件查询")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO){
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 订单状态统计
     * @return 订单状态统计VO
     */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }
    /**
     * 根据订单id查询订单详情
     * @param id 订单ID
     * @return 订单详情VO
     */
    @GetMapping("/details/{id}")
    @ApiOperation("根据订单id查询订单详情")
    public Result<OrderVO> details(@PathVariable Long id){
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }
    /**
     * 接单
     * @param ordersConfirmDTO 订单确认DTO
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception{
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception{
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("配送订单")
    public Result delivery(@PathVariable Long id){
        orderService.delivery(id);
        return Result.success();
    }

    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result complete(@PathVariable Long id){
        orderService.complete(id);
        return Result.success();
    }
}
