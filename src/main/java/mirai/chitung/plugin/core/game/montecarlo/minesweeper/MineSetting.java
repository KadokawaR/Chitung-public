package mirai.chitung.plugin.core.game.montecarlo.minesweeper;

import java.util.Random;

public class MineSetting {
    int x;
    int y;
    int mineNumber;
    double odd;

    MineSetting(int x,int y,int mineNumber){
        this.x = x;
        this.y = y;
        this.mineNumber = mineNumber;
        this.odd = (double) (x*y)/ (double) mineNumber;
    }

    MineSetting(Random random){
        this.x = random.nextInt(15)+9;
        this.y = random.nextInt(21)+9;
        this.mineNumber = (int) Math.floor(((double)(x*y)/6.4*(random.nextDouble()+0.5)));
        this.odd = (double) (x*y)/ (double) mineNumber;
    }
}
