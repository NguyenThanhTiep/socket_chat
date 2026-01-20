package com.oopsw.simplechat.modules.tcp;

import com.oopsw.simplechat.network.ManagedServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.*;

@Component
public class TcpMultiClientChatServer implements ManagedServer {

    @Value("${demo.tcp.port:9090}")
    private int port;

    private volatile boolean running;
    private ServerSocket serverSocket;
    private ExecutorService pool;

    // Lưu client outputs để broadcast
    private final Set<ClientConn> clients = ConcurrentHashMap.newKeySet();

    @Override
    public String name() {
        return "TCP Multi-Client Chat (port " + port + ")";
    }

    @Override
    public void start() throws Exception {
        if (running) return;
        running = true;

        pool = Executors.newCachedThreadPool();

        pool.submit(() -> {
            try (ServerSocket ss = new ServerSocket(port)) {
                serverSocket = ss;
                System.out.println("[TCP] Listening " + port);

                while (running) {
                    Socket socket = ss.accept();
                    pool.submit(() -> handle(socket));
                }
            } catch (IOException e) {
                if (running) System.err.println("[TCP] " + e.getMessage());
            }
        });
    }

    private void handle(Socket socket) {
        String id = socket.getRemoteSocketAddress().toString();
        System.out.println("[TCP] Connected: " + id);

        try (socket) {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

            ClientConn conn = new ClientConn(id, out);
            clients.add(conn);

            out.write("Welcome TCP Chat. Type and Enter.\n");
            out.flush();

            broadcast("[TCP] " + id + " joined");

            String line;
            while (running && (line = in.readLine()) != null) {
                if (line.equalsIgnoreCase("/quit")) break;
                broadcast("[TCP] " + id + ": " + line);
            }
        } catch (IOException ignored) {
        } finally {
            clients.removeIf(c -> c.id.equals(id));
            broadcast("[TCP] " + id + " left");
            System.out.println("[TCP] Disconnected: " + id);
        }
    }

    private void broadcast(String msg) {
        for (ClientConn c : clients) {
            try {
                c.out.write(msg);
                c.out.write("\n");
                c.out.flush();
            } catch (IOException ignored) {}
        }
    }

    @Override
    public void stop() throws Exception {
        running = false;
        if (serverSocket != null) serverSocket.close();
        if (pool != null) pool.shutdownNow();
        clients.clear();
    }

    private static class ClientConn {
        final String id;
        final BufferedWriter out;
        ClientConn(String id, BufferedWriter out) {
            this.id = id; this.out = out;
        }
    }
}
