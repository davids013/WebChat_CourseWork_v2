package entities;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public abstract class ConfigWorker {
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final int DEFAULT_PORT = 11211;
    public static final byte HOST_INDEX = 0;
    public static final byte PORT_INDEX = 1;
    public static final String HOST_CONFIG_KEY = "HOST: ";
    public static final String PORT_CONFIG_KEY = "PORT: ";

    public static String[] getHostAndPortFromConfig(String configFilename) {
        final String[] hostAndPort = new String[2];
        final File configFile = new File(configFilename);
        if (!configFile.exists() || !configFile.isFile() || !configFile.canRead())
            createNewSettings(configFile);
        if (configFile.exists() && configFile.isFile() && configFile.canRead()) {
            try {
                final List<String> lines = Files.readAllLines(Paths.get(configFilename), StandardCharsets.UTF_8);
                final String hostLine = lines.get(HOST_INDEX);
                final String portLine = lines.get(PORT_INDEX);
                if (lines.size() == hostAndPort.length
                        && hostLine.contains(HOST_CONFIG_KEY)
                        && portLine.contains(PORT_CONFIG_KEY)) {
                    final String hostStr = hostLine.replace(HOST_CONFIG_KEY, "").trim();
                    final String portStr = portLine.replace(PORT_CONFIG_KEY, "").trim();
                    int dotCounter = 0;
                    for (char c : hostStr.toCharArray()) {
                        if (c == '.') dotCounter++;
                    }
                    if (dotCounter == 3) {
                        long hostLong = Long.parseLong(hostStr.replace(".", ""));
                        int portInt = Integer.parseInt(portStr);
                        if (hostLong <= 255255255255L && portInt > 1000 && portInt < 100000) {
                            hostAndPort[HOST_INDEX] = hostStr;
                            hostAndPort[PORT_INDEX] = portLine.replace(PORT_CONFIG_KEY, "");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (hostAndPort[HOST_INDEX] == null || hostAndPort[PORT_INDEX] == null) {
            System.out.println("Некорректные настройки соединения");
            if (createNewSettings(configFile)) {
                System.out.println("Настройки соединения сброшены к исходным");
            } else {
                System.out.println("Не удалось сбросить настройки к исходным");
            }
            return getHostAndPortFromConfig(configFilename);
        }
        return hostAndPort;
    }

    private static boolean createNewSettings(File configFile) {
        try (final PrintWriter writer = new PrintWriter(configFile)) {
            if (!configFile.exists()) {
                if (new File(configFile.getParent()).exists()) {
                    Files.createDirectories(Paths.get(configFile.getParent()));
                }
            }
            writer.println(HOST_CONFIG_KEY + DEFAULT_HOST);
            writer.println(PORT_CONFIG_KEY + DEFAULT_PORT);
            writer.flush();
            return configFile.exists();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
