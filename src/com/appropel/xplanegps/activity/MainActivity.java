package com.appropel.xplanegps.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import com.example.R;
import roboguice.activity.RoboTabActivity;

/**
 * Main activity of the application.
 */
public final class MainActivity extends RoboTabActivity
{
    /** {@inheritDoc} */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;

        // Data tab
        spec = tabHost.newTabSpec("data").setIndicator(getString(R.string.data))
                .setContent(new Intent(this, DataActivity.class));
        tabHost.addTab(spec);

        // Instructions tab
        spec = tabHost.newTabSpec("instructions").setIndicator(getString(R.string.instructions))
                .setContent(new Intent(this, InstructionActivity.class));
        tabHost.addTab(spec);
    }
}
