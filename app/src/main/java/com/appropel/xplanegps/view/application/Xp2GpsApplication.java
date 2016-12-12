package com.appropel.xplanegps.view.application;

import android.app.Application;
import android.os.Build;

import com.appropel.xplanegps.R;
import com.appropel.xplanegps.dagger.DaggerWrapper;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Main application.
 */
public final class Xp2GpsApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            // Calligraphy doesn't seem to work under KitKat
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath("Anonymous Pro.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build()
            );
        }
        DaggerWrapper.INSTANCE.init(this);
    }
}
