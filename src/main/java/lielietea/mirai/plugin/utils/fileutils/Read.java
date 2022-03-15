package lielietea.mirai.plugin.utils.fileutils;

import lielietea.mirai.plugin.core.game.fish.Fishing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Read {

    public static String fromReader(BufferedReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        String temp;
        while ((temp = reader.readLine()) != null) {
            builder.append(temp);
        }
        return builder.toString();
    }

    public static String fromFile(String path) {
        InputStream is = Read.class.getResourceAsStream(path);
        assert is != null;
        String res = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        try {
            res = fromReader(br);
            is.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

}
