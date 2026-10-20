package com.cloudtea.controller.admin;

import com.cloudtea.result.Result;
import com.cloudtea.service.WorkspaceService;
import com.cloudtea.vo.BusinessDataVO;
import com.cloudtea.vo.DrinkOverViewVO;
import com.cloudtea.vo.OrderOverViewVO;
import com.cloudtea.vo.ComboOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 工作台
 */
@RestController
@RequestMapping("/admin/workspace")
@Slf4j
@Api(tags = "工作台相关接口")
public class WorkSpaceController {

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 工作台今日数据查询
     * @return
     */
    @GetMapping("/businessData")
    @ApiOperation("工作台今日数据查询")
    public Result<BusinessDataVO> businessData(){
        //获得当天的开始时间
        LocalDateTime begin = LocalDateTime.now().with(LocalTime.MIN);
        //获得当天的结束时间
        LocalDateTime end = LocalDateTime.now().with(LocalTime.MAX);

        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);
        return Result.success(businessDataVO);
    }

    /**
     * 查询订单管理数据
     * @return
     */
    @GetMapping("/overviewOrders")
    @ApiOperation("查询订单管理数据")
    public Result<OrderOverViewVO> orderOverView(){
        return Result.success(workspaceService.getOrderOverView());
    }

    /**
     * 查询饮品总览
     * @return
     */
    @GetMapping("/overviewDrinks")
    @ApiOperation("查询饮品总览")
    public Result<DrinkOverViewVO> drinkOverView(){
        return Result.success(workspaceService.getDrinkOverView());
    }

    /**
     * 查询组合套餐总览
     * @return
     */
    @GetMapping("/overviewCombos")
    @ApiOperation("查询组合套餐总览")
    public Result<ComboOverViewVO> comboOverView(){
        return Result.success(workspaceService.getComboOverView());
    }
}
