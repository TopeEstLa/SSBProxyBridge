package com.bgsoftware.ssbproxybridge.manager.util;

import org.springframework.lang.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

public class Resources {

    private Resources() {

    }

    public static void saveResource(String resourcePath, File outputFile) throws IOException {
        try (InputStream in = getResource(resourcePath.replace('\\', '/'))) {
            if (in == null)
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found.");

            File outDir = outputFile.getParentFile();
            if (outDir != null && !outDir.exists())
                outDir.mkdirs();

            try (OutputStream out = Files.newOutputStream(outputFile.toPath())) {
                byte[] buf = new byte[1024];

                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }

    @Nullable
    public static InputStream getResource(String filename) throws IOException {
        URL url = Resources.class.getClassLoader().getResource(filename);

        if (url == null)
            return null;

        URLConnection connection = url.openConnection();
        connection.setUseCaches(false);
        return connection.getInputStream();
    }

}
