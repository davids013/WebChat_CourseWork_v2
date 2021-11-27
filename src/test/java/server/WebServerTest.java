package server;

import com.google.gson.Gson;
import entities.*;
import methods.Methods;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class WebServerTest {
    @BeforeAll
    static void start() {
        Methods.start();
    }

    @AfterAll
    static void end() {
        Methods.end();
    }

    @BeforeEach
    public void newTest() {
        Methods.newTest();
    }

    @AfterEach
    public void endTest() {
        Methods.endTest();
    }

    @Test
    void startTest() throws IOException, InterruptedException {
        boolean result = false;
        String userName = "testUser";
        final Gson gson = new Gson();
        final String[] hostAndPort = ConfigWorker.getHostAndPortFromConfig(WebServer.SETTINGS_FILE_PATH);
        final String host = hostAndPort[ConfigWorker.HOST_INDEX];
        final int port = Integer.parseInt(hostAndPort[ConfigWorker.PORT_INDEX]);
        final Thread serverThread = new Thread(WebServer::start);
        serverThread.start();

        try (final SocketChannel socketChannel = SocketChannel.open()) {
            final InetSocketAddress address = new InetSocketAddress(host, port);
            socketChannel.connect(address);
            final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 20);

            while (WebServer.getUsers().containsKey(userName)) {
                userName += "+";
            }
            Request request = new Request(Commands.REGISTER_USER, userName);
            String requestStr = gson.toJson(request);
            socketChannel.write(ByteBuffer.wrap(requestStr.getBytes(StandardCharsets.UTF_8)));
            if (socketChannel.read(inputBuffer) >= 0) {
                inputBuffer.clear();
                if (WebServer.getUsers().containsKey(userName)) {
                    result = true;
                }
            }
            Assertions.assertTrue(result);

            result = false;
            final Message message = new Message(userName, "testMessage");
            request = new Request(Commands.SEND_MESSAGE, gson.toJson(message));
            requestStr = Serializer.serialize(request);
            socketChannel.write(ByteBuffer.wrap(requestStr.getBytes(StandardCharsets.UTF_8)));
            Thread.sleep(500);
            if (socketChannel.read(inputBuffer) >= 0) {
                inputBuffer.clear();
                if (WebServer.getUsers().get(userName).getOutgoingMessages().contains(message)) {
                    result = true;
                }
            }
            Assertions.assertTrue(result);

            result = false;
            System.out.println("\t\tExit test");
            request = new Request(Commands.EXIT, "exit");
            final int online = WebServer.getOnline();
            requestStr = Serializer.serialize(request);
            socketChannel.write(ByteBuffer.wrap(requestStr.getBytes(StandardCharsets.UTF_8)));
            Thread.sleep(100);          // Пауза на удаление пользователя сервером
            if (WebServer.getOnline() == online - 1) {
                result = true;
            }
            Assertions.assertTrue(result);
        }
    }

}
