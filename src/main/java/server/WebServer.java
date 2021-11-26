package server;

import entities.ConfigWorker;
import entities.Message;
import entities.Serializer;
import entities.User;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class WebServer {
    public static final char SEP = File.separatorChar;
    protected static final String COLOR = "\033[33m";
    private static final String RESOURCES_PATH = "src" + SEP + "main" + SEP + "resources";
    public static final String SETTINGS_FILE_PATH = RESOURCES_PATH + SEP + "settings.txt";
    public static final String LOG_EXTENSION = ".log";
    public static final String ROOT_LOG_DIRECTORY = "logs";
    public static final String CHAT_LOG_DIRECTORY = ROOT_LOG_DIRECTORY + SEP + "chat_log";
    protected static final String SERVER_LOG_PATH = ROOT_LOG_DIRECTORY + SEP + "requests" + LOG_EXTENSION;
    private static final String HOST =
            ConfigWorker.getHostAndPortFromConfig(SETTINGS_FILE_PATH)[ConfigWorker.HOST_INDEX];
    private static final int PORT =
            Integer.parseInt(ConfigWorker.getHostAndPortFromConfig(SETTINGS_FILE_PATH)[ConfigWorker.PORT_INDEX]);
    public static final int MAX_ONLINE = 10;
    protected static final String USERS_STORAGE_PATH = RESOURCES_PATH + SEP + "users.info";
    private static final Map<String, User> users = new ConcurrentSkipListMap<>();
    protected static final AtomicInteger online = new AtomicInteger(0);
    protected static final List<Message> chatList = new CopyOnWriteArrayList<>();
    protected static final List<SocketChannel> clientList = new CopyOnWriteArrayList<>();

    public static void start() {
        System.out.println(COLOR + "Сервер запускается...");
        final ExecutorService pool = Executors.newFixedThreadPool(MAX_ONLINE);
        try {
            final ServerSocketChannel server = ServerSocketChannel.open();
            server.bind(new InetSocketAddress(HOST, PORT));
            if (Files.exists(Paths.get(USERS_STORAGE_PATH))) {
                final List<String> savedUsers =
                        Files.readAllLines(Paths.get(USERS_STORAGE_PATH), StandardCharsets.UTF_8);
                for (String userJson : savedUsers) {
                    if (!userJson.trim().isEmpty()) {
                        final User user = Serializer.deserialize(userJson, User.class);
                        users.put(user.getName(), user);
                    }
                }

            }

            while (!Thread.currentThread().isInterrupted()) {
                pool.submit(new ServerTask(server.accept()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }

    public static int getOnline() {
        return online.get();
    }

    public static Map<String, User> getUsers() {
        return users;
    }

    public static void sendToAll(String text) {
        System.out.println("sendToAll() call");
        clientList.forEach((client) -> {
            try {
                client.write(ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static List<Message> getChatList() {
        return new ArrayList<>(chatList);
    }
}