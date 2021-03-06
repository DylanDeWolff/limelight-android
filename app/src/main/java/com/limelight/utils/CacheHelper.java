package com.limelight.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

public class CacheHelper {
    private static File openPath(boolean createPath, File root, String... path) {
        File f = root;
        for (int i = 0; i < path.length; i++) {
            String component = path[i];

            if (i == path.length - 1) {
                // This is the file component so now we create parent directories
                if (createPath) {
                    f.mkdirs();
                }
            }

            f = new File(f, component);
        }
        return f;
    }

    public static FileInputStream openCacheFileForInput(File root, String... path) throws FileNotFoundException {
        return new FileInputStream(openPath(false, root, path));
    }

    public static FileOutputStream openCacheFileForOutput(File root, String... path) throws FileNotFoundException {
        return new FileOutputStream(openPath(true, root, path));
    }

    public static String readInputStreamToString(InputStream in) throws IOException {
        Reader r = new InputStreamReader(in);

        StringBuilder sb = new StringBuilder();
        char[] buf = new char[256];
        int bytesRead;
        while ((bytesRead = r.read(buf)) != -1) {
            sb.append(buf, 0, bytesRead);
        }

        return sb.toString();
    }

    public static void writeStringToOutputStream(OutputStream out, String str) throws IOException {
        out.write(str.getBytes("UTF-8"));
    }
}
