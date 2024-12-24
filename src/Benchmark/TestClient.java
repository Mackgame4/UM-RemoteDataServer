package Benchmark;

import java.net.Socket;
import java.util.List;

import Shared.CmdProtocol;
import Shared.Notify;
import Shared.FramedConnection;
import Client.Demultiplexer;

public class TestClient {
    private static int thread_request_tag = CmdProtocol.REQUEST_TAG;

    public static void incrementTag() {
        thread_request_tag += 2;
    }

    public static boolean run(List<String> commands) {
        try {
            Socket s = new Socket(CmdProtocol.LOCAL_IP, CmdProtocol.PORT);
            Demultiplexer m = new Demultiplexer(new FramedConnection(s));
            m.start();
            m.sendBytes(CmdProtocol.ONE_WAY_TAG, CmdProtocol.CONNECT);
            for (String command : commands) {
                //System.out.println("Sending command: " + command + " with tag: " + thread_request_tag);
                m.sendBytes(thread_request_tag, command);
                // wait for response of thread_request_tag from server
                byte[] data = m.receive(thread_request_tag);
                String response = new String(data);
                Notify.debug("Received response: " + response);
                incrementTag();
            }
            m.shutdown();
            Notify.notify("error", "TestClient exiting...");
            m.close();
            s.close();
        } catch (Exception e) {
            Notify.error("Subprocess error: " + e.getMessage());
            return false;
        }
        return true;
    }
}