package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import Shared.Terminal;
import Shared.ConnectedClient;
import Shared.ServerAccount;

public class S_Main {
    private static int client_count = 0;
    private static List<ConnectedClient> connected_clients = Collections.synchronizedList(new ArrayList<ConnectedClient>());
    private static List<ServerAccount> server_accounts = new ArrayList<ServerAccount>();

    public static void main() throws IOException {
        int port = 8888;
        ServerSocket ss = new ServerSocket(port);
        String addr = ss.getInetAddress().getHostAddress();
        System.out.println(Terminal.ANSI_GREEN + "Server started at " + addr + ":" + port + Terminal.ANSI_RESET);

        // Pre-populate the server with some accounts
        server_accounts.add(new ServerAccount("admin", "admin", 2));
        server_accounts.add(new ServerAccount("user", "user", 0));
        server_accounts.add(new ServerAccount("writer", "writer", 1));

        boolean running = true;
        while (running) {
            Socket socket = ss.accept();
            client_count++;
            Thread worker = new Thread(new ServerWorker(socket, connected_clients, client_count, server_accounts));
            worker.start();
        }
        ss.close();
    }
}