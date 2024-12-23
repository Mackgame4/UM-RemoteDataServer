package Shared;

import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FramedConnection implements AutoCloseable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private ReentrantLock send_lock;
    private ReentrantLock rec_lock;

    public static class Frame {
        public final int tag;
        public final byte[] data;
        public Frame(int tag, byte[] data) {
            this.tag = tag;
            this.data = data;
        }
    }

    public FramedConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(socket.getInputStream());
        this.out = new DataOutputStream(socket.getOutputStream());
        this.send_lock = new ReentrantLock();
        this.rec_lock = new ReentrantLock();
    }

    public void send(Frame frame) throws IOException {
        send(frame.tag, frame.data);
    }

    public void send(int tag, byte[] data) throws IOException {
        send_lock.lock();
        try {
            out.writeInt(tag);
            out.writeInt(data.length);
            out.write(data);
            out.flush();
        } finally {
            send_lock.unlock();
        }
    }

    public void sendBytes(int tag, String data) throws IOException {
        send(tag, data.getBytes());
    }

    public Frame receive() throws IOException {
        rec_lock.lock();
        try {
            int tag = in.readInt();
            int len = in.readInt();
            byte[] data = new byte[len];
            in.readFully(data);
            return new Frame(tag, data);
        } finally {
            rec_lock.unlock();
        }
    }

    public void close() throws IOException {
        socket.close();
    }
}    