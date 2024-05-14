package org.example.server.graph;

import java.util.*;

public class FastGraph extends Graph{

    public List<List<Integer>> dict = new ArrayList<>();

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

    public void generateDictionary(){
        //Costly process, this code should be parallelized per node
        //Assumption graph size < 256 (Number of threads per JVM)
        for(int i=0; i < numOfNodes; i++){
            dict.add(new ArrayList<>(numOfNodes));
            for(int j=0; j < numOfNodes; j++){
                int distance = bfs(adj, i, j);
                dict.get(i).set(j, distance);
            }
        }
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
