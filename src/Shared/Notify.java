package Shared;

public class Notify {
    public static final boolean Debug = true;

    public static void notify(String type, String title, String message) {
        System.out.print("\r"); // Move cursor to the beginning of the line
        if (type.equals("error")) {
            System.out.println(Terminal.ANSI_RED_BACKGROUND + title + Terminal.ANSI_RESET + " " + Terminal.ANSI_RED + message + Terminal.ANSI_RESET);
        } else if (type.equals("success")) {
            System.out.println(Terminal.ANSI_GREEN_BACKGROUND + title + Terminal.ANSI_RESET + " " + Terminal.ANSI_GREEN + message + Terminal.ANSI_RESET);
        } else if (type.equals("warning")) {
            System.out.println(Terminal.ANSI_YELLOW_BACKGROUND + title + Terminal.ANSI_RESET + " " + Terminal.ANSI_YELLOW + message + Terminal.ANSI_RESET);
        } else if (type.equals("info")) {
            System.out.println(Terminal.ANSI_BLUE_BACKGROUND + title + Terminal.ANSI_RESET + " " + Terminal.ANSI_BLUE + message + Terminal.ANSI_RESET);
        } else if (type.equals("debug")) {
            if (!Debug) {
                return;
            }
            System.out.println(Terminal.ANSI_CYAN_BACKGROUND + title + Terminal.ANSI_RESET + " " + Terminal.ANSI_CYAN + message + Terminal.ANSI_RESET);
        } else {
            System.out.println(title + " " + message);
        }
    }

    public static void notify(String type, String message) {
        notify(type, "Notify:", message);
    }

    public static void info(String message) {
        notify("info", "Info:", message);
    }

    public static void error(String message) {
        notify("error", "Error:", message);
    }

    public static void success(String message) {
        notify("success", "Success:", message);
    }

    public static void warning(String message) {
        notify("warning", "Warning:", message);
    }

    public static void debug(String message) {
        notify("debug", "Debug:", message);
    }
}