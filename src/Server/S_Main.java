package Server;

import Shared.FramedConnection;
import Shared.Notify;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class S_Main {
    private static final int MAX_CONNECTIONS = 5; 
    private final static int WORKERS_PER_CONNECTION = 3; 
    public static final Semaphore connectionSemaphore = new Semaphore(MAX_CONNECTIONS);

    public static void main() throws IOException, InterruptedException{
        try (ServerSocket ss = new ServerSocket(8888)) {
            Notify.info("Server started at " + ss.getInetAddress() + ":" + ss.getLocalPort());
            boolean running = true;

            while (running) {
                Socket s = ss.accept();

                if (!connectionSemaphore.tryAcquire()) {
                    Notify.warning("Server at full capacity. Connection from " + s.getInetAddress() + " is rejected.");
                    s.close();
                    continue; 
                }

                FramedConnection c = new FramedConnection(s);
                Notify.success("Accepted connection from " + s.getInetAddress() + ":" + s.getPort());

                for (int i = 0; i < WORKERS_PER_CONNECTION; ++i) {
                    Thread t = new Thread(new ServerWorker(s, c));
                    t.start();
                }
            }
        }
    }
}
