package client;

import entities.*;
import entities.ConfigWorker;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Scanner;

public class WebClient {
//    private static final byte SESSION_MESSAGES = 5;
    private static final char SEP = File.separatorChar;
    private static final String OUT_COLOR = "\033[34m";
    private static final String IN_COLOR = "\033[35m";
    private static final String SYS_COLOR = "\033[0m";
    private static final String CURSOR = ">> ";
    private static final String CHAT_OUT_PREFIX = "\t\t\t\t\t\t\t\t\t";
    private static final String CHAT_IN_PREFIX = "\t";
    private static final String EXIT_WORD_EN = "exit";
    private static final String EXIT_WORD_RU = "учше";
    private static final String SETTINGS_FILE_PATH = "src" + SEP + "main" + SEP + "resources" + SEP + "settings.txt";
    private final static String HOST = ConfigWorker.getHostAndPortFromConfig(SETTINGS_FILE_PATH)[ConfigWorker.HOST_INDEX];
    private final static int PORT =
            Integer.parseInt(ConfigWorker.getHostAndPortFromConfig(SETTINGS_FILE_PATH)[ConfigWorker.PORT_INDEX]);
    private static String userName;

    public static void start() {
//        System.out.println(OUT_COLOR + "Клиент " + Thread.currentThread().getName()
//                + " подключается к серверу " + HOST + " (порт " + PORT + ")...");

        final Scanner sc = new Scanner(System.in);
        final InetSocketAddress address = new InetSocketAddress(HOST, PORT);
        try (final SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(address);
            final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 20);

            String input = null;
//            String userName = null;
            Request request = null;
//            int counterInt = 0;
            while (true) {
                if (request == null) {
                    Thread.sleep(750); // Костыль для правильной очередности печати в консоль
                    System.out.print(SYS_COLOR +
                            "Введите имя пользователя (`" + EXIT_WORD_EN + "` для отказа):\n" + CURSOR);
                    input = sc.nextLine();
                    request = requestAccess(input);
                    userName = request.getBody();
                    Thread.currentThread().setName(userName);
                } else {
                    System.out.print(SYS_COLOR + CURSOR);
                    input = sc.nextLine();
                    request = requestSendMessage(input);
//                    socketChannel.write(ByteBuffer.wrap(
//                            Serializer.serialize(request).getBytes(StandardCharsets.UTF_8)));
                }
                final String requestStr = Serializer.serialize(request);
//                System.out.println(COLOR + "Клиент " + Thread.currentThread().getName()
//                        + " отсылает запрос:\t" + requestStr);
                socketChannel.write(ByteBuffer.wrap(requestStr.getBytes(StandardCharsets.UTF_8)));
                Thread.sleep(500);      // Костыль, чтобы последнее сообщение выводилось перед следующим вводом
                if (request.getCommand() == Commands.EXIT) break;

                int bytesCount = -1;
//                try {
//                    bytesCount = socketChannel.read(inputBuffer);
//                } catch (IOException ignored) { }
                if ((bytesCount = socketChannel.read(inputBuffer)) >= 0) {
//                while ((bytesCount = socketChannel.read(inputBuffer)) > 0) {
//                    System.out.println(bytesCount);
                    input = new String(inputBuffer.array(), 0, bytesCount, StandardCharsets.UTF_8).trim();
                    try {
                        final String requestsSep = "\"command\":";
//                        System.out.println(input);
                        String[] inputArray = input.split(requestsSep);
                        if (inputArray.length <= 2) {
//                            System.out.println("one request");
                            request = Serializer.deserialize(input, Request.class);
                            analyzeRequest(request);
                        } else {
//                            System.out.println("many requests");
                            for (int i = 1; i < inputArray.length; i++) {
                                inputArray[i] = (char)123 + requestsSep + inputArray[i];
                                if (i < inputArray.length - 1)
                                    inputArray[i] = inputArray[i].substring(0, inputArray[i].lastIndexOf((char)123));
//                                System.out.println(inputArray[i]);
                                request = Serializer.deserialize(inputArray[i], Request.class);
                                analyzeRequest(request);
                            }
                        }
                    } catch (Exception e) {
//                        e.printStackTrace();
                        System.out.println("Serializer.deserialize() exception");
                        e.printStackTrace();
                    }
//                    System.out.println(OUT_COLOR + "Ответ сервера получен:\t" +
//                            new String(inputBuffer.array(), 0, bytesCount, StandardCharsets.UTF_8).trim());
                }
                inputBuffer.clear();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
//        System.out.println(COLOR + "Клиент " + Thread.currentThread().getName() + " завершил работу" + "\033[0m");
        System.out.println(OUT_COLOR + "Вы покинули чат" + "\033[0m");
    }

    private static void analyzeRequest(Request request) {
        switch (request.getCommand()) {
            case REGISTER_USER:
                System.out.println(SYS_COLOR + "Пользователь \"" + request.getBody() + "\" вошёл в чат");
                break;
            case EXIT:
                System.out.println(SYS_COLOR + "Пользователь \"" + request.getBody() + "\" покинул чат");
                break;
            case SEND_MESSAGE:
                final Message message = Serializer.deserialize(request.getBody(), Message.class);
                final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
                final String time = message.getSentTime().format(formatter);
                final String prefix;
                final String color;
                if (userName.equals(message.getAuthor())) {
                    prefix = CHAT_OUT_PREFIX;
                    color = OUT_COLOR;
                } else {
                    prefix = CHAT_IN_PREFIX;
                    color = IN_COLOR;
                }
                System.out.println(SYS_COLOR +
                        prefix + message.getAuthor() + " (" + time + ")" + "\n" +
                        color + prefix + "\t" + message.getText());
                break;
        }
    }

    private static Request requestAccess(String text) {
        if (EXIT_WORD_EN.equalsIgnoreCase(text.trim())
                || EXIT_WORD_RU.equalsIgnoreCase((text).trim()))
            return new Request(Commands.EXIT, userName);
        return new Request(Commands.REGISTER_USER, text);
    }

    private static Request requestSendMessage(String text) {
        if (EXIT_WORD_EN.equalsIgnoreCase(text.trim())
                || EXIT_WORD_RU.equalsIgnoreCase((text).trim()))
            return new Request(Commands.EXIT, userName);
        final Message message = new Message(userName, text);
        final String body = Serializer.serialize(message);
        return new Request(Commands.SEND_MESSAGE, body);
    }
}
