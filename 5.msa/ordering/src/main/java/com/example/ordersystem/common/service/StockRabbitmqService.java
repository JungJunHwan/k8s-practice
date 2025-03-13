package com.example.ordersystem.common.service;

import com.example.ordersystem.common.config.RabbitmqConfig;
import com.example.ordersystem.common.dto.StockRabbitDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockRabbitmqService {
    // private final RabbitTemplate rabbitTemplate;

    // public StockRabbitmqService(RabbitTemplate rabbitTemplate) {
    //     this.rabbitTemplate = rabbitTemplate;
    // }

    // //    mq에 rdb 동기화 관련 메시지를 발행
    // public void publish(StockRabbitDto stockRabbitDto){
    //     rabbitTemplate.convertAndSend(RabbitmqConfig.STOCK_DECREASE_QUEUE, stockRabbitDto);
    // }
}
