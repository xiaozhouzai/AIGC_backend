package com.lcy.aigc.mq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class DeadProducer {
    //声明死信交换机
    public static final String DEAD_EXCHANGE_NAME = "dead-direct-exchange";
    public static final String EXCHANGE_NAME = "direct-exchange";
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.161.128");
        factory.setUsername("lcy");
        factory.setPassword("158574");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        //设置死信参数
//        Map<String,Object> map = new HashMap<>();
//        //死信交换机
//        map.put("x-dead-letter-exchange",DEAD_EXCHANGE_NAME);
//        //死信队列
//        map.put("x-dead-letter-routing-key","dead.exchange");
//        channel.queueDeclare("dead-queue",true,false,false,map);
        //给某条消息指定过期时间
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .expiration("3000")
                .build();
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            String next = scanner.next();
            String[] split = next.split(",");
            String message = split[0];
            String routingKey = split[1];
            channel.basicPublish(EXCHANGE_NAME,routingKey,properties,message.getBytes(StandardCharsets.UTF_8));
            System.out.println("send message:"+ message + "routingKey"+ routingKey);
        }

    }
}
