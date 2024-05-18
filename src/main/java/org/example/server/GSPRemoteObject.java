package org.example.server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.example.server.graph.solvers.ShortestPathSolver;
import org.example.server.graph.Graph;
import org.example.server.metadata.ClientMetaData;
import org.example.utils.GetPropValues;

public class GSPRemoteObject extends UnicastRemoteObject implements GSPRemoteInterface {
    ConcurrentHashMap<String, ClientMetaData> clients;
    int maxClients = 0;
    Long startTimeStamp;

    final int useIncrementalSolver;

    Graph graph = Graph.getInstance();
    ShortestPathSolver shortestPathSolver;

    public void report(String outputDirectory) {
        try {
            generateOverallReport(outputDirectory);
            generateClientDetailsReport(outputDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateOverallReport(String outputDirectory) throws IOException {
        int totalClients = clients.size();
        int totalRequests = clients.values().stream().mapToInt(ClientMetaData::getTotalRequests).sum();
        long totalProcessingTime = clients.values().stream().mapToLong(ClientMetaData::getTotalProcessingTime).sum();
        double meanProcessingTime = totalRequests > 0 ? (double) totalProcessingTime / totalRequests : 0;

        Path overallReportPath = Paths.get(outputDirectory, "server_report_overall.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(overallReportPath)) {
            writer.write("Total Clients: " + totalClients + "\n");
            writer.write("Max Clients: " + maxClients + "\n");
            writer.write("Total Requests: " + totalRequests + "\n");
            writer.write("Mean Request Processing Time: " + meanProcessingTime + "\n");
        }
    }

    private void generateClientDetailsReport(String outputDirectory) throws IOException {
        Path clientDetailsReportPath = Paths.get(outputDirectory, "server_report_client_details.txt");
        try (BufferedWriter writer = Files.newBufferedWriter(clientDetailsReportPath)) {
            writer.write("ClientID, Unsubscribed, Subscription TimeStamp, Unsubscription TimeStamp, Total Requests, Total Processing Time\n");
            for (Map.Entry<String, ClientMetaData> entry : clients.entrySet()) {
                ClientMetaData client = entry.getValue();
                writer.write(client.getClientId() + ", " +
                        client.isUnSubscribed() + ", " +
                        client.getSubscriptionTimeStamp() + ", " +
                        client.getUnSubscriptionTimeStamp() + ", " +
                        client.getTotalRequests() + ", " +
                        client.getTotalProcessingTime() + "\n");
            }
        }
    }

    protected GSPRemoteObject() throws IOException {
        super();
        clients = new ConcurrentHashMap<>();
        startTimeStamp = System.currentTimeMillis();

        Properties params = GetPropValues.getPropValues();
        useIncrementalSolver = Integer.parseInt(params.getProperty("GSP.useIncrementalSolver").trim());
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
        switch (useIncrementalSolver) {
            case 1:
                return processBatchIncremental(nodeId, batch);

            case 0:
                return processBatchBFS(nodeId, batch);

            default:
                throw new UnsupportedOperationException();
        }
    }

    public synchronized String processBatchIncremental(String nodeId, String batch){
        System.out.println("Server received a batch from " + nodeId);
        System.out.println(batch);
        // If not subscribed, do not serve
        if((!clients.containsKey(nodeId)) || clients.get(nodeId).isUnSubscribed()){
            return "Call subscribe first";
        }

        Long processingStartTimeStamp = System.currentTimeMillis();

        System.out.println("Server started a batch");
        StringBuilder result = new StringBuilder();
        String[] operations = batch.split("\n");
        for (int i = 0; i < operations.length - 1; i++) {
            String operation = operations[i];
            String[] parameters = operation.split(" ");
            String operator = parameters[0];
            String fromNode = parameters[1];
            String toNode = parameters[2];
            switch (operator) {
                case "A":
                    shortestPathSolver.add(Integer.parseInt(fromNode), Integer.parseInt(toNode));
                    break;
                case "D":
                    shortestPathSolver.delete(Integer.parseInt(fromNode), Integer.parseInt(toNode));
                    break;
                case "Q":
                    int distance = shortestPathSolver.query(Integer.parseInt(fromNode), Integer.parseInt(toNode));
                    System.out.println("distance : "+ distance);
                    result.append(distance).append("\n");
                    break;
                default:
                    break;
            }
        }

        System.out.println("Server finished a batch");
        // System.out.println(result.toString());

        Long processingEndTimeStamp = System.currentTimeMillis();
        Long processingTime = processingEndTimeStamp - processingStartTimeStamp;

        // For reporting
        clients.get(nodeId).registerProcessingTime(processingTime);
        clients.get(nodeId).registerCompletedRequests(1);

        // Return the batch output
        return result.toString();

    }


    public String processBatchBFS(String nodeId, String batch) {

        System.out.println("Server received a batch from " + nodeId);
        System.out.println(batch);
        // If not subscribed, do not serve
        if((!clients.containsKey(nodeId)) || clients.get(nodeId).isUnSubscribed()){
            return "Call subscribe first";
        }

        Long processingStartTimeStamp = System.currentTimeMillis();

        System.out.println("Server started a batch");
        StringBuilder result = new StringBuilder();
        String[] batchLines = batch.split("\\s*\r?\n\\s*");
        for(String line : batchLines){
            proccessBatchLine(nodeId, line, result);
        }
        System.out.println("Server finished a batch");
        // System.out.println(result.toString());


        Long processingEndTimeStamp = System.currentTimeMillis();
        Long processingTime = processingEndTimeStamp - processingStartTimeStamp;

        // For reporting
        clients.get(nodeId).registerProcessingTime(processingTime);
        clients.get(nodeId).registerCompletedRequests(1);

        // Return the batch output
        return result.toString();
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
            System.out.println("Shortest path between " + u + " and " + v + " using " + "BFS" + " is: " + out);
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
