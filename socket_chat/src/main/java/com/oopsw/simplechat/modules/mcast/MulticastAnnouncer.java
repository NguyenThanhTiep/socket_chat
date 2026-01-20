package com.oopsw.simplechat.modules.mcast;

import com.oopsw.simplechat.network.ManagedServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;

@Component
public class MulticastAnnouncer implements ManagedServer {

    @Value("${demo.mcast.group:230.0.0.0}")
    private String group;

    @Value("${demo.mcast.port:4446}")
    private int port;

    @Value("${demo.tcp.port:9090}")
    private int tcpPort;

    @Value("${demo.nio.port:9091}")
    private int nioPort;

    @Value("${demo.udp.port:7070}")
    private int udpPort;

    private volatile boolean running;
    private Thread thread;

    @Override
    public String name() {
        return "Multicast Announcer (" + group + ":" + port + ")";
    }

    @Override
    public void start() {
        if (running) return;
        running = true;

        thread = new Thread(() -> {
            try (MulticastSocket socket = new MulticastSocket()) {
                InetAddress addr = InetAddress.getByName(group);

                while (running) {
                    String msg = "DISCOVERY TCP=" + tcpPort + " UDP=" + udpPort + " NIO=" + nioPort + " HTTP/WS=8080";
                    byte[] data = msg.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket p = new DatagramPacket(data, data.length, addr, port);
                    socket.send(p);

                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                if (running) System.err.println("[MCAST] " + e.getMessage());
            }
        }, "mcast-announcer");

        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop() {
        running = false;
        if (thread != null) thread.interrupt();
    }
}
