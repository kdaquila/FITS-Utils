import ij.ImagePlus;
import ij.io.FileInfo;
import ij.io.ImageReader;
import ij.io.Opener;
import ij.io.TiffDecoder;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {

    public static void main(String[] args) throws IOException {
        String inputString = "C:\\Users\\kfd18\\kfd18_Downloads\\gradient.tiff";
        Opener opener = new Opener();
        ImagePlus imagePlus = opener.openImage(inputString);
        BufferedImage bufferedImage = imagePlus.getBufferedImage();
        TiffDecoder tiffDecoder = new TiffDecoder(dirname, filename);
        FileInfo[] fileInfos = tiffDecoder.getTiffInfo();

        // find the main file's info
        FileInfo mainFileInfo = new FileInfo();
        boolean foundMain = false;
        for (FileInfo fileInfo: fileInfos) {
            if (fileInfo.fileType==FileInfo.RGB48) {
                mainFileInfo = fileInfo;
                foundMain = true;
                break;
            }
        }
        if (foundMain == false) throw new RuntimeException("Could not find the GRAY16 image inside the given file");

        ImageReader imageReader = new ImageReader(mainFileInfo);
        String inputString = "C:\\Users\\kfd18\\kfd18_Downloads\\gradient.tiff";
        InputStream inputStream = new FileInputStream(inputString);
        short[][] data = (short[][]) imageReader.readPixels(inputStream);
        int[][] dataInt = new int[3][1920*1280];
        for (int channel_num = 0; channel_num < 3; channel_num++) {
            for (int index = 0; index < 1920*1280; index++) {
                dataInt[channel_num][index] = (int) data[channel_num][index]&0xFFFF;
            }
        }
        int x = 0;
    }
}
