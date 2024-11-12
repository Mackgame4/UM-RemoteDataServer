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

    private void send_server(String message) {
        socket_out.println(message);
        socket_out.flush();
    }

    public void start() throws IOException {
        System.out.println(socket_in.readLine()); // Receive first message from server
        System.out.print(Terminal.ANSI_YELLOW + "$ " + Terminal.ANSI_RESET);
        String userInput;
        while ((userInput = in.readLine()) != null) {
            String[] input = CmdProtocol.parse(userInput);
            String command = input[0];
            String args = input[1];
            if (command.equals(CmdProtocol.EXIT)) {
                break;
            }
            
            System.out.println("Command: " + command + ", Args: " + args);
            send_server(userInput);

            String response = socket_in.readLine();
            System.out.println("Server response: " + response);

            System.out.print(Terminal.ANSI_YELLOW + "$ " + Terminal.ANSI_RESET);
        }

        // If Ctrl+C is pressed, or exit command is sent, close the socket
        send_server(CmdProtocol.EXIT);
        System.out.println(socket_in.readLine()); // Receive last message from server

        socket.shutdownOutput();
        socket.shutdownInput();
        socket.close();
    }

    public static void main() throws IOException {
        String addr = "localhost";
        Integer port = 8888;
        C_Main client = new C_Main(addr, port);
        System.out.println(Terminal.ANSI_GREEN + "Client started at " + addr + ":" + port + Terminal.ANSI_RESET);
        client.start();
    }
}
