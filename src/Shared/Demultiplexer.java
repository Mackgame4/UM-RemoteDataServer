package Shared;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import Shared.FramedConnection.Frame;

public class Demultiplexer implements AutoCloseable {
    private final FramedConnection conn;
    private final Map<Integer, Queue<byte[]>> buffers = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private boolean closed = false;

    public Demultiplexer(FramedConnection conn) {
        this.conn = conn;
    }

    public void start() {
        Thread readerThread = new Thread(() -> {
            try {
                while (true) {
                    Frame frame = conn.receive();
                    lock.lock();
                    try {
                        if (closed) break;
                        buffers.putIfAbsent(frame.tag, new ArrayDeque<>());
                        buffers.get(frame.tag).offer(frame.data);
                        notEmpty.signalAll();
                    } finally {
                        lock.unlock();
                    }
                }
            } catch (IOException e) {
                if (!closed) e.printStackTrace();
            } finally {
                try {
                    close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        readerThread.start();
    }

    public void send(Frame frame) throws IOException {
        conn.send(frame);
    }

    public void send(int tag, byte[] data) throws IOException {
        conn.send(tag, data);
    }

    public void sendBytes(int tag, String data) throws IOException {
        conn.send(tag, data.getBytes());
    }

    public byte[] receive(int tag) throws IOException, InterruptedException {
        lock.lock();
        try {
            while (!buffers.containsKey(tag) || buffers.get(tag).isEmpty()) {
                if (closed) throw new IOException("Demultiplexer is closed");
                notEmpty.await();
            }
            return buffers.get(tag).poll();
        } finally {
            lock.unlock();
        }
    }

    public void close() throws IOException {
        lock.lock();
        try {
            closed = true;
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
        conn.close();
    }
}