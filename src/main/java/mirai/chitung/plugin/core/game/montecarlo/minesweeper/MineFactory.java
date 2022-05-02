package mirai.chitung.plugin.core.game.montecarlo.minesweeper;

import java.util.Arrays;
import java.util.Random;

public class MineFactory {

    static int[][] randomMine(int x, int y, int num) {

        int[][] mines = new int[x][y];

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                mines[i][j] = 0;
            }
        }

        while (num > 0) {
            Random random = new Random();
            int randomX = random.nextInt(x);
            int randomY = random.nextInt(y);
            if (mines[randomX][randomY] == 0) {
                mines[randomX][randomY] = 1;
                num--;
            }
        }

        return mines;

    }

    public static int[][] getMineNumber(int[][] mines) {

        int[][] mineNumber = new int[mines.length][mines[0].length];
        int count;

        for (int i = 0; i < mines.length; i++) {
            for (int j = 0; j < mines[0].length; j++) {
                count = 0;
                for (int k = i - 1; k <= i + 1; k++) {
                    for (int l = j - 1; l <= j + 1; l++) {
                        if (k >= 0 && k < mines.length && l >= 0 && l < mines[0].length) {
                            if (mines[k][l] == 1) count += 1;
                        }
                    }
                }
                mineNumber[i][j] = count;
            }
        }
        return mineNumber;
    }

    //扫描

    static private int[][] scan(int[][] mineNumber, int[][] checklist, int x, int y) {

        if (mineNumber[x][y] == 0) {

            for (int k = x - 1; k <= x + 1; k++) {
                for (int l = y - 1; l <= y + 1; l++) {

                    if (k < 0 || k >= mineNumber.length || l < 0 || l >= mineNumber[0].length) continue;
                    if (mineNumber[k][l] == 0) checklist[k][l] = 1;
                }
            }
        }

        return checklist;
    }

    static int[][] scan(int[][] mineNumber, int[][] checklist) {

        int[][] result = checklist;

        if (!containsZeroNearby(mineNumber, checklist)) return checklist;

        for (int i = 0; i < mineNumber.length; i++) {
            for (int j = 0; j < mineNumber[0].length; j++) {
                if (checklist[i][j] == 1) result = scan(mineNumber, checklist, i, j);
            }
        }

        return scan(mineNumber, result);
    }

    static int count(int[][] array, int value) {
        int count = 0;
        for (int[] ints : array) {
            for (int j = 0; j < array[0].length; j++) {
                if (ints[j] == value) count++;
            }
        }
        return count;
    }

    public static int[][] getMask(int[][] mines, int[][] checklist) {
        int[][] scan = scan(getMineNumber(mines), checklist);
        if (count(scan, 1) <= 1) return scan;
        return expandEdge(mines, scan);

    }

    //检查
    static boolean containsZeroNearby(int[][] mineNumber, int[][] checklist, int x, int y) {

        if (mineNumber[x][y] == 0) {
            for (int k = x - 1; k <= x + 1; k++) {
                for (int l = y - 1; l <= y + 1; l++) {
                    if (k < 0 || k >= mineNumber.length || l < 0 || l >= mineNumber[0].length) continue;
                    if (mineNumber[k][l] == 0 && checklist[k][l] == 0) return true;
                }
            }
        }
        return false;
    }

    static boolean containsZeroNearby(int[][] mineNumber, int[][] checklist) {
        for (int i = 0; i < mineNumber.length; i++) {
            for (int j = 0; j < mineNumber[0].length; j++) {
                if (checklist[i][j] == 0) continue;
                if (containsZeroNearby(mineNumber, checklist, i, j)) return true;
            }
        }
        return false;
    }

    static int[][] expandEdge(int[][] mines, int[][] checklist) {

        int[][] result = new int[checklist.length][checklist[0].length];

        for (int i = 0; i < checklist.length; i++) {
            for (int j = 0; j < checklist[0].length; j++) {

                if (checklist[i][j] == 0) continue;

                if (mines[i][j] == 1) {
                    result[i][j] = 1;
                    continue;
                }

                if (getMineNumber(mines)[i][j] != 0) {
                    result[i][j] = 1;
                    continue;
                }

                for (int k = i - 1; k <= i + 1; k++) {
                    for (int l = j - 1; l <= j + 1; l++) {
                        if (k < 0 || k >= checklist.length || l < 0 || l >= checklist[0].length) continue;
                        result[k][l] = 1;
                    }
                }

            }
        }

        return result;

    }

    static int[][] initializeArray(int[][] array, int value) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                array[i][j] = value;
            }
        }
        return array;
    }

    public static int[][] getExplodedMines(int[][] mines, int[][] checklist) {
        int[][] result = new int[mines.length][mines[0].length];
        for (int i = 0; i < mines.length; i++) {
            for (int j = 0; j < mines[0].length; j++) {
                if (checklist[i][j] == 1 && mines[i][j] == 1) result[i][j] = 1;
            }
        }
        return result;
    }

    public static int[] intToArray(int number) {
        if(number<0) number = Math.abs(number);
        if(number>1000) number = number % 1000;
        if(number<10) return new int[]{0,0,number};
        if(number<100) return new int[]{0,number/10,number%10};
        return new int[]{number/100,(number/10)%10,number%10};
    }

    public static int[] doubleToArray(double odd){
        if(odd<0.01){
            return new int[]{10,10,10};
        }

        if(odd<1){
            int temp = (int) (odd*100);
            return new int[]{0,(temp-temp%10)/10,temp%10};
        }

        if(odd<100)return new int[]{0,(int)odd/10,(int)odd%10};

        if(odd>999) return new int[]{10,10,10};

        return new int[]{(int)odd/100,(int)(odd/10)%10,(int)odd%10};

    }


}