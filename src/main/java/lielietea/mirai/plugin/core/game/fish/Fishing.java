package lielietea.mirai.plugin.core.game.fish;

import com.google.gson.Gson;
import lielietea.mirai.plugin.administration.statistics.GameCenterCount;

import lielietea.mirai.plugin.core.bank.PumpkinPesoWindow;
import lielietea.mirai.plugin.utils.image.ImageSender;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChainBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Fishing extends FishingUtil{

    public static final int FISHING_COST = 800;
    public static final int MAX_COUNT_IN_ONE_HOUR = 60;

    public enum Time{
        Day,
        All,
        Night
    }

    public enum Waters{
        Amur(1), //A
        Caroline(2), //B
        Chishima(3), //C
        General(4);

        private final int code;

        private Waters(int code) { this.code = code; }

        public int getCode(){
            return this.code;
        }

    }

    static class Fish{
        int code;
        String name;
        int price;
        Time time;
    }

    static class FishingList{
        List<Fish> fishingList;
    }

    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    static final List<Long> isInFishingProcessFlag = new ArrayList<>();
    final List<Fish> loadedFishingList;
    List<Date> fishRecord;

    Fishing() {
        loadedFishingList = new ArrayList<>();
        fishRecord = new ArrayList<>();
        String FISHINGLIST_PATH = "/fishing/FishingList.json";
        InputStream is = Fishing.class.getResourceAsStream(FISHINGLIST_PATH);
        assert is != null;
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        Gson gson = new Gson();
        FishingList fl = gson.fromJson(br,FishingList.class);
        loadedFishingList.addAll(fl.fishingList);
        touchRecord();
        try {
            is.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static final Fishing INSTANCE = new Fishing();

    public static Fishing getINSTANCE() {
        return INSTANCE;
    }

    static final String FISH_INFO_PATH = "/pics/fishing/fishinfo.png";
    static final String HANDBOOK_PATH = "/pics/fishing/handbook.png";

    public static void go(MessageEvent event){
        if(event.getMessage().contentToString().equals("/fishhelp")){

            GameCenterCount.count(GameCenterCount.Functions.FishingInfo);
            try (InputStream img = Fishing.class.getResourceAsStream(FISH_INFO_PATH)) {
                assert img != null;
                event.getSubject().sendMessage(Contact.uploadImage(event.getSubject(), img));
            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        }

        if(event.getMessage().contentToString().equals("/handbook")){
            GameCenterCount.count(GameCenterCount.Functions.FishingHandbook);
            try (InputStream img = Fishing.class.getResourceAsStream(HANDBOOK_PATH)) {
                assert img != null;
                event.getSubject().sendMessage(Contact.uploadImage(event.getSubject(), img));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        if(event.getMessage().contentToString().equals("/collection")){
            GameCenterCount.count(GameCenterCount.Functions.FishingCollection);
            MessageChainBuilder mcb = mcbProcessor(event);
            mcb.append("您的图鉴完成度目前为").append(String.valueOf(handbookProportion(event.getSender().getId()))).append("%\n\n");
            try {
                mcb.append(Contact.uploadImage(event.getSubject(),ImageSender.getBufferedImageAsSource(getHandbook(event))));
                event.getSubject().sendMessage(mcb.asMessageChain());
            } catch (IOException e) {
                e.printStackTrace();
            }

            return;
        }

        if (event.getMessage().contentToString().contains("/fish")){
            if (!isInFishingProcessFlag.contains(event.getSender().getId())){
                isInFishingProcessFlag.add(event.getSender().getId());
                GameCenterCount.count(GameCenterCount.Functions.FishingGo);
                getFish(event,getWater(event.getMessage().contentToString()));
            } else {
                MessageChainBuilder mcb = new MessageChainBuilder();
                if (event.getClass().equals(GroupMessageEvent.class)){
                    mcb.append((new At(event.getSender().getId()))).append(" ");
                }
                mcb.append("上次抛竿还在进行中。");
                GameCenterCount.count(GameCenterCount.Functions.FishingNotReadyYet);
                event.getSubject().sendMessage(mcb.asMessageChain());
            }
        }

        if(event.getMessage().contentToString().equals("/endfish")){
            MessageChainBuilder mcb = new MessageChainBuilder();
            if (event.getClass().equals(GroupMessageEvent.class)){
                mcb.append((new At(event.getSender().getId()))).append(" ");
            }
            if(isInFishingProcessFlag.contains(event.getSender().getId())) {
                isInFishingProcessFlag.remove(event.getSender().getId());
                event.getSubject().sendMessage(mcb.append("已经停止钓鱼。").asMessageChain());
            } else {
                event.getSubject().sendMessage(mcb.append("您未在钓鱼中。").asMessageChain());
            }
        }

    }

    public static void getFish(MessageEvent event,Waters waters){
        MessageChainBuilder mcb = mcbProcessor(event);
        Random random = new Random();

        //非常规水域进行扣费
        if(!waters.equals(Waters.General)){
            if(PumpkinPesoWindow.hasEnoughMoney(event,FISHING_COST)){
                PumpkinPesoWindow.minusMoney(event.getSender().getId(),FISHING_COST);
                mcb.append("已收到您的捕鱼费用").append(String.valueOf(FISHING_COST)).append("南瓜比索。");
            } else {
                event.getSubject().sendMessage(mcb.append("您的南瓜比索数量不够，请检查。").asMessageChain());
                return;
            }
        }

        updateRecord();
        int recordInOneHour = fishInOneHour(getINSTANCE().fishRecord);
        int time = 3+random.nextInt(4)+recordInOneHour;//线性增加时间
        int itemNumber = 3+random.nextInt(2);

        getINSTANCE().fishRecord.add(new Date());

        mcb.append("本次钓鱼预计时间为").append(String.valueOf(time)).append("分钟。");
        if(event instanceof GroupMessageEvent) mcb.append("麦氏渔业公司提醒您使用/fishhelp查询钓鱼功能的相关信息，如果长时间钓鱼未收杆，请使用/endfish 强制停止钓鱼。");
        else mcb.append("麦氏渔业公司提醒您使用/fishhelp查询钓鱼功能的相关信息，如果长时间钓鱼未收杆，请使用/endfish 强制停止钓鱼。");
        event.getSubject().sendMessage(mcb.asMessageChain());

        executor.schedule(new fishRunnable(event,itemNumber,waters,recordInOneHour),time, TimeUnit.MINUTES);
    }

    public static Map<Integer,Integer> getItemIDRandomly(int amount,Waters waters){
        List<Integer> Weight = new ArrayList<>();
        List<Fish> fishList = INSTANCE.loadedFishingList;
        Map<Integer,Integer> fishMap = new HashMap<>();

        boolean isInDaytime = isInDaytime();
        List<Fish> actualFishList = new ArrayList<>();

        //获得实际的Fish列表，根据水域、时间获得
        for(Fish fish:fishList){
            if(fish.code/100!=waters.code&&fish.code/100!=Waters.General.code) continue;
            if(fish.time.equals(Time.All)) actualFishList.add(fish);
            if(isInDaytime&&fish.time.equals(Time.Day)) actualFishList.add(fish);
            if(!isInDaytime&&fish.time.equals(Time.Night)) actualFishList.add(fish);
        }

        //计算实际Fish列表里的最大值
        int maxPrice = 0;
        for(Fish fish:fishList){
            if(fish.price>maxPrice) maxPrice=fish.price;
        }

        //用最大值+50-价格来作为每个物品的权重
        int totalWeight = 0;
        for (Fish fish: actualFishList){
            Weight.add((maxPrice + 50 - fish.price));
            totalWeight = totalWeight + maxPrice + 50 - fish.price;
        }

        for(int j=0;j<amount;j++) {
            Random random = new Random();
            int randomNumber = random.nextInt(totalWeight);

            //随机一个数，依次减去每一条鱼的权重，如果小于0则返回该index
            int randomIndex = 0;

            for (int i = 0; i < fishList.size(); i++) {
                if ((randomNumber - Weight.get(i)) < 0) {
                    randomIndex = i;
                    break;
                } else {
                    randomNumber = randomNumber - Weight.get(i);
                }
            }

            //通过index返回鱼的code
            Fish fish = actualFishList.get(randomIndex);
            if(fishMap.containsKey(fish.code)){
                int amountOfFish = fishMap.get(fish.code);
                fishMap.replace(fish.code, amountOfFish+1);
            } else {
                fishMap.put(fish.code, 1);
            }
        }
        return fishMap;
    }

    static Fish getFishFromCode (int code){
        for (Fish fish: INSTANCE.loadedFishingList){
            if (fish.code == code){
                return fish;
            }
        }
        return null;
    }

    //给群聊的消息前面加AT
    public static MessageChainBuilder mcbProcessor(MessageEvent event){
        MessageChainBuilder mcb = new MessageChainBuilder();
        if (event.getClass().equals(GroupMessageEvent.class)){
            mcb.append((new At(event.getSender().getId()))).append(" ");
        }
        return mcb;
    }

    static class fishRunnable implements Runnable {

        MessageEvent event;
        int itemNumber;
        Waters waters;
        int recordInOneHour;

        fishRunnable(MessageEvent event,int itemNumber,Waters waters,int recordInOneHour){
            this.event=event;
            this.itemNumber=itemNumber;
            this.waters=waters;
            this.recordInOneHour=recordInOneHour;
        }

        @Override
        public void run(){
            try {
                //随机生成包含鱼的code和数量的Map
                if(!isInFishingProcessFlag.contains(event.getSender().getId())) return;

                Map<Integer, Integer> fishList = getItemIDRandomly(itemNumber, waters);
                MessageChainBuilder mcb = new MessageChainBuilder();
                if (event.getClass().equals(GroupMessageEvent.class)) {
                    mcb.append((new At(event.getSender().getId()))).append(" ");
                }
                mcb.append("您钓到了：\n\n");

                int totalValue = 0;
                for (Map.Entry<Integer, Integer> entry : fishList.entrySet()) {
                    Fish fish = getFishFromCode(entry.getKey());
                    assert fish != null;
                    mcb.append(fish.name).append("x").append(String.valueOf(entry.getValue())).append("，价值").append(String.valueOf(fish.price * entry.getValue())).append("南瓜比索\n");
                    totalValue = totalValue + fish.price * entry.getValue();
                }

                totalValue = (int) (totalValue * (1F + (float)recordInOneHour * 0.05F));
                mcb.append("\n时间修正系数为").append(String.valueOf(1F + (float)recordInOneHour * 0.05F)).append("，共值").append(String.valueOf(totalValue)).append("南瓜比索。\n\n").append(Contact.uploadImage(event.getSubject(), ImageSender.getBufferedImageAsSource(getImage(new ArrayList<>(fishList.keySet())))));
                //向银行存钱
                PumpkinPesoWindow.addMoney(event.getSender().getId(), totalValue);
                //存储钓鱼信息
                saveRecord(event.getSender().getId(), new ArrayList<>(fishList.keySet()));
                //发送消息
                event.getSubject().sendMessage(mcb.asMessageChain());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //解除正在钓鱼的flag
                isInFishingProcessFlag.remove(event.getSender().getId());
            }
        }
    }
}
