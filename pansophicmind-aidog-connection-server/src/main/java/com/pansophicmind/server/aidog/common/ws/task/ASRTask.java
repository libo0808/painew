package com.pansophicmind.server.aidog.common.ws.task;

import com.pansophicmind.server.aidog.common.ws.interf.Task;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.websocket.Session;
import java.util.concurrent.atomic.AtomicBoolean;

// 定义 ASR 任务类
@EqualsAndHashCode(callSuper = true)
@Data
public class ASRTask extends BaseTask implements Task {
    /*
     * 音频文件地址
     */
    private String filePath;

    private AtomicBoolean isStart;


    public ASRTask(Session session, String filePath, long asrStartTime, String taskId) {
        this.filePath = filePath;
        this.setSession(session);
        this.setAsrStartTime(asrStartTime);
        this.setTaskId(taskId);
    }
}