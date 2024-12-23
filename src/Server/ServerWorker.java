package Server;

import Shared.CmdProtocol;
import Shared.FramedConnection;
import Shared.Notify;
import Shared.FramedConnection.Frame;

import java.io.IOException;
import java.net.Socket;

public class ServerWorker implements Runnable {
    private Socket s;
    private final FramedConnection c;
    private static AccountManager account_manager = new AccountManager();
    private ConnectedClient client;

    public ServerWorker(Socket s, FramedConnection c, ConnectedClient client) {
        this.s = s;
        this.c = c;
        this.client = client;
    }

    @Override
    public void run() {
        try (c) {
            for (;;) {
                Frame frame = c.receive();
                int tag = frame.tag;
                String data = new String(frame.data);
                if (tag == CmdProtocol.ONE_WAY_TAG) { // One-way communication
                    Notify.info("Got One-Way: " + data);
                    if (data.equals(CmdProtocol.CONNECT)) {
                        String client_ip = s.getInetAddress().toString();
                        int client_port = s.getPort();
                        Notify.success("Client connected from " + client_ip + ":" + client_port);
                        client.setIp(client_ip);
                        client.setPort(client_port);
                        account_manager.addClient(client); // Register client
                    }
                    if (data.equals(CmdProtocol.EXIT)) {
                        if (client != null) {
                            Notify.notify("error", "Received " + CmdProtocol.EXIT + " command from client " + client.getId());
                            account_manager.removeClient(client);
                            c.close();
                        } else {
                            Notify.error("Received " + CmdProtocol.EXIT + " command, but client is null.");
                        }
                        break;
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
