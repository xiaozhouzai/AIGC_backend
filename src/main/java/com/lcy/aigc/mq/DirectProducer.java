package com.lcy.aigc.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class DirectProducer {


    private static final String EXCHANGE_NAME = "direct_logs";
    //交换机类型 direct: 发送向指定路由规则的队列

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.161.128");
        factory.setUsername("lcy");
        factory.setPassword("158574");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {  //创建链接
            channel.exchangeDeclare(EXCHANGE_NAME, "direct"); //声明交换机
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String next = scanner.next();
                String[] split = next.split(",");

                if (split.length < 1){
                    continue;
                }
                String message = split[0];
                String routerKey = split[1];
                //发送两部分，用空格分开第一部分消息 第二部分路由键
                //发消息
                //routerKey: 路由键，指定发送规则,带上一个标识
                channel.basicPublish(EXCHANGE_NAME, routerKey, null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + routerKey + "':'" + message + "'");
            }

        }
    }
}
