package entities;

import methods.Methods;
import org.junit.jupiter.api.*;

public class RequestTest {
    final Commands command = Commands.SEND_MESSAGE;
    final String body = "Hello World";

    @BeforeAll
    static void start() { Methods.start(); }

    @AfterAll
    static void end() { Methods.end(); }

    @BeforeEach
    public void newTest() { Methods.newTest(); }

    @AfterEach
    public void endTest() { Methods.endTest(); }

    @Test
    void getCommandTest() {
        final Request request = new Request(command, body);

        final Commands expected = command;
        final Commands result = request.getCommand();

        Assertions.assertEquals(expected, result);
    }

    @Test
    void getBodyTest() {
        final Request request = new Request(command, body);

        final String expected = body;
        final String result = request.getBody();

        Assertions.assertEquals(expected, result);
    }
}
