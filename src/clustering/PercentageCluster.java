package clustering;

import java.util.ArrayList;

/**
 * Created by Johnny on 11/11/2016.
 */
public class PercentageCluster
{
    private ArrayList<StockDataObject> includedStocks;

    public PercentageCluster()
    {
        includedStocks = new ArrayList<StockDataObject>();
    }

    public PercentageCluster(ArrayList<StockDataObject> includedStocks)
    {
        this.includedStocks = includedStocks;
    }

    /*
        Returns a new cluster object whos stock list is the merge
        of this stock list and passed in cluster's stock list
    */
    public PercentageCluster merge(PercentageCluster b)
    {
        int newArraySize = includedStocks.size() + b.getIncludedStocks().size();

        ArrayList<StockDataObject> mergedList = new ArrayList<StockDataObject>(newArraySize);

        for(StockDataObject zc : includedStocks)
        {
            mergedList.add(zc);
        }

        for(StockDataObject zc : b.getIncludedStocks())
        {
            mergedList.add(zc);
        }


        return new PercentageCluster(mergedList);
    }

    public double clusterAverageChange()
    {
        double sumPercentChange = 0;

        for(StockDataObject sdo : includedStocks)
        {
            sumPercentChange += sdo.getPercentChange();
        }

        return sumPercentChange / includedStocks.size();
    }


    public ArrayList<StockDataObject> getIncludedStocks()
    {
        return includedStocks;
    }

    public String toString()
    {
        String output = "";

        output += "Total stocks included: " + includedStocks.size() + "\n" +
                "Cluster's average percent change: "+ clusterAverageChange() + "\n" +
                "Stocks: " ;

        for(StockDataObject zc : includedStocks)
        {
            output += zc.toString() + "\n";
        }

        //remove the last \n
        output = output.substring(output.length() - 1);

        return output;
    }
}
