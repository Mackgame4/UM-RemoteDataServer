package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import Shared.Notify;
import Shared.FramedConnection;
import Shared.CmdProtocol;
import Shared.FramedConnection.Frame;
import Shared.Account;

class ServerWorker implements Runnable {
    private Socket s;
    private final FramedConnection c;
    private final AccountManager account_manager;
    private ConnectedClient client;

    public ServerWorker(Socket s, FramedConnection c, AccountManager account_manager) {
        this.s = s;
        this.c = c;
        this.account_manager = account_manager;
        this.client = new ConnectedClient(s.getInetAddress().toString(), s.getPort());
    }

    @Override
    public void run() {
        try (c) {
            for (;;) {
                Frame frame = c.receive();
                int tag = frame.tag;
                String data = new String(frame.data);
                Object[] data_array = CmdProtocol.parse(data);
                String command = (String) data_array[0];
                String[] args = (String[]) data_array[1];
                if (tag == CmdProtocol.ONE_WAY_TAG) { // One-way communication
                    Notify.debug("Got One-Way: " + data);
                    if (command.equals(CmdProtocol.CONNECT)) {
                        account_manager.addClient(client);
                        Notify.notify("success", "Connected:", "Client Id " + client.getId() + " connected from " + client.getIp() + ":" + client.getPort());
                    }
                    else if (command.equals(CmdProtocol.EXIT)) {
                        account_manager.removeClient(client);
                        Notify.notify("error", "Disconnected:", "Client Id " + client.getId() + " disconnected from " + client.getIp() + ":" + client.getPort());
                        break;
                    }
                } else if (tag % 2 == CmdProtocol.REQUEST_TAG) { // Reply for odd tags
                    Notify.debug("Replying to: " + data);
                    if (command.equals(CmdProtocol.LOGIN)) {
                        if (args.length != 2) {
                            Notify.error("Received invalid login command.");
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"Invalid login command."}));
                        } else {
                            String username = args[0];
                            String password = args[1];
                            if (account_manager.checkAccount(new Account(username, password))) {
                                client.setAccount(new Account(username, password));
                                c.sendBytes(tag, CmdProtocol.build(CmdProtocol.LOGIN, (String[]) new String[]{username}));
                            } else {
                                c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"Invalid username or password."}));
                            }
                        }
                    }
                    else if (command.equals(CmdProtocol.REGISTER)) {
                        if (args.length != 2) {
                            Notify.error("Received invalid register command.");
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"Invalid register command."}));
                        } else {
                            String username = args[0];
                            String password = args[1];
                            account_manager.addAccount(new Account(username, password));
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_SUCC, (String[]) new String[]{"Account '" + username + "' created."}));
                        }
                    }
                    else if (command.equals(CmdProtocol.WHOAMI)) {
                        if (client.getAccount() == null) {
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"No account logged in."}));
                        } else {
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.WHOAMI, (String[]) new String[]{client.toString()}));
                        }
                    }
                    else {
                        Notify.error("Unknown command.");
                        c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"Unknown command."})); // c.sendBytes(tag, data);
                    }
                } else { // Streaming for even tags
                    for (int i = 0; i < data.length(); ++i) {
                        String str = data.substring(i, i + 1);
                        Notify.debug("Streaming: " + str);
                        c.send(tag, str.getBytes());
                    }
                    c.send(tag, new byte[0]); // Signal end of stream
                }
            }
        } catch (Exception e) {
            if (e instanceof IOException) {
                Notify.error("Connection closed: " + e.getMessage());
            } else {
                Notify.error("Exception in ServerWorker: " + e.getMessage());
            }
        } finally {
            try {
                if (c != null) {
                    c.close(); // Ensure that the connection is closed when the worker thread ends
                }
            } catch (IOException e) {
                Notify.error("Error closing connection: " + e.getMessage());
            }
        }
    }
}

public class S_Main {
    //private final static int WORKERS_PER_CONNECTION = 3;
    public static void main() throws IOException {
        try (ServerSocket ss = new ServerSocket(8888)) {
            Notify.info("Server started at " + ss.getInetAddress() + ":" + ss.getLocalPort());
            AccountManager account_manager = new AccountManager();
            // pre-populate accounts
            account_manager.addAccount(new Account("admin", "admin"));
            boolean running = true;
            while (running) {
                Socket s = ss.accept();
                FramedConnection c = new FramedConnection(s);
                /*for (int i = 0; i < WORKERS_PER_CONNECTION; ++i) {
                    Thread t = new Thread(new ServerWorker(s, c, account_manager));
                    t.start();
                }*/
                Thread t = new Thread(new ServerWorker(s, c, account_manager));
                t.start();
            }
            ss.close();
        }
    }
}