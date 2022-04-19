package mirai.chitung.plugin.utils.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mirai.chitung.plugin.utils.fileutils.Read;
import mirai.chitung.plugin.utils.fileutils.Write;

import java.io.*;
import java.lang.reflect.Type;

public class JsonUtil<T> {

    private T t;

    Type type = new TypeToken<T>(){}.getType();

    public static boolean isWindows(){
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    public void write(String path){
        String jsonString = null;
        try {
            if(isWindows()) {
                jsonString = new String(new GsonBuilder().setPrettyPrinting().create().toJson(t).getBytes("GBK"));
            } else {
                jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(t);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Write.cover(jsonString, path);
    }

    public T read(String path){
        try {
            if(isWindows()){
                return new Gson().fromJson(new String(Read.fromReader(new BufferedReader(new InputStreamReader(new FileInputStream(path)))).getBytes("GBK")), type);
            } else {
                return new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(new FileInputStream(path)))), type);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
