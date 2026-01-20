package com.oopsw.simplechat.modules.udp;

import com.oopsw.simplechat.network.ManagedServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class UdpEchoServer implements ManagedServer {

    @Value("${demo.udp.port:7070}")
    private int port;

    private DatagramSocket socket;
    private Thread thread;
    private volatile boolean running;

    @Override
    public String name() {
        return "UDP Echo (port " + port + ")";
    }

    @Override
    public void start() throws Exception {
        if (running) return;
        running = true;

        thread = new Thread(() -> {
            try (DatagramSocket ds = new DatagramSocket(port)) {
                socket = ds;
                System.out.println("[UDP] Listening " + port);

                byte[] buf = new byte[2048];
                while (running) {
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    ds.receive(p);

                    String req = new String(p.getData(), p.getOffset(), p.getLength(), StandardCharsets.UTF_8);
                    String resp = "UDP_ACK [" + Instant.now() + "]: " + req;

                    byte[] out = resp.getBytes(StandardCharsets.UTF_8);
                    DatagramPacket reply = new DatagramPacket(out, out.length, p.getAddress(), p.getPort());
                    ds.send(reply);
                }
            } catch (Exception e) {
                if (running) System.err.println("[UDP] " + e.getMessage());
            }
        }, "udp-echo");

        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop() throws Exception {
        running = false;
        if (socket != null) socket.close();
        if (thread != null) thread.interrupt();
    }
}
