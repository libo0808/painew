package com.pansophicmind.server.aidog.converter;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Slf4j
public class OpusConverterWav3 {

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;
    private static final int MAX_FRAME_SIZE = 960; // 120ms 的帧大小（16kHz 时）

    public interface OpusLibrary extends Library {
        OpusLibrary INSTANCE = Native.load("opus", OpusLibrary.class);

        long opus_decoder_create(int fs, int channels, IntByReference error);

        int opus_decode(long decoder, byte[] data, int len, short[] pcm, int frame_size, int decode_fec);

        void opus_decoder_destroy(long decoder);
    }

    public static byte[] convert(byte[] opusData) throws Exception {
        IntByReference error = new IntByReference();
        long decoder = OpusLibrary.INSTANCE.opus_decoder_create(SAMPLE_RATE, CHANNELS, error);
        if (error.getValue() != 0) {
            throw new RuntimeException("Decoder init failed: " + error.getValue());
        }

        try {
            short[] pcmData = new short[MAX_FRAME_SIZE * CHANNELS];
            int decodedFrames = OpusLibrary.INSTANCE.opus_decode(
                    decoder, opusData, opusData.length, pcmData, MAX_FRAME_SIZE, 0
            );
            if (decodedFrames < 0) {
                throw new RuntimeException("Decode error: " + decodedFrames);
            }

            // 计算实际解码的样本数并转换为字节数组
            int totalSamples = decodedFrames * CHANNELS;
            ByteBuffer byteBuffer = ByteBuffer.allocate(totalSamples * 2);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            for (int i = 0; i < totalSamples; i++) {
                byteBuffer.putShort(pcmData[i]);
            }
            byte[] pcmBytes = byteBuffer.array();

            // 创建音频格式
            AudioFormat format = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    SAMPLE_RATE,
                    16,
                    CHANNELS,
                    CHANNELS * 2, // 每帧的字节数（单声道16-bit为2字节）
                    SAMPLE_RATE,
                    false
            );

            // 创建音频输入流
            ByteArrayInputStream bais = new ByteArrayInputStream(pcmBytes);
            AudioInputStream audioStream = new AudioInputStream(
                    bais,
                    format,
                    pcmBytes.length / format.getFrameSize()
            );

            // 写入内存中的字节流
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, baos);
            return baos.toByteArray();
        } finally {
            OpusLibrary.INSTANCE.opus_decoder_destroy(decoder);
        }
    }
}
