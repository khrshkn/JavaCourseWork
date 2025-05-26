package khoroshkin.practice2;

import java.util.concurrent.ThreadFactory;

public class MyThreadFactory implements ThreadFactory {
    private final String name;
    private int counter;

    public MyThreadFactory(String name) {
        this.name = name;
        this.counter = 0;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, name + counter++);
    }
}