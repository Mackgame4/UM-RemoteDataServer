import java.util.Scanner;

import Shared.Terminal;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(Terminal.ANSI_YELLOW + "1. " + Terminal.ANSI_RESET + "Run Server");
        System.out.println(Terminal.ANSI_YELLOW + "2. " + Terminal.ANSI_RESET + "Run Client");
        System.out.println(Terminal.ANSI_YELLOW + "0. " + Terminal.ANSI_RESET + "Exit");
        System.out.print(Terminal.ANSI_YELLOW + "Choose an option: " + Terminal.ANSI_RESET);
        try {
            int option = scanner.nextInt();
            switch (option) {
                case 1:
                    System.out.println(Terminal.ANSI_GREEN + "Running Server..." + Terminal.ANSI_RESET);
                    // TODO: Run server
                    main(args);
                    break;
                case 2:
                    System.out.println(Terminal.ANSI_GREEN + "Running Client..." + Terminal.ANSI_RESET);
                    // TODO: Run client
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

    public static void exit(int status) {
        System.exit(status);
    }
}