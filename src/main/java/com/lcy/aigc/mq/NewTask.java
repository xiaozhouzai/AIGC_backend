package com.lcy.aigc.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class NewTask {

    private static final String TASK_QUEUE_NAME = "multi_queue"; //多队列

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.161.128");
        factory.setUsername("lcy");
        factory.setPassword("158574");
        try (Connection connection = factory.newConnection(); //连接
             Channel channel = connection.createChannel()) { //创建频道
            //声明队列
            //此时开启 队列持久化 durable 设置为TRUE
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
//            channel.basicAck();
//            long deliveryTag,
//            boolean multiple

            //scanner方便测试
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()){
                String message = scanner.next();
                //发布消息
                //消息持久化配置 MessageProperties.PERSISTENT_TEXT_PLAIN
                channel.basicPublish("", TASK_QUEUE_NAME,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "'");
            }

        }
    }

}
