import java.util.Scanner;

import Shared.Terminal;
import Client.C_Main;
import Server.S_Main;

public class Main {
    public static void main(String[] args) {
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
        System.out.println(Terminal.ANSI_YELLOW + "0. " + Terminal.ANSI_RESET + "Exit");
        System.out.print(Terminal.ANSI_YELLOW + "Choose an option: " + Terminal.ANSI_RESET);
        try {
            int option = scanner.nextInt();
            switch (option) {
                case 1:
                    run_server();
                    main(args);
                    break;
                case 2:
                    run_client();
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

    public static void run_server() {
        System.out.println(Terminal.ANSI_GREEN + "Running Server..." + Terminal.ANSI_RESET);
        S_Main.start();
    }

    public static void run_client() {
        System.out.println(Terminal.ANSI_GREEN + "Running Client..." + Terminal.ANSI_RESET);
        C_Main.start();
    }

    public static void exit(int status) {
        System.exit(status);
    }
}