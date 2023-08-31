package com.lcy.aigc.manager;

import com.lcy.aigc.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AiManager {
    @Resource
    private YuCongMingClient yuCongMingClient;

    public  String doChat(String questions) {
        DevChatRequest devChatRequest = new DevChatRequest();
        devChatRequest.setModelId(1693876669409927169L);
        devChatRequest.setMessage(questions);
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        if (response == null){
            throw new BusinessException(100010,"Ai响应异常");
        }
        return response.getData().getContent();
    }
}
