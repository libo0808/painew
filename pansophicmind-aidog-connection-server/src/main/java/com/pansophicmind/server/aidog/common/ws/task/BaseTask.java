package com.pansophicmind.server.aidog.common.ws.task;

import cn.hutool.log.StaticLog;
import lombok.Data;

import javax.websocket.Session;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
public class BaseTask {
    /*
     * webSocket会话
     */
    private Session session;

    /*
     *取消状态
     */
    private AtomicBoolean cancelled = new AtomicBoolean(false);

    /*
     * asr任务开始时间
     */
    private long asrStartTime;

    /**
     * 任务ID
     */
    private String taskId;

    public void cancel() {
        cancelled.set(true);
    }

    public boolean isCancelled() {
        return cancelled.get();
    }

    public boolean isOverTime(int interruptTime) {
        // 计算时间差
        long second = (System.currentTimeMillis() - asrStartTime) / 1000;
        //是否超过打断设定的时间
        return second < interruptTime;
    }
}
