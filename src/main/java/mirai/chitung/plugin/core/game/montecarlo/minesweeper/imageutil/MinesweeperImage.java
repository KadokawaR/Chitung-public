package mirai.chitung.plugin.core.game.montecarlo.minesweeper.imageutil;

import mirai.chitung.plugin.core.game.montecarlo.minesweeper.data.MinesweeperConstant;
import mirai.chitung.plugin.core.game.montecarlo.minesweeper.data.MinesweeperFace;
import mirai.chitung.plugin.core.game.montecarlo.minesweeper.data.MinesweeperPoolType;
import mirai.chitung.plugin.core.game.montecarlo.minesweeper.sheet.MinesweeperSheet;
import mirai.chitung.plugin.core.game.montecarlo.minesweeper.sheet.MinesweeperSheetType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class MinesweeperImage {

    static class Position{
        int x;
        int y;
        boolean convertible;

        Position(int x,int y){
            this.x=x;
            this.y=y;
            convertible=false;
        }

        Position(int x,int y,boolean convertible){
            this.x=x;
            this.y=y;
            this.convertible=convertible;
        }
    }

    static ConcurrentHashMap<MinesweeperImageType, Position> data = new ConcurrentHashMap<>();

    static{
        data.put(MinesweeperImageType.MainBox,new Position(0,0));
        data.put(MinesweeperImageType.SecondBox,new Position(32,72));
        data.put(MinesweeperImageType.Title,new Position(6,6));
        data.put(MinesweeperImageType.TitleBox,new Position(6,6));
        data.put(MinesweeperImageType.ThirdBoxLeft,new Position(49,88));
        data.put(MinesweeperImageType.ThirdBoxRight,new Position(-39,88,true));
        data.put(MinesweeperImageType.NumberBoxLeft,new Position(51,90));
        data.put(MinesweeperImageType.NumberBoxRight,new Position(-41,90,true));
        data.put(MinesweeperImageType.Face,new Position(0,88,true));
        data.put(MinesweeperImageType.ContentBox,new Position(30,172));
        data.put(MinesweeperImageType.Pool,new Position(33,175));
    }

    static BufferedImage drawElement(BufferedImage image, BufferedImage element, MinesweeperImageType type){

        Position position = data.get(type);

        Graphics2D g2d = image.createGraphics();

        if(position.convertible){
            switch (type) {
                case ThirdBoxRight:
                    g2d.drawImage(element, image.getWidth() + position.x - MinesweeperSheet.data.get(MinesweeperSheetType.ThirdBox).elementX, position.y, null);
                    break;
                case NumberBoxRight:
                    g2d.drawImage(element, image.getWidth() + position.x - MinesweeperSheet.data.get(MinesweeperSheetType.Number).elementX*3, position.y, null);
                    break;
                case Face:
                    g2d.drawImage(element, (image.getWidth() - element.getWidth()) / 2 + 6, position.y, null);
            }
        } else {
            g2d.drawImage(element,position.x,position.y,null);
        }

        g2d.dispose();
        return image;
    }

    static BufferedImage drawElement(BufferedImage image, BufferedImage element, MinesweeperImageType type, boolean convertible, int x, int y){

        Position position = data.get(type);

        if(convertible) element = MinesweeperImageUtil.convert(element,x,y,type);

        Graphics2D g2d = image.createGraphics();

        if(position.convertible){
            switch (type) {
                case ThirdBoxRight:
                case NumberBoxRight:
                    g2d.drawImage(element, image.getWidth() + position.x, position.y, null);
                    break;
                case Face:
                    g2d.drawImage(element, (image.getWidth() - element.getWidth()) / 2, position.y, null);
            }
        } else {
            g2d.drawImage(element,position.x,position.y,null);
        }

        g2d.dispose();

        return image;
    }

    static BufferedImage getNumberBox(int[] num){

        MinesweeperSheet.SheetData sd = MinesweeperSheet.data.get(MinesweeperSheetType.Number);
        BufferedImage bi = new BufferedImage(sd.elementX*3,sd.elementY,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bi.createGraphics();

        for(int i=0;i<3;i++) {
            g2d.drawImage(MinesweeperSheet.getElement(MinesweeperSheetType.Number,num[i]+1),sd.elementX*i,0,null);
        }

        g2d.dispose();
        return bi;

    }

    static BufferedImage getFace(MinesweeperFace face){
        return MinesweeperSheet.getElement(MinesweeperSheetType.Face, face.getCode());
    }

    public static BufferedImage assembleStructure(int blockNumX, int blockNumY, int[] leftBoxNumber, int[] rightBoxNumber, MinesweeperFace face){

        int width = MinesweeperSheet.getElement(MinesweeperSheetType.MainBox).getWidth() + (blockNumX - MinesweeperConstant.StartBlockNumberX)* MinesweeperConstant.BlockSize;
        int height = MinesweeperSheet.getElement(MinesweeperSheetType.MainBox).getHeight() + (blockNumY - MinesweeperConstant.StartBlockNumberY)* MinesweeperConstant.BlockSize;

        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);

        drawElement(image, MinesweeperSheet.getElement(MinesweeperSheetType.MainBox), MinesweeperImageType.MainBox, true, blockNumX, blockNumY);
        drawElement(image, MinesweeperSheet.getElement(MinesweeperSheetType.SecondBox), MinesweeperImageType.SecondBox, true, blockNumX, blockNumY);
        drawElement(image, MinesweeperSheet.getElement(MinesweeperSheetType.ContentBox), MinesweeperImageType.ContentBox,true,blockNumX,blockNumY);
        drawElement(image, MinesweeperSheet.getElement(MinesweeperSheetType.ThirdBox), MinesweeperImageType.ThirdBoxLeft);
        drawElement(image, MinesweeperSheet.getElement(MinesweeperSheetType.ThirdBox), MinesweeperImageType.ThirdBoxRight);
        drawElement(image, getFace(face), MinesweeperImageType.Face);
        drawElement(image, getNumberBox(leftBoxNumber), MinesweeperImageType.NumberBoxLeft);
        drawElement(image, getNumberBox(rightBoxNumber), MinesweeperImageType.NumberBoxRight);
        drawElement(image, MinesweeperSheet.getElement(MinesweeperSheetType.TitleBox), MinesweeperImageType.TitleBox,true,blockNumX,blockNumY);
        drawElement(image, MinesweeperSheet.getElement(MinesweeperSheetType.Title), MinesweeperImageType.Title);

        //drawElement(image, ImageUtil.createPoolWithAllButtons(blockNumX,blockNumY),ImageType.Pool);
        //drawElement(image,ImageUtil.createTransparentPool(MineFactory.randomMine(blockNumX,blockNumY,blockNumX*blockNumY/10)),ImageType.Pool);

        return image;
    }

    public static BufferedImage addPool(BufferedImage image, int blockNumX, int blockNumY, MinesweeperPoolType type, int[][] mines, int[][] checklist){

        switch (type) {
            case NewPool:
                drawElement(image, MinesweeperImageUtil.createPoolWithAllButtons(blockNumX, blockNumY), MinesweeperImageType.Pool);
                break;
            case TouchedPool:
                drawElement(image, MinesweeperImageUtil.createCertainMoveImage(mines, checklist), MinesweeperImageType.Pool);
                break;
            case TransparentPool:
                drawElement(image, MinesweeperImageUtil.createTransparentPool(mines), MinesweeperImageType.Pool);
        }

        return image;

    }

    public static void saveImage(){

        int[] left = new int[]{0,0,1};
        int[] right = new int[]{2,9,3};
        BufferedImage bi = assembleStructure(20,20,left,right, MinesweeperFace.Dead);
        //addPool(bi,20,20,PoolType.TransparentPool,MineFactory.randomMine(20,20,10),null);
        addPool(bi,20,20, MinesweeperPoolType.NewPool,null,null);
        File outputfile = new File("C:\\Users\\Yinhao Lu\\Desktop\\save.png");

        try {
            ImageIO.write(bi, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void saveImage(BufferedImage bi,String path){

        File outputfile = new File("C:\\Users\\Yinhao Lu\\Desktop\\wtf\\"+path+".png");

        try {
            ImageIO.write(bi, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
