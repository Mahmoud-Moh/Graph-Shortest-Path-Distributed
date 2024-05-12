package org.example.client;


import org.example.server.GSPRemoteInterface;
import org.example.server.GSPRemoteObject;
import org.example.server.graph.Node;
import org.example.utils.GetPropValues;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Properties;

public class Client extends Thread{
    public GSPRemoteInterface stub;
    public Client() throws IOException, NotBoundException {
        stub = (GSPRemoteInterface) Naming.lookup(GetPropValues.getRemoteObjectReference());
    }

    @Override
    public void run(){
        long threadId = Thread.currentThread().getId();
        System.out.println("Calling stub from thread " + threadId);
        try {
            stub.block_5s(threadId);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Thread " + threadId + "finished his nap");
    }
    /*public static void main(String args[]) {
        //Get Rmi Registery Server and port from system.properties
        try {
            GSPRemoteInterface stub = (GSPRemoteInterface) Naming.lookup(GetPropValues.getRemoteObjectReference());
            int n1 = 3;
            int n2 = 4;

            Whatever the client is going to do here

            for(int i=0; i<10; i++){
                /*if(i % 10 == 0){
                    System.out.println(stub.delete(n1, n2));
                }

                else if(i % 5 == 0){
                    System.out.println(stub.insert(n1, n2));
                }
                else{
                    System.out.println(stub.query(n1, n2));
                }
                System.out.println("I'm just about to call the " + i + "th sleep");
                stub.block_5s(i);

            }
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }*/
}
