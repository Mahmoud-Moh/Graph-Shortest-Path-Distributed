package org.example.client;


import org.example.Graph.Node;
import org.example.server.GSPRemoteInterface;
import org.example.server.GSPRemoteObject;
import org.example.utils.GetPropValues;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.util.Properties;

public class Client{
    public static void main(String args[]) {
        //Get Rmi Registery Server and port from system.properties
        try {
            GSPRemoteInterface stub = (GSPRemoteInterface) Naming.lookup(GetPropValues.getRemoteObjectReference());
            int n1 = 3;
            int n2 = 4;
            System.out.println("We are in client now");
            /*
            Whatever the client is going to do here
             */
            for(int i=0; i<1000; i++){
                if(i % 10 == 0){
                    System.out.println(stub.delete(n1, n2));
                }

                else if(i % 5 == 0){
                    System.out.println(stub.insert(n1, n2));
                }
                else{
                    System.out.println(stub.query(n1, n2));
                }
            }
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
