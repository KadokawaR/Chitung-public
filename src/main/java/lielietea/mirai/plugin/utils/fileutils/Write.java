package lielietea.mirai.plugin.utils.fileutils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

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
}
