package mirai.chitung.plugin.core.game.montecarlo.minesweeper;

import java.util.Arrays;
import java.util.Random;

public class MineFactory {

    static int[][] randomMine(int x,int y, int num){

        int[][] mines = new int[x][y];

        for(int i=0;i<x;i++){
            for(int j=0;j<y;j++){
                mines[i][j] = 0;
            }
        }

        while(num>0){
            Random random = new Random();
            int randomX = random.nextInt(x);
            int randomY = random.nextInt(y);
            if(mines[randomX][randomY]==0){
                mines[randomX][randomY]=1;
                num--;
            }
        }

        return mines;

    }

    public static int[][] getMineNumber(int[][] mines){

        int[][] mineNumber = new int[mines.length][mines[0].length];
        int count;

        for(int i=0;i<mines.length;i++){
            for(int j=0;j<mines[0].length;j++){
                count = 0;
                for(int k=i-1;k<=i+1;k++){
                    for(int l=j-1;l<=j+1;l++) {
                        if(k>=0&&k<mines.length&&l>=0&&l< mines[0].length){
                            if(mines[k][l]==1) count += 1;
                        }
                    }
                }
                mineNumber[i][j]=count;
            }
        }
        return mineNumber;
    }

    //ɨ��

    static private int[][] scan(int[][] mineNumber, int[][] checklist, int x, int y){

        if(mineNumber[x][y]==0) {

            for (int k = x - 1; k <= x + 1; k++) {
                for (int l = y - 1; l <= y + 1; l++) {

                    if (k < 0 || k >= mineNumber.length || l < 0 || l >= mineNumber[0].length) continue;
                    if (mineNumber[k][l] == 0) checklist[k][l] = 1;
                }
            }
        }

        return checklist;
    }

    static int[][] scan(int[][] mineNumber, int[][] checklist){

        if(!containsZeroNearby(mineNumber,checklist)) return checklist;

        for(int i=0;i<mineNumber.length;i++){
            for(int j=0;j<mineNumber[0].length;j++){
                if(checklist[i][j]==1) checklist = scan(mineNumber,checklist,i,j);
            }
        }

        return scan(mineNumber,checklist);
    }

    static int count(int[][] array, int value){
        int count=0;
        for(int i=0;i<array.length;i++){
            for(int j=0;j<array[0].length;j++){
                if (array[i][j]==value) count++;
            }
        }
        return count;
    }

    public static int[][] getMask(int[][] mines, int[][] checklist){
        int[][] scan = scan(getMineNumber(mines),checklist);
        if(count(scan,1)==1) return scan;
        return expandEdge(scan);

    }

    //���
    static boolean containsZeroNearby(int[][] mineNumber, int[][] checklist, int x, int y){

        if(mineNumber[x][y]==0) {
            for (int k = x - 1; k <= x + 1; k++) {
                for (int l = y - 1; l <= y + 1; l++) {
                    if (k < 0 || k >= mineNumber.length || l < 0 || l >= mineNumber[0].length) continue;
                    if (mineNumber[k][l] == 0 && checklist[k][l] == 0) return true;
                }
            }
        }
        return false;
    }

    static boolean containsZeroNearby(int[][] mineNumber, int[][] checklist){
        for(int i=0;i<mineNumber.length;i++){
            for(int j=0;j<mineNumber[0].length;j++){
                if(checklist[i][j]==0) continue;
                if(containsZeroNearby(mineNumber,checklist,i,j)) return true;
            }
        }
        return false;
    }

    static int[][] expandEdge(int[][] checklist){

        int[][] result = new int[checklist.length][checklist[0].length];

        for(int i=0;i<checklist.length;i++){
            for(int j=0;j<checklist[0].length;j++){

                if(checklist[i][j]==0) continue;

                for(int k=i-1;k<=i+1;k++){
                    for(int l=j-1;l<=j+1;l++){
                        if(k<0||k>=checklist.length||l<0||l>=checklist[0].length) continue;
                        result[k][l] =1;
                    }
                }

            }
        }

        return result;

    }


    static int[][] initializeArray(int[][] array, int value){
        for(int i=0;i<array.length;i++){
            for(int j=0;j<array[0].length;j++){
                array[i][j]=value;
            }
        }
        return array;
    }

    static void print(int[][] array){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i< array.length;i++){
            for(int j=0;j<array[0].length;j++){
                sb.append(array[i][j]).append(" ");
            }
            sb.append("\n");
        }
        System.out.println(sb);
    }

    public static void test(){
        int[][] mines = randomMine(8,8,8);
        int[][] mineNumber = getMineNumber(mines);
        int[][] checklist = initializeArray(new int[mines.length][mines[0].length],0);

        checklist[new Random().nextInt(mines.length)][new Random().nextInt(mines[0].length)]=1;

        print(mineNumber);
        print(checklist);

        int[][] mask = scan(mineNumber,checklist);

        print(mask);

    }

}
