package com.pansophicmind.server.aidog.common.ws.interf;

import com.pansophicmind.server.aidog.common.ws.enums.TextMessageTypeEnum;
import com.pansophicmind.server.aidog.common.ws.service.DeviceConnectionService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public abstract class TextMessageHandlerAbstract implements TextMessageHandler {

    @Resource
    protected DeviceConnectionService deviceConnectionService;
    private final TextMessageTypeEnum type;

    protected TextMessageHandlerAbstract(TextMessageTypeEnum type) {
        this.type = type;
    }

    @Override
    public TextMessageTypeEnum getType() {
        return type;
    }

}
