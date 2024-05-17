package org.example;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import org.example.server.GSPRemoteInterface;
import org.example.server.GSPRemoteObject;
import org.example.server.ServerThread;
import org.example.utils.GetPropValues;

public class Start {

    // Specify the classpath
    static final String CLASSPATH = System.getProperty("os.name").toLowerCase().contains("linux") ? "target/classes" : "target\\classes";
    private static final String SERVER_LOG_DIRECTORY = "";

    public static void main(String[] args) throws IOException, InterruptedException {
        // a signaling mechanism to get notified when the server thread is ready to recieve requests
        CountDownLatch latch = new CountDownLatch(1);

        // Create the server thread
        ServerThread serverThread = new ServerThread(latch);

        // Start the server thread (it should start reading the graph)
        serverThread.start();

        latch.await(); // Wait for server's signal

        System.out.println("Received signal from the server thread, starting clients...");

        // Start the client processes
        try{

            Properties props = GetPropValues.getPropValues();
            int numOfClients = Integer.parseInt(props.getProperty("GSP.numberOfnodes"));

            Process[] processes = new Process[numOfClients];
            String[] clientIds = new String[numOfClients];

            for(int i=0; i< numOfClients; i++){
                clientIds[i] = props.getProperty("GSP.node"+i);
            }

            int seed = 42;
            for (int i = 0; i < clientIds.length; i++) {
                // Command to run the client process
                String[] command = {"java", "-cp", CLASSPATH, "org.example.client.Client", clientIds[i], String.valueOf(seed)};

                // Create a ProcessBuilder with the command
                ProcessBuilder pb = new ProcessBuilder(command);

                // Start the process and store a reference to it
                processes[i] = pb.start();

                seed++;
            }

            // Join on the processes
            for (Process process : processes) {
                int exitCode = process.waitFor();
                System.out.println("Exited with error code " + exitCode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        GSPRemoteObject gsp = serverThread.getGsp();
        gsp.report(SERVER_LOG_DIRECTORY);


        // Save the used parameters 
        String filePath = Paths.get(GetPropValues.getClientParams().getProperty("logDirectory"), "parameters.properties").toString();

        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            GetPropValues.getClientParams().store(outputStream, "Client Parameters");
            System.out.println("Client Parameters saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving properties to file: " + e.getMessage());
        }

        // Save the used parameters 
        filePath = Paths.get(GetPropValues.getClientParams().getProperty("logDirectory"), "system.properties").toString();

        try (OutputStream outputStream = new FileOutputStream(filePath)) {
            GetPropValues.getPropValues().store(outputStream, "System Parameters");
            System.out.println("System Parameters saved to " + filePath);
        } catch (IOException e) {
            System.err.println("Error saving properties to file: " + e.getMessage());
        }

        serverThread.join(); // Wait for the server thread
    }
}