package mirai.chitung.plugin.utils.fileutils;

import java.io.*;

public class Copy {

    public static void fromInnerResource(String path,String targetPath){
        File file = new File(targetPath);
        InputStream fis = null;
        DataInputStream dis = null;
        OutputStream fos = null;
        DataOutputStream dos = null;
        try {
            fis = Copy.class.getResourceAsStream(path);
            assert fis != null;
            dis = new DataInputStream(fis);
            fos = new FileOutputStream(file);
            dos = new DataOutputStream(fos);
            int index=0;
            while((index=dis.read())!=-1){
                dos.write(index);
            }
            dos.flush();

        } catch(Exception e){
            e.printStackTrace();
        } finally {
            try {
                assert dos != null;
                dos.close();
                fos.close();
                dis.close();
                fis.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
