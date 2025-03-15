package com.pansophicmind.server.aidog.common.ws.impl;

import cn.hutool.json.JSONObject;
import com.pansophicmind.server.aidog.common.utils.OpusToWavConverter;
import com.pansophicmind.server.aidog.common.utils.WavToOpusConverter;
import com.pansophicmind.server.aidog.common.ws.enums.TextMessageTypeEnum;
import com.pansophicmind.server.aidog.common.ws.interf.TextMessageHandlerAbstract;
import com.pansophicmind.server.third.tts.enums.ThirdTtsAudioFormatEnum;
import com.pansophicmind.server.third.tts.enums.ThirdTtsAudioSampleRateEnum;
import com.pansophicmind.server.third.tts.enums.ThirdTtsPlatformEnum;
import com.pansophicmind.server.third.tts.factory.ThirdTtsHandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.Session;

@Component
@Slf4j
public class TextMessageHandlerTest extends TextMessageHandlerAbstract {
    @Value("${audioHandler.config.temp.path}")
    private String tempPath;
    @Resource
    private ThirdTtsHandlerFactory thirdTtsHandlerFactory;

    protected TextMessageHandlerTest() {
        super(TextMessageTypeEnum.test);
    }

    @Override
    public void clientToServer(Session session, JSONObject message) {
        // WAV 转 Opus
        //1、实现 TTS 逻辑
        String c = System.currentTimeMillis() + "";
        String wavPath = tempPath + c + ".wav";
        thirdTtsHandlerFactory.getHandler(ThirdTtsPlatformEnum.COZE).textToSpeech(null, wavPath, ThirdTtsAudioFormatEnum.OGG_OPUS, ThirdTtsAudioSampleRateEnum.RATE_16000, "7426720361753968677", "请帮我介绍一下你自己");

        WavToOpusConverter.convertWavToOpus(wavPath, tempPath + "wavToOpus" + c + ".opus");
        log.info("WAV to Opus conversion completed successfully.");
        OpusToWavConverter.convertOpusToWav(tempPath + "wavToOpus" + c + ".opus", tempPath + "opusToWav" + c + ".wav");
        log.info("Opus to WAV conversion completed successfully.");

    }

    @Override
    public void serverToClient(Session session, JSONObject message) {
    }


}
