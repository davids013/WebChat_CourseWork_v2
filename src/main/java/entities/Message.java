package entities;

import java.time.LocalDateTime;
import java.util.Objects;

public class Message {
    private final LocalDateTime sentTime;
    private final String author;
    private final String text;

    public Message(String author, String text) {
        sentTime = LocalDateTime.now();
        this.author = author;
        this.text = text;
    }

    public LocalDateTime getSentTime() {
        return sentTime;
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Message message = (Message) o;
        return author.equals(message.author)
                && Objects.equals(text, message.text)
                && sentTime.equals(message.sentTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, text, sentTime);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Message{");
        sb.append("SENT_TIME=").append(sentTime)
                .append(", AUTHOR='").append(author).append('\'')
                .append(", text='").append(text).append('\'')
                .append('}');
        return sb.toString();
    }
}
