package mirai.chitung.plugin.core.game.montecarlo.minesweeper.imageutil;

import mirai.chitung.plugin.core.game.montecarlo.minesweeper.MineFactory;
import mirai.chitung.plugin.core.game.montecarlo.minesweeper.data.MinesweeperConstant;
import mirai.chitung.plugin.core.game.montecarlo.minesweeper.sheet.MinesweeperSheet;
import mirai.chitung.plugin.core.game.montecarlo.minesweeper.sheet.MinesweeperSheetType;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class MinesweeperImageUtil {



    public static BufferedImage resize(BufferedImage originalImage, int targetWidth, int targetHeight) {

        BufferedImage bufferedImage = new BufferedImage(targetWidth,targetHeight,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        g2d.drawImage(originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH),0,0,null);
        g2d.dispose();

        return bufferedImage;
    }


    static BufferedImage enlarge(BufferedImage image, int leftProtected, int rightProtected, int upProtected, int downProtected, int expectedWidth , int expectedHeight){

        if(expectedWidth<image.getWidth()||expectedHeight<image.getHeight()) return image;

        BufferedImage temp1 = new BufferedImage(expectedWidth,image.getHeight(),BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d1 = temp1.createGraphics();

        //if(expectedWidth>image.getWidth()) {

            int xMiddle = image.getWidth()-leftProtected-rightProtected;

            BufferedImage left = image.getSubimage(0, 0, leftProtected, image.getHeight());
            BufferedImage middleX = image.getSubimage(leftProtected, 0, xMiddle, image.getHeight());
            BufferedImage right = image.getSubimage(image.getWidth() - rightProtected, 0, rightProtected, image.getHeight());

            middleX = resize(middleX, expectedWidth, middleX.getHeight());

            g2d1.drawImage(left, 0, 0, null);
            g2d1.drawImage(middleX, leftProtected, 0, null);
            g2d1.drawImage(right, expectedWidth - rightProtected, 0, null);

        //}

        g2d1.dispose();

        BufferedImage temp2 = new BufferedImage(temp1.getWidth(), expectedHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d2 = temp2.createGraphics();

        //if(expectedHeight>image.getHeight()) {

            int yMiddle = temp1.getHeight()-upProtected-downProtected;

            BufferedImage up = temp1.getSubimage(0, 0, temp1.getWidth(), temp1.getHeight());
            BufferedImage middleY = temp1.getSubimage(0, upProtected, temp1.getWidth(), yMiddle);
            BufferedImage down = temp1.getSubimage(0, temp1.getHeight() - downProtected, temp1.getWidth(), downProtected);

            middleY = resize(middleY, middleY.getWidth(), expectedHeight);

            g2d2.drawImage(up, 0, 0, null);
            g2d2.drawImage(middleY, 0, upProtected, null);
            g2d2.drawImage(down, 0, expectedHeight - downProtected, null);

        //}

        g2d2.dispose();
        return temp2;

    }

    static BufferedImage paint(int x,int y) {

        BufferedImage image = new BufferedImage(x,y,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = image.createGraphics();

        GradientPaint gp = new GradientPaint(0, 0, new Color(0,84,227),
                x, y, new Color(61,199,225));
        g2.setPaint(gp);
        g2.fill(new Rectangle2D.Double(0, 0, x, y));

        g2.dispose();

        return image;

    }

    public static BufferedImage convert(BufferedImage image, int blockNumberX, int blockNumberY, MinesweeperImageType type){

        int expectedWidth = ( blockNumberX - MinesweeperConstant.StartBlockNumberX) * MinesweeperConstant.BlockSize + image.getWidth();
        int expectedHeight = ( blockNumberY - MinesweeperConstant.StartBlockNumberY) * MinesweeperConstant.BlockSize + image.getHeight();

        switch (type){
            case MainBox:
                return enlarge(image,30,24,172,23,expectedWidth,expectedHeight);
            case SecondBox:
                return enlarge(image,6,6,6,6,expectedWidth, image.getHeight());
            case ContentBox:
                return enlarge(image,6,6,6,6,expectedWidth,expectedHeight);
            case TitleBox:
                if(blockNumberX == MinesweeperConstant.StartBlockNumberX) return image;
                return paint(expectedWidth,image.getHeight());
        }

        return image;
    }

    public static BufferedImage createEmptyPool(int x, int y){
        int poolWidth = MinesweeperConstant.TinyBlockSize * x + MinesweeperConstant.PoolGap * (x-1);
        int poolHeight = MinesweeperConstant.TinyBlockSize * y + MinesweeperConstant.PoolGap * (y-1);

        BufferedImage image = new BufferedImage(poolWidth,poolHeight,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(new Color(123,123,123));
        g2d.fillRect(0,0, image.getWidth(), image.getHeight());

        g2d.setColor(new Color(189,189,189));
        for(int i = MinesweeperConstant. PoolGap; i < poolWidth ; i+= MinesweeperConstant.PoolGap+ MinesweeperConstant.TinyBlockSize){
            for(int j = MinesweeperConstant.PoolGap; j < poolHeight ; j+= MinesweeperConstant.PoolGap+ MinesweeperConstant.TinyBlockSize){
                g2d.fillRect(i,j, MinesweeperConstant.TinyBlockSize, MinesweeperConstant.TinyBlockSize);
            }
        }

        g2d.dispose();
        return image;
    }

    public static BufferedImage createTransparentPool(int[][] mines){

        int x = mines.length;
        int y = mines[0].length;

        int[][] mineNumber = MineFactory.getMineNumber(mines);

        BufferedImage image = createEmptyPool(x,y);
        Graphics2D g2d = image.createGraphics();

        for(int i=0;i<x;i++){

            for(int j=0;j<y;j++){

                int positionX = MinesweeperConstant.PoolGap + (MinesweeperConstant.PoolGap + MinesweeperConstant.TinyBlockSize) * i;
                int positionY = MinesweeperConstant.PoolGap + (MinesweeperConstant.PoolGap + MinesweeperConstant.TinyBlockSize) * j;

                if(mines[i][j]==1){
                    g2d.drawImage(MinesweeperSheet.getElement(MinesweeperSheetType.Mine,2),positionX, positionY,null);
                } else {
                    if(mineNumber[i][j]>0){
                        g2d.drawImage(MinesweeperSheet.getElement(MinesweeperSheetType.TinyNumber,mineNumber[i][j]),positionX,positionY,null);
                    }
                }

            }
        }

        g2d.dispose();
        return image;

    }

    public static BufferedImage createCertainMoveImage(int[][] mines,int[][] checklist){

        System.out.println(Arrays.deepToString(mines));
        System.out.println(Arrays.deepToString(checklist));

        int[][] mask = MineFactory.getMask(mines,checklist);

        BufferedImage bi = createTransparentPool(mines);
        BufferedImage buttons = createCertainButtons(mask);

        Graphics2D g2d = bi.createGraphics();
        g2d.drawImage(buttons,0,0,null);
        g2d.drawImage(createExplodedMines(mines,checklist),0,0,null);

        g2d.dispose();
        return bi;

    }

    public static BufferedImage createExplodedMines(int[][]mines, int[][] checklist){
        int[][] exploded = MineFactory.getExplodedMines(mines,checklist);

        int x = mines.length;
        int y = mines[0].length;

        int poolWidth = MinesweeperConstant.TinyBlockSize * x + MinesweeperConstant.PoolGap * (x-1);
        int poolHeight = MinesweeperConstant.TinyBlockSize * y + MinesweeperConstant.PoolGap * (y-1);

        BufferedImage image = new BufferedImage(poolWidth,poolHeight,BufferedImage.TYPE_INT_ARGB_PRE);

        Graphics2D g2d = image.createGraphics();

        for(int i=0;i<x;i++){

            for(int j=0;j<y;j++){

                int positionX = MinesweeperConstant.PoolGap + (MinesweeperConstant.PoolGap + MinesweeperConstant.TinyBlockSize) * i;
                int positionY = MinesweeperConstant.PoolGap + (MinesweeperConstant.PoolGap + MinesweeperConstant.TinyBlockSize) * j;

                if(exploded[i][j]==1){
                    g2d.drawImage(MinesweeperSheet.getElement(MinesweeperSheetType.Mine,4),positionX, positionY,null);
                }
            }
        }

        g2d.dispose();
        return image;

    }

    public static BufferedImage createCertainButtons(int[][] mask){

        int x = mask.length;
        int y = mask[0].length;

        BufferedImage button = MinesweeperSheet.getElement(MinesweeperSheetType.Flag,2);

        int poolWidth = button.getWidth() * x;
        int poolHeight = button.getHeight() * y;

        BufferedImage image = new BufferedImage(poolWidth,poolHeight,BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g2d = image.createGraphics();

        for(int i = 0;i< mask.length;i++){
            for(int j = 0;j< mask[0].length;j++){

                int positionX = MinesweeperConstant.PoolGap + (MinesweeperConstant.PoolGap + MinesweeperConstant.TinyBlockSize) * i;
                int positionY = MinesweeperConstant.PoolGap + (MinesweeperConstant.PoolGap + MinesweeperConstant.TinyBlockSize) * j;

                if(mask[i][j]==0) {
                    g2d.drawImage(button, positionX, positionY, null);
                }
            }
        }

        g2d.dispose();
        return image;

    }

    public static BufferedImage createPoolWithAllButtons(int x, int y){

        BufferedImage button = MinesweeperSheet.getElement(MinesweeperSheetType.Flag,2);

        int poolWidth = button.getWidth() * x;
        int poolHeight = button.getHeight() * y;

        BufferedImage image = new BufferedImage(poolWidth,poolHeight,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(new Color(123,123,123));
        g2d.fillRect(0,0, image.getWidth(), image.getHeight());

        for(int i = MinesweeperConstant.PoolGap; i < poolWidth ; i+= MinesweeperConstant.PoolGap + MinesweeperConstant.TinyBlockSize){
            for(int j = MinesweeperConstant.PoolGap; j < poolHeight ; j+= MinesweeperConstant.PoolGap + MinesweeperConstant.TinyBlockSize){
                g2d.drawImage(button,i,j,null);
            }
        }

        g2d.dispose();
        return image;
    }

}
