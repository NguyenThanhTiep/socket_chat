package com.oopsw.simplechat.modules.tls;

import com.oopsw.simplechat.network.ManagedServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.net.ssl.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TlsTcpServer implements ManagedServer {

    @Value("${demo.tls.port:9443}")
    private int port;

    @Value("${demo.tls.keystore}")
    private Resource keystore;

    @Value("${demo.tls.keystorePassword}")
    private String keystorePassword;

    @Value("${demo.tls.keyPassword}")
    private String keyPassword;

    private volatile boolean running;
    private SSLServerSocket server;
    private ExecutorService pool;

    @Override
    public String name() {
        return "TLS TCP Server (port " + port + ")";
    }

    @Override
    public void start() throws Exception {
        if (running) return;
        running = true;

        pool = Executors.newCachedThreadPool();
        pool.submit(() -> {
            try {
                SSLContext ctx = buildSslContext();
                server = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(port);

                System.out.println("[TLS] Listening " + port);

                while (running) {
                    SSLSocket client = (SSLSocket) server.accept();
                    pool.submit(() -> handle(client));
                }
            } catch (Exception e) {
                if (running) System.err.println("[TLS] " + e.getMessage());
            }
        });
    }

    private SSLContext buildSslContext() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (InputStream is = keystore.getInputStream()) {
            ks.load(is, keystorePassword.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, keyPassword.toCharArray());

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), null, null);
        return ctx;
    }

    private void handle(SSLSocket socket) {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            socket.startHandshake();

            out.write("TLS server ready. Send a line.\n");
            out.flush();

            String line = in.readLine();
            out.write("TLS_OK: " + line + "\n");
            out.flush();
        } catch (IOException ignored) {}
    }

    @Override
    public void stop() throws Exception {
        running = false;
        if (server != null) server.close();
        if (pool != null) pool.shutdownNow();
    }
}
