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
    static void start() { Methods.start(); }

    @AfterAll
    static void end() { Methods.end(); }

    @BeforeEach
    public void newTest() { Methods.newTest(); }

    @AfterEach
    public void endTest() { Methods.endTest(); }

    @Test
    void startTest() throws IOException, InterruptedException {
        boolean[] result = {false, false, false};
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
            int bytesCount = socketChannel.read(inputBuffer);
            String input;
            if (bytesCount >= 0) {
                input = new String(inputBuffer.array(), 0, bytesCount, StandardCharsets.UTF_8).trim();
                inputBuffer.clear();
                if (input.startsWith("Успешно") && WebServer.getUsers().containsKey(userName)) {
                    result[0] = true;
                }
            }

            final Message message = new Message(userName, "testMessage");
            request = new Request(Commands.SEND_MESSAGE, gson.toJson(message));
            requestStr = Serializer.serialize(request);
            socketChannel.write(ByteBuffer.wrap(requestStr.getBytes(StandardCharsets.UTF_8)));
            Thread.sleep(500);
            if (bytesCount >= 0) {
                input = new String(inputBuffer.array(), 0, bytesCount, StandardCharsets.UTF_8).trim();
                inputBuffer.clear();
                if (input.startsWith("Успешно")
                        && WebServer.getUsers().get(userName).getIncomingMessages().contains(message)
                        && WebServer.getUsers().get(userName).getOutgoingMessages().contains(message)) {
                    result[1] = true;
                }
            }

            System.out.println("\t\tExit test");
            request = new Request(Commands.EXIT, "exit");
            final int online = WebServer.getOnline();
            requestStr = Serializer.serialize(request);
            socketChannel.write(ByteBuffer.wrap(requestStr.getBytes(StandardCharsets.UTF_8)));
            Thread.sleep(100);                      // Пауза на удаление
            if (WebServer.getOnline() == online - 1) {
                result[2] = true;
            }

            final String logFile = WebServer.getUsers().get(userName).getLogFile();
            new File(logFile).delete();
//            new File(WebServer.CHAT_LOG_DIRECTORY + WebServer.SEP
//                    + userName + WebServer.LOG_EXTENSION).delete();

            Assertions.assertTrue(result[0] && result[1] && result[2]);
        }
    }

}
