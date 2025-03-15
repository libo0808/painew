package com.pansophicmind.server.aidog.common.ws.utils;

import cn.hutool.core.util.ArrayUtil;

import javax.websocket.Session;

public class WebsocketSessionData {

    private static final String WAV_AUDIO = "sessionWavAudio";

    public static byte[] getSessionWavAudio(Session session, byte[] wavAudio) {
        byte[] sessionWavAudio = (byte[]) session.getUserProperties().get(WAV_AUDIO);
        if (sessionWavAudio == null) {
            sessionWavAudio = wavAudio;
        } else {
            sessionWavAudio = ArrayUtil.addAll(sessionWavAudio, wavAudio);
        }
        session.getUserProperties().put(WAV_AUDIO, sessionWavAudio);
        return sessionWavAudio;
    }

    public static void removeSessionWavAudio(Session session) {
        session.getUserProperties().remove(WAV_AUDIO);
    }

}
