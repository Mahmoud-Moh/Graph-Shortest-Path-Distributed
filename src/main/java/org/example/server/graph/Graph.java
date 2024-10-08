package org.example.server.graph;
import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;


public class Graph {
    private static Graph instance;
    public int numOfNodes;
    public HashMap<Integer, HashSet<Integer>> adj;

    // Private constructor to prevent instantiation
    private Graph() {
        numOfNodes = 0;
        adj = new HashMap<>();
    }

    // Method to get the singleton instance
    public static Graph getInstance() {
        if (instance == null) {
            synchronized (Graph.class) {
                if (instance == null) {
                    instance = new Graph();
                }
            }
        }
        return instance;
    }

    // add a node to the graph
    public void addNode(int node) {
        if (!adj.containsKey(node)) {
            adj.put(node, new HashSet<>());
            numOfNodes++;
        }
    }

    // add an edge between two nodes
    public void addEdge(int fromNode, int toNode) {
        // Add nodes that do not already exist
        if (!adj.containsKey(fromNode))
            addNode(fromNode);
        if (!adj.containsKey(toNode))
            addNode(toNode);

        // Add a directed edge between the two nodes
        adj.get(fromNode).add(toNode);
    }

    // remove an edge between two nodes
    public boolean removeEdge(int fromNode, int toNode) {
        if (adj.containsKey(fromNode) && adj.containsKey(toNode)) {
            HashSet<Integer> fromNodeNeighbors = adj.get(fromNode);
            if (fromNodeNeighbors.contains(toNode)) {
                fromNodeNeighbors.remove(toNode);
                return true;
            }
        }
        // either a node or the edge didn't exist in the first place.
        return false;
    }

    // remove a node and all its edges from the graph
    public boolean removeNode(int node) {
        if (adj.containsKey(node)) {
            // Remove all edges originating from this node
            adj.remove(node);

            // Remove all edges leading to this node
            for (HashSet<Integer> neighbors : adj.values()) {
                if (neighbors.contains(node)) {
                    neighbors.remove(node);
                }
            }

            numOfNodes--;
            return true;
        }
        // the node didn't exist in the first place.
        return false;
    }

    // get all neighbors of a node, returns null if node doesn't exist in the graph.
    public Set<Integer> getNeighbors(int node) {
        return adj.getOrDefault(node, null);
    }

    // Method to reverse the graph
    public Graph reverseGraph() {
        Graph reversedGraph = new Graph();

        // Iterate through each node and its neighbors in the original graph
        for (Map.Entry<Integer, HashSet<Integer>> entry : adj.entrySet()) {
            Integer node = entry.getKey();
            HashSet<Integer> neighbors = entry.getValue();

            // Ensure the node exists in the reversed graph
            reversedGraph.addNode(node);

            // For each neighbor, add the reversed edge to the reversed graph
            for (Integer neighbor : neighbors) {
                reversedGraph.addNode(neighbor); // Ensure the neighbor exists
                reversedGraph.addEdge(neighbor, node); // Reverse the edge direction
            }
        }

        return reversedGraph;
    }

    public int shortestPath(int u, int v , String algorithm) {
        if (algorithm.equals("BFS") ) {
            return BFS(u, v);
        } else if (algorithm.equals("BIDIRECTIONAL_BFS")) {
            return bidirectionalBFS(u, v);
        }
        return 0;
    }

    private int BFS(int u, int v){
        if (u == v) return 0;
        HashMap<Integer, Integer> visited= new HashMap<>();
        Queue<Integer> queue = new LinkedList<>();

        visited.put(u, 0);
        queue.add(u);

        while (!queue.isEmpty()) {
            int current= queue.remove();
            if (adj.containsKey(current)) {
                for (int neighbor : adj.get(current)) {
                    if (!visited.containsKey(neighbor)) {
                        if(neighbor == v)
                            return visited.get(current) + 1;
                        visited.put(neighbor, visited.get(current) + 1);
                        queue.add(neighbor);
                    }
                }
            }
        }

        return -1;
    }

    int bidirectionalBFS(int u, int v){
        // Queue for BFS from start and end nodes
        Queue<Integer> startQueue = new LinkedList<>();
        Queue<Integer> endQueue = new LinkedList<>();

        // Visited sets for start and end BFS
        HashSet<Integer> startVisited = new HashSet<>();
        HashSet<Integer> endVisited = new HashSet<>();

        // Initialize start and end queues and visited sets
        startQueue.add(u);
        endQueue.add(v);
        startVisited.add(u);
        endVisited.add(v);

        // Steps from start and end
        int stepsStart = 0;
        int stepsEnd = 0;

        // Start BFS from both start and end nodes
        while (!startQueue.isEmpty() && !endQueue.isEmpty()) {
            // Perform BFS from start node
            stepsStart++;
            int size = startQueue.size();
            for (int i = 0; i < size; i++) {
                int currentStart = startQueue.poll();
                for (int neighbor : adj.getOrDefault(currentStart, new HashSet<>())) {
                    if (!startVisited.contains(neighbor)) {
                        startQueue.add(neighbor);
                        startVisited.add(neighbor);
                    }
                }
                if (endVisited.contains(currentStart)) {
                    return stepsStart + stepsEnd - 1; // Subtract 1 because we count the meeting node twice
                }
            }

            // Perform BFS from end node
            stepsEnd++;
            size = endQueue.size();
            for (int i = 0; i < size; i++) {
                int currentEnd = endQueue.poll();
                for (int neighbor : adj.getOrDefault(currentEnd, new HashSet<>())) {
                    if (!endVisited.contains(neighbor)) {
                        endQueue.add(neighbor);
                        endVisited.add(neighbor);
                    }
                }
                if (startVisited.contains(currentEnd)) {
                    return stepsStart + stepsEnd - 1; // Subtract 1 because we count the meeting node twice
                }
            }
        }

        return -1;
    }
}
