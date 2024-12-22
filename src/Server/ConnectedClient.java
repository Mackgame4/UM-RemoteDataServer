package Server;

import Shared.Account;

public class ConnectedClient {
    public int id;
    protected String ip;
    protected int port;
    private Account account;

    public ConnectedClient(int id, String ip, int port, Account account) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.account = account;
    }

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public Account getAccount() {
        return account;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    
    public void setPort(int port) {
        this.port = port;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String toString() {
        return "Client ID: " + id + ", IP: " + ip + ", Port: " + port + ", Account: " + account;
    }

    public boolean equals(ConnectedClient client) {
        return this.id == client.getId() && this.ip.equals(client.getIp()) && this.port == client.getPort();
    }

    public boolean equals(int id, String ip, int port) {
        return this.id == id && this.ip.equals(ip) && this.port == port;
    }
}