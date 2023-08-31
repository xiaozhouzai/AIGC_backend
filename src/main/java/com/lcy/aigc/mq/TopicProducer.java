package com.lcy.aigc.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class TopicProducer {
    public static final String EXCHANGE_NAME = "topic-exchange";
    public static void main(String[] argv) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.161.128");
        factory.setUsername("lcy");
        factory.setPassword("158574");
        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME,"topic");

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String next = scanner.next();
            String[] split = next.split(",");
            String message = split[0];
            String routerKey = split[1];
            channel.basicPublish(EXCHANGE_NAME,routerKey,null,message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + routerKey + "':'" + message + "'");
        }
    }
}
