package org.example.server;

import org.example.server.graph.Graph;
import org.example.utils.GetPropValues;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;

import java.util.concurrent.CountDownLatch;

public class ServerThread extends Thread {
    private CountDownLatch latch;   // To signal the parent thread (Start.java:main) when ready to handle requests.
    private int port = 49053;
    private Graph graph;

    public ServerThread(CountDownLatch latch) {
        this.latch = latch;
        
        try {
            // Get Rmi Registery Server and port from system.properties
            Properties props = GetPropValues.getPropValues();
            port = Integer.parseInt(props.getProperty("GSP.rmiregistry.port"));
            
            // Create Object of the implementation of remote interface
            GSPRemoteInterface obj = new GSPRemoteObject();

            // Bind the remote object with the name //rmi:://localhost:port/gsp
            LocateRegistry.createRegistry(port);
            Naming.rebind(GetPropValues.getRemoteObjectReference(), obj);
        
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }

    @Override
    public void run() {
        // Read Graph from standard input.
        graph = GraphReader.readGraph();

        // Signal the parent thread when ready.
        latch.countDown();

        // Accept requests.
        // ...
    }
}
