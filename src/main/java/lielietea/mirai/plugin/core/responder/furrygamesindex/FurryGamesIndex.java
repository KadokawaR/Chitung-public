package lielietea.mirai.plugin.core.responder.furrygamesindex;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class FurryGamesIndex{
    public static final String FGIUrl = "https://furrygames.top/";
    public static final String FGIListUrl = "https://furrygames.top/zh-cn/list.html";

    //从furrygames.top中获得所有游戏的列表，存储成中英混合名-链接的形式
    public static Map<String,String> getFGIlist(){
        Map<String,String> FGIlist = new HashMap<>();
        Document document = null;
        try {
            document = Jsoup.parse(new URL(FGIListUrl).openStream(), "utf-8", FGIListUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert document != null;
        Elements links = document.select("a[href]");

        String linkStr = "";
        for (Element link : links) {
            if (link.attr("href").contains("/game") && (!link.attr("href").contains("http"))) {
                linkStr = link.attr("href").replace("../","");
                linkStr = FGIUrl + linkStr;
                FGIlist.put(link.text(),linkStr);
            }
        }
        return FGIlist;
    }

    //通过游戏页面链接获得游戏介绍
    public static String getGameDescription(String gameURL){
        Document document = null;
        try {
            document = Jsoup.parse(new URL(gameURL).openStream(), "utf-8", gameURL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert document != null;
        Elements links = document.getElementsByClass("description");
        for (Element link : links) {
            return link.text();
        }
        return null;
    }

    //通过游戏页面链接获得游戏图片
    public static String getGameImageURL(String gameURL){
        Document document = null;
        try {
            document = Jsoup.parse(new URL(gameURL).openStream(), "utf-8", gameURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert document != null;
        Elements links = document.select("img[src]");
        for (Element link : links) {
            String gameImageLink = link.baseUri() + link.attr("src");
            gameImageLink = gameImageLink.replace(gameURL,"");
            if (!gameImageLink.contains("https")){
                gameImageLink = gameImageLink.replace("../../","");
                gameImageLink = FGIUrl + gameImageLink;
                return gameImageLink;
            }
        }
        return null;
    }

    //通过用户给的名字或者随机获取游戏信息
    public static String[] getGameInfo(String givenGameName, boolean isRandom){
        Map<String, String> FGIList = getFGIlist();
        String[] result = new String[4];
        String name;

        if (!isRandom) {
            name = searchGameName(givenGameName, FGIList);
        } else {
            Random random = new Random();
            int randomNumber = random.nextInt(FGIList.size());
            name = getCertainKeyOut(FGIList,randomNumber);
        }

        if (name == null) {
            return null;
        } else {
            String gameURL = FGIList.get(name);
            if (gameURL == null) {
                return null;
            } else {
                String gameDescription = getGameDescription(gameURL);
                String newGameDescription = gameDescription.substring(0,150);
                if (newGameDescription!=gameDescription) newGameDescription+="……";
                String gameImageURL = getGameImageURL(gameURL);
                result[0] = name;
                result[1] = gameURL;
                result[2] = newGameDescription;
                result[3] = gameImageURL;
            }
        }
        return result;

    }

    //通过用户给的名字查看List中是否含有相应的游戏
    public static String searchGameName(String givenGameName, Map<String,String> FGIList){
        for (String key : FGIList.keySet()) {
            if (key.contains(givenGameName)) {
                return key;
            }
        }
        return null;
    }


    public static String getCertainKeyOut(Map<String,String> FGIList, int num){
        int count = 0;
        for (String key : FGIList.keySet()){
            if (count >= num){
                return key;
            }
            count++;
        }
        return null;
    }
    
}
