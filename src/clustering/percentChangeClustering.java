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


    public PercentChangeClustering(String dataFileLoc,int stockIDIndex, int openIndex, int closeIndex, int percentIndex )
    {
        ArrayList<StockDataObject> dataPoints;

        dataPoints = readData(dataFileLoc, stockIDIndex, openIndex, closeIndex, percentIndex);

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

            //add cluster j to taboo list so it is never search again
            tabooInsertIndex = Collections.binarySearch(clusterTabooList, foundJ);

            if (tabooInsertIndex < 0) {
                tabooInsertIndex += 1;
                tabooInsertIndex *= -1;

                clusterTabooList.add(tabooInsertIndex, foundJ);
            }

            //update the values of the ProxMatrix
            updateMaxtrix(foundI, foundJ);
        }
    }

    public int[] searchArray()
    {
        //holds the found zipcode,  lat, and long
        int returnArray[] = new int[2];

        double curMin = Integer.MAX_VALUE;
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

                        if(dist != 0 && dist < curMin)
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
        //cluster update the istance to this new merged cluster by selecting the minimum
        //distance from the selcted cluster to our two merged clusters.
        for(int i = 0; i < CLUSTER_LIST.length; i++)
        {
            double minDist = Math.min(proxMatrix[i][foundI], proxMatrix[i][foundJ]);

            proxMatrix[foundI][i] = minDist;
            proxMatrix[i][foundI] = minDist;
        }
    }


    public ArrayList<StockDataObject> readData(String dataFileLoc, int stockIDIndex, int openIndex, int closeIndex, int percentIndex)
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

        if(dataObjects.size() == 0)
        {
            System.err.println("No data read from file " + dataFileLoc + "\nExiting Program.");
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
}
