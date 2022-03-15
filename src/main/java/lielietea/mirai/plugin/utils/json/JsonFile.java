package lielietea.mirai.plugin.utils.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JsonFile {

    //读取网页上的json文本
    public static String read(String urlPath) throws Exception {
        URL url = new URL(urlPath);
        BufferedReader reader = new BufferedReader
                (new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
        String line;
        StringBuilder jsonstring = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            jsonstring.append(line);
        }
        reader.close();
        return jsonstring.toString();
    }

    //读取该URL地址的图片
    public static InputStream getInputStream(String urlPath) {
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(urlPath);
            try {
                httpURLConnection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 设置网络连接超时时间
            assert httpURLConnection != null;
            httpURLConnection.setConnectTimeout(3000);
            // 设置应用程序要从网络连接读取数据
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
                // 从服务器返回一个输入流
                inputStream = httpURLConnection.getInputStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return inputStream;
    }
}
