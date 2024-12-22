package Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import Shared.Notify;

public class AccountManager {
    private List<ConnectedClient> connected_clients;
    private static ReentrantLock lock = new ReentrantLock();
    private static int client_count = 0;

    public AccountManager() {
        connected_clients = new ArrayList<>();
    }

    public List<ConnectedClient> getConnectedClients() {
        return connected_clients;
    }

    public void addClient(ConnectedClient client) {
        lock.lock();
        try {
            client_count++;
            client.setId(client_count);
            connected_clients.add(client);
            Notify.debug("Connected clients: " + connected_clients);
        } finally {
            lock.unlock();
        }
    }

    public void removeClient(ConnectedClient client) {
        lock.lock();
        try {
            connected_clients.remove(client);
            Notify.debug("Connected clients: " + connected_clients);
        } finally {
            lock.unlock();
        }
    }
}
