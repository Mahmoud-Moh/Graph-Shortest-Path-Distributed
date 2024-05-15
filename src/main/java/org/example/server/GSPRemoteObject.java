package org.example.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

import org.example.server.graph.solvers.ShortestPathSolver;
import org.example.server.metadata.ClientMetaData;

public class GSPRemoteObject extends UnicastRemoteObject implements GSPRemoteInterface {
    ConcurrentHashMap<String, ClientMetaData> clients;
    int maxClients = 0;
    Long startTimeStamp;
    ShortestPathSolver shortestPathSolver;
    // cache query outputs? (variant idea)

    public void report(String outputDirectory){
        //TODO: report total number of  clients, maxClients, total number of completed requests, mean request processing time, etc.
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
        //Processing tha batch and returning query outputs
        StringBuilder response = new StringBuilder();
        String[] operations = batch.split("\n");
        //System.out.println("Mock processing the batch for client with Id " + nodeId);
        for (int i=0; i < operations.length - 1; i++) {
            String operation = operations[i];
            String[] parameters = operation.split(" ");
            String operator = parameters[0];
            String fromNode = parameters[1];
            String toNode = parameters[2];
            System.out.println(operation + "p");
            switch (operator) {
                case "A":
                    shortestPathSolver.add(Integer.parseInt(fromNode), Integer.parseInt(toNode));
                    break;
                case "D":
                    shortestPathSolver.delete(Integer.parseInt(fromNode), Integer.parseInt(toNode));
                    break;
                case "Q":
                    int distance = shortestPathSolver.query(Integer.parseInt(fromNode), Integer.parseInt(toNode));
                    response.append(distance).append("\n");
                    break;
                default:
                    break;
            }
        }

        Long processingEndTimeStamp = System.currentTimeMillis();
        Long processingTime = processingEndTimeStamp - processingStartTimeStamp;
        System.out.println("Processing Time: " + processingTime);
        // For reporting
        clients.get(nodeId).registerProcessingTime(processingTime);
        clients.get(nodeId).registerCompletedRequests(1);

        // Return the batch output
        System.out.println("We reached this part");
        return "This part should be for processing the batch";
    }

    public void setShortestPathSolver(ShortestPathSolver shortestPathSolver){
        this.shortestPathSolver = shortestPathSolver;
    }
}
