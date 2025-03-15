package com.pansophicmind.server.aidog.vad;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
public class WavVadDetectionTest {

    public static void main(String[] args) {
//         String path = "C:\\Users\\62362\\A_workspace\\pansophicmind\\1741944271343liushuang.wav";
//        String path = "C:\\Users\\62362\\A_workspace\\pansophicmind\\abcde.wav";
//        String path = "C:\\Users\\62362\\A_workspace\\pansophicmind\\opusAudio2.wav";
//        String path = "C:\\Users\\62362\\A_workspace\\pansophicmind\\hello.wav";
        String path = "C:\\Users\\62362\\A_workspace\\pansophicmind\\null.wav";
        byte[] wavAudio = fileToByteArray(new File(path));
//        System.out.println("WavVadDetection ---- " + WavVadDetection.process(wavAudio));
//        System.out.println("WavVadDetection2 ---- " + WavVadDetection2.process(wavAudio));
    }

    public static byte[] fileToByteArray(File file) {
        byte[] buffer = null;
        try (FileInputStream fis = new FileInputStream(file)) {
            buffer = new byte[(int) file.length()];
            fis.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }
}
