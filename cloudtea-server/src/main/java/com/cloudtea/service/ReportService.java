package com.cloudtea.service;

import com.cloudtea.vo.OrderReportVO;
import com.cloudtea.vo.SalesTop10ReportVO;
import com.cloudtea.vo.TurnoverReportVO;
import com.cloudtea.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    /**
     * 营业额统计
     * @param begin 开始时间
     * @param end 结束时间
     * @return 营业额统计结果
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);
    /**
     * 用户统计
     * @param begin 开始时间
     * @param end 结束时间
     * @return 用户统计结果
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);
    /**
     * 订单统计
     * @param begin 开始时间
     * @param end 结束时间
     * @return 订单统计结果
     */
    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);
    /**
     * 销售排名top10
     * @param begin 开始时间
     * @param end 结束时间
     * @return 销售排名top10结果
     */
    SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end);
    /**
     * 导出数据统计
     * @param response 响应对象
     */
    void exportBusinessData(HttpServletResponse response);



}
