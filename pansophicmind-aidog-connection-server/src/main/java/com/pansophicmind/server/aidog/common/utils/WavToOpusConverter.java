package com.pansophicmind.server.aidog.common.utils;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
@Slf4j
public class WavToOpusConverter {
    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;
    private static final int FRAME_SIZE = 960;
    private static final int APPLICATION = 2048; // OPUS_APPLICATION_AUDIO

    public static void convertWavToOpus(String wavFilePath, String opusFilePath) {
        try {
            // 读取 WAV 文件
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(wavFilePath));
            AudioFormat audioFormat = audioInputStream.getFormat();

            // 检查音频格式
            if (audioFormat.getSampleRate() != SAMPLE_RATE || audioFormat.getChannels() != CHANNELS) {
                throw new IllegalArgumentException("不支持的音频格式，采样率或声道数不匹配");
            }

            // 创建 Opus 编码器
            IntByReference error = new IntByReference();
            Pointer encoder = OpusLibrary.INSTANCE.opus_encoder_create(FRAME_SIZE, CHANNELS, APPLICATION, error);
            if (error.getValue() != 0) {
                throw new RuntimeException("无法创建 Opus 编码器，错误码: " + error.getValue());
            }

            ByteArrayOutputStream opusOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[FRAME_SIZE * CHANNELS * (audioFormat.getSampleSizeInBits() / 8)];
            short[] pcmBuffer = new short[FRAME_SIZE * CHANNELS];
            byte[] opusBuffer = new byte[4096];

            int bytesRead;
            while ((bytesRead = audioInputStream.read(buffer)) != -1) {
                // 将字节数组转换为 short 数组
                for (int i = 0; i < bytesRead / 2; i++) {
                    pcmBuffer[i] = (short) ((buffer[i * 2] & 0xff) | (buffer[i * 2 + 1] << 8));
                }
                // 编码 PCM 数据
                int encodedBytes = OpusLibrary.INSTANCE.opus_encode(encoder, pcmBuffer, FRAME_SIZE, opusBuffer, opusBuffer.length);
                if (encodedBytes < 0) {
                    throw new RuntimeException("Opus 编码失败，错误码: " + encodedBytes);
                }

                // 写入编码后的 Opus 数据
                opusOutputStream.write(opusBuffer, 0, encodedBytes);
            }

            // 关闭输入流
            audioInputStream.close();

            // 销毁编码器
            OpusLibrary.INSTANCE.opus_encoder_destroy(encoder);

            // 将 Opus 数据写入文件
            java.nio.file.Files.write(new File(opusFilePath).toPath(), opusOutputStream.toByteArray());

            log.info("WAV 文件已成功转换为 Opus 文件: " + opusFilePath);
        } catch (UnsupportedAudioFileException | IOException e) {
            e.printStackTrace();
        }
    }
}