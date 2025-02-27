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
    private static String username = null;

    public static void incrementTag() {
        thread_request_tag += 2;
    }

    public static void askForInput() {
        if (username == null) {
            System.out.print(Terminal.ANSI_YELLOW + "$ " + Terminal.ANSI_RESET);
        } else {
            System.out.print(Terminal.ANSI_YELLOW + username + "$ " + Terminal.ANSI_RESET);
        }
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

            // Start a new thread to handle receiving responses
            new Thread(() -> {
                try {
                    while (true) {
                        byte[] data = m.receive(thread_request_tag);
                        String response = new String(data);
                        Object[] data_array = CmdProtocol.parse(response);
                        String command = (String) data_array[0];
                        String[] args = (String[]) data_array[1];
                        String message = CmdProtocol.argsAsMessage(args);

                        if (command.equals(CmdProtocol.COMMAND_ERROR)) {
                            if (args.length > 0) {
                                Notify.error(message);
                            } else {
                                Notify.error("Unknown error.");
                            }
                        } else if (command.equals(CmdProtocol.COMMAND_SUCC)) {
                            if (args.length > 0) {
                                Notify.success(message);
                            } else {
                                Notify.success("Unknown command.");
                            }
                        } else if (command.equals(CmdProtocol.LOGIN)) {
                            username = args[0];
                            Notify.success("Logged in as '" + username + "'.");
                        } else if (command.equals(CmdProtocol.WHOAMI) || command.equals(CmdProtocol.LIST_FILES)) {
                            Notify.info(message);
                        } else {
                            Notify.info(response);
                        }

                        askForInput(); // Request input again after processing response
                    }
                } catch (Exception e) {
                    Notify.error(e.getMessage());
                }
            }).start();

            // Main loop for accepting commands from user input
            while ((userInput = system_in.readLine()) != null) {
                if (userInput.equals(CmdProtocol.EXIT)) {
                    break;
                }
                // A thread for each command (no waiting for response here)
                final String sending = userInput;
                new Thread(() -> {
                    try {
                        Notify.debug("Sending from thread " + Thread.currentThread().threadId() + " with tag " + thread_request_tag);
                        Notify.debug("Sending: " + sending);
                        m.sendBytes(thread_request_tag, sending);
                        incrementTag();  // Increment tag after sending
                        askForInput();   // Prompt for next input
                    } catch (Exception e) {
                        Notify.error(e.getMessage());
                    }
                }).start();
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
