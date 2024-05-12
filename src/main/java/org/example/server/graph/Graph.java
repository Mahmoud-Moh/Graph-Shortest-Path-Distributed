package org.example.server.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
}
