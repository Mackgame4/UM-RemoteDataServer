import Client.C_Main;
import Server.S_Main;
import Shared.Terminal;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        if (args.length >= 1) {
            if (args[0].equals("server")) {
                run_server();
            }
            if (args[0].equals("client")) {
                run_client();
            }
        } else {
            menu(args);
        }
    }

    public static void menu(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(Terminal.ANSI_YELLOW + "1. " + Terminal.ANSI_RESET + "Run Server");
        System.out.println(Terminal.ANSI_YELLOW + "2. " + Terminal.ANSI_RESET + "Run Client");
        System.out.println(Terminal.ANSI_YELLOW + "3. " + Terminal.ANSI_RESET + "Run New Process Server");
        System.out.println(Terminal.ANSI_YELLOW + "4. " + Terminal.ANSI_RESET + "Run New Process Client");
        System.out.println(Terminal.ANSI_YELLOW + "0. " + Terminal.ANSI_RESET + "Exit");
        System.out.print(Terminal.ANSI_YELLOW + "Choose an option: " + Terminal.ANSI_RESET);
        try {
            int option = scanner.nextInt();
            switch (option) {
                case 1:
                    run_server();
                    break;
                case 2:
                    run_client();
                    break;
                case 3:
                    new_shell("server");
                    main(args);
                    break;
                case 4:
                    new_shell("client");
                    main(args);
                    break;
                case 0:
                    System.out.println(Terminal.ANSI_RED + "Exiting..." + Terminal.ANSI_RESET);
                    exit(0);
                    break;
                default:
                    System.out.println(Terminal.ANSI_RED + "Invalid option!" + Terminal.ANSI_RESET);
                    break;
            }
        } catch (Exception e) {
            System.out.println(Terminal.ANSI_RED + "Invalid option!" + Terminal.ANSI_RESET);
        }
        scanner.close();
    }

    public static void run_server() throws InterruptedException {
        try {
            S_Main.main();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void run_client() {
        try {
            C_Main.main();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void new_shell(String type) {
        String os = System.getProperty("os.name").toLowerCase();
        ProcessBuilder processBuilder = null;
        try {
            if (os.contains("win")) {
                // Windows command to open a new command prompt window
                processBuilder = new ProcessBuilder("cmd", "/c", "start", "java", "-cp", "bin", "Main", type);
            } else if (os.contains("mac")) {
                // MacOS command to open a new terminal window
                processBuilder = new ProcessBuilder("osascript", "-e", "tell application \"Terminal\" to do script \"java -cp bin Main " + type + "\"");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
                // Linux command to open a new terminal window (using gnome-terminal)
                processBuilder = new ProcessBuilder("gnome-terminal", "--", "java", "-cp", "bin", "Main", type);
            }
            if (processBuilder != null) {
                processBuilder.inheritIO().start();
            } else {
                System.out.println(Terminal.ANSI_RED + "Unsupported OS!" + Terminal.ANSI_RESET);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }    

    public static void exit(int status) {
        System.exit(status);
    }
}