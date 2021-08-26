package com.virjar.sshdroid;


import android.util.Log;

import com.virjar.ratel.api.rposed.IRposedHookLoadPackage;
import com.virjar.ratel.api.rposed.callbacks.RC_LoadPackage;

public class Main implements IRposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final RC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            SSHD.startup(lpparam.processName, lpparam.modulePath);
        } catch (Throwable throwable) {
            Log.e(SSHD.TAG, "start SSHD error", throwable);
        }

    }

}
