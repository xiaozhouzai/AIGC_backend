package com.lcy.aigc.amqp;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.lcy.aigc.constant.AigcConstant.AIGC_EXCHANGE_NAME;
import static com.lcy.aigc.constant.AigcConstant.AIGC_ROUTINGKEY;

/**
 * 消息生产者
 */
@Component
public class MyMessageProducer {



    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message){
        rabbitTemplate.convertAndSend(AIGC_EXCHANGE_NAME,AIGC_ROUTINGKEY,message);
    }

}
