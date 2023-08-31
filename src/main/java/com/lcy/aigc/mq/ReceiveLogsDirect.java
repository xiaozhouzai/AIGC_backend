package com.lcy.aigc.mq;

import com.rabbitmq.client.*;

import java.nio.charset.StandardCharsets;

public class ReceiveLogsDirect {

    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.161.128");
        factory.setUsername("lcy");
        factory.setPassword("158574");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        String queueName1 = "xiaozao-queue";
        channel.queueDeclare(queueName1, true, false, false, null);
        channel.queueBind(queueName1, EXCHANGE_NAME, "xiaozao");

        String queueName2 = "xiaobai-queue";
        channel.queueDeclare(queueName2, true, false, false, null);
        channel.queueBind(queueName2, EXCHANGE_NAME, "xiaobai");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        //处理消息
        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [" + queueName1 + "] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        //处理消息
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [" + queueName2 + "] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        //消费消息
        channel.basicConsume(queueName1, true, deliverCallback1, consumerTag -> {
        });
        channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> {
        });
    }
}