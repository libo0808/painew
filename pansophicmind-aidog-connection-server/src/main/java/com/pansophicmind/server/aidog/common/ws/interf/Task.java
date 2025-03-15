package com.pansophicmind.server.aidog.common.ws.interf;

import javax.websocket.Session;

public interface Task {
    boolean isCancelled();

    boolean isOverTime(int interruptTime);

    void cancel();

    Session getSession();
}