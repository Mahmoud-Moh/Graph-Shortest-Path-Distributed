package org.example.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GSPRemoteInterface extends Remote {
    public String processBatch(String nodeId, String batch) throws RemoteException;
    public String processBatch_fastQuery(String nodeId, String batch) throws RemoteException;
    public Boolean subscribe(String nodeId) throws RemoteException;
    public void unSubscribe(String nodeId) throws RemoteException;
    public void x(String nodeId) throws RemoteException;
}
