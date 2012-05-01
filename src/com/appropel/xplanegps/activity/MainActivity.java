package com.appropel.xplanegps.activity;

import android.os.Bundle;
import android.widget.TextView;
import com.appropel.xplanegps.thread.UdpReceiverThread;
import com.appropel.xplanegps.utility.NetworkUtility;
import com.example.R;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Main activity of the application.
 */
public final class MainActivity extends RoboActivity
{
    /** View holding server code text. */
    @InjectView(R.id.ip_text_view)
    private TextView ipInstructionsView;

    /** {@inheritDoc} */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        ipInstructionsView.setText(
                String.format(getString(R.string.ip_instructions), NetworkUtility.getLocalIpAddress()));

        new Thread(new UdpReceiverThread(this)).start();
    }
}
