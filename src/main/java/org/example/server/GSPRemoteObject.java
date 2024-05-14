package org.example.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

import org.example.server.metadata.ClientMetaData;

public class GSPRemoteObject extends UnicastRemoteObject implements GSPRemoteInterface {
    ConcurrentHashMap<String, ClientMetaData> clients;
    int maxClients = 0;
    Long startTimeStamp;
    
    // cache query outputs? (variant idea)

    public void report(String outputDirectory){
        //TODO: report total number of clients, maxClients, total number of completed requests, mean request processing time, etc. 
        //TODO: report for each client its own row in subscribedNodes in a table.
        // Feel free to output multiple files in the `outputDirectory`, use any format you see nice.
        return;
    }
    
    protected GSPRemoteObject() throws RemoteException {
        super();
        clients = new ConcurrentHashMap<>();
        startTimeStamp = System.currentTimeMillis();
    }

    @Override
    public Boolean subscribe(String clientId) throws RemoteException {
        if(clients.containsKey(clientId)){
            return false;
        }
        clients.put(clientId, new ClientMetaData(clientId));
        maxClients = Math.max(clients.size(), maxClients);
        System.out.println(clientId + " subscribed");
        return true;

    }

    @Override
    public void unSubscribe(String nodeId) throws RemoteException {
        if(clients.containsKey(nodeId)){
            clients.get(nodeId).unSubscribe();
        }
    }

    @Override
    public synchronized String processBatch(String nodeId, String batch) throws RemoteException {
        // If not subscribed, do not serve
        if((!clients.containsKey(nodeId)) || clients.get(nodeId).isUnSubscribed()){
            return "Call subscribe first";
        }

        Long processingStartTimeStamp = System.currentTimeMillis();
        
        // TODO: Process the Batch of operations and return the query outputs (a String of multiple lines).
        // ..
        
        Long processingEndTimeStamp = System.currentTimeMillis();
        Long processingTime = processingEndTimeStamp - processingStartTimeStamp;

        // For reporting
        clients.get(nodeId).registerProcessingTime(processingTime);
        clients.get(nodeId).registerCompletedRequests(1);

        // Return the batch output
        return "GSPRemoteObject.processBatch called";
    }
}
