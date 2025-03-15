package com.pansophicmind.server.aidog.common.ws.interf;

public interface TaskProcessor<T extends Task> {
    String process(T task) throws InterruptedException;

    void submitNextTask(T task, String result);
}