package com.lcy.aigc.amqp;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static com.lcy.aigc.constant.AigcConstant.*;

public class CreateQueueAndExchange {
    public static void main(String[] args) throws IOException, TimeoutException {
        //创建项目专属消息队列 只执行一次即可，可以放在定时任务中
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.161.128");
        factory.setUsername("lcy");
        factory.setPassword("158574");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(AIGC_EXCHANGE_NAME,"direct");
        channel.queueDeclare(AIGC_QUEUE_NAME,true,false,false,null);
        channel.queueBind(AIGC_QUEUE_NAME,AIGC_EXCHANGE_NAME,AIGC_ROUTINGKEY);
    }
}
