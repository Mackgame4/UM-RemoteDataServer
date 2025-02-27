package Shared;

public class Account {
    private String username;
    private String password;

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toString() {
        return "Username: " + username + ", Password: " + password;
    }
    
    public boolean equals(Account account) {
        return this.username.equals(account.getUsername()) && this.password.equals(account.getPassword());
    }

    public boolean equals(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }
}
