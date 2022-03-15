package lielietea.mirai.plugin.utils.fileutils;

import java.io.File;
import java.io.IOException;

public class Touch {

    public static boolean file(String path){
        File file = new File(path);
        boolean res = file.exists();
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static boolean dir(String path){
        File file = new File(path);
        boolean res = file.exists();
        if(!file.exists()){
            file.mkdir();
        }
        return res;
    }

}
