package Preprocessing;

import java.io.*;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Johnny on 11/3/2016.
 */
public class StockDataCollector {
    private ArrayList<String> parsedTerroristDates;
    private ArrayList<String> stockIDs;
    private String outputDirectory;
    private String inputTerrorFileLoc;
    private String inputStockIDFile;

    public StockDataCollector(String inputStockIDFile, String inputTerrorFileLoc, String outputDirectory)
    {
        this.inputStockIDFile = inputStockIDFile;
        this.inputTerrorFileLoc = inputTerrorFileLoc;
        this.outputDirectory = outputDirectory;

        parsedTerroristDates = new ArrayList<String>();
        stockIDs = new ArrayList<String>();

        parseTerroristData();
        parseStockIDs();
    }

    public void parseStockIDs()
    {
        BufferedReader dataReader = null;

        try
        {
            dataReader = new BufferedReader(new FileReader(inputStockIDFile));
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Stock input file not found");
            System.exit(-1);
        }


        try
        {
            String line = "";

            while ((line = dataReader.readLine()) != null)
            {
                stockIDs.add(line);
            }

            dataReader.close();
        }
        catch (IOException e)
        {
            System.err.println("Error reading Stock IDs");
            System.exit(-1);
        }
    }
    public void parseTerroristData()
    {
        BufferedReader dataReader = null;
        ArrayList<String> tempDataStorage = new ArrayList<String>();

        try
        {
            dataReader = new BufferedReader(new FileReader(inputTerrorFileLoc));
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Terrorism date input file not found");
            e.printStackTrace();
            System.exit(-1);
        }

        try
        {
            String line = "";
            while ((line = dataReader.readLine()) != null)
            {
                tempDataStorage.add(line);
            }

            dataReader.close();
        }
        catch (IOException e)
        {
            System.err.println("Error reading Stock IDs");
            e.printStackTrace();
            System.exit(-1);
        }

        //remove the duplicates in the array list
        for(String date : tempDataStorage )
        {
            if(!parsedTerroristDates.contains(date))
            {
                parsedTerroristDates.add(date);
            }
        }
    }


    public void pullStockData()
    {
        //this is the wait time to prevent hitting the query limit of 200 per hour.
        //waiting 1.9 sec will give about 1800-1900 queries per hour based on how long the other statements take to execute.
        long waitTime = 1900;

        for(int i = 0; i < parsedTerroristDates.size(); i++)
        {
            System.out.println("Started reading stock data for " + parsedTerroristDates.get(i) + " at " + new Date().toString());

            ArrayList<String> stockData = new ArrayList<String>();

            String[] dateLine = parsedTerroristDates.get(i).split("/");


            String fileName = "StockData_" + dateLine[0] + "_" + dateLine[1] + "_" + dateLine[2];
            String fileContents = "";
            File outputFile = new File(outputDirectory  + File.separator + fileName + ".csv");
            BufferedWriter dataWriter;

            File outputErrorFile = new File(outputDirectory + File.separator + fileName + ".txt");
            boolean missingStock = false;
            String missingStocks = "";


            for(int j = 0; j < stockIDs.size(); j++)
            {
                String stockDataLine;

                //parts of the url
                String apiURL;
                String urlBase = "http://chart.finance.yahoo.com/table.csv?s=";
                String stockID = stockIDs.get(j);
                String dateArgs;
                String endOfUrl = "&g=d&ignore=.csv";

                dateArgs = "&a=" + dateLine[0] + "&b=" +  dateLine[1] + "&c=" +  dateLine[2] +
                             "&e=" + dateLine[0] + "&f=" +  dateLine[1] + "&g=" +  dateLine[2];

                apiURL = urlBase+stockID+dateArgs+endOfUrl;

                try {
                    URL oracle = new URL(apiURL);
                    BufferedReader apiResult = new BufferedReader(new InputStreamReader(oracle.openStream()));

                    //reads the header line and trashes them
                    apiResult.readLine();

                    //reads the actual data
                    stockDataLine = apiResult.readLine();

                    if (stockDataLine != null)
                    {
                        //format the data line to remove the date and add the stock id
                        stockDataLine = stockID + ","+ stockDataLine.substring(stockDataLine.indexOf(",")+1, stockDataLine.length());
                        stockData.add(stockDataLine);
                    }
                }
                catch (MalformedURLException e)
                {
                    System.err.println("Error pulling stock data for stock "+ stockID + " on date " + parsedTerroristDates.get(i));
                    e.printStackTrace();
                    System.exit(-1);
                }
                catch(FileNotFoundException e)
                {
                    System.err.println("Error pulling stock data for stock "+ stockID + " on date " + parsedTerroristDates.get(i) +
                                        " No data returned. Skipping stock");
                    e.printStackTrace();
                    missingStock = true;
                    missingStocks += stockID+"\n";

                }
                catch (IOException e)
                {
                    System.err.println("Error pulling stock data for stock "+ stockID + " on date " + parsedTerroristDates.get(i));
                    e.printStackTrace();
                    System.exit(-1);
                }

                try
                {
                    //we have to wait between requests to prevent hitting the query limit on the api
                    Thread.sleep(waitTime);
                }
                catch(InterruptedException  e)
                {
                    e.printStackTrace();
                    System.exit(-1);
                }
            }

            for(String s : stockData)
            {
                fileContents += s + "\n";
            }

            try
            {
                //if the output file already exists delete it
                if(outputFile.exists())
                    outputFile.delete();

                //create the file to write too.
                outputFile.createNewFile();

               dataWriter = new BufferedWriter(new FileWriter(outputFile));

               dataWriter.write(fileContents);
               dataWriter.flush();

                if(missingStock)
                {
                    //if the output file already exists delete it
                    if(outputErrorFile.exists())
                        outputErrorFile.delete();

                    //create the file to write too.
                    outputErrorFile.createNewFile();

                    dataWriter = new BufferedWriter(new FileWriter(outputErrorFile));

                    dataWriter.write("Data for the following stocks was not found\n" + missingStocks);
                    dataWriter.flush();
                }
            }
            catch(IOException e)
            {
                System.err.println("Error writing stock data for stock "+ fileName);
                e.printStackTrace();
                System.exit(-1);
            }

            System.out.println("Finished reading stock data for " + parsedTerroristDates.get(i) + " at " + new Date().toString());
        }
    }

    public static void main(String[] args)
    {
        int numArgs = args.length;

        if(numArgs != 3)
        {
            System.err.println("Error with command line parameters.\n" +
                    "Note the correct format us stockID file then terror data file then output location");
            System.exit(-1);
        }

        StockDataCollector collector = new StockDataCollector(args[0],args[1], args[2]);

        collector.pullStockData();
    }
}
