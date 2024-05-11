package org.example.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GSPRemoteObject extends UnicastRemoteObject implements GSPRemoteInterface {

    protected GSPRemoteObject() throws RemoteException {
        super();
    }

    @Override
    public String query(int node1, int node2) throws RemoteException {
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
}
