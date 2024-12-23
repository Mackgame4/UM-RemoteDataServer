package Server;

import java.util.concurrent.locks.ReentrantLock;
import java.util.Map;
import java.util.HashMap;

public class DataManager {
    private Map<String, String> data = new HashMap<>();
    private static ReentrantLock lock = new ReentrantLock();

    public void put(String key, String value) {
        lock.lock();
        try {
            data.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    public String get(String key) {
        lock.lock();
        try {
            return data.get(key);
        } finally {
            lock.unlock();
        }
    }

    public void remove(String key) {
        lock.lock();
        try {
            data.remove(key);
        } finally {
            lock.unlock();
        }
    }

    public Map<String, String> getAll() {
        lock.lock();
        try {
            return new HashMap<>(data);
        } finally {
            lock.unlock();
        }
    }

    public void multiPut(Map<String, byte[]> pairs) {
        lock.lock();
        try {
            for (Map.Entry<String, byte[]> pair : pairs.entrySet()) {
                data.put(pair.getKey(), new String(pair.getValue()));
            }
        } finally {
            lock.unlock();
        }
    }

    public Map<String, String> multiGet(String[] keys) {
        lock.lock();
        try {
            Map<String, String> result = new HashMap<>();
            for (String key : keys) {
                result.put(key, data.get(key));
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    public void multiRemove(String[] keys) {
        lock.lock();
        try {
            for (String key : keys) {
                data.remove(key);
            }
        } finally {
            lock.unlock();
        }
    }

    public byte[] getWhen(String key, String keyCond, byte[] valueCond) {
        lock.lock();
        try {
            String value = data.get(keyCond);
            if (value != null && value.equals(new String(valueCond))) {
                return data.get(key).getBytes();
            }
            return null;
        } finally {
            lock.unlock();
        }
    }
}
