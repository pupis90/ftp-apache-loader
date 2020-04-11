package vg.ftp.services;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static java.nio.file.Files.newOutputStream;

public class VLogger {

    static public void writeLog(String mess, String logFile) throws IOException {

        Path path = null;
        try {
            path = Paths.get(new URI("file:///" + logFile));

        } catch (URISyntaxException e) {

        }
        if (!Files.exists(path.getParent())) Files.createDirectories(path.getParent());
        if (!Files.exists(path)) Files.createFile(path);
        OutputStream outputStream = newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        outputStream.write(mess.getBytes());
        outputStream.flush();
        outputStream.close();
    }

}

