package mirai.chitung.plugin.core.game.montecarlo.minesweeper.data;

public enum MinesweeperFace {

    Normal(1),
    Cool(2),
    Dead(3),
    Surprise(4);

    private final int code;

    private MinesweeperFace(int code){
        this.code=code;
    }

    public int getCode(){
        return this.code;
    }


}
