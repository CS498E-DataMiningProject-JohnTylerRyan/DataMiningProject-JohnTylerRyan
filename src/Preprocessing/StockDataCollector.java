package Preprocessing;

import java.util.ArrayList;

/**
 * Created by Johnny on 11/3/2016.
 */
public class StockDataCollector {
    private ArrayList<String[]> parsedTerroristData;
    private String outputDirectory;
    private String inputFileLoc;

    public StockDataCollector(String inputFileLoc, String outputDirectory) {
        this.inputFileLoc = inputFileLoc;
        this.outputDirectory = outputDirectory;

        parseTerroristData();
    }

    public void parseTerroristData() {

    }

    public String[] extractDates() {
        return null;
    }

    public void pullStockData() {

    }

    public static void main(String[] args) {

    }
}
