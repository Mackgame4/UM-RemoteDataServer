package Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

import Shared.Notify;
import Shared.Account;
import Shared.CmdProtocol;

public class AccountManager {
    private List<Account> accounts;
    private List<ConnectedClient> connected_clients;
    private static ReentrantLock lock = new ReentrantLock();
    private static Condition max_connections = lock.newCondition();
    private static int client_count = 0;

    public AccountManager() {
        accounts = new ArrayList<>();
        connected_clients = new ArrayList<>();
    }

    public List<Account> getAccounts() {
        return accounts;
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
            if (connected_clients.remove(client)) {
                Notify.debug("Connected clients: " + connected_clients);
                max_connections.signal(); // Notify one waiting thread that a client has disconnected.
            }
        } finally {
            lock.unlock();
        }
    }

    public void addAccount(Account account) {
        lock.lock();
        try {
            accounts.add(account);
            Notify.debug("Accounts: " + accounts);
        } finally {
            lock.unlock();
        }
    }

    public void removeAccount(Account account) {
        lock.lock();
        try {
            accounts.remove(account);
            Notify.debug("Accounts: " + accounts);
        } finally {
            lock.unlock();
        }
    }

    public int getLoggedClients() {
        lock.lock();
        int logged_clients = 0;
        try {
            for (ConnectedClient client : connected_clients) {
                if (client.isLoggedIn()) {
                    logged_clients++;
                }
            }
            return logged_clients;
        } finally {
            lock.unlock();
        }
    }

    public boolean checkAccount(Account account) {
        lock.lock();
        try {
            while (getLoggedClients() >= CmdProtocol.MAX_LOGGED_CLIENTS && CmdProtocol.MAX_LOGGED_CLIENTS != 0) {
                Notify.debug("Maximum connections reached. Waiting for a client to disconnect...");
                max_connections.await(); // Wait until a client disconnects.
            }
            for (Account a : accounts) {
                if (a.equals(account)) {
                    return true;
                }
            }
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status.
            Notify.error("Thread interrupted while waiting to add client.");
            return false;
        } finally {
            lock.unlock();
        }
    }

    public Account getAccount(String username, String password) {
        lock.lock();
        try {
            for (Account a : accounts) {
                if (a.equals(username, password)) {
                    return a;
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }
}
