/*
 * CSS 490A
 * Project 1: Implement a Simple Content-Based Image Retrieval System
 * Modified by: Wai Kwan Shum
 * Last modified: Jan 15, 2018
 *
 * This class fires up the GUI for the Content-Based Image Retrieval System.
 * - It reads intensity file and color-code file to calculate the distance between the query image and the others.
 * - The retrieved images should be displayed touser in a ascending order from left to right and top to bottom
 * (similarity rank).
 * - Each page displays 20 images, there are buttons to navigate between pages (next and previous)
 * - Reset button resets the GUI to its default state (same as when the GUI first launches)
 * - Random button randomly selects an image to be tested by either intensity or color-code method
 *
 * Assumption: There are 100 images in the image database
 *             There are 25 bins in the intensity color histogram
 *             There are 64 bins in the color-code color histogram
 *
*/

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * CBIR class
 *
 */
public class CBIR extends JFrame
{
    
    private JLabel photographLabel = new JLabel();  //container to hold a large 
    private JButton [] button; //creates an array of JButtons
    private int [] buttonOrder = new int [101]; //creates an array to keep up with the image order
    private double [] imageSize = new double[101]; //keeps up with the image sizes
    private GridLayout gridLayout1;
    private GridLayout gridLayout2;
    private GridLayout gridLayout3;
    private GridLayout gridLayout4;
    private JPanel panelBottom1;
    private JPanel panelBottom2;
    private JPanel panelTop;
    private JPanel buttonPanel;
    private JPanel Text;
    private JLabel picLabel;
    private Double [][] intensityMatrix = new Double [101][25];
    private Double [][] colorCodeMatrix = new Double [101][64];
    private HashMap <Double , ArrayList<Integer>> map;
    int picNo = 0;
    int imageCount = 1; //keeps up with the number of images displayed since the first page.
    int pageNo = 1;


    /**
     * This is the main method for this class.
     * It implements the run method which starts the GUI
     *
     */
    public static void main(String args[]) 
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run() 
            {
                CBIR app = new CBIR();
                app.setVisible(true);
            }
        });
    }

    /**
     * CBIR constructor
     * It initializes the components for the GUI
     *
     */
    public CBIR() 
    {
      //The following lines set up the interface including the layout of the buttons and JPanels.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Content-Based Image Retrieval System: Please Select an Image");
        panelBottom1 = new JPanel();
        panelBottom2 = new JPanel();
        panelTop = new JPanel();
        buttonPanel = new JPanel();
        gridLayout1 = new GridLayout(4, 5, 5, 5);
        gridLayout2 = new GridLayout(2, 1, 5, 5);
        gridLayout3 = new GridLayout(1, 2, 5, 5);
        gridLayout4 = new GridLayout(5, 1, 5, 5);
        setLayout(gridLayout2);
        panelBottom1.setLayout(gridLayout1);
        panelBottom2.setLayout(gridLayout1);
        panelTop.setLayout(gridLayout3);
        add(panelTop);
        add(panelBottom1);
        photographLabel.setVerticalTextPosition(JLabel.BOTTOM);
        photographLabel.setHorizontalTextPosition(JLabel.CENTER);
        photographLabel.setHorizontalAlignment(JLabel.CENTER);
        photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        buttonPanel.setLayout(gridLayout4);
        panelTop.add(buttonPanel);
        panelTop.add(photographLabel);


        // These are the buttons that user can use to interact with the GUI
        JButton previousPage = new JButton("Previous Page");
        JButton nextPage = new JButton("Next Page");
        JButton intensity = new JButton("Intensity");
        JButton colorCode = new JButton("Color Code");
        JButton reset = new JButton("Reset System");
        JButton random = new JButton("Random Image");

        buttonPanel.add(previousPage);
        buttonPanel.add(nextPage);
        buttonPanel.add(intensity);
        buttonPanel.add(colorCode);
        buttonPanel.add(reset);
        buttonPanel.add(random);

        nextPage.addActionListener(new nextPageHandler());
        previousPage.addActionListener(new previousPageHandler());
        intensity.addActionListener(new intensityHandler());
        colorCode.addActionListener(new colorCodeHandler());
        reset.addActionListener(new resetHandler());
        random.addActionListener(new randomHandler());
        setSize(1100, 750);
        // this centers the frame on the screen
        setLocationRelativeTo(null);

        // get the images' height and width by reading the image_size.txt
        try
        {
            Scanner read = new Scanner(new File("image_size.txt"));
            String[] str = (read.nextLine()).split(",");
            for (int i = 0; i < str.length; i++)
            {
                imageSize[i+1] = Double.parseDouble(str[i]);
            }
        }
        catch(FileNotFoundException EE)
        {
            System.out.println("The file intensity.txt does not exist");
        }

        // This loop goes through the image database and and stores them as icons and add the images
        // to JButtons and then to the JButton array
        button = new JButton[101];
        for (int i = 1; i < 101; i++) 
        {
            ImageIcon icon;
            String fileName = "images/" + i + ".jpg";
            ImageIcon origIcon = new ImageIcon(getClass().getResource(fileName));
            icon = origIcon;

            // resizing icon
            Image newImg = (icon.getImage()).getScaledInstance(180, 130, Image.SCALE_SMOOTH);
            icon = new ImageIcon(newImg);


            if(icon != null)
            {
                button[i] = new JButton(icon);
                button[i].addActionListener(new IconButtonHandler(i, origIcon));
                buttonOrder[i] = i;
            }
        }

        readIntensityFile();
        readColorCodeFile();
        displayFirstPage();
    }


    /**
     * This method opens the intensity text file containing the intensity matrix with the histogram bin values for
     * each image.
     * The contents of the matrix are processed and stored in a two dimensional array called intensityMatrix.
     *
     */
    public void readIntensityFile()
    {
      StringTokenizer token;
      Scanner read;
      double intensityBin;
      int lineNumber = 1;

         try
         {
           read =new Scanner(new File ("intensity.txt"));
          
           while(read.hasNextLine())
           {
               // tokenize the line by comma
               token = new StringTokenizer(read.nextLine(), ",");
               int binNumber = 0;
               while(token.hasMoreTokens())
               {
                   // stores histogram bin values in matrix
                   intensityBin = Double.parseDouble(token.nextToken());
                   intensityMatrix[lineNumber][binNumber++] = intensityBin;

               }
               lineNumber++;
           }
         }
         catch(FileNotFoundException EE){
           System.out.println("The file intensity.txt does not exist");
         }
    }


    /**
     * This method opens the color code text file containing the color code matrix with the histogram bin values for
     * each image.
     * The contents of the matrix are processed and stored in a two dimensional array called colorCodeMatrix.
     *
     */
    private void readColorCodeFile()
    {
      StringTokenizer token;
      Scanner read;
      Double colorCodeBin;
      int lineNumber = 1;
         try
         {
             read =new Scanner(new File ("colorCodes.txt"));
             while(read.hasNextLine())
             {
                 // tokenize the line by comma
                 token = new StringTokenizer(read.nextLine(), ",");
                 int binNumber = 0;
                 while(token.hasMoreTokens())
                 {
                     // stores histogram bin values in matrix
                     colorCodeBin = Double.parseDouble(token.nextToken());
                     colorCodeMatrix[lineNumber][binNumber++] = colorCodeBin;
                 }
                 lineNumber++;
             }
         }
         catch(FileNotFoundException EE)
         {
           System.out.println("The file intensity.txt does not exist");
         }
    }


    /**
     * This method displays the first twenty images in the panelBottom. The for loop starts at number one and gets
     * the image number stored in the buttonOrder array and assigns the value to imageButNo.
     * The button associated with the image is then added to panelBottom1. The name of each image displays below the
     * image icon.
     * The for loop continues this process until twenty images are displayed in the panelBottom1
     *
     */
    private void displayFirstPage()
    {
      int imageButNo = 0;
      panelBottom1.removeAll(); 
      for(int i = 1; i < 21; i++)
      {
          imageButNo = buttonOrder[i];
          Text = new JPanel(new BorderLayout());
          Text.add(button[imageButNo], BorderLayout.CENTER);
          panelBottom1.add(Text);
          picLabel = new JLabel();
          picLabel.setText("" + imageButNo + ".jpg");
          Text.add(picLabel, BorderLayout.SOUTH);
          imageCount ++;
      }
      panelBottom1.revalidate();  
      panelBottom1.repaint();

    }


    /**
     * This class implements an ActionListener for each iconButton. When an icon button is clicked, the image on the
     * the button is added to the photographLabel and the picNo is set to the image number selected and being displayed.
     *
     */
    private class IconButtonHandler implements ActionListener
    {
      int pNo = 0;
      ImageIcon iconUsed;
      
      IconButtonHandler(int i, ImageIcon j)
      {
        pNo = i;
        iconUsed = j;  //sets the icon to the one used in the button
      }
      
      public void actionPerformed( ActionEvent e)
      {
        photographLabel.setIcon(iconUsed);
        picNo = pNo;
        // changes title according to the image file selected
        setTitle("Content-based Image Retrieval System: " + picNo + ".jpg is selected");

        photographLabel.setText(picNo + ".jpg");
      }
      
    }
    
    /**
     * This class implements an ActionListener for the nextPageButton. The last image number to be displayed is set to the
     * current image count plus 20.  If the endImage number equals 101, then the next page button does not display any new 
     * images because there are only 100 images to be displayed. The first picture on the next page is the image located in
     * the buttonOrder array at the imageCount
     * The name of each image displays below the image icon.
     *
     */
    private class nextPageHandler implements ActionListener
    {

      public void actionPerformed( ActionEvent e)
      {
          int imageButNo = 0;
          int endImage = imageCount + 20;
          if(endImage <= 101)
          {
            panelBottom1.removeAll(); 
            for (int i = imageCount; i < endImage; i++) 
            {
                    imageButNo = buttonOrder[i];
                    Text = new JPanel(new BorderLayout());
                    Text.add(button[imageButNo], BorderLayout.CENTER);
                    panelBottom1.add(Text);
                    picLabel = new JLabel();
                    picLabel.setText("" + imageButNo + ".jpg");
                    Text.add(picLabel, BorderLayout.SOUTH);
                    imageCount++;
          
            }
  
            panelBottom1.revalidate();  
            panelBottom1.repaint();
          }
      }
      
    }
    
    /**
     * This class implements an ActionListener for the previousPageButton.  The last image number to be displayed is set to the
     * current image count minus 40.  If the endImage number is less than 1, then the previous page button does not display any new 
     * images because the starting image is 1.  The first picture on the next page is the image located in 
     * the buttonOrder array at the imageCount
     * The name of each image displays below the image icon.
     *
     */
    private class previousPageHandler implements ActionListener
    {

      public void actionPerformed( ActionEvent e)
      {
          int imageButNo = 0;
          int startImage = imageCount - 40;
          int endImage = imageCount - 20;
          if(startImage >= 1)
          {
            panelBottom1.removeAll();
            /*The for loop goes through the buttonOrder array starting with the startImage value
             * and retrieves the image at that place and then adds the button to the panelBottom1.
            */
            for (int i = startImage; i < endImage; i++) 
            {
                    imageButNo = buttonOrder[i];
                    Text = new JPanel(new BorderLayout());
                    Text.add(button[imageButNo], BorderLayout.CENTER);
                    panelBottom1.add(Text);
                    picLabel = new JLabel();
                    picLabel.setText("" + imageButNo + ".jpg");
                    Text.add(picLabel, BorderLayout.SOUTH);
                    imageCount--;
          
            }
  
            panelBottom1.revalidate();  
            panelBottom1.repaint();
          }
      }
      
    }

    /**
     * This class implements an ActionListener when the user selects the resetHandler button.
     * It reset the GUI to its initial status (same as when GUI first launches)
     *
     */
    private class resetHandler implements ActionListener
    {

        public void actionPerformed( ActionEvent e) {
            // reset map
            try
            {
                if (!map.isEmpty())
                {
                    map.clear();
                }
            }
            catch(NullPointerException exception){}

            // reset image display
            photographLabel.setIcon(null);
            photographLabel.setText(null);
            picNo = 0;
            setTitle("Content-based Image Retrieval System: Please Select an Image");

            // reset image display order to initial order
            for (int i = 0; i < 101; i++)
            {
                buttonOrder[i] = i;
            }
            imageCount = 1;
            displayFirstPage();
        }
    }

    /**
     * This class implements an ActionListener when the user selects the randomHandler button.
     * It randomly picks an image number and simulates a click action
     *
     */
    private class randomHandler implements ActionListener
    {
        public void actionPerformed( ActionEvent e)
        {
            // get an random int between 1 and 100
            int pNo = ThreadLocalRandom.current().nextInt(1, 101);
            // simulates a click action
            button[pNo].doClick();
        }

    }
    
    
    /**
     * This class implements an ActionListener when the user selects the intensityHandler button. The image number that the
     * user would like to find similar images for is stored in the variable pic. It calls manhattanDistance method to
     * calculate the distance between pic and the others
     */
    private class intensityHandler implements ActionListener
    {

      public void actionPerformed( ActionEvent e)
      {
          int pic = picNo;
          manhattanDistance(intensityMatrix, pic, 25);
      }
      
    }


    /**
     * This class implements an ActionListener when the user selects the colorCodeHandler button. The image number that the
     * user would like to find similar images for is stored in the variable pic. It calls manhattanDistance method to
     * calculate the distance between pic and the others
     */
    private class colorCodeHandler implements ActionListener
    {

      public void actionPerformed( ActionEvent e)
      {
          int pic = picNo;
          manhattanDistance(colorCodeMatrix, pic, 64);
      }
    }


    /**
     * This method calculates the distance between query image and the others by using Manhattan Distance.
     * The size of the image is retrieved from the imageSize array. The selected image's bin values are
     * compared to all the other image's bin values and a score is determined for how well the images compare.
     * The images are then arranged from most similar to the least.
     * This distance values are stored as keys in a HashMap where the value of the associated key is an image number
     *
     */
    private void manhattanDistance(Double[][] matrix, int pic, int bin)
    {
        map = new HashMap<Double, ArrayList<Integer>>();
        double [] distance = new double [101];

        // Goes through all the images and calculates the distance values
        for (int img = 1; img < 101; img++)
        {
            double d = 0;
            for (int j = 0; j < bin; j++)
            {
                d += Math.abs((matrix[pic][j]/imageSize[pic]) - (matrix[img][j]/imageSize[img]));
            }
            // update array of map keys
            distance[img] = d;

            // add key and value to HashMap
            if (map.containsKey(d))
            {
                map.get(d).add(img);
            }
            else
            {
                ArrayList<Integer> arrList = new ArrayList<>();
                arrList.add(img);
                map.put(d, arrList);
            }
        }

        // sorts keys
        Arrays.sort(distance);

        // update GUI display
        updateDisplay(distance, map);
    }

    /**
     * This method updates the buttonOrder array with images in an ascending order (similarity rank)
     * It also handles the situation where multiple images share the same distance score (key) with the query image
     *
     */
    private void updateDisplay(double[] distanceKeys, HashMap<Double, ArrayList<Integer>> map) {
        double key;
        panelBottom1.removeAll();

        for (int i = 1; i < 101; i++) {
            key = distanceKeys[i];
            ArrayList<Integer> arrList = map.get(key);
            for (int j = 0; j < arrList.size(); j++)
            {
                buttonOrder[i+j] = arrList.get(j);
            }
            i = i + arrList.size() - 1;
        }
        imageCount = 1;
        displayFirstPage();
    }
}
