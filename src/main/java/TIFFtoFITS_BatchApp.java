import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.util.BufferedFile;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

public class TIFFtoFITS_BatchApp {

    public static void main(String[] args) {
        String dirname = "C:\\Users\\kfd18\\kfd18_Downloads\\Stack2";
        File dirFile = new File(dirname);
        String[] fileNames = dirFile.list();
        for (String filename: fileNames) {

            if (!(filename.toLowerCase().endsWith(".tif") || filename.toLowerCase().endsWith(".tiff"))) {
                continue;
            }

            TIFFtoFITS app = new TIFFtoFITS(dirname, filename);

            short[][] dataShorts_viaOpener = app.getRawData();

            double[][] dataDoubles = app.convertShortToDouble2D(dataShorts_viaOpener);

            double[][][] images = new double[app.nBands][app.nRows][app.nCols];
            for (int band_num = 0; band_num < app.nBands; band_num++) {
                images[band_num] = app.reshape1Dto2D(dataDoubles[band_num], app.nRows, app.nCols);
            }

            // Create the FITS data structure
            Fits f = new Fits();
            try {
                f.addHDU(FitsFactory.HDUFactory(images));
            } catch (FitsException e) {
                throw new RuntimeException(e.getMessage());
            }

            // Save the FITS structure
            String saveDirName = "C:\\Users\\kfd18\\kfd18_Downloads\\Stack2";
            String baseFileName = FilenameUtils.removeExtension(filename);
            String saveFileName = baseFileName + ".FITS";
            try {
                BufferedFile bf = new BufferedFile(saveDirName + "\\" + saveFileName, "rw");
                f.write(bf);
                bf.close();
            } catch (FitsException | IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }



    }
}
