package server;

import entities.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ServerTask implements Runnable {
    private final SocketChannel client;
    private final String COLOR = WebServer.COLOR;
    private final String LOG_SEPARATOR = FileLogger.LINE_SEPARATOR;
    private final String usersLogFile = WebServer.USERS_STORAGE_PATH;
    private final Logger usersLogger;
    private final String requestLogFile = WebServer.SERVER_LOG_PATH;
    private final Logger requestLogger;

    public ServerTask(SocketChannel client) {
        this.client = client;
        usersLogger = new FileLogger(usersLogFile, false);
        requestLogger = new FileLogger(requestLogFile, true);
    }

    @Override
    public void run() {
        try {
            WebServer.clientList.add(client);
            System.out.println(COLOR + Thread.currentThread().getName() +
                    " Установлено соединение. Онлайн " + WebServer.online.incrementAndGet());
            while (client.isConnected()) {
                final ByteBuffer buf = ByteBuffer.allocate(2 << 20);
                System.out.println(COLOR + "Сервер ожидает запроса");
                int bytes = 0;
                bytes = client.read(buf);
                if (bytes == -1) {
                    System.out.println(COLOR + "Разорвано соединение. Онлайн " + WebServer.online.decrementAndGet());
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

//                    client.write(ByteBuffer.wrap(requestStr.getBytes(StandardCharsets.UTF_8)));

                    final Request request = Serializer.deserialize(requestStr, Request.class);
                    final Commands command = request.getCommand();
                    if (Commands.EXIT.equals(command)) {
                        System.out.println(COLOR + "Разорвано соединение. Онлайн " + WebServer.online.decrementAndGet());
                        break;
                    }
                    final String body = request.getBody();
                    switch (command) {
                        case REGISTER_USER:
                            result = registerUser(body);
                            final StringBuilder sbr = new StringBuilder();
                            WebServer.getUsers().values().forEach(user ->
                                    sbr.append(Serializer.serialize(user)).append(LOG_SEPARATOR));
                            sbr.delete(sbr.lastIndexOf(LOG_SEPARATOR), sbr.length());
                            usersLogger.log(sbr.toString());
                            break;
                        case SEND_MESSAGE:
                            result = sendMessage(Serializer.deserialize(body, Message.class));
                            final Message message = Serializer.deserialize(body, Message.class);
                            WebServer.chatList.add(message);
//                            client.write(ByteBuffer.wrap(requestStr.getBytes(StandardCharsets.UTF_8)));

//                            final StringBuilder sbs = new StringBuilder();
//                            WebServer.getUsers().values().forEach(user ->
//                                    sbs.append(Serializer.serialize(user)).append(LOG_SEPARATOR));
//                            sbs.delete(sbs.lastIndexOf(LOG_SEPARATOR), sbs.length());
//                            usersLogger.log(sbs.toString());
                            break;
                        default:
                            result = "Ошибка. Неизвестный запрос";
                    }
                } else result = "<!!!> " + requestStr;

                WebServer.sendToAll(requestStr);

                System.out.println(COLOR + "Сервер отправляет ответ:\t" + result);
//                client.write(ByteBuffer.wrap(result.getBytes(StandardCharsets.UTF_8)));
//                client.write(ByteBuffer.wrap(requestStr.getBytes(StandardCharsets.UTF_8)));
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
        if (!WebServer.getUsers().containsKey(user.getName())) {
            WebServer.getUsers().put(name, user);
            return "Успешно. Пользователь с именем \"" + name + "" + "\" зарегистрирован";
        }
        return "Успешно. Пользователь с именем \"" + name + "" + "\" авторизован";
    }

    private String sendMessage(Message message) {
        final String authorName = message.getAuthor();
        if (!WebServer.getUsers().containsKey(authorName)) {
            return "Отказано. Отправитель \"" + authorName + "\" не состоит в чате";
        } else {
//            WebServer.getUsers().get(authorName).sendMessage(message);
//    TODO: вызывать добавление сообщения в лог чата
            return "Успешно. Ваше сообщение отправлено в чат";
        }
    }
}
