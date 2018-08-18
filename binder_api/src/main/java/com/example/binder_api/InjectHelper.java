package com.example.binder_api;

import android.app.Activity;

import java.lang.reflect.Constructor;

/**
 * Created by Administrator on 2018/8/18.
 * using   inject  view
 */

public class InjectHelper {
    public static void inject(Activity host) {
        try {
            String classFullName = host.getClass().getName() + "$$bindInject";

            Class proxty = Class.forName(classFullName);

            Constructor constructor = proxty.getConstructor(host.getClass());

            constructor.newInstance(host);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
