package entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {
    private final String name;
    private final List<Message> outgoingMessages = new ArrayList<>();

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Message> getOutgoingMessages() {
        return outgoingMessages;
    }

    public User sendMessage(Message message) {
        outgoingMessages.add(message);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final User user = (User) o;
        return name.equals(user.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "User{" + "NAME='" + name +
//                "', incomingMessages=" + incomingMessages +
                ", outgoingMessages=" + outgoingMessages + "}";
    }
}
