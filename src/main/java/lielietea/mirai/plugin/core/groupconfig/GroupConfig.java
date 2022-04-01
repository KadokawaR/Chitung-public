package lielietea.mirai.plugin.core.groupconfig;

import java.util.ArrayList;
import java.util.List;

public class GroupConfig {
    private long groupID;
    private boolean global;
    private boolean fish;
    private boolean casino;//bank是casino的配套设施
    private boolean responder;
    private boolean lottery;//C4 和 Bummer
    private boolean game;//包含fish casino mahjong-riddle
    private List<Long> blockedUser;//global 黑名单

    GroupConfig(){
        this.groupID = 0L;
        this.global = true;
        this.fish = true;
        this.casino = true;
        this.responder = true;
        this.lottery = true;
        this.game = true;
        this.blockedUser = new ArrayList<>();
    }

    GroupConfig(long groupID){
        this.groupID = groupID;
        this.global = true;
        this.fish = true;
        this.casino = true;
        this.responder = true;
        this.lottery = true;
        this.game = true;
        this.blockedUser = new ArrayList<>();
    }

    public long getGroupID() {
        return groupID;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public boolean isFish() {
        return fish;
    }

    public void setFish(boolean fish) {
        this.fish = fish;
    }

    public boolean isCasino() {
        return casino;
    }

    public void setCasino(boolean casino) {
        this.casino = casino;
    }

    public boolean isResponder() {
        return responder;
    }

    public void setResponder(boolean responder) {
        this.responder = responder;
    }

    public boolean isGame() {
        return game;
    }

    public void setGame(boolean game) {
        this.game = game;
    }

    public List<Long> getBlockedUser() {
        return blockedUser;
    }

    public void setBlockedUser(List<Long> blockedUser) {
        this.blockedUser = blockedUser;
    }

    public boolean isLottery() {
        return lottery;
    }

    public void setLottery(boolean lottery) {
        this.lottery = lottery;
    }
}
