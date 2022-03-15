package lielietea.mirai.plugin.utils.image;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class BufferedImageToInputStream {
    public static InputStream execute(BufferedImage image) throws IOException {
        InputStream is;
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ImageOutputStream imOut;
        imOut = ImageIO.createImageOutputStream(bs);
        ImageIO.write(image, "png", imOut);
        return new ByteArrayInputStream(bs.toByteArray());
    }
}
