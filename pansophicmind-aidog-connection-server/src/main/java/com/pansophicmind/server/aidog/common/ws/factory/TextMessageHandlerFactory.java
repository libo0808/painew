package com.pansophicmind.server.aidog.common.ws.factory;

import com.pansophicmind.server.aidog.common.ws.interf.TextMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
@Slf4j
public class TextMessageHandlerFactory {
    @Resource
    private List<TextMessageHandler> textMessageHandlerList;

    public TextMessageHandler getHandler(String textMessageType) {

        for (TextMessageHandler handler : textMessageHandlerList) {
            if (handler.getType().name().equals(textMessageType)) {
                return handler;
            }
        }
        log.info("TextMessageHandlerFactory getHandler textMessageType:{}", textMessageType);
        throw new IllegalArgumentException("未找到对应TextMessage处理策略" + textMessageType);
    }

}
