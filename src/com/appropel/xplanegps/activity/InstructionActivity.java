package com.appropel.xplanegps.activity;

import android.os.Bundle;
import android.widget.TextView;
import com.appropel.xplanegps.R;
import com.appropel.xplanegps.utility.NetworkUtility;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * Activity which displays the instructions.
 */
public final class InstructionActivity extends RoboActivity
{
    /** View holding server code text. */
    @InjectView(R.id.ip_text_view)
    private TextView ipInstructionsView;

    /** {@inheritDoc} */
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        ipInstructionsView.setText(
                String.format(getString(R.string.ip_instructions), NetworkUtility.getLocalIpAddress()));
    }
}
