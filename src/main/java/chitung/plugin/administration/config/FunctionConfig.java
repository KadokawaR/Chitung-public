package chitung.plugin.administration.config;

public class FunctionConfig{
    private boolean fish;
    private boolean casino;//bank是casino的配套设施
    private boolean responder;
    private boolean lottery;//C4 和 Bummer, FriendFC 里面该值不影响
    private boolean game;//包含fish casino mahjong-riddle

    public FunctionConfig(){
        this.fish = true;
        this.casino = true;
        this.responder = true;
        this.lottery = true;
        this.game = true;
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

    public boolean isLottery() {
        return lottery;
    }

    public void setLottery(boolean lottery) {
        this.lottery = lottery;
    }

    public boolean isGame() {
        return game;
    }

    public void setGame(boolean game) {
        this.game = game;
    }
}