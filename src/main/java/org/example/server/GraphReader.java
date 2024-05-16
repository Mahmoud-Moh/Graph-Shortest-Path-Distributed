package org.example.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import org.example.server.graph.Graph;

public class GraphReader {
    public static Graph readGraphFromStdInput() {
        Scanner scanner = new Scanner(System.in);
        Graph graph = Graph.getInstance();
        
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

        public static Graph readGraphFromFile(String filename) {
        Graph graph = Graph.getInstance();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.equals("S")) // End of input
                    break;
                String[] tokens = line.split(" ");
                int fromNode = Integer.parseInt(tokens[0]);
                int toNode = Integer.parseInt(tokens[1]);
                graph.addEdge(fromNode, toNode);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
        
        return graph;
    }
}
