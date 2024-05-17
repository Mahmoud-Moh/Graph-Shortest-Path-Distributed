package org.example.server.graph.solvers;


import org.example.server.graph.Graph;

import java.util.*;

import static java.lang.Integer.min;

public class ShortestPathSolver {

    Graph graph, reversed_graph;
    HashMap<Integer, HashMap<Integer, Integer>>  dict;

    public ShortestPathSolver(Graph graph){
        this.graph = graph;
        this.reversed_graph = graph.reverseGraph();
        generateDictionary();
    }

    public int query(int fromNode, int toNode){
        if(!dict.containsKey(fromNode) || !dict.get(fromNode).containsKey(toNode))
            return -1;
        return dict.get(fromNode).get(toNode);
    }

//    public void add(int fromNode, int toNode){
//        //If there's already an edge between the two nodes
//        //Update graph
//        graph.addEdge(fromNode, toNode);
//
//        //Update dictionary
//        //Find all nodes that can reach fromNode (node = fromNode, graph = reversed_graph)
//        HashMap<Integer, Integer> node_distance_map1 = bfs(reversed_graph, fromNode);
//        //Find all nodes that are reachable from toNode (node = fromNode, graph = graph)
//        HashMap<Integer, Integer> node_distance_map2 = bfs(graph, toNode);
//        //Update distances from all these nodes to each other
//        for(Map.Entry<Integer, Integer> entry1 : node_distance_map1.entrySet()){
//            int node1 = entry1.getKey();
//            int distance_to_fromNode = entry1.getValue();
//            for(Map.Entry<Integer, Integer> entry2 : node_distance_map2.entrySet()){
//                int node2 = entry2.getKey();
//                int distance_to_toNode = entry2.getValue();
//                if(dict.get(node1).get(node2) == -1)
//                    dict.get(node1).set(node2, distance_to_fromNode + 1 + distance_to_toNode);
//                else
//                    dict.get(node1).set(node2, min(dict.get(node1).get(node2), distance_to_fromNode + 1 + distance_to_toNode));
//
//            }
//        }
//        //Update Reverse Graph
//        reversed_graph.addEdge(toNode, fromNode);
//    }

    public void add(int fromNode, int toNode){
        //Update graph
        graph.addEdge(fromNode, toNode);

        //Update dictionary
        generateDictionary();

        //Update Reverse Graph
        reversed_graph.addEdge(toNode, fromNode);

    }

    public void delete(int fromNode, int toNode){
        //Update graph
        graph.removeEdge(fromNode, toNode);

        //Update dictionary
        generateDictionary();

        //Update Reverse Graph
        reversed_graph.removeEdge(toNode, fromNode);
    }
    public void generateDictionary(){
        //Costly process, this code should be parallelized per node
        //Assumption graph size < 256 (Number of threads per JVM)
        HashMap<Integer, HashMap<Integer, Integer>> new_dict = new HashMap<>();

        //O(n^2)
        for(Map.Entry<Integer, HashSet<Integer>> entry : graph.adj.entrySet()){
            int i = entry.getKey();
            HashMap<Integer, Integer> node_distance_map = bfs(graph, i);
            new_dict.put(i, new HashMap<>());
            for(Map.Entry<Integer, Integer> entry2 : node_distance_map.entrySet()){
                int j = entry2.getKey();
                int distance_i_j = entry2.getValue();
                new_dict.get(i).put(j, distance_i_j);
            }
        }
        this.dict = new_dict;
    }

    /*public static int bfs(HashMap<Integer, HashSet<Integer>> graph, int start, int end) {
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
    }*/

    public static HashMap<Integer, Integer> bfs(Graph graph, int start) {
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
            for (int neighbor : graph.adj.get(node)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(new Pair(neighbor, distance + 1));
                }
            }
        }
        for(int i=1; i < graph.numOfNodes & i != start; i++){
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
