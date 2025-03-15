package com.pansophicmind.server.aidog.common.ws.task;

import com.pansophicmind.server.aidog.common.ws.interf.Task;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.websocket.Session;

// 定义 LLM 任务类
@EqualsAndHashCode(callSuper = true)
@Data
public class LLMTask extends BaseTask implements Task {
    /*
     * 音频流
     */
    private String asrResult;

    public LLMTask(Session session, String asrResult, long asrStartTime) {
        this.asrResult = asrResult;
        this.setSession(session);
        this.setAsrStartTime(asrStartTime);
    }

}