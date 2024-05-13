package org.example.client;


import org.example.server.GSPRemoteInterface;
import org.example.utils.GetPropValues;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Random;

public class Client{
    private static String logFilePath = "log.csv";

    public static GSPRemoteInterface setupStub(){
        //Get Rmi Registery Server and port from system.properties
        try {
            return (GSPRemoteInterface) Naming.lookup(GetPropValues.getRemoteObjectReference());
        } catch (NotBoundException | IOException e) {
            e.printStackTrace();
            return null;
        } 
    }
    public static void main(String[] args) {
        int numOfRequests = 10;
        String clientId = "";
        int seed;
        boolean fixedSleepMode = false;
        int sleepDuration;

        try {
            clientId = args[0];
            numOfRequests = Integer.parseInt(args[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.err.println("Invalid arguments. Usage: java Main <clientId> <numOfRequests> [<seed> <sleepDuration>]");
            numOfRequests = 10;
        }

        try{
            seed = Integer.parseInt(args[2]);
        } catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
            System.err.println("Invalid arguments. Usage: java Main <clientId> <numOfRequests> [<seed> <sleepDuration>]");
            seed = 42;
        }

        Random random = new Random(seed*10L);

        try{
            sleepDuration = Integer.parseInt(args[3]);
            fixedSleepMode = true;
        } catch(NumberFormatException e){
            System.err.println("Invalid arguments. Usage: java Main <clientId> <numOfRequests> [<seed> <sleepDuration>]");
            System.exit(2);
            return;
        } catch(ArrayIndexOutOfBoundsException e){
            fixedSleepMode = false;
            sleepDuration = random.nextInt(10000);
        }

        

        logFilePath = "log" + clientId + ".csv";

        // Initialize the stub
        GSPRemoteInterface stub = setupStub();
        if(stub==null){
            System.exit(3);
        }
        
        // Subscribe to the server
        try {
            stub.subscribe("node"+clientId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        
        // Initialize the batch generator
        BatchGenerator batchGenerator = new BatchGenerator(seed, 30, 10);

        // Initialize the batch size random variable
        NormalRandomVariable batchSizeVariable = new NormalRandomVariable(random, 10, 5);

        // Initialize batch index
        int i = 0;

        while(i < numOfRequests){
            // determine the size for the next batch
            int batchSize = 10 + (int) Math.min(Math.max(batchSizeVariable.nextValue(), 0), 30);
            
            // generate the batch
            String batch = batchGenerator.generateBatch(batchSize);

            if(!fixedSleepMode){
                sleepDuration = random.nextInt(10000);
            }

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
                ClientLogger.log(logFilePath, i, startTimestamp, endTimestamp, latency, batch, batchOutput, batchGenerator.mostRecentWritePercentage, batchSize, sleepDuration);

            } catch (RemoteException e) {
                e.printStackTrace();
                ClientLogger.log(logFilePath, i, -1, -1, -1, batch, e.getMessage(),batchGenerator.mostRecentWritePercentage, batchSize, sleepDuration);
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
}
