package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.InputStreamReader;
import java.util.List;

import Shared.Terminal;
import Shared.CmdProtocol;
import Shared.FramedConnection;
import Shared.FramedConnection.Frame;
import Shared.Account;

public class ServerWorker implements Runnable {
    private Socket socket;
    private final FramedConnection c;

    public ServerWorker(Socket socket, FramedConnection c) {
        this.socket = socket;
        this.c = c;
    }

    @Override
    public void run() {
        try (c) {
            for (;;) {
                Frame frame = c.receive();
                int tag = frame.tag;
                String data = new String(frame.data);
                if (frame.tag == 0)
                    System.out.println("Got one-way: " + data);
                else if (frame.tag % 2 == 1) {
                    System.out.println("Replying to: " + data);
                    c.send(frame.tag, data.toUpperCase().getBytes());
                } else {
                    for (int i = 0; i < data.length(); ++i) {
                        String str = data.substring(i, i+1);
                        System.out.println("Streaming: " + str);
                        c.send(tag, str.getBytes());
                        Thread.sleep(100);
                    }
                    c.send(tag, new byte[0]);
                }
            }
        } catch (Exception ignored) { }
    }
}