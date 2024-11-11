package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import Shared.Terminal;

public class S_Main {
    static ServerSocket ss;

    public S_Main(int port) throws IOException {
        ss = new ServerSocket(port);
    }
    
    public void start() throws IOException {
        while (true) {
            Socket socket = ss.accept();

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            String line;
            while ((line = in.readLine()) != null) {
                out.println(line);
                out.flush();
            }

            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();
            ss.close();
        }
    }
    
    public static void main() throws IOException {
        Integer port = 8888;
        S_Main server = new S_Main(port);
        String addr = ss.getInetAddress().getHostAddress();
        System.out.println(Terminal.ANSI_GREEN + "Server started at " + addr + ":" + port + Terminal.ANSI_RESET);
        server.start();
    }
}
