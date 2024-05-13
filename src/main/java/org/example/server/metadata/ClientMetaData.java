package org.example.server.metadata;

public class ClientMetaData {
    String clientId;
    boolean unSubscribed;
    long subscriptionTimeStamp;
    long unSubscriptionTimeStamp;

    int totalRequests = 0;
    long totalProcessingTime = 0;
    
    public String getClientId() {
        return clientId;
    }
    public boolean isUnSubscribed() {
        return unSubscribed;
    }
    public Long getSubscriptionTimeStamp() {
        return subscriptionTimeStamp;
    }
    public Long getUnSubscriptionTimeStamp() {
        return unSubscriptionTimeStamp;
    }
    public int getTotalRequests() {
        return totalRequests;
    }
    public Long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    public ClientMetaData(String clientId) {
        this.clientId = clientId;
        this.subscriptionTimeStamp = System.currentTimeMillis();
        unSubscriptionTimeStamp = -1L;
        unSubscribed = false;
    }
    public void registerProcessingTime(Long processingTime){
        totalProcessingTime += processingTime;
    }
    public void registerCompletedRequests(int count){
        totalRequests += count;
    }
    public void unSubscribe(){
        unSubscribed = true;
        unSubscriptionTimeStamp = System.currentTimeMillis();
    }
    
}
