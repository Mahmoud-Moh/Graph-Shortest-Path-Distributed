package org.example.client;

import java.util.Random;



public class BatchGenerator {    
    Random random;
    NormalRandomVariable writePercentageVariable;

    static final int MAX_VERTICES = 200;
    
    double lastBatchWritePercentage = -1;
    
    int lastBatchAddOpsCount;
    int lastBatchDeleteOpsCount;
    int lastBatchQueryOpsCount;

    public void adjustWritePercentageVariable(double newMean, double newDeviation){
        writePercentageVariable.setMean(newMean);
        writePercentageVariable.setStdDev(newDeviation);
    }

    public BatchGenerator(double writePercentageMean, double writePercentageDeviation) {
        random = new Random(42);
        writePercentageVariable= new NormalRandomVariable(random, writePercentageMean, writePercentageDeviation);
    }
    public BatchGenerator(int seed, double writePercentageMean, double writePercentageDeviation) {
        random = new Random(seed);
        writePercentageVariable= new NormalRandomVariable(random, writePercentageMean, writePercentageDeviation);
    }

    public String generateBatch(int numOfOperations) {
        return generateBatch(numOfOperations, 65);
    }
    
    public String generateBatch(int numOfOperations, double addOfWritePercentage) {
        double writePercentage = Math.min(Math.max(writePercentageVariable.nextValue(), 0), 100);
        lastBatchWritePercentage = writePercentage;
        double addPercentage = (addOfWritePercentage / 100) * writePercentage;
        return generateBatch(numOfOperations, addPercentage, writePercentage-addPercentage);
    }

    public String generateBatch(int numOfOperations, double percentageOfAdd, double percentageOfDelete) {
        StringBuilder batchString = new StringBuilder();
        int addOps = 0;
        int delOps = 0;
        int queryOps = 0;
        while (numOfOperations > 0) {
            char operationType;
            // determine the type of operation based on the percentage of writes
            int randomNumber = random.nextInt(100);
            if (randomNumber < percentageOfAdd) {
                operationType = 'A';
                addOps++;
            } else if (randomNumber < percentageOfAdd + percentageOfDelete) {
                operationType = 'D';
                delOps++;
            } else {
                operationType = 'Q';
                queryOps++;
            }
            
            // generate two random positive integer node IDs
            int nodeID1 = random.nextInt(MAX_VERTICES) + 1;
            int nodeID2 = random.nextInt(MAX_VERTICES) + 1;
            
            // append the operation to the batch
            batchString.append(operationType).append(" ").append(nodeID1).append(" ").append(nodeID2).append("\n");
            
            numOfOperations--;
        }

        lastBatchAddOpsCount = addOps;
        lastBatchDeleteOpsCount = delOps;
        lastBatchQueryOpsCount = queryOps;

        batchString.append("F");

        return batchString.toString();
    }
    
    
}
