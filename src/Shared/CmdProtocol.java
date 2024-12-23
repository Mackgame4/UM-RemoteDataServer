package Shared;

public class CmdProtocol {
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

    // 0 - one way, odd - request, even - stream of messages
    public static final int ONE_WAY_TAG = 0;
    public static final int REQUEST_TAG = 1;
    public static final int STREAM_TAG = 2;

    public static String[] parse(String line) {
        int index = line.indexOf(" ");
        String command;
        String args;
        if (index != -1) {
            command = line.substring(0, index).trim(); // Text before the first index, trimmed
            args = line.substring(index + 1).trim(); // Text after the first index, trimmed
        } else {
            command = line.trim(); // No index found, entire line is the command
            args = ""; // No arguments
        }
        return new String[] {command, args};
    }
}
