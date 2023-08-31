package com.lcy.aigc.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Exchange {
    private final static String EXCHANGE_NAME = "fanout-exchange"; //广播交换机

    public static void main(String[] argv) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.161.128");
        factory.setUsername("lcy");
        factory.setPassword("158574");
        try (Connection connection = factory.newConnection(); //创建连接 Connection
             Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
            //scanner方便测试
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()){
                String message = scanner.next();
                //发布消息  指定路由规则
                channel.basicPublish(EXCHANGE_NAME,"", null,
                        message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "'");
            }


        }
    }
}
