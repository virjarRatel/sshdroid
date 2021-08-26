package com.virjar.sshdroid;

import android.os.Build;
import android.util.Log;

import com.virjar.ratel.api.RatelToolKit;

import org.apache.sshd.common.keyprovider.ClassLoadableResourceKeyPairProvider;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.apache.sshd.common.util.security.bouncycastle.BouncyCastleKeyPairResourceParser;
import org.apache.sshd.scp.server.ScpCommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;
import org.apache.sshd.server.shell.ProcessShellFactory;
import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.Security;
import java.util.Collections;
import java.util.Locale;

import external.org.apache.commons.io.IOUtils;

public class SSHD {
    public static final String TAG = "SSHDroid";

    public static void startup(String nowProcessName, String modulePath) throws IOException {

        if (!nowProcessName.equals(Configs.targetPackage)) {
            return;
        }

        Log.i(TAG, "begin start SSHDroid service");

        String mockUserHome = RatelToolKit.sContext.getFilesDir().getAbsolutePath();

        if (!Configs.newProcess) {
            startup(Configs.ssdServerPort, mockUserHome, false);
            return;
        }

        // 需要在新进程中启动
        String processName = String.format("SSHDroid:%s", nowProcessName);
        String cmd = String.format(Locale.ENGLISH, CMD_FORMATTER,
                modulePath, processName, Configs.ssdServerPort, mockUserHome);
        Log.i(TAG, "cmd: " + cmd);


        Process process = Runtime.getRuntime().exec("sh");
        OutputStream os = process.getOutputStream();
        os.write(cmd.getBytes());
        os.flush();
        os.close();

        new StreamReadTask(process.getInputStream()).start();
        new StreamReadTask(process.getErrorStream()).start();
    }

    private static final String CMD_FORMATTER =
            "(CLASSPATH=%s /system/bin/app_process /system/bin --nice-name=%s "
                    + SSHD.class.getName()
                    + " %d %s)&";


    public static void main(String[] args) {
        int ssdServerPort = Integer.parseInt(args[0]);
        String userHome = args[1];

        Security.addProvider(new BouncyCastleProvider());
        startup(ssdServerPort, userHome, true);
    }


    private static void reinstallBCProvider() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            // android8之前的不需要处理
            // https://stackoverflow.com/questions/55934898/how-to-run-an-apache-mina-sshd-server-2-2-0-on-android-pie/68251093#68251093
            return;
        }
        Security.removeProvider("BC");
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void startup(int ssdServerPort, String userHome, boolean newProcess) {
        Log.i(TAG, "prepare start SshServer with port: " + ssdServerPort + " mock userHome: " + userHome);

        reinstallBCProvider();

        // android 下没有这个环境
        System.setProperty("user.home", userHome);
        try {
            SshServer sshd = SshServer.setUpDefaultServer();
            sshd.setPort(ssdServerPort);
            sshd.setShellFactory(new ProcessShellFactory("bash", "sh"));
            sshd.setCommandFactory(new ScpCommandFactory());
            sshd.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
            SecurityUtils.setKeyPairResourceParser(BouncyCastleKeyPairResourceParser.INSTANCE);
            sshd.setKeyPairProvider(new ClassLoadableResourceKeyPairProvider(SSHD.class.getClassLoader(), "assets/sshdroid.pem"));
            sshd.setPasswordAuthenticator(AcceptAllPasswordAuthenticator.INSTANCE);
            sshd.setPublickeyAuthenticator(AcceptAllPublickeyAuthenticator.INSTANCE);
            sshd.setForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
            try {
                sshd.start();
                Log.i(TAG, "server startup finished");
                if (newProcess) {
                    Thread.sleep(Integer.MAX_VALUE);
                }
            } catch (IOException e) {
                Log.e(SSHD.class.getSimpleName(), "start ssd error", e);
            }
        } catch (Throwable throwable) {
            Log.e(TAG, "startup error", throwable);
        } finally {
            System.setProperty("user.home", "");
        }
    }


    private static class StreamReadTask extends Thread {
        private final InputStream inputStream;

        StreamReadTask(InputStream inputStream) {
            this.inputStream = inputStream;
        }


        @Override
        public void run() {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            try {

                while ((line = bufferedReader.readLine()) != null) {
                    Log.i(SSHD.TAG, line);
                }
            } catch (IOException e) {
                Log.e(SSHD.TAG, "error", e);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }


}
