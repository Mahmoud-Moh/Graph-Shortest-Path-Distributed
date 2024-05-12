package org.example.server;

import java.util.Scanner;

import org.example.server.graph.Graph;

public class GraphReader {
    public static Graph readGraph() {
        Scanner scanner = new Scanner(System.in);
        Graph graph = new Graph();
        
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.equals("S")) // End of input
                break;
            String[] tokens = line.split(" ");
            int fromNode = Integer.parseInt(tokens[0]);
            int toNode = Integer.parseInt(tokens[1]);
            graph.addEdge(fromNode, toNode);
        }
        
        scanner.close();
        return graph;
    }
}
