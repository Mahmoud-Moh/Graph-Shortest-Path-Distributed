package org.example.server;

import org.example.utils.GetPropValues;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Properties;

public class Server {

    public static void main(String args[]){
        try {
            //Get Rmi Registery Server and port from system.properties
            Properties props = GetPropValues.getPropValues();
            //Create Object of the implementation of remote interface
            GSPRemoteInterface obj = new GSPRemoteObject();

            //Bind the remote object with the name //rmi:://localhost:port/gsp
            LocateRegistry.createRegistry(Integer.parseInt(props.getProperty("GSP.rmiregistry.port")));
            Naming.rebind(GetPropValues.getRemoteObjectReference(), obj);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
