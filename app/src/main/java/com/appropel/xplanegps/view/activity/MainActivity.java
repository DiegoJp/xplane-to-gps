package com.appropel.xplanegps.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.appropel.xplanegps.R;
import com.appropel.xplanegps.view.fragment.DataFragment;
import com.appropel.xplanegps.view.fragment.SettingsFragment;
import com.appropel.xplanegps.view.util.SettingsUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Main activity of the application.
 */
public final class MainActivity extends Activity
{
    /** Logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);

    /** Tab layout. */
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    /** View pager. */
    @BindView(R.id.pager)
    ViewPager viewPager;

    /** Alert dialog to warn about mock locations. */
    private AlertDialog alertDialog;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ButterKnife.bind(this);

        final MyAdapter adapter = new MyAdapter(getFragmentManager());
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

        if (tabLayout.getTabCount() >= 2)
        {
            tabLayout.getTabAt(0).setIcon(R.drawable.ic_tab_gear);
            tabLayout.getTabAt(1).setIcon(R.drawable.ic_tab_plane);
        }
        else
        {
            LOGGER.error("TabLayout should have 2 tabs!");
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // Ensure that mock locations are enabled. If not, pop up an eternal dialog.
        try
        {
            if (!SettingsUtility.isMockLocationEnabled(this))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? R.string.mock_location_app_warning : R.string.mock_location_warning)
                        .setCancelable(true);
                alertDialog = builder.create();
                alertDialog.show();
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error checking device settings", e);
        }

        // Restore app to the previous tab seen, using Settings as the default.
//        final SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
//        final String tabTag = sharedPreferences.getString(TabConstants.TAB_TAG_KEY, SETTINGS_TAB_TAG);
//        Log.i(TAG, "Previous tab was: " + tabTag);
//        getTabHost().setCurrentTabByTag(tabTag);
    }

    @Override
    protected void onStop()
    {
        if (alertDialog != null)
        {
            alertDialog.dismiss();
            alertDialog = null;
        }
        super.onStop();
    }

    /**
     * Adapter which returns the proper fragment for each tab.
     */
    public final class MyAdapter extends FragmentPagerAdapter
    {
        public MyAdapter(final FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public int getCount()
        {
            return 2;
        }

        @Override
        public Fragment getItem(final int position)
        {
            switch (position)
            {
                case 0:
                    return new SettingsFragment();
                case 1:
                    return new DataFragment();
                default:
                    LOGGER.error("Unknown position {}", position);
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(final int position)
        {
            switch (position)
            {
                case 0:
                    return getResources().getString(R.string.settings);
                case 1:
                    return getResources().getString(R.string.data);
                default:
                    LOGGER.error("Unknown position {}", position);
                    return null;
            }
        }
    }
}
