package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.HashMap;

import Shared.Notify;
import Shared.FramedConnection;
import Shared.CmdProtocol;
import Shared.FramedConnection.Frame;
import Shared.Account;

class ServerWorker implements Runnable {
    private final FramedConnection c;
    private final AccountManager account_manager;
    private ConnectedClient client;
    private DataManager data_manager;

    public ServerWorker(FramedConnection c, AccountManager account_manager, DataManager data_manager, ConnectedClient client) {
        this.c = c;
        this.account_manager = account_manager;
        this.data_manager = data_manager;
        this.client = client;
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
                    else if (command.equals(CmdProtocol.WRITE_FILE)) { // example: "write key1 value1"
                        if (args.length != 2) {
                            Notify.error("Received invalid write command.");
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"Invalid write command."}));
                        }
                        else if (client.getAccount() == null) {
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"No account logged in."}));
                        }
                        else {
                            String key = args[0];
                            String value = args[1];
                            data_manager.put(key, value);
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_SUCC, (String[]) new String[]{"{ key: '" + key + "', value: '" + value + "' } written."}));
                        }
                    }
                    else if (command.equals(CmdProtocol.READ_FILE)) { // example: "read key1"
                        if (args.length != 1) {
                            Notify.error("Received invalid read command.");
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"Invalid read command."}));
                        }
                        else if (client.getAccount() == null) {
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"No account logged in."}));
                        }
                        else {
                            String key = args[0];
                            String value = data_manager.get(key);
                            if (value == null) {
                                c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"Key '" + key + "' not found."}));
                            } else {
                                c.sendBytes(tag, CmdProtocol.build(CmdProtocol.READ_FILE, (String[]) new String[]{value}));
                            }
                        }
                    }
                    else if (command.equals(CmdProtocol.DELETE_FILE)) { // example: "del key1"
                        if (args.length != 1) {
                            Notify.error("Received invalid delete command.");
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"Invalid delete command."}));
                        }
                        else if (client.getAccount() == null) {
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"No account logged in."}));
                        }
                        else {
                            String key = args[0];
                            data_manager.remove(key);
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_SUCC, (String[]) new String[]{"Key '" + key + "' deleted."}));
                        }
                    }
                    else if (command.equals(CmdProtocol.LIST_FILES)) {
                        if (args.length != 0) {
                            Notify.error("Received invalid list command.");
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"Invalid list command."}));
                        }
                        else if (client.getAccount() == null) {
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"No account logged in."}));
                        }
                        else {
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_SUCC, new String[]{data_manager.getAll().toString()}));
                        }
                    }
                    else if (command.equals(CmdProtocol.MWRITE_FILE)) { // example: "mwrite key1 value1 key2 value2 key3 value3"
                        if (args.length % 2 != 0) {
                            Notify.error("Received invalid mwrite command.");
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"Invalid mwrite command."}));
                        }
                        else if (client.getAccount() == null) {
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"No account logged in."}));
                        }
                        else {
                            Map<String, byte[]> pairs = new HashMap<>();
                            for (int i = 0; i < args.length; i += 2) {
                                pairs.put(args[i], args[i + 1].getBytes());
                            }
                            data_manager.multiPut(pairs);
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_SUCC, (String[]) new String[]{"Multiple keys written."}));
                        }
                    }
                    else if (command.equals(CmdProtocol.MREAD_FILE)) { // example: "mread key1 key2 key3"
                        if (args.length == 0) {
                            Notify.error("Received invalid mread command.");
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"Invalid mread command."}));
                        }
                        else if (client.getAccount() == null) {
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"No account logged in."}));
                        }
                        else {
                            Map<String, String> result = data_manager.multiGet(args);
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.MREAD_FILE, new String[]{result.toString()}));
                        }
                    }
                    else if (command.equals(CmdProtocol.MDELETE_FILE)) { // example: "mdel key1 key2 key3"
                        if (args.length == 0) {
                            Notify.error("Received invalid mdelete command.");
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"Invalid mdelete command."}));
                        }
                        else if (client.getAccount() == null) {
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"No account logged in."}));
                        }
                        else {
                            data_manager.multiRemove(args);
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_SUCC, (String[]) new String[]{"Multiple keys deleted."}));
                        }
                    }
                    else if (command.equals(CmdProtocol.READ_FILE_WHEN)) { // example: "readw key1 key2 value2"
                        if (args.length != 3) {
                            Notify.error("Received invalid readw command.");
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"Invalid readw command."}));
                        }
                        else if (client.getAccount() == null) {
                            c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"No account logged in."}));
                        }
                        else {
                            String key = args[0];
                            String keyCond = args[1];
                            byte[] valueCond = args[2].getBytes();
                            byte[] value = data_manager.getWhen(key, keyCond, valueCond);
                            if (value == null) {
                                //c.sendBytes(tag, CmdProtocol.build(CmdProtocol.COMMAND_ERROR, (String[]) new String[]{"Key '" + key + "' not found or condition not met."}));
                                // TODO: do it
                                // simulate the condition being met later
                                Thread.sleep(2000);
                                c.sendBytes(tag, CmdProtocol.build(CmdProtocol.READ_FILE_WHEN, (String[]) new String[]{"Condition met."}));
                            } else {
                                c.sendBytes(tag, CmdProtocol.build(CmdProtocol.READ_FILE_WHEN, (String[]) new String[]{new String(value)}));
                            }
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
            DataManager data_manager = new DataManager();
            // pre-populate accounts
            account_manager.addAccount(new Account("admin", "admin"));
            boolean running = true;
            while (running) {
                Socket s = ss.accept();
                FramedConnection c = new FramedConnection(s);
                ConnectedClient client = new ConnectedClient(s.getInetAddress().toString(), s.getPort());
                /*for (int i = 0; i < WORKERS_PER_CONNECTION; ++i) {
                    Thread t = new Thread(new ServerWorker(s, c, account_manager));
                    t.start();
                }*/
                Thread t = new Thread(new ServerWorker(c, account_manager, data_manager, client));
                t.start();
            }
            ss.close();
        }
    }
}