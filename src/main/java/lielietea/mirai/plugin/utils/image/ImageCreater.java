package lielietea.mirai.plugin.utils.image;

import lielietea.mirai.plugin.utils.json.JsonFile;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.GroupMessageEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class ImageCreater {

    static final int wx = 94;
    static final int hx = 251;


    public static BufferedImage createWinnerImage(Contact winner) throws IOException {
        //读入头像和wanted底图
        InputStream is1 = JsonFile.getInputStream(winner.getAvatarUrl());
        BufferedImage img1 = ImageIO.read(is1);
        InputStream is2 = ImageCreater.class.getResourceAsStream("/pics/winner/wanted.jpg");
        BufferedImage img2 = ImageIO.read(is2);
        BufferedImage img0;
        //头像变形
        if (img1.getHeight() < 512) {
            img0 = ImageEnlarger.zoomInImage(img1, 512, 512);
        } else {
            ImageScale isc = new ImageScale();
            img0 = isc.imageZoomOut(img1, 512, 512, false);
        }

        int w0 = img0.getWidth();
        int h0 = img0.getHeight();
        int w2 = img2.getWidth();
        int h2 = img2.getHeight();

        // 从图片中读取RGB
        int[] ImageArrayOne = new int[w0 * h0];
        ImageArrayOne = img0.getRGB(0, 0, w0, h0, ImageArrayOne, 0, w0); // 逐行扫描图像中各个像素的RGB到数组中

        int[] ImageArrayTwo = new int[w2 * h2];
        ImageArrayTwo = img2.getRGB(0, 0, w2, h2, ImageArrayTwo, 0, w2);

        // 生成新图片
        BufferedImage imgNew;
        imgNew = new BufferedImage(w2, h2, BufferedImage.TYPE_INT_RGB);
        imgNew.setRGB(0, 0, w2, h2, ImageArrayTwo, 0, w2); // 设置上半部分或左半部分的RGB
        imgNew.setRGB(wx, hx, w0, h0, ImageArrayOne, 0, w0);

        return imgNew;
    }

    public static BufferedImage getImageFromResource(String filepath) throws IOException {
        InputStream is2 = ImageCreater.class.getResourceAsStream(filepath);
        assert is2 != null;
        return ImageIO.read(is2);
    }

    //裁剪图片
    public static BufferedImage cutImage(BufferedImage img, int xStart, int yStart, int xEnd, int yEnd) {
        int w = img.getWidth();
        int h = img.getHeight();
        if (xEnd > w) {
            xEnd = w;
        }
        if (yEnd > h) {
            yEnd = h;
        }
        int[] imageArray = new int[(xEnd - xStart) * (yEnd - yStart)];
        imageArray = img.getRGB(xStart, yStart, xEnd - xStart, yEnd - yStart, imageArray, 0, xEnd - xStart);
        BufferedImage imgNew;
        imgNew = new BufferedImage(xEnd - xStart, yEnd - yStart, BufferedImage.TYPE_INT_RGB);
        imgNew.setRGB(0, 0, xEnd - xStart, yEnd - yStart, imageArray, 0, xEnd - xStart);

        return imgNew;
    }

    //在小图上加大图
    public static BufferedImage addImage(BufferedImage imgBig, BufferedImage imgSmall, int xStart, int yStart) {
        int wB = imgBig.getWidth();
        int hB = imgBig.getHeight();
        int wS = imgSmall.getWidth();
        int hS = imgSmall.getHeight();
        if (xStart < 0) {
            imgSmall = cutImage(imgSmall, -xStart, 0, wS, hS);
        }
        if (yStart < 0) {
            imgSmall = cutImage(imgSmall, 0, -yStart, wS, hS);
        }
        if (xStart + wS > wB) {
            imgSmall = cutImage(imgSmall, 0, 0, wB - xStart, hS);
        }
        if (xStart + wS > wB) {
            imgSmall = cutImage(imgSmall, 0, 0, wS, hB - yStart);
        }
        int wS_new = imgSmall.getWidth();
        int hS_new = imgSmall.getHeight();

        Graphics2D g2d = imgBig.createGraphics();
        g2d.drawImage(imgSmall.getScaledInstance(wS_new, hS_new, Image.SCALE_SMOOTH), xStart, yStart, null);
        //g2d.drawImage(imgSmall, xStart, yStart, wS_new, hS_new, null);
        g2d.dispose();
        return imgBig;
    }

    public static BufferedImage addImageAtCenter(BufferedImage imgBig, BufferedImage imgSmall) {
        int wB = imgBig.getWidth();
        int hB = imgBig.getHeight();
        int wS = imgSmall.getWidth();
        int hS = imgSmall.getHeight();

        int xStart = (wB - wS) / 2;
        int yStart = (hB - hS) / 2;

        Graphics2D g2d = imgBig.createGraphics();
        g2d.drawImage(imgSmall.getScaledInstance(wS, hS, Image.SCALE_SMOOTH), xStart, yStart, null);
        //g2d.drawImage(imgSmall,xStart,yStart,wS,hS,null);
        g2d.dispose();
        return imgBig;
    }

    public static void sendImage(BufferedImage image, GroupMessageEvent event) throws IOException {
        event.getSubject().sendMessage(Contact.uploadImage(event.getSubject(), BufferedImageToInputStream.execute(image)));
        BufferedImageToInputStream.execute(image).close();
    }

    //在原图的右边加上一张等高的新图片
    public static BufferedImage addImageAtRight(BufferedImage imgLeft, BufferedImage imgRight) {
        int hL = imgLeft.getHeight();
        int wL = imgLeft.getWidth();
        int wR = imgRight.getWidth();
        BufferedImage imgNew = new BufferedImage(wL + wR, hL, BufferedImage.TYPE_INT_RGB);
        int[] imgLeftArray = new int[wL * hL];
        int[] imgRightArray = new int[wR * hL];
        imgLeftArray = imgLeft.getRGB(0, 0, wL, hL, imgLeftArray, 0, wL);
        imgRightArray = imgLeft.getRGB(0, 0, wR, hL, imgRightArray, 0, wR);
        imgNew.setRGB(0, 0, wL, hL, imgLeftArray, 0, wL);
        imgNew.setRGB(wL, 0, wR, hL, imgRightArray, 0, wR);
        return imgNew;
    }

    //在原图的下边加上一张等宽的新图片
    public static BufferedImage addImageAtBottom(BufferedImage imgUp, BufferedImage imgBottom) {
        int hU = imgUp.getHeight();
        int hB = imgBottom.getHeight();
        int wU = imgUp.getWidth();
        BufferedImage imgNew = new BufferedImage(wU, hU + hB, BufferedImage.TYPE_INT_RGB);
        int[] imgLeftArray = new int[wU * hU];
        int[] imgRightArray = new int[wU * hB];
        imgLeftArray = imgUp.getRGB(0, 0, wU, hU, imgLeftArray, 0, wU);
        imgRightArray = imgUp.getRGB(0, 0, wU, hB, imgRightArray, 0, wU);
        imgNew.setRGB(0, 0, wU, hU, imgLeftArray, 0, wU);
        imgNew.setRGB(0, hU, wU, hB, imgRightArray, 0, wU);
        return imgNew;
    }
}
