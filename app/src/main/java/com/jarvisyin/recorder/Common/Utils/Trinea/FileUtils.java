package com.jarvisyin.recorder.Common.Utils.Trinea;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    private FileUtils() {
        throw new AssertionError();
    }

    public static boolean deleteFile(String path) {
        if (StringUtils.isBlank(path)) {
            return true;
        }

        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }


    public static void join(String outFile, String[] files) throws Exception {
        OutputStream os = new FileOutputStream(outFile);

        for (String file : files) {
            osWrite(file, os);
        }

        os.flush();
        os.close();
    }

    private static void osWrite(String fileName, OutputStream os) throws Exception {
        File file = new File(fileName);

        InputStream is = new FileInputStream(file);
        byte[] b1 = new byte[1024 * 512];
        int len;

        while ((len = is.read(b1)) != -1) {
            os.write(b1, 0, len);
        }

        is.close();
    }

}
