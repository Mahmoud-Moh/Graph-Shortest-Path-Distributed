package org.example.server.graph.solvers;


import org.example.server.graph.Graph;

import java.util.*;

import static java.lang.Integer.min;

// To allow switching to a different algo later if ever needed.
public class ShortestPathSolver {

    Graph graph, reversed_graph;
    int numOfNodes;
    List<List<Integer>> dict;

    public ShortestPathSolver(Graph graph){
        this.graph = graph;
        this.numOfNodes = graph.numOfNodes;
        this.reversed_graph = graph.reverseGraph();
        this.dict = new ArrayList<>(numOfNodes + 1);
        for(int i=0; i < numOfNodes + 1; i++) {
            dict.add(new ArrayList<>(Collections.nCopies(numOfNodes + 1, -1))); // Initial fill with -1 (or any default value)
            }
        generateDictionary();
    }

    public int query(int fromNode, int toNode){
        System.out.println(dict.size());
        return dict.get(fromNode).get(toNode);
    }

    public void add(int fromNode, int toNode){
        //If there's already an edge between the two nodes
        reversed_graph.addEdge(toNode, fromNode);
        if(dict.get(fromNode).get(toNode) != 1){
            dict.get(fromNode).set(toNode, 1);
            for(int i=0; i<numOfNodes && i != fromNode && i != toNode; i++){
                int distance_to_from = dict.get(i).get(fromNode);
                int distance_to_to = dict.get(i).get(toNode);
                if(distance_to_from != -1){
                    if(distance_to_to == -1)
                        dict.get(i).set(toNode, distance_to_from + 1);
                    else {
                        dict.get(i).set(toNode, min(distance_to_to, distance_to_from + 1));
                    }
                }
            }
        }
    }

    public void delete(int fromNode, int toNode){
        reversed_graph.removeEdge(toNode, fromNode);
        dict.get(fromNode).set(toNode, -1);
        HashMap<Integer, Integer> distances = bfs(reversed_graph, toNode);
        for(Map.Entry<Integer, Integer> entry : distances.entrySet()){
            int node = entry.getKey();
            int distance = entry.getValue();
            dict.get(node).set(toNode, distance);
        }
    }
    public void generateDictionary(){
        //Costly process, this code should be parallelized per node
        //Assumption graph size < 256 (Number of threads per JVM)
        HashMap<Integer, HashSet<Integer>> adj = graph.adj;
        for(int i=1; i <= numOfNodes; i++){
            //dict.set(i, new ArrayList<>(numOfNodes + 1)) ;
            for(int j=1; j <= numOfNodes; j++){
                int distance = bfs(adj, i, j);
                dict.get(i).set(j, distance);
            }
        }
    }

    public static int bfs(HashMap<Integer, HashSet<Integer>> graph, int start, int end) {
        // Create a queue for BFS
        Queue<Pair> queue = new LinkedList<>();
        // Enqueue the start node along with its distance
        queue.add(new Pair(start, 0));
        // Mark the start node as visited
        Set<Integer> visited = new HashSet<>();
        visited.add(start);

        // BFS loop
        while (!queue.isEmpty()) {
            // Dequeue a node from the queue
            Pair pair = queue.poll();
            int node = pair.node;
            int distance = pair.distance;
            // If the current node is the destination, return its distance
            if (node == end) {
                return distance;
            }
            // Enqueue all adjacent nodes of the current node
            for (int neighbor : graph.get(node)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(new Pair(neighbor, distance + 1));
                }
            }
        }
        // If the destination is not reachable from the source
        return -1;
    }

    public static HashMap<Integer, Integer> bfs(Graph graph, int start) {
        HashMap<Integer, HashSet<Integer>> adj = graph.adj;
        // Create a queue for BFS
        Queue<Pair> queue = new LinkedList<>();
        // Enqueue the start node along with its distance
        queue.add(new Pair(start, 0));
        // Mark the start node as visited
        Set<Integer> visited = new HashSet<>();
        visited.add(start);
        HashMap<Integer, Integer> node_distance_map = new HashMap<>();
        // BFS loop
        while (!queue.isEmpty()) {
            // Dequeue a node from the queue
            Pair pair = queue.poll();
            int node = pair.node;
            int distance = pair.distance;
            // If the current node is the destination, return its distance
            node_distance_map.put(node, distance);
            // Enqueue all adjacent nodes of the current node
            for (int neighbor : adj.get(node)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(new Pair(neighbor, distance + 1));
                }
            }
        }
        int numOfNodes = graph.numOfNodes;
        for(int i=1; i < numOfNodes & i != start; i++){
            if(!node_distance_map.containsKey(i))
                node_distance_map.put(i, -1);
        }
        // If the destination is not reachable from the source
        return node_distance_map;
    }

    static class Pair {
        int node;

        int distance;
        Pair(int node, int distance) {
            this.node = node;
            this.distance = distance;
        }
    }
}
