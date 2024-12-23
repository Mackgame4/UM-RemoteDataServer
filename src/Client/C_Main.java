package Client;

import Shared.CmdProtocol;
import Shared.Demultiplexer;
import Shared.FramedConnection;
import Shared.Notify;
import Shared.Terminal;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class C_Main {

    private static AtomicInteger thread_request_tag = new AtomicInteger(CmdProtocol.REQUEST_TAG);
    private static final int MAX_THREADS = 10; // Limita o número de threads simultâneas

    public static void incrementTag() {
        thread_request_tag.addAndGet(2);
    }

    public static void askForInput() {
        System.out.print(Terminal.ANSI_YELLOW + "$ " + Terminal.ANSI_RESET);
    }

    public static void main() throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS); // Gerencia o pool de threads

        try {
            Socket s = new Socket("0.0.0.0", 8888);
            BufferedReader system_in = new BufferedReader(new InputStreamReader(System.in));
            Notify.info("Connected to server (" + s.getLocalAddress() + ":" + s.getLocalPort() + ") at " + s.getInetAddress() + ":" + s.getPort());
            Demultiplexer m = new Demultiplexer(new FramedConnection(s));
            m.start();
            m.sendBytes(0, CmdProtocol.CONNECT); // Envia comando de conexão ao servidor
            askForInput();

            String userInput;
            while ((userInput = system_in.readLine()) != null) {
                if (userInput.equals(CmdProtocol.EXIT)) {
                    break;
                }

                final String sending = userInput;
                final int currentTag = thread_request_tag.get();
                executor.submit(() -> {
                    try {
                        Notify.debug("Sending from thread " + Thread.currentThread().threadId() + " with tag " + currentTag);
                        Notify.debug("Sending: " + sending);
                        Notify.debug("Thread ID: " + Thread.currentThread().threadId() + " is processing: " + sending);
                        m.sendBytes(currentTag, sending);
                        byte[] data = m.receive(currentTag);
                        String response = new String(data);
                        Notify.debug("Response: " + response);
                        askForInput();
                    } catch (Exception e) {
                        Notify.error(e.getMessage());
                    }
                });
                incrementTag(); // Incrementa tag para a próxima thread
            }

            m.sendBytes(0, CmdProtocol.EXIT); // Envia comando de saída ao servidor
            Notify.notify("error", "Exiting...");
            executor.shutdown(); // Encerra o executor
            m.close();
            s.close();
        } catch (Exception e) {
            Notify.error(e.getMessage());
        }
    }
}