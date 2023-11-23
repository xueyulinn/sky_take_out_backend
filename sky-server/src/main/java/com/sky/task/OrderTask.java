package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrdersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 薛坤
 * @version 1.0
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrdersMapper ordersMapper;

    //超时订单处理
    @Scheduled(cron = "0 0/1 * * * ?")
    public void proceedTimeOutOrder() {
        log.info("处理超时订单"+LocalDateTime.now());
        //查询超时订单
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(15);
        //查询出来多个超时订单  没有查到的话list为空
        List<Orders> timeoutOrders = ordersMapper.getTimeoutOrder(Orders.PENDING_PAYMENT,localDateTime);

        if (timeoutOrders != null && timeoutOrders.size() > 0) {
            for (Orders order : timeoutOrders) {
                //更改超时订单状态
                Orders time_outOrders = Orders.builder()
                        .status(Orders.CANCELLED)
                        .cancelReason("订单超时")
                        .cancelTime(LocalDateTime.now())
                        .build();
                BeanUtils.copyProperties(time_outOrders,order,"id");
                ordersMapper.update(order);
            }

        }

    }

    //派送中订单处理
    @Scheduled(cron = "0 0 1 * * ?")
    public void proceedDeliveryOrder() {
        log.info("处理派送中订单"+LocalDateTime.now());
        //查询派送中订单==>派送超过1h的订单
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(60);
        //查询出来多个超时订单
        List<Orders> deliveryOrders = ordersMapper.getTimeoutOrder(Orders.DELIVERY_IN_PROGRESS,localDateTime);

        if (deliveryOrders != null && deliveryOrders.size() > 0) {

            for (Orders deliveryOrder : deliveryOrders) {
                //更改超时订单状态
                Orders delivery_outOrders = Orders.builder()
                        .status(Orders.COMPLETED)
                        .deliveryTime(LocalDateTime.now())
                        .build();
                BeanUtils.copyProperties(delivery_outOrders,deliveryOrder,"id");
                ordersMapper.update(deliveryOrder);

            }

        }

    }
}
