package com.pansophicmind.server.aidog.common.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public interface OpusLibrary extends Library {
    OpusLibrary INSTANCE = Native.load("opus", OpusLibrary.class);

    // 创建 Opus 编码器
    Pointer opus_encoder_create(int Fs, int channels, int application, IntByReference error);

    // 编码 PCM 数据
    int opus_encode(Pointer st, short[] pcm, int frame_size, byte[] data, int max_data_bytes);

    // 销毁 Opus 编码器
    void opus_encoder_destroy(Pointer st);

    // 创建 Opus 解码器
    Pointer opus_decoder_create(int Fs, int channels, IntByReference error);

    // 解码 Opus 数据
    int opus_decode(Pointer st, byte[] data, int len, short[] pcm, int frame_size, int decode_fec);

    // 销毁 Opus 解码器
    void opus_decoder_destroy(Pointer st);
}