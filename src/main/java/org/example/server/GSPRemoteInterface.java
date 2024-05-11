package org.example.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GSPRemoteInterface extends Remote {
    public String query(int int1, int int2) throws RemoteException;
    public String insert(int int1, int int2) throws RemoteException;
    public String delete(int int1, int int2) throws RemoteException;
}
