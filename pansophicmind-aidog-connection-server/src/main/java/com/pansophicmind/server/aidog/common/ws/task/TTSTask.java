package com.pansophicmind.server.aidog.common.ws.task;

import com.pansophicmind.server.aidog.common.ws.interf.Task;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.websocket.Session;

// 定义 TTS 任务类
@EqualsAndHashCode(callSuper = true)
@Data
public class TTSTask extends BaseTask implements Task {
    /*
     * 文本内容
     */
    private String llmResult;

    public TTSTask(Session session, String llmResult, long asrStartTime) {
        this.setSession(session);
        this.llmResult = llmResult;
        this.setAsrStartTime(asrStartTime);
    }
}