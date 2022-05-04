package mirai.chitung.plugin.utils.fileutils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Write {
    //追加写入
    public static void append(String content, String PATH) {
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(Paths.get(PATH), StandardOpenOption.APPEND), StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    //覆盖写入
    public static void cover(String content, String PATH) {
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(Paths.get(PATH)), StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
