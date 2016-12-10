readDistData <- function(dataLocation)
{
  return(read.csv(dataLocation, header=FALSE, stringsAsFactors = FALSE))
}

generateclossnessMatrix <- function(data, closenessMatrix)
{
  #closenessMatrix = upper.tri(matrix(rep(0,10 ^ 2 ), nrow=10, ncol=10), diag = TRUE)
  closenessMatrix =matrix(0.0, nrow=nrow(data), ncol=nrow(data))
  
  for(i in 1:(nrow(data)-1))
  {
    dataRowI = data[i, 2:ncol(data)]

    for(j in (i+1):nrow(data))
    {
      dataRowJ = data[j, 2:ncol(data)]
      
      diffRow = abs(dataRowI - dataRowJ)

      closenessMatrix[i, j] = mean(unlist(diffRow))
      #closenessMatrix[i, j] = (sum(unlist(diffRow))/nrow(diffRow))
    }
  }
  
  return(closenessMatrix)
}

findCloseStocks <- function(data, closenessMatrix, closenessCutoff)
{
  closeStocks = data.frame(stockID1 = "",
                           stockID2 = "", 
                           index1 = 0, 
                           index2 = 0, stringsAsFactors = FALSE)
  nextDataFrameIndex = 1;
  
  for(i in 1:(nrow(data)-1))
  {
    for(j in (i+1):nrow(data))
    {
       if(closenessMatrix[i, j] <= closenessCutoff)
       {
         closeStocks[nextDataFrameIndex, 1:ncol(closeStocks) ] = c(data[i, 1], data[j, 1], i, j)
         nextDataFrameIndex = nextDataFrameIndex+1
       }
    }
  }
  
  return(closeStocks)
}

generateDistributionsHistograms <- function(data)
{
  for(i in 1:nrow(data))
  {
    png(file = paste("./distributionBarplots/", data[i, 1] ,".png", sep = ""))
    barplot(unlist(data[i, 2:ncol(data)]),
            main = paste(data[i, 1], "Cluster Distribution", sep=" "),
            xlab = "Cluster (least change to greatest change)",
            ylab = "cluster occurance percentage")
    dev.off()
  }
}

generateCloseHistorgrams <- function(data, stockList, closenessMatrix, closenessCutoff)
{
  if(!dir.exists(paste("./closenessBarplots/diffMax-",closenessCutoff, "/", sep = "")))
  {
    dir.create(paste("./closenessBarplots/diffMax-",closenessCutoff, "/", sep = ""))
  }
  
  for(i in 1:(nrow(stockList)))
  {
    stockID1 = stockList[i,1]
    stockID2 = stockList[i,2]
    indexi = as.numeric(stockList[i, 3])
    indexj = as.numeric(stockList[i, 4])
    
    plotData = c(unlist(data[indexi, 2:ncol(data)]), unlist(data[indexj, 2:ncol(data)]))
    title = paste(stockID1, "and", stockID2, "Cluster Distribution", "\nDistribution difference:", signif(closenessMatrix[indexi, indexj], 5),sep=" ") 
    legend = c(stockID1, stockID2)
    
   png(file = paste("./closenessBarplots/diffMax-",closenessCutoff, "/", data[indexi, 1], "-", data[indexj, 1] ,".png", sep = ""))
   barplot(plotData,
           ylim = c(0,.5),
           main = title,
           xlab = "Cluster (least change to greatest change)",
           ylab = "cluster occurance percentage",
           legend.text = legend,
           col = c("royalblue", "firebrick"))
   dev.off()
  }
}

main <- function(closenessCutoff, dataLocation, genAllStockHist=FALSE)
{
  data <- readDistData(dataLocation)
  
  if(genAllStockHist)
  {
    generateDistributionsHistograms(data)
  }

  closenessMatrix = generateclossnessMatrix(data)

  closeStocks = findCloseStocks(data, closenessMatrix, closenessCutoff)
  
  generateCloseHistorgrams(data, closeStocks, closenessMatrix, closenessCutoff)
}