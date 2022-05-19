package mirai.chitung.plugin.core.game.montecarlo.minesweeper;

import org.jetbrains.annotations.NotNull;

public class MineData {
    int x;
    int y;

    public MineData(int x, int y) {
        this.x=x;
        this.y=y;
    }

    public boolean hasSameValueWith(@NotNull MineData data){
        return this.x==data.x&&this.y==data.y;
    }
}
