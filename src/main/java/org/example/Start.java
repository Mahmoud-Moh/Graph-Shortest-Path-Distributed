package org.example;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.example.server.ServerThread;
import org.example.utils.GetPropValues;

public class Start {
    public static void main(String[] args) throws IOException, InterruptedException {
        // a signaling mechanism to get notified when the server thread is ready to recieve requests
        CountDownLatch latch = new CountDownLatch(1);
        
        // Create the server thread
        Thread serverThread = new ServerThread(latch);
        
        // Start the server thread (it should start reading the graph)
        serverThread.start();
        
        latch.await(); // Wait for server's signal

        System.out.println("Received signal from the server thread, starting clients...");
        
        // Start the client processes 

        // Specify the classpath
        String classpath = "D:\\Github\\Graph-Shortest-Path-Distributed\\target\\classes";

        // List of arguments

        // Array to hold references to the processes
        
        try{
            
            Properties props = GetPropValues.getPropValues();
            int numOfClients = Integer.parseInt(props.getProperty("GSP.numberOfnodes"));
            
            Process[] processes = new Process[numOfClients];
            String[] clientIds = new String[numOfClients];

            for(int i=0; i< numOfClients; i++){
                clientIds[i] = props.getProperty("GSP.node"+i);
            }
            for (int i = 0; i < clientIds.length; i++) {
                // Command to run the client process
                String[] command = {"java", "-cp", classpath, "org.example.client.Client", clientIds[i]};

                // Create a ProcessBuilder with the command
                ProcessBuilder pb = new ProcessBuilder(command);

                // Start the process and store reference to it
                processes[i] = pb.start();
            }

            // Join on the processes
            for (Process process : processes) {
                int exitCode = process.waitFor();
                // Print the exit code of the process
                System.out.println("Exited with error code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        serverThread.join(); // wait for the server thread
    }
}