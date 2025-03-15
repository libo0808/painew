package com.pansophicmind.server.aidog.vad;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;
import be.tarsos.dsp.pitch.PitchProcessor;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class WavVadDetection2 {

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;
    private static final int FRAME_SIZE = 960;

    public static boolean process(byte[] wavAudio) {
        log.error("wavAudio length: " + wavAudio.length);
        try {
            // 使用提供的 AudioFormat
            AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, 16, CHANNELS, CHANNELS * 2, SAMPLE_RATE, false);

            // 创建音频输入流
            ByteArrayInputStream pcmInputStream = new ByteArrayInputStream(wavAudio);
            AudioInputStream audioInputStream = new AudioInputStream(pcmInputStream, targetFormat, wavAudio.length / (2 * CHANNELS));

            AudioFormat format = audioInputStream.getFormat();
            JVMAudioInputStream jvmAudioInputStream = new JVMAudioInputStream(audioInputStream);

            // 用于存储检测结果的变量
            AtomicBoolean voiceActivityDetected = new AtomicBoolean(false);

            PitchProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, format.getSampleRate(), FRAME_SIZE, (pitchDetectionResult, audioEvent) -> {
                double pitch = pitchDetectionResult.getPitch();
                log.error("--------------------------------------- pitch：" + pitch);
                // 简单VAD：如果pitch值大于某个阈值，则认为是语音活动
                if (pitch > 100) {
                    voiceActivityDetected.set(true);
                }
            });

            AudioDispatcher dispatcher = new AudioDispatcher(jvmAudioInputStream, FRAME_SIZE, 0);
            dispatcher.addAudioProcessor(pitchProcessor);

            // 启动音频处理线程
            Thread dispatcherThread = new Thread(dispatcher, "Audio Dispatcher");
            dispatcherThread.start();

            // 等待音频处理线程结束
            dispatcherThread.join();

            audioInputStream.close();
            jvmAudioInputStream.close();

            // 打印并返回检测结果
            return voiceActivityDetected.get();
        } catch (Exception e) {
            log.error("处理音频时发生异常: " + e.getMessage());
            return false;
        }
    }
}
