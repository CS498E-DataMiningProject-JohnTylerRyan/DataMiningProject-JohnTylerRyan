package Preprocessing;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Ryan on 11/3/2016.
 */
public class PercentageChangeCalc {
    public static void read(String dataFolderPath, int numDataFiles, int openIndex, int closeIndex) {
        File dataFolder = new File(dataFolderPath);

        File[] folderFiles = dataFolder.list();


        for (int i = 0; i < numDataFiles; i++) {
            ArrayList<String> fileLines = new ArrayList<String>();

            String outFilePath = "";

            String[] parsedString;
            String updatedFileLine;

            try {

                BufferedReader br = new BufferedReader(new FileReader("data.txt"));

                // read the first line from the text file
                String fileRead = br.readLine();

                while (fileRead != null) {
                    parsedString = fileRead.split(",");

                    int open = Integer.parseInt(parsedString[openIndex]);
                    int close = Integer.parseInt(parsedString[closeIndex]);

                    int change = (close - open) / close;

                    updatedFileLine = fileRead + "," + change + "\n";

                    fileLines.add(updatedFileLine);
                }

                writeToDataFile(outFilePath, fileLines);



                /*for (int i = 0; fileRead != null; i++) {
                    // use string.split to load a string array with the values from each line of
                    // the file, using a comma as the delimiter
                    String[] tokenize = fileRead.split(",");
                    id[i] = tokenize[0];
                    open[i] = Integer.parseInt(tokenize[1]);
                    close[i] = Integer.parseInt(tokenize[2]);

                    // read next line before looping
                    // if end of file reached
                    fileRead = br.readLine();
                } */

                br.close();
            } catch (FileNotFoundException fnfe) {
                System.out.println("file not found");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static void writeToDataFile(String outFilePath, ArrayList<String> fileLines) {

        String fileContents = "";
        BufferedWriter dataWriter;
        File outFile;


        //construct output string(file contents)
        for (String s : fileLines) {
            fileContents += s;
        }


        try {
            outFile = new File(outFilePath);
            dataWriter = new BufferedWriter(new FileWriter(outFile));

            if (outFile.exists())
                outFile.delete();

            outFile.createNewFile();

            dataWriter.write(fileContents);
            dataWriter.flush();
        } catch (IOException e) {
            System.err.println("Error writing stock data for stock ");
            e.printStackTrace();
            System.exit(-1);
        }
    }


    public static void change(int[] open, int[] close) {
        int[] change = new int[200];
        int num = 1000;
        for (int i = 0; i <= num; i++) {
            change[i] = (close[i] - open[i]) / close[i];
        }
    }

}
