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
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Slf4j
public class OpusConverterWav2 {

    private static final int SAMPLE_RATE = 16000;
    private static final int CHANNELS = 1;
    private static final int MAX_FRAME_SIZE = 960; // 120ms 的帧大小（16kHz 时）

    public interface OpusLibrary extends Library {
        OpusLibrary INSTANCE = Native.load("opus", OpusLibrary.class);

        long opus_decoder_create(int fs, int channels, IntByReference error);

        int opus_decode(long decoder, byte[] data, int len, short[] pcm, int frame_size, int decode_fec);

        void opus_decoder_destroy(long decoder);
    }

    public static void convert(byte[] opusData, String filepath) throws Exception {
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

            // 转换为小端序字节数组
            ByteBuffer byteBuffer = ByteBuffer.allocate(pcmData.length * 2);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            for (short s : pcmData) {
                byteBuffer.putShort(s);
            }
            byte[] pcmBytes = byteBuffer.array();

            AudioFormat format = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    SAMPLE_RATE,
                    16,
                    CHANNELS,
                    CHANNELS * 2, // 正确 frameSize（单声道 16-bit 为 2 字节）
                    SAMPLE_RATE,
                    false
            );

            ByteArrayInputStream bais = new ByteArrayInputStream(pcmBytes);
            AudioInputStream audioStream = new AudioInputStream(bais, format, decodedFrames);

            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, new File(filepath));
            audioStream.close();
        } finally {
            OpusLibrary.INSTANCE.opus_decoder_destroy(decoder);
        }
    }
}
