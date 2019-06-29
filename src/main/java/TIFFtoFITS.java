import ij.ImagePlus;
import ij.io.FileInfo;
import ij.io.ImageReader;
import ij.io.Opener;
import ij.io.TiffDecoder;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.util.BufferedFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TIFFtoFITS {

    String directoryName;
    String fileName;
    int nRows;
    int nCols;
    int nBands;

    public TIFFtoFITS(String dir, String filename) {
        this.directoryName = dir;
        this.fileName = filename;
        nRows = 0;
        nCols = 0;
        nBands = 3;
    }

    public static void main(String[] args) {
        String dirname = "C:\\Users\\kfd18\\kfd18_Downloads\\Stack2";
        String filename = "DSC_0001.TIF";
        TIFFtoFITS app = new TIFFtoFITS(dirname, filename);

        short[][] dataShorts_viaOpener = app.getRawData();

        double[][] dataDoubles = app.convertShortToDouble2D(dataShorts_viaOpener);

        double[][][] images = new double[app.nBands][app.nRows][app.nCols];
        for (int band_num = 0; band_num < app.nBands; band_num++) {
            images[band_num] = app.reshape1Dto2D(dataDoubles[band_num], app.nRows, app.nCols);
        }

        // Create the FITS data structure
        Fits f = new Fits();
//        for (int band_num = 0; band_num < app.nBands; band_num++) {
//            try {
//                double[][] image = images[band_num];
//                BasicHDU basicHDU = FitsFactory.HDUFactory(image);
//                f.addHDU(basicHDU);
//            } catch (FitsException e) {
//                e.printStackTrace();
//                throw new RuntimeException(e.getMessage());
//            }
//        }
        try {
            f.addHDU(FitsFactory.HDUFactory(images));
        } catch (FitsException e) {
            throw new RuntimeException(e.getMessage());
        }

        // Save the FITS structure
        String saveDirName = "C:\\Users\\kfd18\\kfd18_Downloads";
        String saveFileName = "image.FITS";
        try {
            BufferedFile bf = new BufferedFile(saveDirName + "\\" + saveFileName, "rw");
            f.write(bf);
            bf.close();
        } catch (FitsException | IOException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public short[][] getRawData() {
        return getRawData_viaImageReader();
    }

    public short[][] getRawData_viaOpener() {
        Opener opener = new Opener();
        ImagePlus imagePlus = opener.openImage(directoryName, fileName);
        int[] dims = imagePlus.getDimensions();
        nCols = dims[0];
        nRows = dims[1];
        int nPixelsPerBand = nCols*nRows;
        short[][] dataStack =  new short[3][nPixelsPerBand];
        for (int band_num = 0; band_num < 3; band_num++) {
            dataStack[band_num] = (short[]) imagePlus.getStack().getPixels(band_num+1);
        }
        return dataStack;
    }

    public short[][] getRawData_viaImageReader() {
        // Open the TIFF Decoder
        TiffDecoder tiffDecoder = new TiffDecoder(directoryName, fileName);

        // Get the TIFF File Info List
        FileInfo[] fileInfos;
        try {
            fileInfos = tiffDecoder.getTiffInfo();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        // Find the main file's info
        FileInfo mainFileInfo = new FileInfo();
        boolean foundMain = false;
        for (FileInfo fileInfo: fileInfos) {
            if (fileInfo.fileType==FileInfo.RGB48) {
                mainFileInfo = fileInfo;
                foundMain = true;
                break;
            }
        }
        if (!foundMain) throw new RuntimeException("Could not find the RGB48 image inside the given file");

        nCols = mainFileInfo.width;
        nRows = mainFileInfo.height;

        // Read the Data as 16-bit integers
        String inputString = directoryName + "\\" + fileName;
        InputStream inputStream;
        try {
             inputStream = new FileInputStream(inputString);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }

        ImageReader imageReader = new ImageReader(mainFileInfo);

        return (short[][]) imageReader.readPixels(inputStream);
    }

    public double[][] convertShortToDouble2D(short[][] inputs) {
        // Convert to 32-bit floats
        int dim1 = inputs.length;
        int dim2 = inputs[0].length;
        double[][] dataDoubles = new double[dim1][dim2];
        for (int i = 0; i < dim1; i++) {
            for (int j = 0; j < dim2; j++) {
                // cast short to int, but interpret the shorts as unsigned, so apply a bit-mask
                int intValue = (int) inputs[i][j]&0xFFFF;
                // cast int to double
                dataDoubles[i][j] = intValue;
            }
        }
        return dataDoubles;
    }

    public double[][] reshape1Dto2D(double[] input, int nRows, int nCols) {
        double[][] output = new double[nRows][nCols];
        for (int row_num = 0; row_num < nRows; row_num++) {
            for (int col_num = 0; col_num < nCols; col_num++) {
                int index = row_num*nCols + col_num;
                output[row_num][col_num] = input[index];
            }
        }
        return output;
    }
}
