package org.example.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GSPRemoteObject extends UnicastRemoteObject implements GSPRemoteInterface {

    protected GSPRemoteObject() throws RemoteException {
        super();
    }

    @Override
    public String processBatch(String batch) throws RemoteException {
        return "GSPRemoteObject.processBatch called";
    }
}
