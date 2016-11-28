package clustering;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;

/**
 * Created by Johnny on 11/11/2016.
 */
public class PercentChangeClustering
{
    private double[][] proxMatrix;
    private final PercentageCluster[] CLUSTER_LIST;
    private ArrayList<Integer> clusterTabooList;

    private String dataFileLoc;
    private String outputFolder;


    public PercentChangeClustering(String dataFileLoc, String outputFolder, int stockIDIndex, int openIndex, int closeIndex, int percentIndex )
    {
        this.dataFileLoc = dataFileLoc;
        this.outputFolder = outputFolder;

        ArrayList<StockDataObject> dataPoints = readData(stockIDIndex, openIndex, closeIndex, percentIndex);

        CLUSTER_LIST = new PercentageCluster[dataPoints.size()];
        proxMatrix = new double[dataPoints.size()][dataPoints.size()];
        clusterTabooList = new ArrayList<Integer>();

        //populate the list of clusters one for each stock
        for(int i = 0; i < CLUSTER_LIST.length; i++)
        {
            ArrayList<StockDataObject> clustersPoints = new ArrayList<StockDataObject>(1);
            clustersPoints.add(dataPoints.get(i));

            CLUSTER_LIST[i] = new PercentageCluster(clustersPoints);
        }

        populateProxMatrix();

    }

    public void clusteringDriver(int maxClusters)
    {
        int loopCounter = 0;

        while (Math.abs(CLUSTER_LIST.length - clusterTabooList.size()) > maxClusters)
        {
            int tabooInsertIndex;
            int[] foundValues = searchArray();

            int foundI = foundValues[0];
            int foundJ = foundValues[1];

            //if there was no non zero distance end the loop and start working on the results
            //hopefully this will never be the case because we exit before the number of clusters is 0.
            if (foundI == -1 || foundJ == -1)
                break;

            PercentageCluster ci = CLUSTER_LIST[foundI];
            PercentageCluster cj = CLUSTER_LIST[foundJ];

            //update cluster i
            CLUSTER_LIST[foundI] = ci.merge(cj);
//            System.out.println(CLUSTER_LIST[foundI]);

            //add cluster j to taboo list so it is never searched again
            tabooInsertIndex = Collections.binarySearch(clusterTabooList, foundJ);

            if (tabooInsertIndex < 0) {
                tabooInsertIndex += 1;
                tabooInsertIndex *= -1;

                clusterTabooList.add(tabooInsertIndex, foundJ);
            }

            //update the values of the ProxMatrix
            updateMaxtrix(foundI, foundJ);
        }

        writeClusteringOutput();
    }

    public void writeClusteringOutput()
    {
        BufferedWriter dataWriter;
        File outFile;

        String fileName = "clustering_of_" + dataFileLoc.substring(dataFileLoc.lastIndexOf(File.separatorChar) + 1,dataFileLoc.lastIndexOf("."));

        System.out.println(outputFolder + File.separator + fileName.toLowerCase() + ".txt");
        try
        {
            outFile = new File(outputFolder + File.separator  + fileName.toLowerCase() + ".txt");
            dataWriter = new BufferedWriter(new FileWriter(outFile));

            if (outFile.exists())
                outFile.delete();

            outFile.createNewFile();

            dataWriter.write(toString());
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

    public int[] searchArray()
    {
        //holds the found zde,  lat,
        int returnArray[] = new int[2];

        double curMin = Double.MAX_VALUE;
        int foundI = -1;
        int foundJ = -1;

        //Check distance between each pair of clusters (since the matrix is symmetric,
        //we only need to search the upper triangle of the matrix). However, if a cluster
        //is in the taboo list, that cluster has already been merged and discarded so don't
        //search that row/column. (The discarded values are left in the matrix to reduce costly
        //2n cell updates or reallocating the reaming (n^2)-2n values into a new matrix)
        for(int i = 0; i < CLUSTER_LIST.length; i++)
        {
            if(Collections.binarySearch(clusterTabooList, i) < 0)
            {
                for(int j = i+1; j < CLUSTER_LIST.length; j++)
                {
                    if(Collections.binarySearch(clusterTabooList, j) < 0)
                    {
                        double dist = proxMatrix[i][j];

                        if(dist != Double.MIN_VALUE && dist < curMin)
                        {
                            curMin = dist;
                            foundI = i;
                            foundJ = j;
                        }
                    }
                }
            }
        }

        returnArray[0] = foundI;
        returnArray[1] = foundJ;

        return returnArray;
    }

    public void updateMaxtrix(int foundI, int foundJ)
    {
        //since we are using minimum distance based clustering. for each other non merged
        //cluster update the instance to this new merged cluster by selecting the minimum
        //distance from the selected cluster to our two merged clusters.
        for(int i = 0; i < CLUSTER_LIST.length; i++)
        {
            double minDist = Math.max(proxMatrix[i][foundI], proxMatrix[i][foundJ]);

            proxMatrix[foundI][i] = minDist;
            proxMatrix[i][foundI] = minDist;
        }
    }

    public ArrayList<StockDataObject> readData(int stockIDIndex, int openIndex, int closeIndex, int percentIndex)
    {
        ArrayList<StockDataObject> dataObjects = new ArrayList<StockDataObject>();
        BufferedReader br;

        String[] parsedString;

        try
        {
            br = new BufferedReader(new FileReader(new File(dataFileLoc)));

            String fileRead;

            while ((fileRead = br.readLine()) != null)
            {
                parsedString = fileRead.split(",");

                String stockID = parsedString[stockIDIndex];
                double open = Double.parseDouble(parsedString[openIndex]);
                double close = Double.parseDouble(parsedString[closeIndex]);
                double percent = Double.parseDouble(parsedString[percentIndex]);

                dataObjects.add(new StockDataObject(stockID, open, close, percent));
            }
            br.close();
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Data file not found. Entered file: " + dataFileLoc);
            e.printStackTrace();
            System.exit(-1);
        }
        catch (IOException e)
        {
            System.err.println("IO Exception. Entered file: " + dataFileLoc);
            e.printStackTrace();
            System.exit(-1);
        }

        return dataObjects;
    }

    public void populateProxMatrix()
    {
        //populate the proximity matrix
        for(int i = 0; i < CLUSTER_LIST.length; i++)
        {
            for(int j = i+1; j < CLUSTER_LIST.length; j++)
            {

                //proximity is the difference between the two points
                double percentOne = CLUSTER_LIST[i].getIncludedStocks().get(0).getPercentChange();
                double percentTwo = CLUSTER_LIST[j].getIncludedStocks().get(0).getPercentChange();

                double prox = Math.abs(percentOne - percentTwo);
                //add the distance to the matrix
                proxMatrix[i][j] = prox;
                proxMatrix[j][i] = prox;
            }
        }
    }

    public String toString()
    {
        String output = "Clustering output for data in file: " + dataFileLoc + "\n";
        ArrayList<PercentageCluster> outputClusters = new ArrayList<PercentageCluster>();

        int clusterCount = 0;

        for(int i = 0; i < CLUSTER_LIST.length; i++)
        {
            if(!clusterTabooList.contains(i))
            {
               outputClusters.add(CLUSTER_LIST[i]);
            }
        }

        Collections.sort(outputClusters);

        for(PercentageCluster out : outputClusters)
        {
            clusterCount++;
            output += "Cluster " + clusterCount + " {\n" + out.toString() + "\n} \n\n";
        }

        return output;
    }

    public static void main(String[] args)
    {
        if(args.length != 7)
        {
            System.err.println("Error: Passed arguments are not properly formatted. There needs to be four arguments: " +
                    "\nData folder path, output folder path, stock ID column number, open column number, close column number, percent change column number, number of clusters\n" +
                    "IN THAT ORDER!!");
            System.exit(-1);
        }

        PercentChangeClustering percentClustering;

        File dataFolder = new File(args[0]);
        File[] folderFiles = dataFolder.listFiles();

        for (File f : folderFiles)
        {
            String fileName;
            String fileType;
            int periodLastIndex;

            if (f.isFile())
            {
                fileName = f.getAbsolutePath();
                periodLastIndex = fileName.lastIndexOf(".");

                fileType = fileName.substring(periodLastIndex + 1);

                if (fileType.equals("csv"))
                {
                    percentClustering = new PercentChangeClustering(fileName, args[1],Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                    percentClustering.clusteringDriver(Integer.parseInt(args[6]));
                }
            }
        }
    }
}
