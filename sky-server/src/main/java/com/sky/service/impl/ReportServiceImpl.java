package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 薛坤
 * @version 1.0
 */
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额查询
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        //先找出区间内的日期
        ArrayList<LocalDate> dateArrayList = getLocalDates(begin, end);


        String dateStr = StringUtils.join(dateArrayList, ",");

        //再查日期对应的营业额
        ArrayList<Double> dailyTurnover = new ArrayList<>();

        for (LocalDate date : dateArrayList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap<>();
            map.put("status", Orders.COMPLETED);
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);

            Double sumTurnover = ordersMapper.getByStatusAndOrderTime(map);

            //这边加了个判断null是因为sum函数如果没有求出总值会是null
            sumTurnover = sumTurnover == null ? 0.0 : sumTurnover;

            dailyTurnover.add(sumTurnover);
        }

        String turnoverStr = StringUtils.join(dailyTurnover, ",");

        return TurnoverReportVO.builder().dateList(dateStr).turnoverList(turnoverStr).build();
    }


    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {

        //查找区间日期
        ArrayList<LocalDate> localDates = getLocalDates(begin, end);
        String dateStr = StringUtils.join(localDates, ",");

        //查询区间内每日新增用户数量
        ArrayList<Integer> newUserAmount = new ArrayList<>();
        ArrayList<Integer> totalUserAmount = new ArrayList<>();
        for (LocalDate date : localDates) {

            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //查出每日新增用户数量
            //下面这两个没做null校验是因为count函数没count出来是0
            Integer newUserNumber = getUserAmount(beginTime, endTime);
            newUserAmount.add(newUserNumber);
            //查出每日总用户数量
            Integer totalUserNumber = getUserAmount(null, endTime);
            totalUserAmount.add(totalUserNumber);
        }

        String newUserStr = StringUtils.join(newUserAmount, ",");
        /*ArrayList<String> list = new ArrayList<>();
                                      charsequence序列
        String join = String.join(",", list)*/
        ;
        String totalUserStr = StringUtils.join(totalUserAmount, ",");

        return UserReportVO.builder()
                .dateList(dateStr)
                .newUserList(newUserStr)
                .totalUserList(totalUserStr)
                .build();
    }

    /**
     * 订单统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        //时间区间数据
        ArrayList<LocalDate> dates = getLocalDates(begin, end);

        ArrayList<Integer> dailyTotalOrder = new ArrayList<>();
        ArrayList<Integer> dailyValidOrder = new ArrayList<>();

        //获取每日总订单数和有效订单数
        for (LocalDate date : dates) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //订单数据(全部订单, 有效订单)
            Integer totalOrders = getOrders(beginTime, endTime, null);
            Integer validOrders = getOrders(beginTime, endTime, Orders.COMPLETED);
            dailyTotalOrder.add(totalOrders);
            dailyValidOrder.add(validOrders);
        }

        //获取订单总数和有效订单总数
        /*dailyValidOrder.stream().reduce((a, b) ->
                Integer.sum(a, b)).get();*/
        //订单总数
        Integer totalOrders = dailyTotalOrder.stream().reduce(Integer::sum).get();
        //有效订单总数
        Integer totalValidOrders = dailyValidOrder.stream().reduce(Integer::sum).get();

        //订单完成率
        Double orderCompleteRatio = 0.0;
        if (totalOrders != 0) {
            orderCompleteRatio = (totalValidOrders * 1.0 / totalOrders);
            // orderCompleteRatio = (totalValidOrders.doubleValue()/totalOrders);
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dates, ","))
                .orderCompletionRate(orderCompleteRatio)
                .orderCountList(StringUtils.join(dailyTotalOrder, ","))
                .totalOrderCount(totalOrders)
                .validOrderCount(totalValidOrders)
                .validOrderCountList(StringUtils.join(dailyValidOrder, ","))
                .build();
    }

    /**
     * 销量排名top10接口
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO salesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> goodsSalesDTOS = new ArrayList<>();
        goodsSalesDTOS = orderDetailMapper.getByStatusAndOrderTime(beginTime, endTime);
        //将stream流里的每一个元素映射为另一个元素并转为新的stream
        //方法引用 .map(String::trim) // 去空格
        //      .map(String::toLowerCase) // 变小写
        //         .forEach(System.out::println); // 打印
        List<String> nameList = goodsSalesDTOS.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = goodsSalesDTOS.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();

    }

    /**
     * 导出Excel报表接口
     *
     * @param
     * @return
     */
    @Override
    public void exportExcel(HttpServletResponse response) {
        //近三十天数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now().minusDays(1);
        //要传入两个ldt类型的时间
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //调用工作台service层方法
        BusinessDataVO businessData = workspaceService.getBusinessData(beginTime, endTime);
        log.info("查询出近30天的业务数据为:{}", businessData);

        InputStream resourceAsStream = null;
        XSSFWorkbook excel = null;

        try {
                                                                //获取制定资源的输入流
            resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
            excel = new XSSFWorkbook(resourceAsStream);

            //两个方法获得sheet对象
            // excel.getSheet(String name);
            XSSFSheet sheet = excel.getSheetAt(0);

            XSSFRow row = sheet.getRow(1);
            row.getCell(1).setCellValue("时间:" + begin + "至" + end);

            row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            //填充每日字段
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                LocalDateTime dateBegin = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime dateEnd = LocalDateTime.of(date, LocalTime.MAX);

                BusinessDataVO businessRowData = workspaceService.getBusinessData(dateBegin, dateEnd);

                row = sheet.getRow(7 + i);

                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessRowData.getTurnover());
                row.getCell(3).setCellValue(businessRowData.getValidOrderCount());
                row.getCell(4).setCellValue(businessRowData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessRowData.getUnitPrice());
                row.getCell(6).setCellValue(businessRowData.getNewUsers());
            }

            //把数据写出去
            excel.write(response.getOutputStream());
        } catch (IOException e) {

            e.printStackTrace();
        } finally {
            //避免出现npe
            if (resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            if (excel != null) {
                try {
                    excel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private Integer getOrders(LocalDateTime beginTime, LocalDateTime endTime, Integer status) {
        Map map = new HashMap<>();
        map.put("status", status);
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        return ordersMapper.getByOrderTimeAndStatus(map);
    }


    private ArrayList<LocalDate> getLocalDates(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> dateArrayList = new ArrayList<>();
        StringJoiner stringJoiner = new StringJoiner(",", "(", ")");
        dateArrayList.add(begin);

        //这样就把begin到end内的所有日期都装在集合里了
        while (begin.isBefore(end)) {
            begin = begin.plusDays(1);
            stringJoiner.add(String.valueOf(begin));
            dateArrayList.add(begin);
        }
        return dateArrayList;
    }


    private Integer getUserAmount(LocalDateTime beginTime, LocalDateTime endTime) {
        Map map = new HashMap<>();
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        return userMapper.getByCreateTime(map);
    }

}
