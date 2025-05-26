package khoroshkin.practice2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.ThreadLocalRandom.current;

public class Consumer implements Runnable {
    private final int TIMEOUT = 3;
    private final BlockingQueue<Message> queue;

    public Consumer(BlockingQueue<Message> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(current().nextInt(TIMEOUT));
            out.println(currentThread().getName() + " consumed: " + queue.take().getMessage());
        } catch (InterruptedException e) {
                currentThread().interrupt();
        }
    }
}