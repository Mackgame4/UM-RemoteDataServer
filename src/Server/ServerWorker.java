package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.InputStreamReader;
import java.util.List;

import Shared.Terminal;
import Shared.CmdProtocol;
import Shared.ConnectedClient;
import Shared.ServerAccount;

public class ServerWorker implements Runnable {
    private Socket socket;
    private BufferedReader socket_in;
    private PrintWriter socket_out;
    private List<ConnectedClient> connected_clients;
    private List<ServerAccount> server_accounts;
    private int client_id;

    public ServerWorker(Socket socket, List<ConnectedClient> connected_clients, int client_id, List<ServerAccount> server_accounts) throws IOException {
        this.socket = socket;
        this.connected_clients = connected_clients;
        this.client_id = client_id;
        this.server_accounts = server_accounts;
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
        System.out.println(Terminal.ANSI_CYAN + connected_clients + Terminal.ANSI_RESET);
        return client;
    }

    private int remove_client(ConnectedClient client) {
        synchronized (connected_clients) { // Remove the client from the list on exit/logout
            connected_clients.remove(client);
        }
        System.out.println(Terminal.ANSI_CYAN + connected_clients + Terminal.ANSI_RESET);
        return client.getId();
    }

    private ServerAccount register_account(ServerAccount account) {
        synchronized (server_accounts) {
            server_accounts.add(account);
        }
        System.out.println(Terminal.ANSI_CYAN + server_accounts + Terminal.ANSI_RESET);
        return account;
    }

    private String remove_account(ServerAccount account) {
        synchronized (server_accounts) {
            server_accounts.remove(account);
        }
        System.out.println(Terminal.ANSI_CYAN + server_accounts + Terminal.ANSI_RESET);
        return account.getUsername();
    }

    @Override
    public void run() {
        try {
            // When the client connects, add it to the list
            String addr = socket.getInetAddress().getHostAddress();
            int port = socket.getPort();
            ConnectedClient client = new ConnectedClient(client_id, addr, port, null);
            register_client(client);
            send_client(Terminal.ANSI_GREEN + "User ID " + client_id + " connected successfully!" + Terminal.ANSI_RESET);
            // Main loop for processing client commands
            String line;
            while ((line = socket_in.readLine()) != null) {
                String[] input = CmdProtocol.parse(line);
                String command = input[0];
                String[] args = input[1].split(" ");
                // Start command processing
                // Exit command
                if (command.equals(CmdProtocol.EXIT)) {
                    remove_client(client);
                    send_client(Terminal.ANSI_RED + "Disconnected from server!" + Terminal.ANSI_RESET);
                    break;
                }
                // Register command
                else if (command.equals(CmdProtocol.REGISTER)) {
                    if (args.length < 3) {
                        send_client(Terminal.ANSI_YELLOW + "Usage: " + Terminal.ANSI_RESET + "register <username> <password> <permission_level>");
                        continue;
                    }
                    String username = args[0];
                    String password = args[1];
                    String permissionLevel = args[2];
                    if (Integer.parseInt(permissionLevel) < 0 || Integer.parseInt(permissionLevel) > 2) {
                        send_client(Terminal.ANSI_RED + "Invalid permission level!" + Terminal.ANSI_RESET);
                        continue;
                    }
                    if (server_accounts.stream().anyMatch(account -> account.getUsername().equals(username))) {
                        send_client(Terminal.ANSI_RED + "Username already exists!" + Terminal.ANSI_RESET);
                        continue;
                    }
                    ServerAccount account = new ServerAccount(username, password, Integer.parseInt(permissionLevel));
                    register_account(account);
                    send_client(Terminal.ANSI_GREEN + "Account created successfully!" + Terminal.ANSI_RESET);
                }
                // Login command
                else if (command.equals(CmdProtocol.LOGIN)) {
                    if (args.length < 2) {
                        send_client(Terminal.ANSI_YELLOW + "Usage: " + Terminal.ANSI_RESET + "login <username> <password>");
                        continue;
                    }
                    String username = args[0];
                    String password = args[1];
                    boolean found = false;
                    for (ServerAccount account : server_accounts) {
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
                        continue;
                    }
                }
                // Logout command
                else if (command.equals(CmdProtocol.LOGOUT)) {
                    client.setAccount(null);
                    send_client(CmdProtocol.LOGOUT + ":");
                    send_client(Terminal.ANSI_RED + "Logged out!" + Terminal.ANSI_RESET);
                }
                // Remove command
                else if (command.equals(CmdProtocol.REMOVE)) {
                    if (client.getAccount() == null || client.getAccount().getPermissionLevel() != 2) {
                        send_client(Terminal.ANSI_RED + "Permission denied!" + Terminal.ANSI_RESET);
                        continue;
                    }
                    if (args.length < 1) {
                        send_client(Terminal.ANSI_YELLOW + "Usage: " + Terminal.ANSI_RESET + "remove <username>");
                        continue;
                    }
                    String username = args[0];
                    boolean found = false;
                    for (ServerAccount account : server_accounts) {
                        if (account.getUsername().equals(username)) {
                            if (client.getAccount() != null && client.getAccount().getUsername().equals(username)) {
                                client.setAccount(null);
                                send_client(CmdProtocol.LOGOUT + ":"); // After a command send we can only send one text response after
                            }
                            remove_account(account);
                            send_client(Terminal.ANSI_GREEN + "Account removed successfully!" + Terminal.ANSI_RESET);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        send_client(Terminal.ANSI_RED + "Account not found!" + Terminal.ANSI_RESET);
                        continue;
                    }
                }
                // Whoami command
                else if (command.equals(CmdProtocol.WHOAMI)) {
                    if (client.getAccount() != null) {
                        send_client(Terminal.ANSI_GREEN + "Logged in as " + client.getAccount().getUsername() + Terminal.ANSI_RESET);
                    } else {
                        send_client(Terminal.ANSI_RED + "You are not logged in!" + Terminal.ANSI_RESET);
                    }
                }
                // Invalid command handling
                else {
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