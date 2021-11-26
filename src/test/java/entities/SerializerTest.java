package entities;

import com.google.gson.Gson;
import methods.Methods;
import org.junit.jupiter.api.*;

public class SerializerTest {
    final Message testMessage = new Message("Mark", "Hello, Kate!");

    @BeforeAll
    static void start() { Methods.start(); }

    @AfterAll
    static void end() { Methods.end(); }

    @BeforeEach
    public void newTest() { Methods.newTest(); }

    @AfterEach
    public void endTest() { Methods.endTest(); }

    @Test
    void serializeTest() {
        final String expected = new Gson().toJson(testMessage);
        final String result = Serializer.serialize(testMessage);
        Assertions.assertEquals(expected, result);
    }

    @Test
    void deserializeTest() {
        final Gson gson = new Gson();
        final String arg = gson.toJson(testMessage);

        final Message expected = gson.fromJson(arg, testMessage.getClass());
        final Message result = Serializer.deserialize(arg, testMessage.getClass());

        Assertions.assertEquals(expected, result);
    }

    @Test
    void combineTest() {
        final Message expected = testMessage;

        final String temp = Serializer.serialize(expected);
        final Message result = Serializer.deserialize(temp, expected.getClass());

        Assertions.assertEquals(expected, result);
    }
}
