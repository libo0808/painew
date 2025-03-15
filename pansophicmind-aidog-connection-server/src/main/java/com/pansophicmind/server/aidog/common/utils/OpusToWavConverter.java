package com.pansophicmind.server.aidog.common.utils;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

@Slf4j
public class OpusToWavConverter {
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;
    private static final int FRAME_SIZE = 960;

    public static void convertOpusToWav(String opusFilePath, String wavFilePath) {
        try {
            // 读取 Opus 文件
            byte[] opusData = java.nio.file.Files.readAllBytes(new File(opusFilePath).toPath());

            // 创建 Opus 解码器
            IntByReference error = new IntByReference();
            Pointer decoder = OpusLibrary.INSTANCE.opus_decoder_create(SAMPLE_RATE, CHANNELS, error);
            if (error.getValue() != 0) {
                throw new RuntimeException("无法创建 Opus 解码器，错误码: " + error.getValue());
            }

            // 解码 Opus 数据
            short[] pcmData = new short[opusData.length * 2];
            int decodedFrames = OpusLibrary.INSTANCE.opus_decode(decoder, opusData, opusData.length, pcmData, FRAME_SIZE, 0);
            if (decodedFrames < 0) {
                throw new RuntimeException("Opus 解码失败，错误码: " + decodedFrames);
            }

            // 将 short 数组转换为字节数组
            byte[] pcmBytes = new byte[pcmData.length * 2];
            for (int i = 0; i < pcmData.length; i++) {
                pcmBytes[i * 2] = (byte) (pcmData[i] & 0xff);
                pcmBytes[i * 2 + 1] = (byte) ((pcmData[i] >> 8) & 0xff);
            }

            // 使用你提供的 AudioFormat
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    SAMPLE_RATE,
                    16,
                    CHANNELS,
                    2,
                    SAMPLE_RATE,
                    false
            );

            // 创建音频输入流
            ByteArrayInputStream pcmInputStream = new ByteArrayInputStream(pcmBytes);
            AudioInputStream audioInputStream = new AudioInputStream(pcmInputStream, targetFormat, pcmData.length / CHANNELS);

            // 将音频流写入 WAV 文件
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File(wavFilePath));

            // 关闭输入流
            audioInputStream.close();

            // 销毁解码器
            OpusLibrary.INSTANCE.opus_decoder_destroy(decoder);

            log.info("Opus 文件已成功转换为 WAV 文件: " + wavFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
