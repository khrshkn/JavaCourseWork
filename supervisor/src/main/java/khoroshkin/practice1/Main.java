package khoroshkin.practice1;

public class Main {
  public static void main(String[] args) {
    Thread mainThread = new Thread(new Program.Supervisor());
    mainThread.start();
  }
}