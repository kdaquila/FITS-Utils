package tifftofits;

import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.util.BufferedFile;
import org.apache.commons.io.FilenameUtils;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class BatchApp {

    public static void main(String[] args) {

        System.out.println("Running the TIFF to FITS Batch App");

        // Get the input directory
        String dirname;
        if (args.length >= 2) {
            throw new IllegalArgumentException("Must provide exactly 0-1 arguments. Found " + args.length);
        } else if (args.length == 1) {
            dirname = args[0];
        } else {
            JFileChooser f = new JFileChooser();
            f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            f.setCurrentDirectory(new File(System.getProperty("user.dir")));
            int result = f.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                dirname = f.getSelectedFile().getAbsolutePath();
            } else {
                System.out.println("Ended by user");
                return;
            }
        }
        System.out.println("Loading images from: " + dirname);

        // Get all the file names
        File dirFile = new File(dirname);
        String[] fileNames = dirFile.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.toLowerCase().endsWith(".tif") || name.toLowerCase().endsWith(".tiff"));
            }
        });
        if (fileNames == null) throw new RuntimeException("did not find any filenames in the folder");

        GUI gui = new GUI();
        gui.updateProgressBar(0);

        // Convert TIFF to FITS
        for (int file_num = 0; file_num < fileNames.length; file_num++) {

            // Get the filename
            String filename = fileNames[file_num];

            System.out.println("Now converting image: " + filename);

            // update the progress bar
            int percentProgress = (int)(100.0*file_num/fileNames.length);
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    gui.updateProgressBar(percentProgress);
                    gui.updateLabel(filename);
                }
            });

            TIFFtoFITS app = new TIFFtoFITS(dirname, filename);

            // Get the data
            short[][] dataShorts_viaOpener = app.getRawData();

            // Convert to doubles
            double[][] dataDoubles = app.convertShort2DToDouble2D(dataShorts_viaOpener);

            // Build the image stack
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
            String baseFileName = FilenameUtils.removeExtension(filename);
            String saveFileName = baseFileName + ".FITS";
            try {
                BufferedFile bf = new BufferedFile(dirname + "\\" + saveFileName, "rw");
                f.write(bf);
                bf.close();
            } catch (FitsException | IOException e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        // Close the gui
        gui.dispatchEvent(new WindowEvent(gui, WindowEvent.WINDOW_CLOSING));

    }
}
