package Preprocessing;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ryan on 11/3/2016.
 */
public class PercentageChangeCalc {
    public static void read() {
        String[] id = new String[200];
        int[] open = new int[200];
        int[] close = new int[200];

        try {

            BufferedReader br = new BufferedReader(new FileReader("data.txt"));

            // read the first line from the text file
            String fileRead = br.readLine();

            for (int i = 0; fileRead != null; i++) {
                // use string.split to load a string array with the values from each line of
                // the file, using a comma as the delimiter
                String[] tokenize = fileRead.split(",");
                id[i] = tokenize[0];
                open[i] = Integer.parseInt(tokenize[1]);
                close[i] = Integer.parseInt(tokenize[2]);

                // read next line before looping
                // if end of file reached
                fileRead = br.readLine();
            }

            br.close();
        } catch (FileNotFoundException fnfe) {
            System.out.println("file not found");
        } catch (IOException ioe) {
            ioe.printStackTrace();
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
