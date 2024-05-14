package org.example.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentHashMap;

import org.example.server.graph.Graph;
import org.example.server.metadata.ClientMetaData;

public class GSPRemoteObject extends UnicastRemoteObject implements GSPRemoteInterface {
    ConcurrentHashMap<String, ClientMetaData> clients;
    int maxClients = 0;
    Long startTimeStamp;

    Graph graph = Graph.getInstance(); ;
    
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
       
        System.out.println("Server received a batch from " + nodeId);
        System.out.println(batch);
        // If not subscribed, do not serve
        if((!clients.containsKey(nodeId)) || clients.get(nodeId).isUnSubscribed()){
            return "Call subscribe first";
        }

        Long processingStartTimeStamp = System.currentTimeMillis();
        
        System.out.println("Server started a batch");
		StringBuilder result = new StringBuilder();
		String[] batchLines=batch.split("\n");
		for(String line : batchLines){
			proccessBatchLine(nodeId, line, result);
		}
        System.out.println("Server finished a batch");
        System.out.println(result.toString());
	
        
        Long processingEndTimeStamp = System.currentTimeMillis();
        Long processingTime = processingEndTimeStamp - processingStartTimeStamp;

        // For reporting
        clients.get(nodeId).registerProcessingTime(processingTime);
        clients.get(nodeId).registerCompletedRequests(1);

        // Return the batch output
        return "GSPRemoteObject.processBatch called";
    }

    private void proccessBatchLine(String nodeId, String line, StringBuilder result) {
        String[] operation =line.split(" ");
        char queryType =operation[0].charAt(0);
        if (queryType == 'F') {
            return ;
        }
        int u = Integer.parseInt(operation[1]);
        int v = Integer.parseInt(operation[2]);

        if(queryType == 'A'){
            graph.addEdge(u, v);
            System.out.println("Edge added from " + u + " to " + v);
        }
        else if(queryType == 'D'){
            graph.removeEdge(u, v);
            System.out.println("Edge removed from " + u + " to " + v);
        }
        else{
            int out = graph.shortestPath(u, v, "BFS");
            System.out.println("Shortest path between " + u + " and " + v + " usign " + "BFS" + " is: " + out);
            result.append(out);
            result.append("\n");
        }
        return ;
    }
 

    @Override
    public void x(String nodeId) throws RemoteException {
        // TODO Auto-generated method stub
    
        throw new UnsupportedOperationException("Unimplemented method 'x'");
    }
}
