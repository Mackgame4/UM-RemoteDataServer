package Server;

import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;
import java.util.HashMap;

public class DataManager {
    private static Map<String, String> data = new HashMap<>();
    private static ReentrantLock lock = new ReentrantLock();
}
