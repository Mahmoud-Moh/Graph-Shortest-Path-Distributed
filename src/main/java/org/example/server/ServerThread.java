package org.example.server;

import org.example.server.graph.Graph;
import org.example.utils.GetPropValues;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;

import java.util.concurrent.CountDownLatch;

public class ServerThread {

    public static void main(String args[]) {
        int port = 49053;
        Graph graph;

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
}
