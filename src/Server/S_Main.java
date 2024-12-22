package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import Shared.Terminal;
import Shared.Account;
import Shared.FramedConnection;
import Shared.FramedConnection.Frame;
import Shared.Notify;
import Shared.Demultiplexer;

public class S_Main {
    private final static int WORKERS_PER_CONNECTION = 3;

    public static void main() throws IOException {
        try (ServerSocket ss = new ServerSocket(8888)) {
            Notify.info("Server started at " + ss.getInetAddress() + ":" + ss.getLocalPort());
            boolean running = true;
            while (running) {
                Socket s = ss.accept();
                FramedConnection c = new FramedConnection(s);
                for (int i = 0; i < WORKERS_PER_CONNECTION; ++i) {
                    Thread t = new Thread(new ServerWorker(s, c));
                    t.start();
                }
            }
            ss.close();
        }
    }
}