package com.appropel.xplanegps.dagger;

import com.appropel.xplanegps.view.activity.MainActivity;
import com.appropel.xplanegps.view.fragment.DataFragment;
import com.appropel.xplanegps.view.fragment.SettingsFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Component interface for Dagger injection.
 */
@Component(modules = DaggerModule.class)
@Singleton
public interface DaggerComponent
{
    // CSOFF: EmptyLineSeparator
    void inject(DataFragment dataFragment);
    void inject(MainActivity mainActivity);
    void inject(SettingsFragment settingsFragment);
}
