package com.lcy.aigc.controller;

import cn.hutool.json.JSONUtil;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
@RestController

@RequestMapping("/queue")
//@EnvCheck(mustEnv = "dev")
@Profile({"dev","local"}) //制定配置的生效环境
public class QueueController {

    @Resource
    private ThreadPoolExecutor poolExecutor;

    @PostMapping("/add")
    public void add(String name){
        //新建一个没有返回值的任务
        //runAsync 方法适用于那些不需要返回结果的异步任务，比如执行一些耗时的操作或者触发一些异步事件。
        // 可以通过方法链式调用，与其他 CompletableFuture 方法结合使用，来处理任务的结果、异常和其他操作。
        CompletableFuture.runAsync(() -> {
            System.out.println("任务执行中任务: "+ name + "当前执行线程: " + Thread.currentThread().getName());
            try {
                Thread.sleep(60000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        },poolExecutor);
    }

    @PostMapping("/get")
    public String get(){
        int size = poolExecutor.getQueue().size();
        int corePoolSize = poolExecutor.getCorePoolSize();
        long taskCount = poolExecutor.getTaskCount();
        long completedTaskCount = poolExecutor.getCompletedTaskCount();
        int activeCount = poolExecutor.getActiveCount();
        Map<String,Object> map = new HashMap<>();
        map.put("队列长度",size);
        map.put("核心线程数",corePoolSize);
        map.put("任务总数",taskCount);
        map.put("当前活动的线程数",activeCount);
        map.put("已完成任务数",completedTaskCount);
        return JSONUtil.toJsonStr(map);
    }


}
