package Server;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import Shared.Notify;
import Shared.Account;

public class AccountManager {
    private List<Account> accounts;
    private List<ConnectedClient> connected_clients;
    private static ReentrantLock lock = new ReentrantLock();
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
            connected_clients.remove(client);
            Notify.debug("Connected clients: " + connected_clients);
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

    public boolean checkAccount(Account account) {
        lock.lock();
        try {
            for (Account a : accounts) {
                if (a.equals(account)) {
                    return true;
                }
            }
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
