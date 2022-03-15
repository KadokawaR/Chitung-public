package lielietea.mirai.plugin.utils.image;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageEnlarger {
    public static BufferedImage zoomInImage(BufferedImage originalImage, int maxWidth, int maxHeight) {
        BufferedImage newImage = new BufferedImage(maxWidth, maxHeight, originalImage.getType());
        Graphics g = newImage.getGraphics();
        g.drawImage(originalImage, 0, 0, maxWidth, maxHeight, null);
        g.dispose();
        return newImage;
    }
}
