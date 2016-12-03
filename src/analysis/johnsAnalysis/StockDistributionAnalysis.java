package analysis.johnsAnalysis;

import clustering.StockDataObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by Johnny on 11/28/2016.
 */
public class StockDistributionAnalysis
{
    private Hashtable<String, double[]> stockDistributions = new Hashtable<String, double[]>();

    private String dataLocation;
    private String outputLocation;


    public StockDistributionAnalysis(String dataLocation, String outputLocation)
    {
        this.dataLocation = dataLocation;
        this.outputLocation = outputLocation;

        stockDistributions = new Hashtable<String, double[]>();
    }

    public void calculateDistributions()
    {
        File dataFolder = new File(dataLocation);
        File[] folderFiles = dataFolder.listFiles();

        for (File f : folderFiles)
        {
            String fileName;
            String fileType;
            int periodLastIndex;

            if (f.isFile())
            {
                fileName = f.getAbsolutePath();

                readFileAndUpdateStocks(fileName);
            }
        }


        normalizeDistributions();
        writeDistributions();

        /*//True toString for the hash table
        for (String key : stockDistributions.keySet())
        {
            System.out.print(key + ": [");

            for(double i : stockDistributions.get(key))
            {
                System.out.print(i + " ");
            }

            System.out.println("]");
        }
        */

    }

    public void readFileAndUpdateStocks(String dataFile)
    {
        BufferedReader br;

        try
        {
            br = new BufferedReader(new FileReader(new File(dataFile)));

            String fileRead ;
            String[] parsedString;

            int currentCluster = -1;
            boolean inStockList = false;

            fileRead = br.readLine(); //discard first like with file path info

            while ((fileRead = br.readLine()) != null)
            {
                if(fileRead.contains("Cluster "))
                {
                    parsedString = fileRead.split(" ");
                    currentCluster = Integer.parseInt(parsedString[1]);
                }

                //if this line contains the closing line for the stock list
                // update the flag so it doesn't try to read any more stocks
                if(fileRead.contains("}"))
                    inStockList = false;

                if(inStockList)
                {
                    parsedString = fileRead.split(" ");
                    double[] stockDist;

                    if(!stockDistributions.containsKey(parsedString[1]))
                    {
                        stockDistributions.put(parsedString[1], new double[10]);
                    }

                    //pull the stocks current count
                    stockDist = stockDistributions.get(parsedString[1]);

                    //update the count for whatever cluster it is in
                    stockDist[currentCluster-1] = stockDist[currentCluster-1] + 1;

                    //update the HashTable entry
                    stockDistributions.put(parsedString[1], stockDist);
                }

                //if this line contains the stocks opening line for the stock list
                //set the flag so that the program will start reading stocks next iteration
                if(fileRead.contains("Stocks: "))
                    inStockList = true;
            }
            br.close();
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Data file not found. Entered file: " + dataFile);
            e.printStackTrace();
            System.exit(-1);
        }
        catch (IOException e)
        {
            System.err.println("IO Exception. Entered file: " + dataFile);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void normalizeDistributions()
    {
        for (String key : stockDistributions.keySet())
        {
            double[] stockDist = stockDistributions.get(key);

            double totalOccurances = 0;

            for(double d : stockDist)
            {
                totalOccurances += d;
            }

            for(int i = 0; i < stockDist.length; i++)
            {
                stockDist[i] = stockDist[i]/totalOccurances;
            }
        }
    }

    public void writeDistributions()
    {
        BufferedWriter dataWriter;
        File outFile;
        String outputFileText = "";

        for (String key : stockDistributions.keySet())
        {
            outputFileText += key;

            for(double i : stockDistributions.get(key))
            {
                outputFileText += "," + i;
            }

            outputFileText += "\n";
        }

        try
        {
            outFile = new File(outputLocation + File.separatorChar + "stock_distributions.csv");
            dataWriter = new BufferedWriter(new FileWriter(outFile));

            if (outFile.exists())
                outFile.delete();

            outFile.createNewFile();

            dataWriter.write(outputFileText);
            dataWriter.flush();
            dataWriter.close();
        }
        catch (IOException e)
        {
            System.err.println("Error writing clustering output");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String[] args)
    {
        if(args.length != 2)
        {
            System.err.println("Incorrect number of Arguments. Expected 2 arguments the clusters folder and an output location");
            System.exit(-1);
        }

        StockDistributionAnalysis stockDists = new StockDistributionAnalysis(args[0], args[1]);
        stockDists.calculateDistributions();
    }
}
