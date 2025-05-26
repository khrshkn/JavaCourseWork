package khoroshkin.practice2;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.ThreadLocalRandom.current;

public class Producer implements Runnable {
    private final int TIMEOUT = 3;
    private final BlockingQueue<Message> queue;

    public Producer(BlockingQueue<Message> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        Message newMessage = new Message("Message", current().nextInt(500));
        try {
            TimeUnit.SECONDS.sleep(current().nextInt(TIMEOUT));
            queue.put(newMessage);
            out.println(currentThread().getName() + " produced: " + newMessage.getMessage());
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}