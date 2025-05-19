package khoroshkin.coursework;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int numThreads = 0, timeout = 0;
        String[] services = new String[0];
        String format = "";
        try {
            if (args.length < 1) {
                throw new IllegalArgumentException("Required format: java -cp src/main/java khoroshkin.coursework.Main <threads> <timeout> <service1> [service2...] <format>\n" +
                        "Example: java -cp src/main/java khoroshkin.coursework.Main 2 60 spotify assembly json");
            }
            numThreads = Integer.parseInt(args[0]);
            timeout = Integer.parseInt(args[1]);
            if (numThreads <= 0 || timeout <= 0) {
                throw new IllegalArgumentException("Threads and timeout must be greater than 0");
            }

            services = Arrays.copyOfRange(args, 2, args.length - 1);
            format = args[args.length - 1];
            if (services.length == 0) {
                throw new IllegalArgumentException("Services must have at least one service");
            }

            if (!format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("csv")) {
                throw new IllegalArgumentException("Format must be either \"json\" or \"csv\"");
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        APIScraper scraper = new APIScraper(numThreads, timeout, services, format);
        scraper.start();
    }
}
