package entities;

import methods.Methods;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class FileLoggerTest {
    @BeforeAll
    static void start() { Methods.start(); }

    @AfterAll
    static void end() { Methods.end(); }

    @BeforeEach
    public void newTest() { Methods.newTest(); }

    @AfterEach
    public void endTest() { Methods.endTest(); }

    @Test
    void checkFileTest() {
        final String outerDir = "temp";
        final String innerDir = outerDir + File.separator + "file";
        final String testFilePath = innerDir + File.separator + "test.txt";
        final File testFile = new File(testFilePath);
        if (!testFile.exists() || testFile.delete()) {
            new FileLogger(testFilePath, false);
        }
        final boolean result = testFile.exists();
        testFile.delete();
        new File(innerDir).delete();
        new File(outerDir).delete();
        Assertions.assertTrue(result);
    }

    @Test
    void logTest() throws IOException {
        final String testDir = "temp";
        final String testFilePath = testDir + File.separator + "test.txt";
        final FileLogger logger = new FileLogger(testFilePath, false);

        final String arg = "Hello World";

        logger.log(arg);

        final String expected = arg + FileLogger.LINE_SEPARATOR;
        final String result = new String(Files.readAllBytes(Paths.get(testFilePath)));

        Files.delete(Paths.get(testFilePath));
        Files.delete(Paths.get(testDir));

        Assertions.assertEquals(expected, result);
    }
}
