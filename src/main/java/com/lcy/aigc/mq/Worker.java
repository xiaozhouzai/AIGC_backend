package com.lcy.aigc.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class Worker {

    private static final String TASK_QUEUE_NAME = "multi_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("lcy");
        factory.setPassword("158574");
        factory.setHost("192.168.161.128");
        final Connection connection = factory.newConnection();




        //使用for循环
        for (int i = 0; i < 2; i++) {
            final Channel channel = connection.createChannel();
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            //basicQos(1)设置消费者每次从队列中获取的消息数量。
            //参数1表示每次只获取一条消息。
            //该方法可以用于控制消费者的负载均衡，确保每个消费者只处理一条消息，避免消息堆积和不均衡消费
            channel.basicQos(1);
            //定义如何处理消息
            int finalI = i;
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                System.out.println(finalI + " [x] Received '" + message + "'");
                try {
                    //模拟处理工作，任务执行10秒
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    System.out.println(" [x] Done");
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,true);
                    //拒绝策略，第二个参数为是否拒绝当前以前的所有消息
                }
            };
            //开启消费监听
            //autoAck 消息确认机制，保证消息成功被消费，当消费者接收到消息后必须要给出反馈，反馈结果为消费成功，队列才会移除这个消息
            //一般设置为false，开启就是立刻确认成功，就删除了这条消息
            channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });
        }


    }
}