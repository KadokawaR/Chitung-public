package lielietea.mirai.plugin.administration.config;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class Config {
    String botName;
    List<Long> devGroupID;
    List<Long> adminID;
    FunctionConfig fc;
    ResponseConfig rc;

    Config(){
        this.botName = "机器人";
        this.devGroupID = new ArrayList<>();
        this.adminID = new ArrayList<>();
        this.fc = new FunctionConfig();
        this.rc = new ResponseConfig();
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }

    public List<Long> getDevGroupID() {
        return devGroupID;
    }

    public void setDevGroupID(List<Long> devGroupID) {
        this.devGroupID = devGroupID;
    }

    public List<Long> getAdminID() {
        return adminID;
    }

    public void setAdminID(List<Long> adminID) {
        this.adminID = adminID;
    }

    public FunctionConfig getFc() {
        return fc;
    }

    public void setFc(FunctionConfig fc) {
        this.fc = fc;
    }

    public ResponseConfig getRc() {
        return rc;
    }

    public void setRc(ResponseConfig rc) {
        this.rc = rc;
    }

    public void setAnswerFriend(boolean answerFriend){
        this.rc.answerFriend=answerFriend;
    }

    public void setAddFriend(boolean addFriend){
        this.rc.addFriend=addFriend;
    }

    public void setAddGroup(boolean addGroup){
        this.rc.addGroup=addGroup;
    }

    public void setAnswerGroup(boolean answerGroup){
        this.rc.answerGroup=answerGroup;
    }

    public void setAutoAnswer(boolean autoAnswer){
        this.rc.autoAnswer=autoAnswer;
    }
}

class FunctionConfig{
    boolean FursonaPunk;
    boolean LotteryWinner;
    boolean LotteryBummer;
    boolean LotteryC4;
    boolean MealPicker;
    boolean FortuneTeller;
    boolean PlayDice;
    boolean AntiOverwatch;
    boolean AntiDirtyWord;
    boolean GreetingAndGoodbye;
    boolean HeroLinesSelector;
    boolean LovelyImage;
    boolean FurryGamesIndex;
    boolean FeedBack;
    FunctionConfig(){
        this.FursonaPunk=true;
        this.LotteryWinner=true;
        this.LotteryBummer=true;
        this.LotteryC4=true;
        this.MealPicker=true;
        this.FortuneTeller=true;
        this.PlayDice=true;
        this.AntiOverwatch=true;
        this.AntiDirtyWord=true;
        this.GreetingAndGoodbye=true;
        this.HeroLinesSelector=false;
        this.LovelyImage=false;
        this.FurryGamesIndex=false;
        this.FeedBack=false;
    }

    public boolean isFursonaPunk() {
        return FursonaPunk;
    }

    public void setFursonaPunk(boolean fursonaPunk) {
        FursonaPunk = fursonaPunk;
    }
}

class ResponseConfig{
    boolean answerFriend;
    boolean answerGroup;
    boolean addFriend;
    boolean addGroup;
    boolean autoAnswer;
    ResponseConfig(){
        this.answerFriend=true;
        this.addFriend=true;
        this.addGroup=true;
        this.answerGroup=true;
        this.autoAnswer=true;
    }
}
