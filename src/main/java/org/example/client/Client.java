package org.example.client;


import org.example.server.GSPRemoteInterface;
import org.example.utils.GetPropValues;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Random;

public class Client{
    static String clientId;     // arg 0
    static int seed;            // arg 1
    
    static int numOfRequests;
    
    static int sleepDurationLowBound;
    static int sleepDurationHighBound;
    
    static int batchSizeMean;
    static int batchSizeStdDev;

    static int writePercentageMean;
    static int writePercentageStdDev;

    static String logDirectory;

    public static GSPRemoteInterface setupStub(){
        // Get Rmi Registery Server and port from system.properties
        try {
            return (GSPRemoteInterface) Naming.lookup(GetPropValues.getRemoteObjectReference());
        } catch (NotBoundException | IOException e) {
            e.printStackTrace();
            return null;
        } 
    }

    private static void loadParams() throws IOException{
        Properties params = GetPropValues.getClientParams();
        numOfRequests = Integer.parseInt(params.getProperty("numOfRequests"));
        sleepDurationLowBound = Integer.parseInt(params.getProperty("sleepDuration.low"));
        sleepDurationHighBound = Integer.parseInt(params.getProperty("sleepDuration.high"));
        
        writePercentageMean = Integer.parseInt(params.getProperty("writePercentage.mean"));
        writePercentageStdDev = Integer.parseInt(params.getProperty("writePercentage.stdDev"));

        batchSizeMean = Integer.parseInt(params.getProperty("batchSize.mean"));
        batchSizeStdDev = Integer.parseInt(params.getProperty("batchSize.stdDev"));
        
        logDirectory = params.getProperty("logDirectory");
    }

    public static void main(String[] args) {
        try {
            clientId = args[0];
            seed = Integer.parseInt(args[2]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Invalid arguments. Usage: java Main <clientId> <seed>");
            System.exit(2);
            return;
        }

        // clientId = "7";
        // seed = 42;


        try {
            loadParams();
        } catch (IOException e) {
            e.printStackTrace();
        }


        String logFilePath = Paths.get(logDirectory,  "log" + clientId + ".csv").toString();
        
        ensureDirectoryExists(logDirectory);

        deleteFileIfExists(logFilePath);

        Random random = new Random(seed*10L);

        // Initialize the stub
        GSPRemoteInterface stub = setupStub();
        if(stub==null){
            System.exit(3);
        }
        
        // Subscribe to the server
        try {
            boolean success = stub.subscribe("node"+clientId);
            if(!success){
                System.err.println("Failed to subscribe to the server");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            System.exit(4);
            return;
        }
        
        // Initialize the batch generator
        BatchGenerator batchGenerator = new BatchGenerator(seed, writePercentageMean, writePercentageStdDev);

        // Initialize the batch size random variable
        NormalRandomVariable batchSizeVariable = new NormalRandomVariable(random, batchSizeMean, batchSizeStdDev);

        // Initialize batch index
        int i = 0;

        while(i < numOfRequests){
            // determine the size for the next batch
            int batchSize = (int) Math.min(Math.max(batchSizeVariable.nextValue(), 10), 40);
            
            // generate the batch
            String batch = batchGenerator.generateBatch(batchSize);

            int sleepDuration = random.nextInt(sleepDurationHighBound - sleepDurationLowBound + 1) + sleepDurationLowBound;

            try {
                // Record timestamp before RMI call
                long startTimestamp = System.currentTimeMillis();
                
                // Perform RMI call
                String batchOutput = stub.processBatch("node"+clientId, batch);
                
                // Record timestamp after RMI call
                long endTimestamp = System.currentTimeMillis();

                // Calculate latency
                long latency = endTimestamp - startTimestamp;
 
                // Log the generated batch, returned batchOutput, timestamps and latency
                ClientLogger.log(logFilePath, i, startTimestamp, endTimestamp, latency, batch, batchOutput, batchGenerator.lastBatchWritePercentage,batchGenerator.lastBatchAddOpsCount, batchGenerator.lastBatchDeleteOpsCount,batchGenerator.lastBatchQueryOpsCount, batchSize, sleepDuration);

            } catch (RemoteException e) {
                e.printStackTrace();
                ClientLogger.log(logFilePath, i, -1, -1, -1, batch, e.getMessage(),batchGenerator.lastBatchWritePercentage,batchGenerator.lastBatchAddOpsCount, batchGenerator.lastBatchDeleteOpsCount,batchGenerator.lastBatchQueryOpsCount, batchSize, sleepDuration);
            }   

            // Sleep for some random time
            try {
                Thread.sleep(sleepDuration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            i++;
        }
        // Unsubscribe to the server
        try {
            stub.unSubscribe("node"+clientId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFileIfExists(String filePath) {
        Path path = Paths.get(filePath);
        try {
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println("File deleted: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Error deleting file: " + e.getMessage());
        }
    }

    public static void ensureDirectoryExists(String directoryPath) {
        Path path = Paths.get(directoryPath);
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                System.out.println("Directory created: " + directoryPath);
            } else {
                System.out.println("Directory already exists: " + directoryPath);
            }
        } catch (IOException e) {
            System.err.println("Error creating directory: " + e.getMessage());
        }
    }
    
}
