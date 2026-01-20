package com.oopsw.simplechat.network;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NetworkServersRunner implements ApplicationRunner {

    private final List<ManagedServer> servers;

    public NetworkServersRunner(List<ManagedServer> servers) {
        this.servers = servers;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (ManagedServer s : servers) {
            try {
                s.start();
                System.out.println("[NET] STARTED: " + s.name());
            } catch (Exception e) {
                System.err.println("[NET] FAILED: " + s.name() + " -> " + e.getMessage());
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        for (ManagedServer s : servers) {
            try {
                s.stop();
                System.out.println("[NET] STOPPED: " + s.name());
            } catch (Exception ignored) {}
        }
    }
}
