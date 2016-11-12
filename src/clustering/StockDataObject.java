package clustering;

/**
 * This class implements a simple representation of a stock line.
 * It will be used for the percent change calculation
 */
public class StockDataObject
{
    private String stockID;
    private double openPrice;
    private double closePrice;
    private double percentChange;

    public StockDataObject(String stockID, double openPrice, double closePrice, double percentChange)
    {
        this.stockID = stockID;
        this.openPrice = openPrice;
        this.closePrice = closePrice;
        this.percentChange = percentChange;
    }

    public String getStockID()
    {
        return stockID;
    }

    public double getOpenPrice()
    {
        return openPrice;
    }

    public double getClosePrice()
    {
        return closePrice;
    }

    public double getPercentChange()
    {
        return percentChange;
    }
}
