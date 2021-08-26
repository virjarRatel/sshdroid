package com.virjar.sshdroid;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.virjar.sshdroid.SSHD.TAG;

public class Configs {
    public static int ssdServerPort;
    public static boolean newProcess;
    public static String targetPackage;
    public static Properties properties;

    static {
        try {
            load();
        } catch (IOException e) {
            throw new RuntimeException("error", e);
        }
    }

    private static void load() throws IOException {
        InputStream stream = SSHD.class.getClassLoader().getResourceAsStream("assets/config.properties");
        if (stream == null) {
            Log.e(TAG, "can not find resource : assets/config.properties");
            return;
        }
        properties = new Properties();
        properties.load(stream);

        targetPackage = properties.getProperty("targetPackage");


        ssdServerPort = Integer.parseInt(properties.getProperty("ssdServerPort"));
        newProcess = Boolean.parseBoolean(properties.getProperty("newProcess"));
    }

}
