package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import Shared.Terminal;
import Shared.CmdProtocol;
import Shared.Notify;
import Shared.FramedConnection;
import Shared.Demultiplexer;

public class C_Main {
    public static void main() throws IOException {
        try {
            Socket s = new Socket("0.0.0.0", 8888);
            BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));
            Notify.info("Connected to server ("+s.getLocalAddress()+":"+s.getLocalPort()+") at "+s.getInetAddress()+":"+s.getPort());
            Demultiplexer m = new Demultiplexer(new FramedConnection(s));
            m.start();
            /*Thread[] threads = {
                new Thread(() -> {
                    try  {
                        // send request
                        m.send(1, "Ola".getBytes());
                        Thread.sleep(100);
                        // get reply
                        byte[] data = m.receive(1);
                        System.out.println("(1) Reply: " + new String(data));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                })
            };
            for (Thread t : threads) {
                t.start();
            }
            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/
            System.out.print(Terminal.ANSI_YELLOW + "$ " + Terminal.ANSI_RESET);
            String userInput;
            while ((userInput = systemIn.readLine()) != null) {
                if (userInput.equals(CmdProtocol.EXIT)) {
                    Notify.notify("error", "Exiting...");
                    break;
                }
                m.sendBytes(0, userInput);
                System.out.print(Terminal.ANSI_YELLOW + "$ " + Terminal.ANSI_RESET);
            }
            m.close();
            s.close();
        } catch (Exception e) {
            Notify.error(e.getMessage());
        }
    }
}
