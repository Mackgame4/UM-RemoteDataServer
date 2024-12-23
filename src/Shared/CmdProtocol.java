package Shared;

public class CmdProtocol {
    public static final String LOCAL_IP = "0.0.0.0";
    public static final int PORT = 8888; 

    public static final String REGISTER = "register";
    public static final String LOGIN = "login";
    public static final String LOGOUT = "logout";
    public static final String CONNECT = "connect";
    public static final String EXIT = "exit";
    public static final String WHOAMI = "whoami";
    public static final String WRITE_FILE = "write";
    public static final String READ_FILE = "read";
    public static final String DELETE_FILE = "del";
    public static final String LIST_FILES = "ls";
    public static final String COMMAND_ERROR = "error";

    // 0 - one way, odd - request, even - stream of messages
    public static final int ONE_WAY_TAG = 0;
    public static final int REQUEST_TAG = 1;
    public static final int STREAM_TAG = 2;

    public static String build(String command, String... args) {
        return build(command, " ", args);
    }

    public static String build(String command, String delimiter, String... args) {
        StringBuilder sb = new StringBuilder(command);
        for (String arg : args) {
            sb.append(delimiter).append(arg);
        }
        return sb.toString();
    }

    public static Object[] parse(String line) {
        return parse(line, " ");
    }

    public static Object[] parse(String line, String delimiter) {
        line = line.trim(); // Remove leading/trailing whitespace
        int index = line.indexOf(delimiter); // Find the first index of the delimiter
        String command;
        String[] args;
        if (index != -1) {
            command = line.substring(0, index).trim(); // Text before the first index, trimmed
            args = line.substring(index + 1).trim().split("\\s+"); // Split remaining text by whitespace
        } else {
            command = line; // No index found, entire line is the command
            args = new String[0]; // No arguments
        }
        return new Object[] {command, args};
    }    
}
