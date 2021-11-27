package client;

import entities.*;
import entities.ConfigWorker;
import server.WebServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Scanner;

public class WebClient {
    private static final char SEP = File.separatorChar;
    private static final String OUT_COLOR = "\033[34m";
    private static final String IN_COLOR = "\033[35m";
    private static final String SYS_COLOR = "\033[0m";
    private static final String CURSOR = ">> ";
    private static final String CHAT_OUT_PREFIX = "\t\t\t\t\t\t\t\t\t";
    private static final String CHAT_IN_PREFIX = "\t";
    private static final String EXIT_WORD_EN = "/exit";
    private static final String EXIT_WORD_RU = "/учше";
    private static final String SETTINGS_FILE_PATH = "src" + SEP + "main" + SEP + "resources" + SEP + "settings.txt";
    private static final String CHAT_LOG_DIRECTORY = WebServer.ROOT_LOG_DIRECTORY + SEP + "clients";
    private static String chatLogPath;
    private final static String HOST =
            ConfigWorker.getHostAndPortFromConfig(SETTINGS_FILE_PATH)[ConfigWorker.HOST_INDEX];
    private final static int PORT =
            Integer.parseInt(ConfigWorker.getHostAndPortFromConfig(SETTINGS_FILE_PATH)[ConfigWorker.PORT_INDEX]);
    private static String userName;

    public static void start() {
        final Scanner sc = new Scanner(System.in);
        final InetSocketAddress address = new InetSocketAddress(HOST, PORT);
        try (final SocketChannel socketChannel = SocketChannel.open()) {
            socketChannel.connect(address);
            final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 20);

            String input;
            Request request = null;
            while (true) {
                if (request == null) {
                    System.out.println(SYS_COLOR + "Добро пожаловать в чат!\n" +
                            "Для отправки сообщения, введите текст после '" + CURSOR +
                            "' и нажмите кнопку 'ENTER' (выход из чата - сообщение '" + EXIT_WORD_EN + "')");
                    System.out.print(SYS_COLOR +
                            "Введите имя для участия в чате:\n" + CURSOR);
                    input = sc.nextLine();
                    request = requestAccess(input);
                    if (request.getCommand() == Commands.EXIT) break;
                    userName = request.getBody();
                    Thread.currentThread().setName(userName);
                    chatLogPath = CHAT_LOG_DIRECTORY + SEP + userName + WebServer.LOG_EXTENSION;
                    final File logFile = new File(chatLogPath);
                    if (logFile.exists()) logFile.delete();
                } else {
                    System.out.print(SYS_COLOR + CURSOR);
                    input = sc.nextLine();
                    request = requestSendMessage(input);
                }
                final String requestStr = Serializer.serialize(request);
                socketChannel.write(ByteBuffer.wrap(requestStr.getBytes(StandardCharsets.UTF_8)));
                if (request.getCommand() == Commands.EXIT) {
                    break;
                }
                Thread.sleep(350);      // Костыль, чтобы последнее сообщение выводилось перед следующим вводом

                int bytesCount;
                if ((bytesCount = socketChannel.read(inputBuffer)) >= 0) {
                    input = new String(inputBuffer.array(), 0, bytesCount, StandardCharsets.UTF_8).trim();
                    final String pattern = (char) 123 + "\"command\":";
                    final String replacer = "" + (char) 127;
                    final String modInput = input.replace(pattern, replacer);
                    String[] inputArray= modInput.split(replacer);
                    for (int i = 1; i < inputArray.length; i++) {
                        inputArray[i] = pattern + inputArray[i];
                        request = Serializer.deserialize(inputArray[i], Request.class);
                        analyzeRequest(request);
                    }
                }
                inputBuffer.clear();
                if (request.getCommand() == Commands.EXIT) break;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(SYS_COLOR + "Вы покинули чат" + "\033[0m");
    }

    private static void analyzeRequest(Request request) {
        switch (request.getCommand()) {
            case REGISTER_USER:
                if (userName.equals(request.getBody())) {
                    System.out.println(SYS_COLOR + "Вы вошли в чат");
                } else {
                    System.out.println(SYS_COLOR + "Пользователь \"" + request.getBody() + "\" вошёл в чат");
                }
                break;
            case EXIT:
                System.out.println(SYS_COLOR + "Пользователь \"" + request.getBody() + "\" покинул чат");
                break;
            case SEND_MESSAGE:
                final Logger logger = new FileLogger(chatLogPath, true);
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
                final String output = prefix + message.getAuthor() + " (" + time + ")" + "\n" +
                        color + prefix + "\t" + message.getText();
                System.out.println(SYS_COLOR + output);
                logger.log(output.replace(color, ""));
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
