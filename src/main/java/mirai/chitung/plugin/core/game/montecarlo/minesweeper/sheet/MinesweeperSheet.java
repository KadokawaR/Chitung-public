package mirai.chitung.plugin.core.game.montecarlo.minesweeper.sheet;

import mirai.chitung.plugin.core.game.montecarlo.minesweeper.imageutil.MinesweeperImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

public class MinesweeperSheet {

    static final String SHEET_PATH = "/pics/casino/minesweeper/MineSweeper-sheet.png";

    static BufferedImage Sheet;

    public static class SheetData{
        int contentStartY;
        public int elementX;
        public int elementY;
        SheetData(int contentStartY,int elementX,int elementY){
           this.contentStartY=contentStartY;
           this.elementX=elementX;
           this.elementY=elementY;
        }
    }
    
    public static ConcurrentHashMap<MinesweeperSheetType,SheetData> data = new ConcurrentHashMap<>();

    static {

        Sheet = getSheet();

        data.put(MinesweeperSheetType.Number,new SheetData(0,26,46));
        data.put(MinesweeperSheetType.TinyNumber,new SheetData(46,30,30));
        data.put(MinesweeperSheetType.Mine,new SheetData(76,30,30));
        data.put(MinesweeperSheetType.Flag,new SheetData(106,32,32));
        data.put(MinesweeperSheetType.Face,new SheetData(138,52,52));

        data.put(MinesweeperSheetType.MainBox,new SheetData(190,323,239));
        data.put(MinesweeperSheetType.SecondBox,new SheetData(429,268,82));
        data.put(MinesweeperSheetType.ThirdBox,new SheetData(511,82,50));
        data.put(MinesweeperSheetType.ContentBox,new SheetData(561,268,44));
        data.put(MinesweeperSheetType.Title,new SheetData(605,311,42));
        data.put(MinesweeperSheetType.TitleBox,new SheetData(647,311,42));
    }

    static BufferedImage getSheet(){
        InputStream is = MinesweeperImage.class.getResourceAsStream(SHEET_PATH);
        BufferedImage image = null;
        try {
            assert is != null;
            image = ImageIO.read(is);
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
    
    public static BufferedImage getElement(MinesweeperSheetType type){
        SheetData sd = data.get(type);
        return Sheet.getSubimage(0,sd.contentStartY,sd.elementX,sd.elementY);
    }

    public static BufferedImage getElement(MinesweeperSheetType type, int number/*��ͼƬ�еڼ�������1��ʼ*/){
        SheetData sd = data.get(type);
        return Sheet.getSubimage((number-1)*sd.elementX,sd.contentStartY,sd.elementX,sd.elementY);
    }


}
