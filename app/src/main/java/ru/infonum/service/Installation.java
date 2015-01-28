package ru.infonum.service;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by RassDA on 27.01.15.
 * на входе код или null
 * если null - ничего не делает.
 * иначе проверяет наличие файла
 * если файл создан, читает код и возвращает его
 * если нет файла - создает, записывает код в файл и возващает его же
 */
public class Installation {

    private static final String INSTALLATION = "INSTALLATION";

    public synchronized static void id(Context context, String appId) {
        if (appId != null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists()) {
                    writeInstallationFile(installation, appId);

                } else {
                    appId = readInstallationFile(installation);
                }
            } catch (Exception e) {
                throw new RuntimeException(e); // TODO: обработать
            }
        }
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation, String appId) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        //String id = UUID.randomUUID().toString();
        //out.write(id.getBytes());
        out.write(appId.getBytes());
        out.close();
    }
}
