package entities;

public class Request {
    private final Commands command;
    private final String body;

    public Request(Commands command, String body) {
        this.command = command;
        this.body = body;
    }

    public Commands getCommand() {
        return command;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "Request{command=" + command + ", body=" + body + "}";
    }
}
