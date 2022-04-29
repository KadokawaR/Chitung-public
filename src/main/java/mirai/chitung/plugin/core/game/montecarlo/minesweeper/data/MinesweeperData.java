package mirai.chitung.plugin.core.game.montecarlo.minesweeper.data;

public class MinesweeperData {

    int leftNumber;
    int rightNumber;
    MinesweeperFace face;
    int blockXNumber;
    int blockYNumber;
    int[][] mines;
    int[][] touches;
    boolean shown;

    MinesweeperData(int leftNumber, int rightNumber, MinesweeperFace face, int blockXNumber, int blockYNumber, int[][] mines, int[][] touches, boolean shown){
        this.blockXNumber=blockXNumber;
        this.blockYNumber=blockYNumber;
        this.leftNumber=leftNumber;
        this.rightNumber=rightNumber;
        this.face=face;
        this.mines=mines;
        this.touches=touches;
        this.shown=shown;
    }

    MinesweeperData(int leftNumber, int rightNumber, MinesweeperFace face, int blockXNumber, int blockYNumber, boolean shown){
        this.blockXNumber=blockXNumber;
        this.blockYNumber=blockYNumber;
        this.leftNumber=leftNumber;
        this.rightNumber=rightNumber;
        this.face=face;
        this.mines=new int[blockXNumber][blockYNumber];
        this.touches=new int[blockXNumber][blockYNumber];
        this.shown=shown;
    }

}
