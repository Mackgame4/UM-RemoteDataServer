package Server;

import Shared.Notify;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DataManager {
    private static final Map<String, String> data = new HashMap<>();
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Condition conditionMet = lock.newCondition();

    public static void put(String key, String value) {
        lock.lock();
        try {
            Notify.debug("Updating key: " + key + " with value: " + value);
            data.put(key, value);
            Notify.debug("Notifying waiting threads...");
            conditionMet.signalAll(); // Notifica todas as threads aguardando
        } finally {
            lock.unlock();
        }
    }
    
    

    public static String get(String key) {
        lock.lock();
        try {
            return data.get(key);
        } finally {
            lock.unlock();
        }
    }

    public static void remove(String key) {
        lock.lock();
        try {
            data.remove(key);
            conditionMet.signalAll(); // Notifica mudanças na estrutura de dados
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
            conditionMet.signalAll(); // Notifica mudanças após múltiplas inserções
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
            conditionMet.signalAll(); // Notifica mudanças após múltiplas remoções
        } finally {
            lock.unlock();
        }
    }

    public static byte[] getWhen(String key, String keyCond, byte[] valueCond) {
    lock.lock();
    try {
        while (true) {
            Notify.debug("Checking condition for keyCond: " + keyCond + ", valueCond: " + new String(valueCond));
            String conditionValue = data.get(keyCond);
            if (conditionValue != null && conditionValue.equals(new String(valueCond))) {
                Notify.debug("Condition satisfied for keyCond: " + keyCond + " with value: " + conditionValue);
                String resultValue = data.get(key);
                Notify.debug("Returning value for key: " + key + " -> " + resultValue);
                return resultValue != null ? resultValue.getBytes() : null;
            }
            Notify.debug("Condition not satisfied, waiting...");
            conditionMet.await(); // Bloqueia até que a condição seja notificada
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        Notify.error("Thread interrupted while waiting.");
        return null;
    } finally {
        lock.unlock();
    }
}
  
}
