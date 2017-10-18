package com.appropel.xplanegps.view.activity;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.appropel.xplanegps.BuildConfig;
import com.appropel.xplanegps.R;
import com.appropel.xplanegps.dagger.DaggerWrapper;
import com.appropel.xplanegps.model.Preferences;
import com.appropel.xplanegps.view.fragment.DataFragment;
import com.appropel.xplanegps.view.fragment.SettingsFragment;
import com.appropel.xplanegps.view.util.SettingsUtil;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Main activity of the application.
 */
public final class MainActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener
{
    /** Logger. */
    static final Logger LOGGER = LoggerFactory.getLogger(MainActivity.class);

    /** Tab layout. */
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;

    /** View pager. */
    @BindView(R.id.pager)
    ViewPager viewPager;

    /** Preferences. */
    @Inject
    Preferences preferences;

    /** Alert dialog to warn about mock locations. */
    private AlertDialog alertDialog;

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        DaggerWrapper.INSTANCE.getDaggerComponent().inject(this);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics.Builder().core(
                new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());

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
    protected void attachBaseContext(final Context newBase)
    {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // Ensure that mock locations are enabled. If not, pop up an eternal dialog.
        try
        {
            if (!SettingsUtil.isMockLocationEnabled(this))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        ? R.string.mock_location_app_warning : R.string.mock_location_warning)
                        .setCancelable(true);
                alertDialog = builder.create();
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error checking device settings", e);
        }

        // Restore app to the previous tab seen, using Settings as the default.
        final String tabTag = preferences.getSelectedTab();
        LOGGER.info("Previous tab was: {}",  tabTag);
        if (DataFragment.PREF_VALUE.equals(tabTag))
        {
            viewPager.setCurrentItem(1);
        }
        else
        {
            viewPager.setCurrentItem(0);
        }

        tabLayout.addOnTabSelectedListener(this);
    }

    @Override
    protected void onStop()
    {
        if (alertDialog != null)
        {
            alertDialog.dismiss();
            alertDialog = null;
        }
        tabLayout.removeOnTabSelectedListener(this);
        super.onStop();
    }

    @Override
    public void onTabSelected(final TabLayout.Tab tab)
    {
        switch (tabLayout.getSelectedTabPosition())
        {
            case 0:
                preferences.setSelectedTab(SettingsFragment.PREF_VALUE);
                break;
            case 1:
                preferences.setSelectedTab(DataFragment.PREF_VALUE);
                break;
            default:
                LOGGER.error("Unknown tab: {}", tabLayout.getSelectedTabPosition());
                break;
        }
    }

    @Override
    public void onTabUnselected(final TabLayout.Tab tab)
    {
        // Not used.
    }

    @Override
    public void onTabReselected(final TabLayout.Tab tab)
    {
        // Not used.
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
