package mirai.chitung.plugin.utils.fileutils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Write {
    //追加写入
    public static void append(String content, String PATH) {
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(PATH, true));
            out.write(content);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //覆盖写入
    public static void cover(String content, String PATH) {
        BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(PATH));
            out.write(content);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void cover(String content, String PATH, String Charsets){
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(PATH), Charsets);
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cover(String content, String PATH, boolean isUTF8){
        try {
            OutputStreamWriter writer;
            if(isUTF8) {
                writer = new OutputStreamWriter(new FileOutputStream(PATH), StandardCharsets.UTF_8);
            } else {
                writer = new OutputStreamWriter(new FileOutputStream(PATH));
            }
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
