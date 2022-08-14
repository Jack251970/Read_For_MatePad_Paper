package com.jack.bookshelf.help;

import android.content.ComponentName;
import android.content.pm.PackageManager;

import com.jack.bookshelf.MApplication;

public class ProcessTextHelp {

    private static final PackageManager packageManager = MApplication.getInstance().getPackageManager();
    private static final ComponentName componentName = new ComponentName(MApplication.getInstance(), "com.jack.bookshelf.view.activity.ReceivingSharedActivity");

    public static boolean isProcessTextEnabled() {
        return packageManager.getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    }

    public static void setProcessTextEnable(boolean enable) {
        if (enable) {
            packageManager.setComponentEnabledSetting(componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        } else {
            packageManager.setComponentEnabledSetting(componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        }
    }

}
