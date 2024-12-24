package Benchmark;

import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch; // sort of like a barrier that waits for a fixed number of threads to complete before continuing (doesnt overflow the socket so it crashes)
import java.util.concurrent.TimeUnit;

import Shared.CmdProtocol;
import Shared.Notify;
import Shared.FramedConnection;
import Client.Demultiplexer;

public class TestClient {
    private static int thread_request_tag = CmdProtocol.REQUEST_TAG;

    public static void incrementTag() {
        thread_request_tag += 2;
    }

    public static boolean run(List<String> commands) {
        Socket socket = null;
        CountDownLatch latch = new CountDownLatch(commands.size());
        try {
            socket = new Socket(CmdProtocol.LOCAL_IP, CmdProtocol.PORT);
            Demultiplexer m = new Demultiplexer(new FramedConnection(socket));
            m.start();
            m.sendBytes(CmdProtocol.ONE_WAY_TAG, CmdProtocol.CONNECT);
            if (Config.RUN_THREADED) {
                for (String command : commands) {
                    new Thread(() -> {
                        try {
                            Notify.debug("Sending command: " + command);
                            m.sendBytes(thread_request_tag, command);
                            byte[] data = m.receive(thread_request_tag);
                            String response = new String(data);
                            Notify.debug("Received response for command: " + command + ": " + response);
                            if (response.contains("error")) {
                                Notify.error("Error response received for command: " + command);
                            }
                        } catch (Exception e) {
                            Notify.error("Error during command execution: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            latch.countDown(); // Always count down the latch even if there's an error
                        }
                    }).start();
                }
            } else {
                for (String command : commands) {
                    Notify.debug("Sending command: " + command);
                    m.sendBytes(thread_request_tag, command);
                    byte[] data = m.receive(thread_request_tag);
                    String response = new String(data);
                    Notify.debug("Received response for command: " + command + ": " + response);
                    if (response.contains("error")) {
                        Notify.error("Error response received for command: " + command);
                    }
                    latch.countDown();
                }
            }

            // Wait for all threads to complete with a timeout to avoid hanging indefinitely
            boolean completed = latch.await(30, TimeUnit.SECONDS);
            if (!completed) {
                Notify.error("Benchmark timed out before completion.");
            }
            m.shutdown();
            Notify.notify("error", "TestClient exiting...");
            m.close();
            socket.close();
            return true;
        } catch (Exception e) {
            Notify.error("Subprocess error: " + e.getMessage());
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                    Notify.error("Socket closed due to error: " + socket);
                } catch (Exception closeException) {
                    Notify.error("Error closing socket: " + closeException.getMessage());
                }
            }
            return false;
        }
    }
}