package khoroshkin.practice2;

public class Message {
    private int id;
    private String message;

    public Message(String message, int id) {
        this.message = message;
        this.id = id;
    }

    public String getMessage() {
        return message + id;
    }
}
