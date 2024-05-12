package org.example;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.example.server.ServerThread;

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
        // ...

        serverThread.join(); // Or detach?
    }
}