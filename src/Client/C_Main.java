package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import Shared.Terminal;
import Shared.CmdProtocol;
import Shared.Notify;
import Shared.FramedConnection;

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
            Socket s = new Socket(CmdProtocol.LOCAL_IP, CmdProtocol.PORT);
            BufferedReader system_in = new BufferedReader(new InputStreamReader(System.in));
            Notify.info("Connected to server (" + s.getLocalAddress() + ":" + s.getLocalPort() + ") at " + s.getInetAddress() + ":" + s.getPort());
            Demultiplexer m = new Demultiplexer(new FramedConnection(s));
            m.start();
            m.sendBytes(CmdProtocol.ONE_WAY_TAG, CmdProtocol.CONNECT); // Send connect command to server
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
                        Object[] data_array = CmdProtocol.parse(response);
                        String command = (String) data_array[0];
                        String[] args = (String[]) data_array[1];
                        if (command.equals(CmdProtocol.COMMAND_ERROR)) {
                            if (args.length > 0) {
                                String error = null;
                                for (String arg : args) {
                                    if (error == null) {
                                        error = arg;
                                    } else {
                                        error += " " + arg;
                                    }
                                }
                                Notify.error(error);
                            } else {
                                Notify.error("Unknown error.");
                            }
                        } else {
                            Notify.notify("info", response);
                        }
                        askForInput();
                    } catch (Exception e) {
                        Notify.error(e.getMessage());
                    }
                });
                t.start();
                t.join();
                incrementTag();
            }
            m.shutdown();
            Notify.notify("error", "Exiting...");
            m.close();
            s.close();
        } catch (Exception e) {
            Notify.error(e.getMessage());
        }
    }
}
