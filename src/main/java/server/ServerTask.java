package server;

import entities.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ServerTask implements Runnable {
    private final SocketChannel client;
    private final String COLOR = WebServer.COLOR;
    private final String chatLogFile = WebServer.CHAT_LOG_PATH;
    private final Logger chatLogger;
    private final String requestLogFile = WebServer.SERVER_LOG_PATH;
    private final Logger requestLogger;

    public ServerTask(SocketChannel client) {
        this.client = client;
        chatLogger = new FileLogger(chatLogFile, true);
        requestLogger = new FileLogger(requestLogFile, true);
    }

    @Override
    public void run() {
        try {
            WebServer.clientList.add(client);
            System.out.println(COLOR +
                    "Установлено новое соединение. Онлайн " + WebServer.online.incrementAndGet());
            while (client.isConnected()) {
                final ByteBuffer buf = ByteBuffer.allocate(2 << 20);
                System.out.println(COLOR + "Сервер ожидает запроса");
                final int bytes;
                bytes = client.read(buf);
                if (bytes == -1) {
                    System.out.println(COLOR +
                            "Разорвано соединение. Онлайн " + WebServer.online.decrementAndGet());
                    break;
                }
                final String requestStr = new String(buf.array(), 0, bytes, StandardCharsets.UTF_8);
                buf.clear();
                System.out.println(COLOR + "Сервер получил запрос:\t" + requestStr);

                requestLogger.log(requestStr);

                final String result;
                if (requestStr.contains(Serializer.serialize(Commands.EXIT))
                        || requestStr.contains(Serializer.serialize(Commands.REGISTER_USER))
                        || requestStr.contains(Serializer.serialize(Commands.SEND_MESSAGE))) {

                    final Request request = Serializer.deserialize(requestStr, Request.class);
                    final Commands command = request.getCommand();
                    if (Commands.EXIT.equals(command)) {
                        System.out.println(COLOR +
                                "Разорвано соединение. Онлайн " + WebServer.online.decrementAndGet());
                        WebServer.users.remove(request.getBody());
                        WebServer.clientList.remove(client);
                        WebServer.sendToAll(requestStr);
                        break;
                    }
                    final String body = request.getBody();
                    switch (command) {
                        case REGISTER_USER:
                            result = registerUser(body);
                            WebServer.users.put(body, new User(body));
                            WebServer.sendChatListTo(client);
                            break;
                        case SEND_MESSAGE:
                            result = sendMessage(Serializer.deserialize(body, Message.class));
                            break;
                        default:
                            result = "Ошибка. Неизвестный запрос";
                    }
                } else result = "<!!!> " + requestStr;
                WebServer.sendToAll(requestStr);
                System.out.println(COLOR + "Результат обработки запроса:\t" + result);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String registerUser(String name) {
        final User user = new User(name);
        if (!WebServer.users.containsKey(user.getName())) {
            WebServer.users.put(name, user);
            return "Успешно. Пользователь с именем \"" + name + "" + "\" зарегистрирован";
        }
        return "Успешно. Пользователь с именем \"" + name + "" + "\" авторизован";
    }

    private String sendMessage(Message message) {
        final String authorName = message.getAuthor();
        if (!WebServer.users.containsKey(authorName)) {
            return "Отказано. Отправитель \"" + authorName + "\" не состоит в чате";
        } else {
            WebServer.chatList.add(message);
            WebServer.users.get(authorName).sendMessage(message);
            chatLogger.log(Serializer.serialize(message));
            return "Успешно. Сообщение отправлено в чат";
        }
    }
}
