# Shortest Path Problem in Dynamic Graphs

## Project Overview

In graph theory, the shortest path problem involves finding a path between two vertices (or nodes) in a graph such that the sum of the weights of its constituent edges is minimized. This is a fundamental combinatorial optimization problem with numerous practical applications, including GPS navigation, routing in computer networks, search engine optimization on website interconnectivity graphs, and analyzing social network relationships.

This project focuses on answering shortest path queries on a dynamic graph as efficiently as possible. You will be provided with an initial graph, which you may process and index as needed. After the initial setup, a series of sequential operation batches will be issued to the system. Each operation will either modify the graph or query for the shortest path between nodes.

## Input and Operations

### Initial Graph

The initial graph will be provided through standard input. It is represented as a list of edges, each consisting of a pair of node IDs (start node and end node) represented as non-negative integers. Each edge is specified on a new line in decimal ASCII format, separated by a single space. The initial graph is terminated by a line containing the character 'S'.

### Workload

The workload consists of batches of requests. Each batch includes a sequence of operations provided one per line, followed by a line containing the character 'F' to indicate the end of the batch.

## Server Specification

The server is non-blocking and capable of handling multiple requests simultaneously while managing synchronization. It must track the number of nodes, requests, and processing times for performance reporting.

### RMI Registry

- The RMI registry acts as a naming service that facilitates communication between clients and servers.
- RMI servers register their objects with the RMI registry to make them publicly available.
- RMI clients look up objects in the registry to obtain references and interact with the remote methods.

### Remote Methods

- Remote methods must be declared in a remote interface that extends the `java.rmi.Remote` class.
- The remote interface must be public to ensure accessibility.
- RMI clients interact with the server through this remote interface, using the declared remote methods.

## Algorithms

Two different approaches for solving the shortest path problem in dynamic graphs are utilized in this project. Details of these approaches and their implementations are provided in the codebase.

---

For further details on the implementation and to run the project, refer to the provided source code and documentation.
