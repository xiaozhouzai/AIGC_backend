package com.lcy.aigc.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class TtlProducer {
    public static final String QUEUE_NAME = "ttl-queue";
    public static void main(String[] argv) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.161.128");
        factory.setUsername("lcy");
        factory.setPassword("158574");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //给队列设置过期时间 消费端声明就可以了
//        channel.queueDeclare(QUEUE_NAME,true,false,false,map);
        //声明交换机
        channel.exchangeDeclare("fill-exchange","topic");  //type: topic  direct  fanout
        channel.queueBind(QUEUE_NAME,"fill-exchange","dev");

        String message = "hello word";
        //给某条消息指定过期时间
        AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().expiration("10000").build();
        channel.basicPublish("",QUEUE_NAME,properties,message.getBytes(StandardCharsets.UTF_8));

    }
}
