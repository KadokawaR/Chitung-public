package lielietea.mirai.plugin.administration.statistics.MPSEHandler;

import com.google.gson.Gson;
import lielietea.mirai.plugin.utils.fileutils.Read;
import lielietea.mirai.plugin.utils.fileutils.Write;
import lielietea.mirai.plugin.utils.multibot.MultiBotHandler;

import java.io.*;
import java.util.Date;

public class MPSEProcessor {

    final static String FILE_DIR_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "Statistics";
    final static String FILE_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "Statistics" + File.separator + "MPSEData.json";

    public static boolean touchDataFIle(){
        File dir = new File(FILE_DIR_PATH);
        File json = new File(FILE_PATH);
        if(!dir.exists()){
            dir.mkdir();
        }
        if(!json.exists()){
            try {
                json.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return json.exists();
    }

    public static DataList openData() throws IOException {
        touchDataFIle();
        DataList dataList = new Gson().fromJson(Read.fromReader(new BufferedReader(new InputStreamReader(new FileInputStream(FILE_PATH)))), DataList.class);
        if(dataList == null){
            dataList = new DataList();
            dataList.addDataIntoDatas(new Data(new Date(), MultiBotHandler.BotName.Chitung1));
        }
        return dataList;
    }

    public static void writeData(DataList dataList) throws IOException {
        Write.cover(new Gson().toJson(dataList),FILE_PATH);
    }

    public static Date updateDaysByGetTime(Date dateTime/*日期*/,int n/*加减天数*/) {
        return new Date(dateTime.getTime() + n * 24 * 60 * 60 * 1000L);
    }

    public static Date updateMinutesByGetTime(Date dateTime/*日期*/,int n/*加减分钟*/) {
        return new Date(dateTime.getTime() + n * 60 * 1000L);
    }


}
