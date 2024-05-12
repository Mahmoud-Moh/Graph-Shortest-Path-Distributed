package org.example.server.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

enum Algorithm {
    BFS,
    BidirectionalBFS
}

public class Graph {
    int numOfNodes;
    HashMap<Integer, HashSet<Integer>> adj;

    public Graph() {
        numOfNodes = 0;
        adj = new HashMap<>();
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
            if(fromNodeNeighbors.contains(toNode)){
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
    

    public int shortestPath(int u, int v , Algorithm algorithm) {
        if (algorithm == Algorithm.BFS) {
            return BFS(u, v);
        } else if (algorithm == Algorithm.BidirectionalBFS) {
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
        if (u == v) return 0;
        return -1;
    }
}
