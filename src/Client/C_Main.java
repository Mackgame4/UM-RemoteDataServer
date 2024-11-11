package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import Shared.Terminal;

public class C_Main {
    private Socket socket;

    public C_Main(String ip, int port) throws IOException {
        socket = new Socket(ip, port);
    }

    public void start() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));

        String userInput;
        while ((userInput = systemIn.readLine()) != null) {
            out.println(userInput);
            out.flush();

            String response = in.readLine();
            System.out.println("Server response: " + response);
        }

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
