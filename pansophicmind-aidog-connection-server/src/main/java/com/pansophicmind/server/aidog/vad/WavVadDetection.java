//package com.pansophicmind.server.aidog.vad;
//
//import be.tarsos.dsp.AudioDispatcher;
//import be.tarsos.dsp.AudioEvent;
//import be.tarsos.dsp.AudioProcessor;
//import be.tarsos.dsp.SilenceDetector;
//import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
//import lombok.extern.slf4j.Slf4j;
//
//import javax.sound.sampled.AudioFormat;
//
//@Slf4j
//public class WavVadDetection {
//
//    private static final int SAMPLE_RATE = 16000;
//    private static final int CHANNELS = 1;
//    private static final int FRAME_SIZE = 960;
//
//    public static boolean process(byte[] wavAudio) {
//        log.error("wavAudio length: " + wavAudio.length);
//        try {
//            // 使用提供的 AudioFormat
//            AudioFormat targetFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, 16, CHANNELS, 2, SAMPLE_RATE, false);
//            // 用于存储检测结果的变量
//            AudioDispatcher dispatcher = AudioDispatcherFactory.fromByteArray(wavAudio, targetFormat, FRAME_SIZE, 0);
//
//            SilenceDetector silenceDetector = new SilenceDetector();
//            dispatcher.addAudioProcessor(silenceDetector);
//
//            dispatcher.addAudioProcessor(new AudioProcessor() {
//                @Override
//                public boolean process(AudioEvent audioEvent) {
//                    if (!silenceDetector.isSilence(audioEvent.getFloatBuffer())) {
//                        log.error("--------------------------------------- 有声音");
//                    } else {
//                        log.error("--------------------------------------- 静音");
//                    }
//                    return true;
//                }
//
//                @Override
//                public void processingFinished() {
//                    log.error("处理完成");
//                }
//            });
//
//            // 启动音频处理线程
//            Thread dispatcherThread = new Thread(dispatcher, "Audio Dispatcher");
//            dispatcherThread.start();
//
//            // 等待音频处理线程结束
//            dispatcherThread.join();
//
//            return false;
//        } catch (Exception e) {
//            log.error("处理音频时发生异常: " + e.getMessage());
//            return false;
//        }
//    }
//
//}
