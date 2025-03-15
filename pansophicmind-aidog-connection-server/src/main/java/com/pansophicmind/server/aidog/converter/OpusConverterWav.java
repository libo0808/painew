//package com.pansophicmind.server.aidog.converter;
//
//import com.sun.jna.Library;
//import com.sun.jna.Native;
//import com.sun.jna.Pointer;
//import com.sun.jna.ptr.IntByReference;
//import lombok.extern.slf4j.Slf4j;
//
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//import java.nio.charset.StandardCharsets;
//
//@Slf4j
//public class OpusConverterWav {
//
//    public interface Opus extends Library {
//        Opus INSTANCE = Native.load("opus", Opus.class);
//
//        long opus_decoder_create(int fs, int channels, Pointer error);
//
//        int opus_decode(long decoder, byte[] data, int len, short[] pcm, int frame_size, int decode_fec);
//
//        void opus_decoder_destroy(long decoder);
//    }
//
//    public static byte[] convert(byte[] opusData, int sampleRate, int channels) throws Exception {
//        // 初始化解码器
//        IntByReference error = new IntByReference();
//        long decoder = Opus.INSTANCE.opus_decoder_create(sampleRate, channels, error.getPointer());
//        if (error.getValue() != 0) {
//            throw new Exception("Failed to create Opus decoder. Error code: " + error.getValue());
//        }
//
//        // 解码Opus数据，假设每个包最多解码为5760 samples per channel（120ms）
//        int frameSize = 5760; // 根据实际情况调整
//        short[] pcmBuffer = new short[frameSize * channels];
//        int decodedSamples = Opus.INSTANCE.opus_decode(decoder, opusData, opusData.length, pcmBuffer, frameSize, 0);
//        if (decodedSamples < 0) {
//            Opus.INSTANCE.opus_decoder_destroy(decoder);
//            throw new Exception("Decode failed. Error code: " + decodedSamples);
//        }
//
//        // 转换PCM数据到小端字节序的byte[]
//        ByteBuffer byteBuffer = ByteBuffer.allocate(pcmBuffer.length * 2);
//        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
//        for (short s : pcmBuffer) {
//            byteBuffer.putShort(s);
//        }
//        byte[] pcmBytes = byteBuffer.array();
//
//        // 生成WAV头
//        byte[] wavHeader = createWavHeader(sampleRate, channels, 16, pcmBytes.length);
//
//        // 合并头和PCM数据
//        byte[] wavBytes = new byte[wavHeader.length + pcmBytes.length];
//        System.arraycopy(wavHeader, 0, wavBytes, 0, wavHeader.length);
//        System.arraycopy(pcmBytes, 0, wavBytes, wavHeader.length, pcmBytes.length);
//
//        // 销毁解码器
//        Opus.INSTANCE.opus_decoder_destroy(decoder);
//
//        return wavBytes;
//    }
//
//    private static byte[] createWavHeader(int sampleRate, int numChannels, int bitsPerSample, int dataSize) {
//        ByteBuffer header = ByteBuffer.allocate(44);
//        header.order(ByteOrder.LITTLE_ENDIAN);
//
//        // RIFF头
//        header.put("RIFF".getBytes(StandardCharsets.US_ASCII));
//        header.putInt(36 + dataSize); // ChunkSize: 4 + (24 + dataSize)
//        header.put("WAVE".getBytes(StandardCharsets.US_ASCII));
//
//        // fmt子块
//        header.put("fmt ".getBytes(StandardCharsets.US_ASCII));
//        header.putInt(16); // SubChunk1Size
//        header.putShort((short) 1); // AudioFormat: PCM
//        header.putShort((short) numChannels);
//        header.putInt(sampleRate);
//        header.putInt(sampleRate * numChannels * bitsPerSample / 8); // ByteRate
//        header.putShort((short) (numChannels * bitsPerSample / 8)); // BlockAlign
//        header.putShort((short) bitsPerSample);
//
//        // data子块
//        header.put("data".getBytes(StandardCharsets.US_ASCII));
//        header.putInt(dataSize); // SubChunk2Size
//
//        header.flip();
//        return header.array();
//    }
//
//}
