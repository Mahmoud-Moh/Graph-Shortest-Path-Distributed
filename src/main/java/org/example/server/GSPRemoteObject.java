package org.example.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GSPRemoteObject extends UnicastRemoteObject implements GSPRemoteInterface {

    protected GSPRemoteObject() throws RemoteException {
        super();
    }

    @Override
    public String query(int node1, int node2) throws RemoteException {
        System.out.println("Query from server JVM");
        return "GSPRemoteObject.query called";
    }

    @Override
    public String insert(int node1, int node2) throws RemoteException {
        return "GSPRemoteObject.insert called" + String.valueOf(node1 + node2);
    }

    @Override
    public String delete(int node1, int node2) throws RemoteException {
        return "GSPRemoteObject.delete called";
    }

    @Override
    public void block_5s(long i) throws RemoteException, InterruptedException {
        System.out.println("This is the thread" + i + " sleep.");
        Thread.sleep(5000);
        System.out.println("5 seconds passed");
    }
}
