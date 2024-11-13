package Shared;

public class ServerAccount {
    private String username;
    private String password;
    private Integer permissionLevel; // 0: read; 1: write; 2: read/write

    public ServerAccount(String username, String password, Integer permissionLevel) {
        this.username = username;
        this.password = password;
        this.permissionLevel = permissionLevel;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Integer getPermissionLevel() {
        return permissionLevel;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPermissionLevel(Integer permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public String toString() {
        return "Username: " + username + ", Password: " + password + ", Permission Level: " + permissionLevel;
    }
    
    public boolean equals(ServerAccount account) {
        return this.username.equals(account.getUsername()) && this.password.equals(account.getPassword());
    }

    public boolean equals(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
}
