package com.appropel.xplanegps.view.application;

import android.app.Application;

import com.appropel.xplanegps.dagger.DaggerWrapper;

/**
 * Main application.
 */
public final class Xp2GpsApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath("Inconsolata-Regular.ttf")
//                .setFontAttrId(R.attr.fontPath)
//                .build()
//        );
        DaggerWrapper.INSTANCE.init(this);
    }
}
