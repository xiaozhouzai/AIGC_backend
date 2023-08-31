package com.lcy.aigc.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

public class Producer {
    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.161.128");
        factory.setUsername("lcy");
        factory.setPassword("158574");
        try (Connection connection = factory.newConnection(); //创建连接 Connection
             Channel channel = connection.createChannel()){ // 创建频道 Channel 客户端，及操作消息队列的客户端提供了和消息队列建立通信的传输方法。

//            queue 队列的名称
//            durable 声明一个持久队列(队列将在服务器重启后存活)
//            exclusive 声明一个排他性队列(仅限于此连接)
//            autoDelete 声明一个自动删除队列(服务器将在不再使用时删除它)
//            arguments 队列的其他属性(构造参数）
//            返回值: 一个声明确认方法，用于指示队列已成功声明
            channel.queueDeclare(QUEUE_NAME,false,false,false,null);
            String message = "hello world";
            channel.basicPublish("",QUEUE_NAME,null,message.getBytes(StandardCharsets.UTF_8));
            System.out.println("[x] Sent '" + message + "'");
        }
    }

}
