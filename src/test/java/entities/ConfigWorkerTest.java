package entities;

import methods.Methods;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ConfigWorkerTest {
    @BeforeAll
    static void start() { Methods.start(); }

    @AfterAll
    static void end() { Methods.end(); }

    @BeforeEach
    public void newTest() { Methods.newTest(); }

    @AfterEach
    public void endTest() { Methods.endTest(); }

    @Test
    void getHostAndPortFromConfigTest() throws IOException {
        final String testFileName = "testConfig.txt";
        final File testFile = new File(testFileName);
        boolean[] result = {false, false};
        if (!testFile.exists() || testFile.delete()) {
            System.out.println("start for");
            for (int i = 0; i < 2; i++) {
                String[] hostAndPort = ConfigWorker.getHostAndPortFromConfig(testFileName);

                final List<String> list = Files.readAllLines(Paths.get(testFileName), StandardCharsets.UTF_8);
                if (list.get(ConfigWorker.HOST_INDEX).replace(ConfigWorker.HOST_CONFIG_KEY, "")
                            .equals(hostAndPort[ConfigWorker.HOST_INDEX])
                        && list.get(ConfigWorker.PORT_INDEX).replace(ConfigWorker.PORT_CONFIG_KEY, "")
                            .equals(hostAndPort[ConfigWorker.PORT_INDEX])) {
                    result[i] = true;
                }
            }
            testFile.delete();

            Assertions.assertTrue(result[0] && result[1]);
        }
    }
}
