package com.cloudtea.service;

import com.cloudtea.vo.BusinessDataVO;
import com.cloudtea.vo.DrinkOverViewVO;
import com.cloudtea.vo.OrderOverViewVO;
import com.cloudtea.vo.ComboOverViewVO;
import java.time.LocalDateTime;

public interface WorkspaceService {

    /**
     * 根据时间段统计营业数据
     * @param begin
     * @param end
     * @return
     */
    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    /**
     * 查询订单管理数据
     * @return
     */
    OrderOverViewVO getOrderOverView();

    /**
     * 查询饮品总览
     * @return
     */
    DrinkOverViewVO getDrinkOverView();

    /**
     * 查询组合套餐总览
     * @return
     */
    ComboOverViewVO getComboOverView();

}
