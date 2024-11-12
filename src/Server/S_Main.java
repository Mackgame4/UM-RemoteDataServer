package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import Shared.Terminal;
import Shared.CmdProtocol;
import Shared.ConnectedClient;

class ServerWorker implements Runnable {
    private Socket socket;
    private BufferedReader socket_in;
    private PrintWriter socket_out;
    private List<ConnectedClient> connected_clients;
    private int client_id;

    public ServerWorker(Socket socket, List<ConnectedClient> connected_clients, int client_id) throws IOException {
        this.socket = socket;
        this.connected_clients = connected_clients;
        this.client_id = client_id;
        this.socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.socket_out = new PrintWriter(socket.getOutputStream(), true);
    }

    private void send_client(String message) {
        socket_out.println(message);
        socket_out.flush();
    }

    private ConnectedClient register_client(ConnectedClient client) {
        synchronized (connected_clients) { // Connected clients list is shared among threads
            connected_clients.add(client);
        }
        System.out.println(connected_clients);
        return client;
    }

    private void remove_client(ConnectedClient client) {
        // Remove the client from the list on exit/logout
        synchronized (connected_clients) {
            connected_clients.remove(client);
        }
        System.out.println(connected_clients);
    }

    @Override
    public void run() {
        try {
            // When the client connects, add it to the list
            String addr = socket.getInetAddress().getHostAddress();
            int port = socket.getPort();
            ConnectedClient client = new ConnectedClient(null, null, client_id, addr, port);
            register_client(client);
            send_client(Terminal.ANSI_GREEN + "User ID " + client_id + " connected successfully!" + Terminal.ANSI_RESET);
            // Main loop for processing client commands
            String line;
            while ((line = socket_in.readLine()) != null) {
                String[] input = CmdProtocol.parse(line);
                String command = input[0];

                if (command.equals(CmdProtocol.EXIT)) {
                    remove_client(client);
                    send_client(Terminal.ANSI_RED + "Disconnected from server!" + Terminal.ANSI_RESET);
                    break;
                } else {
                    send_client(Terminal.ANSI_RED + "Invalid command!" + Terminal.ANSI_RESET);
                }
            }

            socket.shutdownInput();
            socket.shutdownOutput();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class S_Main {
    private static int client_count = 0;
    private static List<ConnectedClient> connected_clients = Collections.synchronizedList(new ArrayList<ConnectedClient>());

    public static void main() throws IOException {
        int port = 8888;
        ServerSocket ss = new ServerSocket(port);
        String addr = ss.getInetAddress().getHostAddress();
        System.out.println(Terminal.ANSI_GREEN + "Server started at " + addr + ":" + port + Terminal.ANSI_RESET);

        boolean running = true;
        while (running) {
            Socket socket = ss.accept();
            client_count++;
            Thread worker = new Thread(new ServerWorker(socket, connected_clients, client_count));
            worker.start();
        }
        ss.close();
    }
}