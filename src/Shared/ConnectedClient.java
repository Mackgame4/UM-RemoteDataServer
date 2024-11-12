package Shared;

public class ConnectedClient {
    public int id;
    protected String ip;
    protected int port;
    private ServerAccount account;

    public ConnectedClient(int id, String ip, int port, ServerAccount account) {
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

    public ServerAccount getAccount() {
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

    public void setAccount(ServerAccount account) {
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