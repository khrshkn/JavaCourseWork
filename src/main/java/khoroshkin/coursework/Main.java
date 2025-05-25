package khoroshkin.coursework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) {
        try {
            Arguments parsedArgs = processArguments(args);
            APIScraper scraper = new APIScraper(parsedArgs.numThreads, parsedArgs.timeout, parsedArgs.services, parsedArgs.format);
            Thread taskThread = new Thread(scraper);
            taskThread.start();
            TimeUnit.SECONDS.sleep(200);
            taskThread.interrupt();
        } catch (IllegalArgumentException | InterruptedException e) {
            logger.error("Invalid arguments provided", e);
            System.exit(1);
        }
    }

    static class Arguments {
        int numThreads;
        int timeout;
        String[] services;
        String format;

        Arguments(int numThreads, int timeout, String[] services, String format) {
            this.numThreads = numThreads;
            this.timeout = timeout;
            this.services = services;
            this.format = format;
        }
    }

    static Arguments processArguments(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException("Required format: java -cp src/main/java khoroshkin.coursework.Main <threads> <timeout> <service1> [service2...] <format>\n" +
                    "Example: java -cp src/main/java khoroshkin.coursework.Main 3 20 spotify assembly virustotal json");
        }
        int numThreads = Integer.parseInt(args[0]);
        int timeout = Integer.parseInt(args[1]);
        if (numThreads <= 0 || timeout <= 0) {
            throw new IllegalArgumentException("Threads and timeout must be integers and greater than 0");
        }
        String[] services = Arrays.copyOfRange(args, 2, args.length - 1);
        String format = args[args.length - 1];
        if (services.length == 0) {
            throw new IllegalArgumentException("Services must have at least one service");
        }
        if (!format.equalsIgnoreCase("json") && !format.equalsIgnoreCase("csv")) {
            throw new IllegalArgumentException("Format must be either \"json\" or \"csv\"");
        }
        return new Arguments(numThreads, timeout, services, format);
    }

}
