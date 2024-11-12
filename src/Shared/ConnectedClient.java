package Shared;

public class ConnectedClient {
    private String username;
    private String password;
    public int id;
    protected String ip;
    protected int port;

    public ConnectedClient(String username, String password, int id, String ip, int port) {
        this.username = username;
        this.password = password;
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
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

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String toString() {
        return "Client ID: " + id + ", Username: " + username + ", Password: " + password + ", IP: " + ip + ", Port: " + port;
    }
}