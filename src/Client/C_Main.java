package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Shared.Terminal;
import Shared.CmdProtocol;
import Shared.Notify;
import Shared.FramedConnection;

public class C_Main {
    private static int thread_request_tag = CmdProtocol.REQUEST_TAG;
    private static String username = null;
    private static final Deque<String> commandQueue = new ArrayDeque<>();
    private static final ReentrantLock queueLock = new ReentrantLock();
    private static final Condition notEmpty = queueLock.newCondition();
    private static volatile boolean running = true;

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

            // Thread for sending commands
            Thread commandSender = new Thread(() -> {
                while (running) {
                    queueLock.lock();
                    try {
                        while (commandQueue.isEmpty() && running) {
                            notEmpty.await();
                        }
                        if (!running) break;

                        String command = commandQueue.poll();
                        if (command != null) {
                            Notify.debug("Sending: " + command);
                            m.sendBytes(thread_request_tag, command);
                            incrementTag();
                            askForInput();
                        }
                    } catch (InterruptedException | IOException e) {
                        Notify.error("Error in command sender: " + e.getMessage());
                    } finally {
                        queueLock.unlock();
                    }
                }
            });

            // Thread for receiving responses
            Thread responseReceiver = new Thread(() -> {
                while (running) {
                    try {
                        byte[] data = m.receive(thread_request_tag);
                        String response = new String(data);
                        Object[] data_array = CmdProtocol.parse(response);
                        String command = (String) data_array[0];
                        String[] args = (String[]) data_array[1];
                        String message = CmdProtocol.argsAsMessage(args);

                        switch (command) {
                            case CmdProtocol.COMMAND_ERROR:
                                Notify.error(message.isEmpty() ? "Unknown error." : message);
                                break;
                            case CmdProtocol.COMMAND_SUCC:
                                Notify.success(message.isEmpty() ? "Unknown command." : message);
                                break;
                            case CmdProtocol.LOGIN:
                                username = args[0];
                                Notify.success("Logged in as '" + username + "'.");
                                break;
                            default:
                                Notify.info(message.isEmpty() ? response : message);
                        }

                        // Prompt for next input after handling response
                        askForInput();
                    } catch (IOException | InterruptedException e) {
                        if (running) {
                            Notify.error("Error receiving response: " + e.getMessage());
                        }
                        break;
                    }
                }
            });

            commandSender.start();
            responseReceiver.start();

            // Main thread handles user input
            askForInput();
            String userInput;
            while ((userInput = system_in.readLine()) != null) {
                if (userInput.equals(CmdProtocol.EXIT)) {
                    running = false;
                    queueLock.lock();
                    try {
                        notEmpty.signalAll();
                    } finally {
                        queueLock.unlock();
                    }
                    break;
                }
                queueLock.lock();
                try {
                    commandQueue.add(userInput);
                    notEmpty.signal();
                } finally {
                    queueLock.unlock();
                }
            }

            commandSender.join();
            responseReceiver.join();
            m.shutdown();
            Notify.notify("info", "Exiting...");
            m.close();
            s.close();
        } catch (Exception e) {
            Notify.error("Error: " + e.getMessage());
        }
    }
}
