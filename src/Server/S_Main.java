package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import Shared.Terminal;
import Shared.CmdProtocol;

public class S_Main {
    private static ServerSocket ss;
    private Socket socket;
    private BufferedReader socket_in;
    private PrintWriter socket_out;

    public S_Main(int port) throws IOException {
        ss = new ServerSocket(port);
        socket = ss.accept();
        socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socket_out = new PrintWriter(socket.getOutputStream());
    }
    
    public void start() throws IOException {
        while (true) {
            String line;
            while ((line = socket_in.readLine()) != null) {
                // On user connect, ask for username
                if (line.equals(CmdProtocol.CONNECT)) {
                    send_client(CmdProtocol.LOGIN_REQ);
                    continue;
                }
                // On user login, send welcome message
                if (line.startsWith(CmdProtocol.LOGIN)) {
                    String username = line.split(":")[1];
                    send_client("Welcome, " + username + "!");
                    System.out.println(Terminal.ANSI_GREEN + "User " + username + " connected" + Terminal.ANSI_RESET);
                    continue;
                }
            }

            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();
            ss.close();
        }
    }

    private void send_client(String message) {
        socket_out.println(message);
        socket_out.flush();
    }
    
    public static void main() throws IOException {
        Integer port = 8888;
        S_Main server = new S_Main(port);
        String addr = ss.getInetAddress().getHostAddress();
        System.out.println(Terminal.ANSI_GREEN + "Server started at " + addr + ":" + port + Terminal.ANSI_RESET);
        server.start();
    }
}
