package lielietea.mirai.plugin.core.game.fish;

import com.google.gson.Gson;
import lielietea.mirai.plugin.utils.fileutils.Read;
import lielietea.mirai.plugin.utils.fileutils.Write;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static lielietea.mirai.plugin.utils.image.ImageEnlarger.zoomInImage;

public class FishingUtil {

    final static String FISHING_RECORD_PATH = System.getProperty("user.dir") + File.separator + "data" + File.separator + "fishrecord.json";

    final static int FISH_RECORD_IN_X_HOUR = 1;

    static class SingleRecord{
        long ID;
        List<Integer>  recordList;
        SingleRecord(){
            this.ID=0;
            this.recordList=new ArrayList<>();
            this.recordList.add(101);
        }

        SingleRecord(long ID){
            this.ID=ID;
            this.recordList=new ArrayList<>();
        }
    }

    static class FishingRecord{
        List<SingleRecord> singleRecords;
        FishingRecord(){
            this.singleRecords = new ArrayList<>();
            this.singleRecords.add(new SingleRecord());
        }
    }

    //用来把钓到的鱼的编号存在文件里
    public static void touchRecord(){
        File json = new File(FISHING_RECORD_PATH);
        if(json.exists()) return;
        try {
            json.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FishingRecord fr = new FishingRecord();
        Write.cover(new Gson().toJson(fr),FISHING_RECORD_PATH);
    }

    public static FishingRecord openRecord() throws IOException {
        touchRecord();
        InputStreamReader is = new InputStreamReader(new FileInputStream(FISHING_RECORD_PATH));
        BufferedReader br = new BufferedReader(is);
        Gson gson = new Gson();
        return gson.fromJson(Read.fromReader(br), FishingRecord.class);
    }

    public static void saveRecord(long ID, List<Integer> itemList){
        try {
            FishingRecord fr = openRecord();
            boolean IDhasExisted = false;
            for(SingleRecord sr:fr.singleRecords){
                if(sr.ID==ID){
                    IDhasExisted = true;
                    break;
                }
            }
            if(!IDhasExisted) fr.singleRecords.add(new SingleRecord(ID));
            for ( int index = 0; index < fr.singleRecords.size(); index++){
                if (fr.singleRecords.get(index).ID==ID){
                    for(Integer itemID:itemList){
                        boolean hasExisted=false;
                        for(Integer existedItemID: fr.singleRecords.get(index).recordList){
                            if (existedItemID.equals(itemID)){
                                hasExisted = true;
                                break;
                            }
                        }
                        if(!hasExisted) fr.singleRecords.get(index).recordList.add(itemID);
                    }
                    break;
                }
            }
            Gson gson = new Gson();
            Write.cover(gson.toJson(fr),FISHING_RECORD_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //计算当天的日出日落时间，以上海31.230N 121.474N为基准，但实际上也不是很准
    public static int[] calculateDayTime(){

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        int year = Integer.parseInt(yearFormat.format(date));

        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(year,Calendar.MARCH,21);
        if(calendar.after(date)) calendar1.set(year-1,Calendar.MARCH,21);
        Date equinox = calendar1.getTime();

        long timegap = date.getTime()- equinox.getTime();
        int day = (int)(timegap/(24*60*60*1000)+1);
        boolean isLeapYear = (year%4==0);
        int daysOfYear = 365;
        if(isLeapYear) daysOfYear = 366;

        double theta1 = Math.asin(Math.sin(Math.toRadians(360.0*day/daysOfYear))*Math.sin(Math.toRadians(23+26.0/60+21.0/3600)));
        double theta2 = Math.asin(Math.tan(Math.toRadians(31.23))*Math.tan(theta1));

        double timeSunrise = 6-Math.toDegrees(theta2)/360*24-(121.474-120)/15;
        double timeSunset = 18+Math.toDegrees(theta2)/360*24-(121.474-120)/15;

        int[] data = new int[4];

        data[0] = (int) Math.floor(timeSunrise);
        data[1] = (int)((timeSunrise-data[0])*60);

        data[2] = (int) Math.floor(timeSunset);
        data[3] = (int)((timeSunset-data[2])*60);

        return data;
    }

    //计算是否是白天
    public static boolean isInDaytime(){
        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Asia/Shanghai"));
        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);
        int[] data = calculateDayTime();
        int timeSunrise = data[0]*60+data[1];
        int timeSunset = data[2]*60+data[3];
        int timeNow = nowHour*60+nowMinute;
        return (timeSunrise<=timeNow&&timeNow<timeSunset);
    }

    //获得是否有渔场信息
    static Fishing.Waters getWater(String content){
        content = content.toUpperCase();
        content = content.replace("/FISH","");
        content = content.replace(" ","");
        switch (content) {
            case "A":
                return Fishing.Waters.Amur;
            case "B":
                return Fishing.Waters.Caroline;
            case "C":
                return Fishing.Waters.Chishima;
        }
        return Fishing.Waters.General;
    }

    //根据fishList生成图片
    static BufferedImage getImage(List<Integer> fishList){

        final String PATH = "/pics/fishing/NormalFish/";
        BufferedImage imgEntier = new BufferedImage(32*fishList.size()+20, 32+20, BufferedImage.TYPE_INT_RGB);
        int index=0;
        Graphics2D g2d = imgEntier.createGraphics();
        g2d.setColor(new Color(12,24,30));
        g2d.fillRect(0,0,32*fishList.size()+20,32+20);
        g2d.setColor(new Color(255,203,72));
        g2d.fillRect(0,0,32*fishList.size()+20,3);
        g2d.fillRect(0,32+20-3,32*fishList.size()+20,3);

        for(Integer code:fishList){
            InputStream is = FishingUtil.class.getResourceAsStream(PATH+String.valueOf(code)+".png");
            BufferedImage img = null;
            try {
                assert is != null;
                img = ImageIO.read(is);
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            g2d.drawImage(img,index*32+10,10,null);
            index++;
        }

        g2d.dispose();

        imgEntier = zoomInImage(imgEntier,(Double.valueOf(imgEntier.getWidth()*2.5)).intValue(),(Double.valueOf(imgEntier.getHeight()*2.5)).intValue());
        return imgEntier;
    }

    static List<Boolean> getCollectedList(MessageEvent event){
        long ID;
        if(event.getClass().equals(GroupMessageEvent.class)){
            ID = event.getSender().getId();
        } else {
            ID = event.getSubject().getId();
        }

        FishingRecord fr = new FishingRecord();
        try {
            fr = openRecord();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Integer> usersRecordList = new ArrayList<>();

        for(SingleRecord sr:fr.singleRecords){
            if(sr.ID==ID){
                usersRecordList.addAll(sr.recordList);
                break;
            }
        }

        List<Boolean> hasCollectedList = new ArrayList<>();

        for(Fishing.Fish fish:Fishing.getINSTANCE().loadedFishingList){
            if(usersRecordList.contains(fish.code)){
                hasCollectedList.add(true);
                continue;
            }
            hasCollectedList.add(false);
        }

        return hasCollectedList;
    }

    static String path(boolean isCollected){
        if(isCollected) return "/pics/fishing/NormalFish/";
        return "/pics/fishing/DarkFish/";
    }

    static BufferedImage getHandbook(MessageEvent event) throws IOException {
        List<Boolean> hasCollectedList = getCollectedList(event);
        InputStream is = FishingUtil.class.getResourceAsStream("/pics/fishing/handbookTemplate.png");
        BufferedImage handbookTemplate =  ImageIO.read(Objects.requireNonNull(is));
        is.close();
        BufferedImage handbook = new BufferedImage(32*8,32*9,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d1 = handbookTemplate.createGraphics();
        Graphics2D g2d2 = handbook.createGraphics();
        g2d2.setColor(new Color(62,73,72));
        g2d2.fillRect(0,0,handbook.getWidth(),handbook.getHeight());
        int verticalCount = 0;
        int horizontalCount = 0;
        for(int index=0;index<hasCollectedList.size();index++){
            String path = path(hasCollectedList.get(index))+String.valueOf(Fishing.getINSTANCE().loadedFishingList.get(index).code)+".png";
            BufferedImage fishImg = ImageIO.read(Objects.requireNonNull(FishingUtil.class.getResourceAsStream(path)));
            g2d2.drawImage(fishImg,verticalCount*32,horizontalCount*32,null);
            verticalCount++;
            if(verticalCount==8){
                verticalCount=0;
                horizontalCount++;
            }
        }
        g2d2.dispose();
        g2d1.drawImage(handbook,45,269,null);
        g2d1.dispose();
        handbookTemplate = zoomInImage(handbookTemplate,(Double.valueOf(handbookTemplate.getWidth()*2.5)).intValue(),(Double.valueOf(handbookTemplate.getHeight()*2.5)).intValue());
        return handbookTemplate;
    }

    //图鉴完成度
    static int handbookProportion(long ID){
        FishingRecord fr = new FishingRecord();
        try {
            fr = openRecord();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Integer> usersRecordList = new ArrayList<>();

        for(SingleRecord sr:fr.singleRecords){
            if(sr.ID==ID){
                usersRecordList.addAll(sr.recordList);
                break;
            }
        }

        return usersRecordList.size()*100/72;
    }

    //钓鱼人数统计
    static int fishInOneHour(List<Date> fishRecord){
        Calendar calendar = Calendar.getInstance();
        int index = 0;
        for(Date date:fishRecord){
            if(date.before(new Date(calendar.getTimeInMillis() - FISH_RECORD_IN_X_HOUR*60*60*1000))){ continue; }
            index++;
        }
        return index;
    }

    //更新Map
    static void updateRecord(){
        Date now = new Date();
        Fishing.getINSTANCE().fishRecord.removeIf(d -> now.before(new Date(now.getTime() - FISH_RECORD_IN_X_HOUR * 60 * 60 * 1000)));
    }

}
