package com.oopsw.simplechat.modules.nio;

import com.oopsw.simplechat.network.ManagedServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Component
public class NioAsyncServer implements ManagedServer {

    @Value("${demo.nio.port:9091}")
    private int port;

    private volatile boolean running;
    private Thread thread;
    private Selector selector;
    private ServerSocketChannel server;

    @Override
    public String name() {
        return "NIO Async Server (port " + port + ")";
    }

    @Override
    public void start() throws Exception {
        if (running) return;
        running = true;

        thread = new Thread(() -> {
            try {
                selector = Selector.open();
                server = ServerSocketChannel.open();
                server.configureBlocking(false);
                server.bind(new InetSocketAddress(port));
                server.register(selector, SelectionKey.OP_ACCEPT);

                System.out.println("[NIO] Listening " + port);

                ByteBuffer buf = ByteBuffer.allocate(2048);

                while (running) {
                    selector.select();
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();

                        if (!key.isValid()) continue;

                        if (key.isAcceptable()) {
                            SocketChannel c = server.accept();
                            if (c != null) {
                                c.configureBlocking(false);
                                c.register(selector, SelectionKey.OP_READ);
                            }
                        } else if (key.isReadable()) {
                            SocketChannel c = (SocketChannel) key.channel();

                            buf.clear();
                            int n = c.read(buf);
                            if (n <= 0) {
                                c.close();
                                continue;
                            }

                            buf.flip();
                            String msg = StandardCharsets.UTF_8.decode(buf).toString().trim();
                            String out = "NIO_OK: " + msg + "\n";
                            c.write(ByteBuffer.wrap(out.getBytes(StandardCharsets.UTF_8)));
                        }
                    }
                }
            } catch (Exception e) {
                if (running) System.err.println("[NIO] " + e.getMessage());
            } finally {
                try { if (server != null) server.close(); } catch (IOException ignored) {}
                try { if (selector != null) selector.close(); } catch (IOException ignored) {}
            }
        }, "nio-async");

        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void stop() throws Exception {
        running = false;
        if (selector != null) selector.wakeup();
        if (thread != null) thread.interrupt();
    }
}
