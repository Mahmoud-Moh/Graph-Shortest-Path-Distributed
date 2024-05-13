package org.example.client;

import java.util.Random;



public class BatchGenerator {    
    Random random;
    NormalRandomVariable writePercentageVariable; 

    public BatchGenerator(double writePercentageMean, double writePercentageDeviation) {
        random = new Random(42);
        writePercentageVariable= new NormalRandomVariable(random, writePercentageMean, writePercentageDeviation);
    }
    public BatchGenerator(int seed, double writePercentageMean, double writePercentageDeviation) {
        random = new Random(seed);
        writePercentageVariable= new NormalRandomVariable(random, writePercentageMean, writePercentageDeviation);
    }

    public String generateBatch(int numOfOperations) {
        return generateBatch(numOfOperations, writePercentageVariable.nextValue());
    }
    public String generateBatch(int numOfOperations, double percentageOfWrite) {
        return generateBatch(numOfOperations, percentageOfWrite/2, percentageOfWrite/2);
    }

    public String generateBatch(int numOfOperations, double percentageOfAdd, double percentageOfDelete) {
        StringBuilder batchString = new StringBuilder();
        
        while (numOfOperations > 0) {
            char operationType;

            // determine the type of operation based on the percentage of writes
            int randomNumber = random.nextInt(100);
            if (randomNumber < percentageOfAdd) {
                operationType = 'A';
            } else if (randomNumber < percentageOfAdd + percentageOfDelete) {
                operationType = 'D';
            } else {
                operationType = 'Q';
            }
            
            // generate two random positive integer node IDs
            int nodeID1 = random.nextInt(100) + 1;
            int nodeID2 = random.nextInt(100) + 1;
            
            // append the operation to the batch
            batchString.append(operationType).append(" ").append(nodeID1).append(" ").append(nodeID2).append("\n");
            
            numOfOperations--;
        }

        batchString.append("F");

        return batchString.toString();
    }
    
    
}
