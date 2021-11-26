package entities;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileLogger implements Logger {
    public static final String LINE_SEPARATOR = "\r\n";
    private final File file;
    private boolean isAppendable;

    public FileLogger(String fileName, boolean isAppendable) {
        file = new File(fileName);
        this.isAppendable = isAppendable;
        checkFile();
    }

    private void checkFile() {
        if (!file.exists()) {
            final String parent = file.getParent();
            try {
                if (!(new File(parent).exists())) {
                    Files.createDirectories(Paths.get(file.getParent()));
                }
                Files.createFile(Paths.get(file.getAbsolutePath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean log(String info) {
        try (final FileWriter writer = new FileWriter(file, isAppendable)) {
            writer.write(info + LINE_SEPARATOR);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
