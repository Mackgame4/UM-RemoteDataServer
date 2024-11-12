package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import Shared.Terminal;
import Shared.CmdProtocol;

public class C_Main {
    private Socket socket;
    private BufferedReader socket_in;
    private PrintWriter socket_out;
    private BufferedReader in;

    public C_Main(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
        socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socket_out = new PrintWriter(socket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(System.in));
    }

    public void start() throws IOException {
        send_server(CmdProtocol.CONNECT); // Send connect request message to server
        String response;
        while ((response = socket_in.readLine()) != null) {
            if (response.equals(CmdProtocol.LOGIN_REQ)) {
                System.out.print("Enter username: ");
                String username = in.readLine();
                send_server(CmdProtocol.LOGIN + ":" + username);
                continue;
            }
        }

        socket.shutdownOutput();
        socket.shutdownInput();
        socket.close();
    }

    private void send_server(String message) {
        socket_out.println(message);
        socket_out.flush();
    }

    public static void main() throws IOException {
        String addr = "localhost";
        Integer port = 8888;
        C_Main client = new C_Main(addr, port);
        System.out.println(Terminal.ANSI_GREEN + "Client started at " + addr + ":" + port + Terminal.ANSI_RESET);
        client.start();
    }
}
