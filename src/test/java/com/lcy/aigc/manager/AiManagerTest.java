package com.lcy.aigc.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class AiManagerTest {
    @Resource
    private AiManager aiManager;

    @Test
    void doCht() {
        String message = "你好，今天是周几";
        String s = aiManager.doChat(message);
        System.out.println(s);
    }
}