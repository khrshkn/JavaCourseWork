package khoroshkin.practice1;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Program {

  private static final int TIME_WAITING = 2;
  private static State state = State.UNKNOWN;
  private static boolean stateHandled = true;

  private static final Object lock = new Object();
  private static Thread abstractProgram;

  static class AbstractProgram implements Runnable {

    private void someWork() {
      // Имитация работы
    }

    private State generateNewState() {
      List< State > availableState = Arrays.asList(State.RUNNING, State.STOPPING, State.FATAL_ERROR);
      State newState = availableState.get(new Random().nextInt(availableState.size()));
      return newState;
    }

    @Override
    public void run() {
      Thread daemonThread = new Thread(() -> {
        while (!currentThread().isInterrupted()) {
          try {
            SECONDS.sleep(TIME_WAITING);
          } catch (InterruptedException e) {
            currentThread().interrupt();
          }

          synchronized (lock) {
            if (stateHandled) {
              State oldState = state;
              state = generateNewState();
              out.println("Program status went from " + oldState.toString() + " to " + state.toString() + " (changed by daemon)");
              stateHandled = false;
              lock.notify();
            }
          }
        }
      });
      daemonThread.setDaemon(true);
      daemonThread.start();

      while (!currentThread().isInterrupted()) {
        someWork();
      }
    }
  }

  static class Supervisor implements Runnable {
    @Override
    public void run() {
      out.println("Supervisor started the program\n");
      abstractProgram = new Thread(new AbstractProgram());
      out.println("Current program status:" + state);
      abstractProgram.start();
      while (!abstractProgram.isInterrupted()) {
        synchronized (lock) {
          try {
            lock.wait();
          } catch (InterruptedException e) {
            currentThread().interrupt();
            break;
          }
          if (state == State.FATAL_ERROR && !stateHandled) {
            stopProgram();
          }
          else if ((state == State.UNKNOWN || state == State.STOPPING) && !stateHandled) {
            runProgram();
          }
          else {
            out.println("Supervisor did nothing\n");
          }
          stateHandled = true;
          out.println("Current program status:" + state);
        }
      }
    }

    private static void runProgram() {
      state = State.RUNNING;
      out.println("Supervisor restarted the program\n");
    }

    private static void stopProgram() {
      abstractProgram.interrupt();
      out.println("Supervisor stopped the program\n");
    }
  }
}