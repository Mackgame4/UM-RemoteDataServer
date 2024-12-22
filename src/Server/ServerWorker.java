package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.InputStreamReader;
import java.util.List;

import Shared.Terminal;
import Shared.CmdProtocol;
import Shared.FramedConnection;
import Shared.FramedConnection.Frame;
import Shared.Notify;
import Shared.Account;

public class ServerWorker implements Runnable {
    private Socket s;
    private final FramedConnection c;
    private static AccountManager account_manager = new AccountManager();
    private ConnectedClient client;

    public ServerWorker(Socket s, FramedConnection c) {
        this.s = s;
        this.c = c;
    }

    @Override
    public void run() {
        try (c) {
            for (;;) {
                Frame frame = c.receive();
                int tag = frame.tag;
                String data = new String(frame.data);
                if (frame.tag == 0)
                    Notify.debug("Got one-way: " + data);
                    if (data.equals(CmdProtocol.CONNECT)) {
                        String client_ip = s.getInetAddress().toString();
                        int client_port = s.getPort();
                        Notify.success("Client connected from " + client_ip + ":" + client_port);
                        client = new ConnectedClient(client_ip, client_port);
                        account_manager.addClient(client);
                    }
                    else if (data.equals(CmdProtocol.EXIT)) {
                        Notify.error("Client "+ client.getId() + " disconnected.");
                        account_manager.removeClient(client);
                        break;
                    }
                else if (frame.tag % 2 == 1) {
                    Notify.debug("Replying to: " + data);
                    c.send(frame.tag, data.toUpperCase().getBytes());
                } else {
                    for (int i = 0; i < data.length(); ++i) {
                        String str = data.substring(i, i+1);
                        Notify.debug("Streaming: " + str);
                        c.send(tag, str.getBytes());
                    }
                    c.send(tag, new byte[0]);
                }
            }
        } catch (Exception ignored) { }
    }
}