package Server;

import Shared.CmdProtocol;
import Shared.FramedConnection;
import Shared.FramedConnection.Frame;
import Shared.Notify;
import java.io.IOException;
import java.net.Socket;

public class ServerWorker implements Runnable {
    private Socket s;
    private final FramedConnection c;
    private static final AccountManager account_manager = new AccountManager();
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

                if (frame.tag == 0) {
                    Notify.debug("Got one-way: " + data);

                    // Conexão do cliente
                    if (data.equals(CmdProtocol.CONNECT)) {
                        handleConnect();
                    } 
                    // Comando de tarefa longa
                    else if (data.equals("long-task")) {
                        handleLongTask(tag);
                    } 
                    // Desconexão do cliente
                    else if (data.equals(CmdProtocol.EXIT)) {
                        handleDisconnect();
                        break;
                    } 
                    // Comando getWhen
                    else if (data.startsWith(CmdProtocol.GET_WHEN)) {
                        handleGetWhen(tag, data);
                    }
                } 
                // Comando simples
                else if (frame.tag % 2 == 1) {
                    Notify.debug("Replying to: " + data);
                    c.send(frame.tag, data.toUpperCase().getBytes());
                } 
                // Comando de streaming
                else {
                    handleStreaming(tag, data);
                }
            }
        } catch (Exception e) {
            Notify.error("Exception in ServerWorker: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Libera o semáforo ao final
            S_Main.connectionSemaphore.release();
        }
    }

    private void handleConnect() {
        String client_ip = s.getInetAddress().toString();
        int client_port = s.getPort();
        Notify.success("Client connected from " + client_ip + ":" + client_port);
        client = new ConnectedClient(client_ip, client_port);
        account_manager.addClient(client);
    }

    private void handleDisconnect() {
        if (client != null) {
            Notify.info("Client " + client.getId() + " disconnected.");
            account_manager.removeClient(client);
        } else {
            Notify.warning("Client disconnected without proper initialization.");
        }
    }

    private void handleLongTask(int tag) throws InterruptedException, IOException {
        Notify.debug("Processing long task...");
        Thread.sleep(5000); // Simula 5 segundos de processamento
        c.send(tag, "Long task completed".getBytes());
    }

    private void handleGetWhen(int tag, String data) throws IOException {
        Notify.debug("Processing getWhen command: " + data);
        String[] parts = data.split(" ", 4); // Formato esperado: "getWhen key keyCond valueCond"
        if (parts.length == 4) {
            String key = parts[1];
            String keyCond = parts[2];
            String valueCond = parts[3];
            Notify.debug("Calling DataManager.getWhen for key: " + key + ", keyCond: " + keyCond + ", valueCond: " + valueCond);
            byte[] result = DataManager.getWhen(key, keyCond, valueCond.getBytes());
            Notify.debug("getWhen result: " + (result != null ? new String(result) : "null"));
            c.send(tag, result != null ? result : "null".getBytes());
        } else {
            Notify.error("Invalid getWhen command format: " + data);
            c.send(tag, "Invalid getWhen command format".getBytes());
        }
    }
    
    
    

    private void handleStreaming(int tag, String data) throws IOException {
        for (int i = 0; i < data.length(); ++i) {
            String str = data.substring(i, i + 1);
            Notify.debug("Streaming: " + str);
            c.send(tag, str.getBytes());
        }
        c.send(tag, new byte[0]);
    }
}
