package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import Shared.Terminal;
import Shared.CmdProtocol;
import Shared.Notify;
import Shared.FramedConnection;
import Shared.Demultiplexer;

public class C_Main {
    private static int thread_request_tag = CmdProtocol.REQUEST_TAG;

    public static void incrementTag() {
        thread_request_tag += 2;
    }

    public static void askForInput() {
        System.out.print(Terminal.ANSI_YELLOW + "$ " + Terminal.ANSI_RESET);
    }

    public static void main() throws IOException {
        try {
            Socket s = new Socket("0.0.0.0", 8888);
            BufferedReader system_in = new BufferedReader(new InputStreamReader(System.in));
            Notify.info("Connected to server ("+s.getLocalAddress()+":"+s.getLocalPort()+") at "+s.getInetAddress()+":"+s.getPort());
            Demultiplexer m = new Demultiplexer(new FramedConnection(s));
            m.start();
            m.sendBytes(0, CmdProtocol.CONNECT); // Send connect command to server
            askForInput();
            String userInput;
            while ((userInput = system_in.readLine()) != null) {
                if (userInput.equals(CmdProtocol.EXIT)) {
                    break;
                }
                // A thread for each command
                final String sending = userInput;
                Thread t = new Thread(() -> {
                    try {
                        Notify.debug("Sending from thread " + Thread.currentThread().threadId() + " with tag " + thread_request_tag);
                        Notify.debug("Sending: " + sending);
                        m.sendBytes(thread_request_tag, sending);
                        byte[] data = m.receive(thread_request_tag);
                        String response = new String(data);
                        Notify.debug("Response: " + response);
                        askForInput();
                    } catch (Exception e) {
                        Notify.error(e.getMessage());
                    }
                });
                t.start();
                t.join();
                incrementTag();
            }
            m.sendBytes(0, CmdProtocol.EXIT); // Send exit command to server
            Notify.notify("error", "Exiting...");
            m.close();
            s.close();
        } catch (Exception e) {
            Notify.error(e.getMessage());
        }
    }
}
