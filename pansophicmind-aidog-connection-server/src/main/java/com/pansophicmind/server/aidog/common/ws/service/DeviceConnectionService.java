package com.pansophicmind.server.aidog.common.ws.service;

import com.pansophicmind.server.aidog.common.ws.enums.ClientStateEnum;
import com.pansophicmind.server.aidog.common.ws.enums.ListenStatsEnum;
import com.pansophicmind.server.aidog.common.ws.enums.TtsStatsEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 设备连接管理类
@Component
@Slf4j
public class DeviceConnectionService {
    // 设备连接
    private final Map<String, Session> deviceConnections = new ConcurrentHashMap<>();
    // 设备连接状态
    private final Map<String, ClientStateEnum> clientStates = new ConcurrentHashMap<>();
    //tts状态
    private final Map<String, TtsStatsEnum> ttsStates = new ConcurrentHashMap<>();

    //tts状态
    private final Map<String, ListenStatsEnum> listenStates = new ConcurrentHashMap<>();

    /**
     * 管理设备连接
     *
     * @param session 设备连接
     */
    public void addConnection(Session session) {
        deviceConnections.put(session.getId(), session);
        clientStates.put(session.getId(), ClientStateEnum.CONNECTING);
        log.info("会话ID:[{}] WEB-SOCKET连接成功", session.getId());

    }

    /**
     * 移除设备连接
     *
     * @param deviceId 此处为会话ID
     */
    public void removeConnection(String deviceId) {deviceConnections.remove(deviceId);
        clientStates.remove(deviceId);
        log.info("会话ID:[{}] WEB-SOCKET断开连接", deviceId);
    }

    /**
     * 获取设备连接
     *
     * @param deviceId 此处为会话ID
     * @return 设备连接
     */
    public Session getConnection(String deviceId) {
        return deviceConnections.get(deviceId);
    }

    /**
     * 获取设备连接状态
     *
     * @param deviceId 设备ID
     * @return 设备连接状态
     */
    public ClientStateEnum getClientState(String deviceId) {
        return clientStates.get(deviceId);
    }

    /**
     * 更新设备连接状态
     *
     * @param deviceId 设备ID
     * @param state    设备连接状态
     */
    public void updateClientState(String deviceId, ClientStateEnum state) {
        clientStates.put(deviceId, state);
    }

    /**
     * 获取设备tts状态
     *
     * @param deviceId 设备ID
     * @return 设备tts状态
     */
    public TtsStatsEnum getTtsState(String deviceId) {
        return ttsStates.get(deviceId);
    }

    /**
     * 更新tts状态
     *
     * @param deviceId 设备ID
     * @param state    tts状态
     */
    public void updateTtsState(String deviceId, TtsStatsEnum state) {
        ttsStates.put(deviceId, state);
    }


    /**
     * 获取设备listen状态
     *
     * @param deviceId 设备ID
     * @return 设备listen状态
     */
    public ListenStatsEnum getListenState(String deviceId) {
        return listenStates.get(deviceId);
    }
    /**
     * 更新listen状态
     *
     * @param deviceId 设备ID
     * @param state    listen状态
     */
    public void updateListenState(String deviceId, String state) {
        switch (state) {
            case "start":
                listenStates.put(deviceId, ListenStatsEnum.start);
                break;
            case "stop":
                listenStates.put(deviceId, ListenStatsEnum.stop);
                break;
            case "end":
                listenStates.put(deviceId, ListenStatsEnum.detect);
                break;
                default:
                    log.error("listen状态错误");
                    break;
        }
    }
}