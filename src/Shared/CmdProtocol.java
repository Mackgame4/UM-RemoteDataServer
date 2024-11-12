package Shared;

public class CmdProtocol {
    public static final String CONNECT = "connect";
    public static final String REGISTER = "register";
    public static final String LOGIN = "login";
    public static final String EXIT = "exit";
    public static final String CREATE_FILE = "create_file";
    public static final String DELETE_FILE = "delete_file";
    public static final String READ_FILE = "read_file";

    public static String[] parse(String line) {
        int index = line.indexOf(" ");
        String command;
        String args;
        if (index != -1) { // If index is found in the line
            command = line.substring(0, index).trim(); // Text before the first ":", trimmed
            args = line.substring(index + 1).trim(); // Text after the first index, trimmed
        } else {
            command = line.trim(); // No index found, entire line is the command
            args = ""; // No arguments
        }
        return new String[] {command, args};
    }
}
