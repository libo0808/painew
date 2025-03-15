package com.pansophicmind.server.aidog.common.ws.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.pansophicmind.server.aidog.common.ws.enums.TextMessageTypeEnum;
import com.pansophicmind.server.aidog.common.ws.factory.TextMessageHandlerFactory;
import com.pansophicmind.server.aidog.common.ws.interf.Task;
import com.pansophicmind.server.aidog.common.ws.interf.TaskProcessor;
import com.pansophicmind.server.aidog.common.ws.task.ASRTask;
import com.pansophicmind.server.aidog.common.ws.task.LLMTask;
import com.pansophicmind.server.aidog.common.ws.task.TTSTask;
import com.pansophicmind.server.aidog.converter.OpusConverterWav3;
import com.pansophicmind.server.third.asr.enums.ThirdAsrAudioFormatEnum;
import com.pansophicmind.server.third.asr.enums.ThirdAsrAudioSampleRateEnum;
import com.pansophicmind.server.third.asr.enums.ThirdAsrPlatformEnum;
import com.pansophicmind.server.third.asr.factory.ThirdAsrHandlerFactory;
import com.pansophicmind.server.third.llm.dto.ChatWithAgentResultDTO;
import com.pansophicmind.server.third.llm.enums.ThirdLlmPlatformEnum;
import com.pansophicmind.server.third.llm.factory.ThirdLlmHandlerFactory;
import com.pansophicmind.server.third.tts.enums.ThirdTtsAudioFormatEnum;
import com.pansophicmind.server.third.tts.enums.ThirdTtsAudioSampleRateEnum;
import com.pansophicmind.server.third.tts.enums.ThirdTtsPlatformEnum;
import com.pansophicmind.server.third.tts.factory.ThirdTtsHandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import com.pansophicmind.server.aidog.common.ws.enums.*;
import com.pansophicmind.server.aidog.common.ws.utils.*;
import com.pansophicmind.server.aidog.vad.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.websocket.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 二进制音频数据（Opus 编码）
 */
@Component
@Slf4j
public class BinaryMessageService {
    @Value("${audioHandler.config.interrupt.enable}")
    private Boolean interruptEnable;
    @Value("${audioHandler.config.interrupt.time}")
    private Integer interruptTime;
    @Value("${audioHandler.config.queue.capacity}")
    private int queueCapacity;
    @Value("${audioHandler.config.temp.path}")
    private String tempPath;

    @Resource
    private ThreadPoolTaskExecutor executorService;
    @Resource
    private ThirdAsrHandlerFactory thirdAsrHandlerFactory;
    @Resource
    private ThirdTtsHandlerFactory thirdTtsHandlerFactory;
    @Resource
    private ThirdLlmHandlerFactory thirdLlmHandlerFactory;
    @Resource
    private TextMessageHandlerFactory textMessageHandlerFactory;
    @Resource
    private DeviceConnectionService deviceConnectionService;

    // 存储当前待处理的各项任务
    private BlockingQueue<ASRTask> asrQueue;
    private BlockingQueue<LLMTask> llmQueue;
    private BlockingQueue<TTSTask> ttsQueue;
    private final AtomicBoolean running = new AtomicBoolean(true);
    // 存储当前会话正在执行的各项任务
    private final Map<Session, ASRTask> activeASRTasks = new HashMap<>();
    private final Map<Session, LLMTask> activeLLMTasks = new HashMap<>();
    private final Map<Session, TTSTask> activeTTSTasks = new HashMap<>();

    @PostConstruct
    /*
      初始化工作任务
     */
    private void initWorkTasks() {
        this.asrQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.llmQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.ttsQueue = new LinkedBlockingQueue<>(queueCapacity);
        executorService.submit(() -> processTasks(asrQueue, activeASRTasks, new TaskProcessor<ASRTask>() {
            @Override
            public String process(ASRTask task) {
                return performASR(task.getFilePath(), task.getSession().getId());
            }

            @Override
            public void submitNextTask(ASRTask task, String result) {
                if (ObjectUtil.isEmpty(result)) {
                    return;
                }
                LLMTask llmTask = new LLMTask(task.getSession(), result, task.getAsrStartTime());
                try {
                    activeLLMTasks.put(task.getSession(), llmTask);
                    llmQueue.put(llmTask);
                } catch (InterruptedException e) {
                    log.error("将 LLM 任务放入队列时出错", e);
                }
            }
        }));

        executorService.submit(() -> processTasks(llmQueue, activeLLMTasks, new TaskProcessor<LLMTask>() {
            @Override
            public String process(LLMTask task) {
                return performLLM(task.getAsrResult());
            }

            @Override
            public void submitNextTask(LLMTask task, String result) {
                TTSTask ttsTask = new TTSTask(task.getSession(), result, task.getAsrStartTime());
                try {
                    activeTTSTasks.put(task.getSession(), ttsTask);
                    ttsQueue.put(ttsTask);
                } catch (InterruptedException e) {
                    log.error("将 TTS 任务放入队列时出错", e);
                }
            }
        }));

        executorService.submit(() -> processTasks(ttsQueue, activeTTSTasks, new TaskProcessor<TTSTask>() {
            @Override
            public String process(TTSTask task) {
                Object ttsResult = performTTS(task.getLlmResult());
                return convertToOpus(ttsResult, task.getSession());
            }

            @Override
            public void submitNextTask(TTSTask task, String result) {
                try {
                    if (!task.isCancelled() && task.getSession().isOpen()) {
                        task.getSession().getBasicRemote().sendText(result);
                    }
                } catch (Exception e) {
                    log.error("发送 TTS 结果时出错", e);
                }
            }
        }));
    }

    /*
     * 关闭工作任务
     */
    @PreDestroy
    private void shutdown() {
        running.set(false);
        executorService.shutdown();
    }

    /**
     * 处理音频流
     *
     * @param session   WebSocket 会话
     * @param opusAudio 音频字节流（opus编码）
     */
    public synchronized void handleAudioStream(Session session, byte[] opusAudio) {
        try {
            byte[] wavAudio = OpusConverterWav3.convert(opusAudio);
            boolean isSpeech = WavVadDetection2.process(wavAudio);
            if (isSpeech) {
                byte[] sessionWavAudio = WebsocketSessionData.getSessionWavAudio(session, wavAudio);
                if (sessionWavAudio.length > 200000) {
                    WebsocketSessionData.removeSessionWavAudio(session);
                    log.error("------------------------ isSpeech：" + isSpeech);
                    FileUtil.writeBytes(sessionWavAudio, "/pai/server/temp/" + System.currentTimeMillis() + ".wav");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

//            String sessionId = session.getId();
//            //1、opus转wav
//            long currentTimeMillis = System.currentTimeMillis();
//            //文件名称（本次交互的任务ID）
//            String fileName = sessionId + "-asr" + currentTimeMillis + "liboshishabi.opus";
//            String filePath = tempPath + fileName;
//            //2、写入文件
//            FileUtil.writeBytes(finalAudio, filePath);
//            //3、检查是否有相同 session 的任务正在执行，若有则终止
//            cancelActiveTasks(session);
//            //4、将 ASR 任务放入队列
//            ASRTask asrTask = new ASRTask(session, filePath, currentTimeMillis, fileName);
//            activeASRTasks.put(session, asrTask);
//            log.info("将 ASR 任务放入队列，sessionId: {}", sessionId);
//            try {
//                asrQueue.put(asrTask);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
    }

    /**
     * 添加任务到队列
     */
    public void addTaskToQueue(String type, Session session, String data, String taskId) {
        switch (type) {
            case "asr":
                ASRTask asrTask = new ASRTask(session, data, System.currentTimeMillis(), taskId);
                try {
                    asrQueue.put(asrTask);
                } catch (InterruptedException e) {
                    log.error("将 ASR 任务放入队列时出错", e);
                }
                break;
            case "llm":
                LLMTask llmTask = new LLMTask(session, data, System.currentTimeMillis());
                try {
                    llmQueue.put(llmTask);
                } catch (InterruptedException e) {
                    log.error("将 ASR 任务放入队列时出错", e);
                }
                break;
            case "tts":
                TTSTask ttsTask = new TTSTask(session, data, System.currentTimeMillis());
                try {
                    ttsQueue.put(ttsTask);
                } catch (InterruptedException e) {
                    log.error("将 TTS 任务放入队列时出错", e);
                }
            default:
                log.info("不支持的任务类型");
                break;
        }
    }


    /**
     * 终止当前会话正在执行的各项任务
     *
     * @param session WebSocket 会话
     */
    private void cancelActiveTasks(Session session) {
        if (!interruptEnable) {
            return;
        }
        try {
            // 终止当前会话正在执行的各项任务
            ASRTask asrTask = activeASRTasks.get(session);
            if (asrTask != null && asrTask.isOverTime(interruptTime)) {
                asrTask.cancel();
                activeASRTasks.remove(session);
                log.info("终止当前会话正在执行的 ASR 任务，sessionId: {}", session.getId());
            }
            LLMTask llmTask = activeLLMTasks.get(session);
            if (llmTask != null && llmTask.isOverTime(interruptTime)) {
                llmTask.cancel();
                activeLLMTasks.remove(session);
                log.info("终止当前会话正在执行的 LLM 任务，sessionId: {}", session.getId());
            }
            TTSTask ttsTask = activeTTSTasks.get(session);
            if (ttsTask != null && ttsTask.isOverTime(interruptTime)) {
                ttsTask.cancel();
                activeTTSTasks.remove(session);
                log.info("终止当前会话正在执行的 TTS 任务，sessionId: {}", session.getId());
            }
        } catch (Exception e) {
            log.error("终止当前会话正在执行的任务时出错", e);
        }
    }

    private <T extends Task> void processTasks(BlockingQueue<T> queue, Map<Session, T> activeTasks, TaskProcessor<T> processor) {
        while (running.get()) {
            try {
                T task = queue.take();
                if (task.isCancelled()) {
                    continue;
                }
                activeTasks.put(task.getSession(), task);
                String result = processor.process(task);
                if (result != null && !task.isCancelled()) {
                    processor.submitNextTask(task, result);
                }
                activeTasks.remove(task.getSession());
            } catch (InterruptedException e) {
                log.error("从队列取出任务时出错", e);
            } catch (Exception e) {
                log.error("执行任务时出错", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private String performASR(String audioFilePath, String sessionId) {
        if (deviceConnectionService.getListenState(sessionId) == ListenStatsEnum.stop) {
            log.info("客户端说话完毕，开始 ASR");
            // 实现 ASR 逻辑
            String speechToText = thirdAsrHandlerFactory.getHandler(ThirdAsrPlatformEnum.ALIYUN).speechToText(null, audioFilePath, ThirdAsrAudioFormatEnum.OPUS, ThirdAsrAudioSampleRateEnum.RATE_16000);
            if (StrUtil.isNotBlank(speechToText)) {
                log.info("ASR 结果: {}", speechToText);
                return speechToText;
            }
            log.error("ASR 结果为空，任务终止");
        } else {
            log.info("当前客户端");
        }
        return null;
    }

    private String performLLM(String asrResult) {
        // 实现 TTS 逻辑
        ChatWithAgentResultDTO llmResult = thirdLlmHandlerFactory.getHandler(ThirdLlmPlatformEnum.ALIYUN).chatWithAgent(null, asrResult, null);
        log.info("LLM 结果: {}", llmResult);
        return llmResult == null ? "" : llmResult.getResponseText();
    }

    private String performTTS(String llmResult) {
        //1、实现 TTS 逻辑
        String filePath = tempPath + System.currentTimeMillis() + ".wav";
        thirdTtsHandlerFactory.getHandler(ThirdTtsPlatformEnum.COZE).textToSpeech(null, filePath, ThirdTtsAudioFormatEnum.OGG_OPUS, ThirdTtsAudioSampleRateEnum.RATE_16000, "7426720361753968677", llmResult);
        return filePath;
    }

    private String convertToOpus(Object ttsResult, Session session) {
        //服务器 → 客户端（TTS开始）
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.set("type", TextMessageTypeEnum.tts);
        jsonMessage.set("state", "start");
        textMessageHandlerFactory.getHandler(TextMessageTypeEnum.tts.name()).serverToClient(session, jsonMessage);
        // 实现音频转换逻辑
        return ttsResult.toString();
    }
}
