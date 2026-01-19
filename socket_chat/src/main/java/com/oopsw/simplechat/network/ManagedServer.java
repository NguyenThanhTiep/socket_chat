package com.oopsw.simplechat.network;

public interface ManagedServer {
    String name();
    void start() throws Exception;
    void stop() throws Exception;
}
