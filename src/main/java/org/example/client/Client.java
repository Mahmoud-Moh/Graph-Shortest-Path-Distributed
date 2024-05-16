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
import java.util.regex.Pattern;

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

    static boolean scriptedBatchesMode;
    static String scriptedBatchesDirectory;

    static GSPRemoteInterface stub;

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

        logDirectory = params.getProperty("logDirectory");

        scriptedBatchesMode = Boolean.parseBoolean(params.getProperty("testing.enabled"));

        if(scriptedBatchesMode){
            scriptedBatchesDirectory = params.getProperty("testing.batchesDirectory");
        }
        
        writePercentageMean = Integer.parseInt(params.getProperty("writePercentage.mean"));
        writePercentageStdDev = Integer.parseInt(params.getProperty("writePercentage.stdDev"));

        batchSizeMean = Integer.parseInt(params.getProperty("batchSize.mean"));
        batchSizeStdDev = Integer.parseInt(params.getProperty("batchSize.stdDev"));
    }

    public static void main(String[] args) {
        // try {
        //     clientId = args[0];
        //     seed = Integer.parseInt(args[2]);
        // } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
        //     System.err.println("Invalid arguments. Usage: java Main <clientId> <seed>");
        //     System.exit(2);
        //     return;
        // }

        clientId = "0";
        seed = 42;

        try {
            loadParams();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Random random = new Random(seed*10L);

        NormalRandomVariable batchSizeVariable = new NormalRandomVariable(random, batchSizeMean, batchSizeStdDev);

        String logFilePath = Paths.get(logDirectory,  "log" + clientId + ".csv").toString();
        
        ensureDirectoryExists(logDirectory);

        // deleteFileIfExists(logFilePath);

        // Initialize the stub
        stub = setupStub();
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

        // Send batches

        if(scriptedBatchesMode){
            sendScriptedBatches(random, batchSizeVariable, logFilePath);
        }
        else{
            sendGeneratedBatches(random, batchSizeVariable, logFilePath);
        }
        
        // Unsubscribe to the server
        try {
            stub.unSubscribe("node"+clientId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

   private static void sendGeneratedBatches(Random random, NormalRandomVariable batchSizeVariable, String logFilePath) {

        // Initialize the batch generator
        BatchGenerator batchGenerator = new BatchGenerator(seed, writePercentageMean, writePercentageStdDev);

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
                ClientLogger.log(logFilePath, i, startTimestamp, endTimestamp, latency,
                                batch, batchOutput,
                                batchGenerator.lastBatchWritePercentage,
                                batchGenerator.lastBatchAddOpsCount,
                                batchGenerator.lastBatchDeleteOpsCount,
                                batchGenerator.lastBatchQueryOpsCount,
                                batchSize,
                                sleepDuration
                                );

                i++;
                
                // Sleep for some time
                Thread.sleep(Math.max(sleepDuration-latency, 0));

            } catch (RemoteException | InterruptedException e) {
                e.printStackTrace();
                ClientLogger.log(logFilePath, i, -1, -1, -1, batch, e.getMessage(),batchGenerator.lastBatchWritePercentage,batchGenerator.lastBatchAddOpsCount, batchGenerator.lastBatchDeleteOpsCount,batchGenerator.lastBatchQueryOpsCount, batchSize, sleepDuration);
            }   
        }
    }

    private static String readBatch(String directory, int i) {
        String fileName = i + ".txt";
        Path filePath = Paths.get(directory, fileName);
        try {
            if (Files.exists(filePath)) {
                byte[] fileBytes = Files.readAllBytes(filePath);
                return new String(fileBytes);
            } else {
                System.err.println("File not found: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        return "";
    }

    private static final Pattern FILE_PATTERN = Pattern.compile("\\d+\\.txt");

    private static int countBatches(String directory) {
        Path dirPath = Paths.get(directory);
        try {
            long count = Files.list(dirPath)
                            .filter(path -> FILE_PATTERN.matcher(path.getFileName().toString()).matches())
                            .count();
            return (int) count;
        } catch (IOException e) {
            System.err.println("Error counting batches: " + e.getMessage());
        }
        return 0;
    }

    private static void sendScriptedBatches(Random random, NormalRandomVariable batchSizeVariable, String logFilePath) {
        // Initialize batch index
        int i = 0;
        
        numOfRequests = countBatches(scriptedBatchesDirectory);

        while(i < numOfRequests){
            // determine the size for the next batch
            int batchSize = (int) Math.min(Math.max(batchSizeVariable.nextValue(), 10), 40);
            
            // generate the batch
            String batch = readBatch(scriptedBatchesDirectory, i+1);

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
                ClientLogger.log(logFilePath, i, startTimestamp, endTimestamp, latency,
                                batch, batchOutput,
                                -1,
                                -1,
                                -1,
                                -1,
                                batchSize,
                                sleepDuration
                                );

                i++;
                
                // Sleep for some time
                Thread.sleep(Math.max(sleepDuration-latency, 0));

            } catch (RemoteException | InterruptedException e) {
                e.printStackTrace();
                ClientLogger.log(logFilePath, i, -1, -1, -1, batch, e.getMessage(),-1,-1,-1,-1, batchSize, sleepDuration);
            }   
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
