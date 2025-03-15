package com.pansophicmind.server.aidog.common.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtils {
    public static void writeBytes(byte[] audioStream, String filePath) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(audioStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeBytes(byte[] audioStream, File file, int offset, int length, boolean append) {
        try (FileOutputStream fos = new FileOutputStream(file, append)) {
            // 移动到指定偏移量
            fos.getChannel().position(offset);
            // 检查写入长度是否超出数组边界
            if (length > audioStream.length) {
                length = audioStream.length;
            }
            fos.write(audioStream, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
