package entities;

import server.WebServer;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {
    private final String name;
    private final List<Message> incomingMessages = new ArrayList<>();
    private final List<Message> outgoingMessages = new ArrayList<>();
    private final String logFile;

    public User(String name) {
        this.name = name;
        logFile = WebServer.CHAT_LOG_DIRECTORY + WebServer.SEP + name + WebServer.LOG_EXTENSION;
    }

    public String getName() {
        return name;
    }

    public List<Message> getIncomingMessages() {
        return incomingMessages;
    }

    public List<Message> getOutgoingMessages() {
        return outgoingMessages;
    }

    public String getLogFile() {
        return logFile;
    }

    public User receiveMessage(Message message) {
        incomingMessages.add(message);
        addToChatLog(message, true);
        return this;
    }

    public User sendMessage(Message message) {
        outgoingMessages.add(message);
        addToChatLog(message, false);
        return this;
    }

//    TODO: изменить логику записи для единого чата (удалить targetUserField и т.д.)
    private void addToChatLog(Message message, boolean isIncoming) {
        final Logger logger = new FileLogger(logFile, true);
        final String lineSeparator = FileLogger.LINE_SEPARATOR;
        final String prefix = isIncoming ? "" : "\t\t\t\t\t";
        final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
        final String targetUserField =
                isIncoming ? "From: " + message.getAuthor() : "To: " + "Chat";
        final StringBuilder sb = new StringBuilder();
        sb
                .append(prefix)
                .append(message.getSentTime().format(formatter))
                .append(lineSeparator)
                .append(prefix)
                .append(targetUserField)
                .append(lineSeparator)
                .append(prefix)
                .append(message.getText())
                .append(lineSeparator);
        logger.log(sb.toString());
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
        return "User{" + "NAME='" + name + "', incomingMessages="
                + incomingMessages + ", outgoingMessages=" + outgoingMessages + "}";
    }
}
