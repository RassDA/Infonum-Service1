package ru.infonum.service;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by d1i on 27.01.15.
 */
public class Installation {

    private static String sID = null;
    private static final String INSTALLATION = "INSTALLATION";

    public synchronized static String id(Context context, String appId) {
        if (sID == null) {
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists()) {
                    if (appId != null) writeInstallationFile(installation, appId);
                    sID = null;

                } else {
                    sID = readInstallationFile(installation);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
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
// TODO: сделать всё сегодня
}
