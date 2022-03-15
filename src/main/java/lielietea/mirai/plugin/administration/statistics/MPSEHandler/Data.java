package lielietea.mirai.plugin.administration.statistics.MPSEHandler;

import com.google.gson.annotations.SerializedName;
import lielietea.mirai.plugin.utils.multibot.MultiBotHandler;

import java.util.Date;

public class Data {
    @SerializedName(value = "frM",alternate = {"friendMessage"})
    private int friendMessage;
    @SerializedName(value = "grM",alternate = {"groupMessage"})
    private int groupMessage;
    @SerializedName(value = "faM",alternate = {"failedMessage"})
    private int failedMessage;
    @SerializedName(value = "dt",alternate = {"date"})
    private Date date;
    @SerializedName(value = "bn")
    private MultiBotHandler.BotName bn;

    Data(){
        this.friendMessage = 0;
        this.groupMessage = 0;
        this.failedMessage = 0;
        this.date = null;
    }

    Data(Date date, MultiBotHandler.BotName bn){
        this.friendMessage = 0;
        this.groupMessage = 0;
        this.failedMessage = 0;
        this.date = date;
        this.bn = bn;
    }

    Data(Date date, int friendMessage, int groupMessage, int failedMessage, MultiBotHandler.BotName bn){
        this.friendMessage = friendMessage;
        this.groupMessage = groupMessage;
        this.failedMessage = failedMessage;
        this.date = date;
        this.bn = bn;
    }

    Data(int friendMessage, int groupMessage, int failedMessage, MultiBotHandler.BotName bn){
        this.friendMessage = friendMessage;
        this.groupMessage = groupMessage;
        this.failedMessage = failedMessage;
        this.date = null;
        this.bn = bn;
    }

    public int getFriendMessage() {
        return friendMessage;
    }

    public void setFriendMessage(int friendMessage) {
        this.friendMessage = friendMessage;
    }

    public int getGroupMessage() {
        return groupMessage;
    }

    public void setGroupMessage(int groupMessage) {
        this.groupMessage = groupMessage;
    }

    public int getFailedMessage() {
        return failedMessage;
    }

    public void setFailedMessage(int failedMessage) {
        this.failedMessage = failedMessage;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public MultiBotHandler.BotName getBn() {
        return bn;
    }

    public void setBn(MultiBotHandler.BotName bn) {
        this.bn = bn;
    }
}
