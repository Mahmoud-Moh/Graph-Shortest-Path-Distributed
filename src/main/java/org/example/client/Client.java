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
        int maxRequests;
        String clientId = "";
        Random random = new Random();

        int seed;
        try{
            clientId = args[0];
            maxRequests = Integer.parseInt(args[1]);
        } catch(NumberFormatException e){
            e.printStackTrace();
            maxRequests = 10;
        }
        try{
            seed = Integer.parseInt(args[2]);
        } catch(NumberFormatException e){
            e.printStackTrace();
            seed = random.nextInt();
        }
        
        
        logFilePath = "log" + clientId + ".csv";
        // Initialize the stub
        GSPRemoteInterface stub = setupStub();
        if(stub==null){
            System.exit(0);
        }
        
        // Initialize the batch generator
        BatchGenerator batchGenerator = new BatchGenerator(seed, 30, 10);

        // Initialize the batch size random variable
        NormalRandomVariable batchSizeVariable = new NormalRandomVariable(random, 10, 5);

        // Initialize batch index
        int i = 0;
        while(i < maxRequests){
            // determine the size for the next batch
            int batchSize = 10 + (int) Math.min(Math.max(batchSizeVariable.nextValue(), 0), 30);
            
            // generate the batch
            String batch = batchGenerator.generateBatch(batchSize);

            try {
                // Record timestamp before RMI call
                long startTimestamp = System.currentTimeMillis();
                
                // Perform RMI call
                String batchOutput = stub.processBatch(batch);
                
                // Record timestamp after RMI call
                long endTimestamp = System.currentTimeMillis();

                // Calculate latency
                long latency = endTimestamp - startTimestamp;
 
                // Log the generated batch, returned batchOutput, timestamps and latency
                ClientLogger.log(logFilePath, i, startTimestamp, endTimestamp, latency, batch, batchOutput);

            } catch (RemoteException e) {
                e.printStackTrace();
                ClientLogger.log(logFilePath, i, -1, -1, -1, batch, e.getMessage());
            }   

            // Sleep for some random time
            try {
                int delay = random.nextInt(10000); // Random delay between 0 to 10000 ms
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            i++;
        }
    }
}
