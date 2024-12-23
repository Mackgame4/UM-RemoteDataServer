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
import Shared.Account;
import Shared.FramedConnection;

class ServerWorker implements Runnable {
    private Socket socket;
    private final FramedConnection conn;
    private BufferedReader socket_in;
    private PrintWriter socket_out;
    private static AccountManager account_manager = new AccountManager();
    private ConnectedClient client;

    public ServerWorker(Socket socket, FramedConnection conn, ConnectedClient client) throws IOException {
        this.socket = socket;
        this.conn = conn;
        this.client = client;
        this.socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.socket_out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            // When the client connects, add it to the list
            String addr = socket.getInetAddress().getHostAddress();
            int port = socket.getPort();
            ConnectedClient client = new ConnectedClient(addr, port);
            account_manager.addClient(client);
            send_client(Terminal.ANSI_GREEN + "User ID " + client.getId() + " connected successfully!" + Terminal.ANSI_RESET);
            // Main loop for processing client commands
            String line;
            while ((line = socket_in.readLine()) != null) {
                String[] input = CmdProtocol.parse(line);
                String command = input[0];
                String[] args = input[1].split(" ");

                if (command.equals(CmdProtocol.EXIT)) {
                    remove_client(client);
                    send_client(Terminal.ANSI_RED + "Disconnected from server!" + Terminal.ANSI_RESET);
                    break;
                } else if (command.equals(CmdProtocol.REGISTER)) {
                    String username = args[0];
                    String password = args[1];
                    Account account = new Account(username, password);
                    register_account(account);
                    send_client(Terminal.ANSI_GREEN + "Account created successfully!" + Terminal.ANSI_RESET);
                } else if (command.equals(CmdProtocol.LOGIN)) {
                    String username = args[0];
                    String password = args[1];
                    boolean found = false;
                    for (Account account : server_accounts) {
                        if (account.getUsername().equals(username) && account.getPassword().equals(password)) {
                            client.setAccount(account);
                            send_client(CmdProtocol.LOGIN + ":" + username);
                            send_client(Terminal.ANSI_GREEN + "Logged in successfully!" + Terminal.ANSI_RESET);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        send_client(Terminal.ANSI_RED + "Invalid username or password!" + Terminal.ANSI_RESET);
                    }
                } else if (command.equals(CmdProtocol.LOGOUT)) {
                    remove_account(client.getAccount());
                    client.setAccount(null);
                    send_client(CmdProtocol.LOGOUT);
                    send_client(Terminal.ANSI_GREEN + "Logged out successfully!" + Terminal.ANSI_RESET);
                } else if (command.equals(CmdProtocol.WHOAMI)) {
                    if (client.getAccount() != null) {
                        send_client(Terminal.ANSI_GREEN + "You are logged in as " + client.getAccount().getUsername() + Terminal.ANSI_RESET);
                    } else {
                        send_client(Terminal.ANSI_RED + "You are not logged in!" + Terminal.ANSI_RESET);
                    }
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
    private static List<Account> server_accounts = new ArrayList<Account>();

    public static void main() throws IOException {
        int port = 8888;
        ServerSocket ss = new ServerSocket(port);
        String addr = ss.getInetAddress().getHostAddress();
        System.out.println(Terminal.ANSI_GREEN + "Server started at " + addr + ":" + port + Terminal.ANSI_RESET);

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