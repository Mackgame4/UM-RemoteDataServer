package Server;

import Shared.CmdProtocol;
import Shared.FramedConnection;
import Shared.Notify;
import Shared.FramedConnection.Frame;
import Shared.Account;

import java.io.IOException;
import java.net.Socket;

public class ServerWorker implements Runnable {
    private Socket s;
    private final FramedConnection c;
    private static AccountManager account_manager = new AccountManager();
    private ConnectedClient client;  // Each worker will now use the same ConnectedClient for the connection.

    public ServerWorker(Socket s, FramedConnection c, ConnectedClient client) {
        this.s = s;
        this.c = c;
        this.client = client;  // Shared client for this connection
    }

    @Override
    public void run() {
        try (c) {
            for (;;) {
                Frame frame = c.receive();  // Receiving the frame from the client
                int tag = frame.tag;
                String data = new String(frame.data);

                // Debug log to track the received data and tag
                Notify.info("Received frame with tag: " + tag + ", Data: " + data);

                if (tag == CmdProtocol.ONE_WAY_TAG) { // One-way communication
                    Notify.info("Got One-Way: " + data);
                    if (data.equals(CmdProtocol.CONNECT)) {
                        String client_ip = s.getInetAddress().toString();
                        int client_port = s.getPort();
                        Notify.success("Client connected from " + client_ip + ":" + client_port);
                        client.setIp(client_ip);
                        client.setPort(client_port);
                        account_manager.addClient(client);  // Register client
                    }
                    if (data.equals(CmdProtocol.EXIT)) {
                        if (client != null) {
                            // Log that the exit command is being processed
                            Notify.error("Received EXIT command from client " + client.getId());

                            // Clean up and remove the client from the account manager
                            account_manager.removeClient(client);

                            // Close the framed connection and stop the thread
                            c.close(); // Ensure the connection is properly closed
                        } else {
                            // If client is null, log the issue and exit the worker thread
                            Notify.error("Received EXIT command, but client is null.");
                        }
                        break; // Exit the infinite loop
                    }
                } else if (tag % 2 == CmdProtocol.REQUEST_TAG) { // Reply for odd tags
                    Notify.info("Replying to: " + data);
                    c.sendBytes(tag, data.toUpperCase());
                } else { // Streaming for even tags
                    for (int i = 0; i < data.length(); ++i) {
                        String str = data.substring(i, i + 1);
                        Notify.info("Streaming: " + str);
                        c.send(tag, str.getBytes());
                    }
                    c.send(tag, new byte[0]); // Signal end of stream
                }
            }
        } catch (Exception e) {
            Notify.error("Exception in ServerWorker: " + e.getMessage());
        } finally {
            // Ensure that the connection is closed when the worker thread ends
            try {
                if (c != null) {
                    c.close();
                }
            } catch (IOException e) {
                Notify.error("Error closing connection: " + e.getMessage());
            }
        }
    }
}
