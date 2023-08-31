package com.lcy.aigc.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class ExchangeConsumer {

    private final static String EXCHANGE_NAME = "fanout-exchange"; //广播交换机，给绑定的队列发送同样的消息

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.161.128");
        factory.setUsername("lcy");
        factory.setPassword("158574");
        Connection connection = factory.newConnection(); //创建连接 Connection
        Channel channel1 = connection.createChannel();
        Channel channel2 = connection.createChannel();
        //声明交换机
        channel1.exchangeDeclare(EXCHANGE_NAME, "fanout");
        //小王队列名
        String queueName1 = "xiaowang-queue";
        //创建队列
        channel1.queueDeclare(queueName1, true, false, false, null);
        //队列绑定交换机
        channel1.queueBind(queueName1, EXCHANGE_NAME, "");

        //小李队列名
        String queueName2 = "xiaoli-queue";
        channel2.queueDeclare(queueName2,true,false,false,null);
        //队列绑定交换机
        channel2.queueBind(queueName2, EXCHANGE_NAME, "");
        //执行任务1
        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("xiaowang-receive message:" + message);

        };
        //执行任务2
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println("xiaoli receive message:" + message);
        };

        channel1.basicConsume(queueName1, true, deliverCallback1, consumerTag -> {});
        channel2.basicConsume(queueName2, true, deliverCallback2, consumerTag -> {});

    }
}
