package khoroshkin.practice2;

import java.util.concurrent.*;

import static java.lang.System.out;

public class Main {
    public static void main(String[] args) {
        int numThreads = 0;
        try {
            if (args.length < 1) {
                throw new IllegalArgumentException("Required argument: number of threads");
            }
            numThreads = Integer.parseInt(args[0]);
            if (numThreads <= 0) {
                throw new IllegalArgumentException("Number of threads must be a positive integer");
            }
        } catch (NumberFormatException e) {
            System.err.println("Number of threads must be a positive integer");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        out.println("N = " + numThreads);
        BlockingQueue<Message> queue = new LinkedBlockingQueue<>(numThreads);
        ThreadFactory producerThreadFactory = new MyThreadFactory("Producer-");
        ThreadFactory consumerThreadFactory = new MyThreadFactory("Consumer-");
        ExecutorService writeService = Executors.newFixedThreadPool(numThreads, producerThreadFactory);
        ExecutorService readService = Executors.newFixedThreadPool(numThreads, consumerThreadFactory);
        for (int i = 0; i < numThreads; i++) {
            writeService.execute(new Producer(queue));
            readService.execute(new Consumer(queue));
        }

        writeService.shutdown();
        readService.shutdown();
    }
}