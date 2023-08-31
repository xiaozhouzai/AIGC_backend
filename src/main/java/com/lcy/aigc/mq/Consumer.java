package com.lcy.aigc.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class Consumer {
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.161.128");
        factory.setUsername("lcy");
        factory.setPassword("158574");
        Connection connection = factory.newConnection(); //创建连接 Connection
        Channel channel = connection.createChannel();
        //声明队列
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        System.out.println("wait for message");
        //定义了如何处理消息
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("receive message:" + message);

        };
        //消费消息，会持续阻塞
//        queue——自动应答队列的名称——如果服务器认为消息一旦发送就被确认，则为true;如果服务器期望显式确认，则为false。
//        deliverCallback -消息传递时的回调。
//        cancelCallback -取消消费者时的回调
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}