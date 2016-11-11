package Preprocessing;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by Ryan on 11/3/2016.
 */
public class PercentageChangeCalc {
    public static void read(String dataFolderPath, int openIndex, int closeIndex) {
        File dataFolder = new File(dataFolderPath);

        File[] folderFiles = dataFolder.listFiles();

        ArrayList<String> dataFiles = new ArrayList<String>();

        //parse which files are data files
        for (File f : folderFiles)
        {
            String fileName;
            String fileType;
            int periodLastIndex;

            if (f.isFile()) {
                fileName = f.getAbsolutePath();
                periodLastIndex = fileName.lastIndexOf(".");

                fileType = fileName.substring(periodLastIndex + 1);

                if (fileType.equals("csv")) {
                    dataFiles.add(fileName);
                }
            }
        }

        for (int i = 0; i < dataFiles.size(); i++)
        {
            ArrayList<String> fileLines = new ArrayList<String>();

            String dataFilePath = dataFiles.get(i);
            File dataFile = new File(dataFilePath);

            String[] parsedString;
            String updatedFileLine;



            try
            {

                BufferedReader br = new BufferedReader(new FileReader(dataFile));

                // read the first line from the text file
                String fileRead = br.readLine();

                while (fileRead != null)
                {
                    parsedString = fileRead.split(",");

                    double open = Double.parseDouble(parsedString[openIndex]);
                    double close = Double.parseDouble(parsedString[closeIndex]);

                    double change = (close - open) / close;

                    updatedFileLine = fileRead + "," + change + "\n";

                    fileLines.add(updatedFileLine);
                }

                writeToDataFile(dataFilePath, fileLines);

                br.close();
            }
            catch (FileNotFoundException e)
            {
                System.out.println("file not found");
            }
            catch (IOException ioe)
            {
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

    public static void main(String[] args)
    {
        if(args.length != 3)
        {
            System.err.println("Error: Passed arguments are in proper formatted. There needs to be three arguments: " +
                    "\nData folder path, open column number, close column number \n" +
                    "IN THAT ORDER!!");
            System.exit(-1);
        }

        read(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }

}
