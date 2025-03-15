package com.pansophicmind.server.aidog.converter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpusConverterWavTest {

    public static void main(String[] args) {
        try {
            // 示例：假设有Opus数据和参数
            byte[] opusData = new byte[0]; // 替换为实际的Opus字节数组
            byte[] wavData = OpusConverterWav3.convert(opusData);
            // 将wavData写入文件或传输
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
