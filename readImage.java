/*
 * CSS 490A
 * Project 1: Implement a Simple Content-Based Image Retrieval System
 * Modified by: Wai Kwan Shum
 * Last modified: Jan 15, 2018
 *
 * This class reads images from image database, calculate color histogram by using intensity and color-code method,
 * and write histogram data into text files.
 *
 * Assumption: There are 100 images in the image database
 *             There are 25 bins in the intensity color histogram
 *             There are 64 bins in the color-code color histogram
 *
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.Object.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * readImage class
 *
 */
public class readImage
{
    int imageCount = 1;
    double intensityMatrix [][] = new double[100][26];
    double colorCodeMatrix [][] = new double[100][64];
    double imageSize[] = new double[101];


    final int INTEN_BIN = 25;
    final int COLOR_CODE_BIN = 64;

    /**
     * readImage constructor
     * Each image is retrieved from the file.  The height and width are found for the image and the getIntensity and
     * getColorCode methods are called.
     *
     */
    public readImage()
    {
        while(imageCount < 101) {
            File file = new File("images/" + imageCount + ".jpg");
            BufferedImage image = null;

            try
            {
                // the line that reads the image file
                image = ImageIO.read(file);
            }
            catch (IOException e)
            {
                System.out.println("Error occurred when reading the file.");
            }

            getIntensity(image, image.getHeight(), image.getWidth());
            getColorCode(image, image.getHeight(), image.getWidth());
            getImageSize(imageCount, image.getHeight(), image.getWidth());
            imageCount++;
        }

        writeIntensity();
        writeColorCode();
        writeImageSize();
    }

    /**
     * This function gets intensity data for each image
     * The intensity value of each pixel in an image is calculated by this formula (I = 0.299R + 0.587G + 0.114B)
     * A matrix is used to store the intensity value for each image
     *
     */
    public void getIntensity(BufferedImage image, int height, int width){

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                Color rgb = new Color(image.getRGB(c, r));
                int red = rgb.getRed();
                int green = rgb.getGreen();
                int blue = rgb.getBlue();

                // Intensity formula
                int intenVal = (int) (0.299 * red + 0.587 * green + 0.114 * blue);
                int bin = intenVal / 10;

                if (bin == INTEN_BIN) {
                    intensityMatrix[imageCount - 1][bin]++;
                } else {
                    intensityMatrix[imageCount - 1][bin + 1]++;
                }

            }
        }
    }

    /**
     * This function gets color-code data for each image
     * The color-code value of each pixel in an image is calculated by combining the most significant 2 bits of RGB and
     * form a 6-bit color code
     * A matrix is used to store the color-code value for each image
     *
     */
    public void getColorCode(BufferedImage image, int height, int width){
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                int rgb = image.getRGB(c, r);

                // rgbStr is a 32 bits value (8 bits for each color [ARGB])
                String rgbStr = Integer.toBinaryString(rgb);

                // colorCodeStr is a 6 bits color code which consists of RGB
                String colorCodeStr = rgbStr.substring(8,10) + rgbStr.substring(16,18) + rgbStr.substring(24,26);

                // converts binary to decimal
                int colorCode = Integer.parseInt(colorCodeStr, 2);

                colorCodeMatrix[imageCount-1][colorCode]++;
            }
        }
    }

    /**
     * This function gets the size (number of pixels) for each image
     * A matrix is used to store the image size for each image
     *
     */
    public void getImageSize(int idx, int height, int width)
    {
        imageSize[idx] = height * width;
    }


    /**
     * This function writes the contents of the colorCode matrix to a file named colorCodes.txt
     *
     */
    public void writeColorCode(){
        try
        {
            PrintWriter writer = new PrintWriter(("colorCodes.txt"));
            for (int r = 0; r < 100; r++)
            {
                for (int c = 0; c < COLOR_CODE_BIN-1; c++)
                {
                    writer.print(colorCodeMatrix[r][c] + ",");
                }
                writer.println(colorCodeMatrix[r][COLOR_CODE_BIN-1]);
            }
            writer.close();
        }
        catch(IOException e)
        {
            System.out.println("Error occurred when writing to the file.");
        }
    }


    /**
     * This function writes the contents of the intensity matrix to a file called intensity.txt
     *
     */
    public void writeIntensity(){
        try
        {
            PrintWriter writer = new PrintWriter(("intensity.txt"));
            for (int r = 0; r < 100; r++)
            {
                for (int c = 1; c < INTEN_BIN; c++)
                {
                    writer.print(intensityMatrix[r][c] + ",");
                }
                writer.println(intensityMatrix[r][INTEN_BIN]);
            }
            writer.close();
        }
        catch(IOException e)
        {
            System.out.println("Error occurred when writing to the file.");
        }
    }


    /**
     * This function writes the contents of the image size matrix to a file called image_size.txt
     *
     */
    public void writeImageSize()
    {
        try
        {
            PrintWriter writer = new PrintWriter(("image_size.txt"));
            for (int i = 1; i < 100; i++)
            {
                writer.print(imageSize[i] + ",");
            }
            writer.print(imageSize[100]);
            writer.close();
        }
        catch(IOException e)
        {
            System.out.println("Error occurred when writing to the file.");
        }
    }

    /**
     * This is the main method of this class
     *
     */
    public static void main(String[] args)
    {
        new readImage();
    }

}
