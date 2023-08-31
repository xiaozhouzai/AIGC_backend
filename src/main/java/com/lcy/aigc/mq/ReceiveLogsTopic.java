package com.lcy.aigc.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class ReceiveLogsTopic {

    private static final String EXCHANGE_NAME = "topic-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.161.128");
        factory.setUsername("lcy");
        factory.setPassword("158574");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "topic");
        String queueName1 = "大王";
        String queueName2 = "小王";
        String queueName3 = "中王";
        //使用这个routerKey可以匹配两个下面两个，big.id.big.x

        //*表示一个占位单词，不是一个字符,#,代表所有
        channel.queueDeclare(queueName1, true, false, false, null);
        channel.queueBind(queueName1,EXCHANGE_NAME,"big.id.*.*");
        channel.queueDeclare(queueName2, true, false, false, null);
        channel.queueBind(queueName2,EXCHANGE_NAME,"#.big.#");
        channel.queueDeclare(queueName3, true, false, false, null);
        channel.queueBind(queueName3,EXCHANGE_NAME,"#.small.#");


        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback3 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        channel.basicConsume(queueName1, true, deliverCallback1, consumerTag -> {});
        channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> {});
        channel.basicConsume(queueName3, true, deliverCallback3, consumerTag -> {});
    }
}