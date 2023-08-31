package com.lcy.aigc.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class DeadConsumer {
    public static final String DEAD_EXCHANGE_NAME = "dead-direct-exchange";
    public static final String EXCHANGE_NAME = "direct-exchange";

    public static void main(String[] argv) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setPassword("158574");
        factory.setHost("192.168.161.128");
        factory.setUsername("lcy");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        //创建一个死信交换机
        channel.exchangeDeclare(DEAD_EXCHANGE_NAME,"direct");
        //创建direct交换机
        channel.exchangeDeclare(EXCHANGE_NAME,"direct");

        //设置死信参数
        Map<String,Object> map1 = new HashMap<>();
        //死信交换机
        map1.put("x-dead-letter-exchange",DEAD_EXCHANGE_NAME);
        //死信队列
        map1.put("x-dead-letter-routing-key","deadxiaowang");
        //map参数就是绑定了死信队列，和指定路由
        //声明队列
        String queueName1 = "xiaowang-queue";
        channel.queueDeclare(queueName1,true,false,false,map1);
        channel.queueBind(queueName1,EXCHANGE_NAME,"xiaowang");

        //设置死信参数
        Map<String,Object> map2 = new HashMap<>();
        //死信交换机
        map2.put("x-dead-letter-exchange",DEAD_EXCHANGE_NAME);
        //死信队列
        map2.put("x-dead-letter-routing-key","deadxiaoli");
        //声明队列
        String queueName2 = "xiaoli-queue";
        channel.queueDeclare(queueName2,true,false,false,map2);
        channel.queueBind(queueName2,EXCHANGE_NAME,"xiaoli");
        //正常的小王小李处理消息
        DeliverCallback deliverCallback1 = (consumerTag, message) -> {
            String ss = new String(message.getBody(), StandardCharsets.UTF_8);
            System.out.println("xaiowang  receive "+ ss );
        };

        DeliverCallback deliverCallback2 = (consumerTag, message) -> {
            String ss = new String(message.getBody(), StandardCharsets.UTF_8);
            System.out.println("xaili  receive "+ ss );
        };


        //创建两个死信队列
        String deadQueue1 = "dead1-queue";
        String deadQueue2 = "dead2-queue";
        //死信队列1
        channel.queueDeclare(deadQueue1,true,false,false,null);
        //死信队列2
        channel.queueDeclare(deadQueue2,true,false,false,null);
        //专门处理小王无法处理得死信
        channel.queueBind(deadQueue1,DEAD_EXCHANGE_NAME,"deadxiaowang");
        //专门处理小李无法处理的死信
        channel.queueBind(deadQueue2,DEAD_EXCHANGE_NAME,"deadxiaoli");

        //处理小王小李无法处理的消息
        DeliverCallback deliverCallback3 = (consumerTag, message) -> {
            String ss = new String(message.getBody(), StandardCharsets.UTF_8);
            System.out.println("dead1  receive "+ ss );
        };

        DeliverCallback deliverCallback4 = (consumerTag, message) -> {
            String ss = new String(message.getBody(), StandardCharsets.UTF_8);
            System.out.println("dead2  receive "+ ss );
        };
//        channel.basicConsume(queueName1,deliverCallback1,consumerTag -> {});
        channel.basicConsume(queueName2,deliverCallback2,consumerTag -> {});
        channel.basicConsume(deadQueue1,deliverCallback3,consumerTag -> {});
        channel.basicConsume(deadQueue2,deliverCallback4,consumerTag -> {});
    }
}
