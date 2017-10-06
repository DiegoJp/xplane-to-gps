package com.appropel.xplanegps.model;

import com.appropel.xplanegps.R;

import net.orange_box.storebox.annotations.method.DefaultValue;
import net.orange_box.storebox.annotations.method.KeyByString;

/**
 * Interface to shared preferences.
 */
public interface Preferences
{
    // CSOFF: JavadocVariable
    String KEY_XPLANEVERSION = "xplane_version";
    String KEY_AUTOCONFIGURE = "autoconfigure";
    String KEY_BROADCASTSUBNET = "broadcast_subnet";
    String KEY_SIMULATORADDRESS = "sim_address";
    String KEY_RECEIVEPORT = "port";
    String KEY_UDPFORWARD = "enable_udp_forward";
    String KEY_FORWARDADDRESS = "forward_address";
    String KEY_EASYVFR = "easy_vfr";
    String KEY_SELECTEDTAB = "tab_tag";

    @KeyByString(KEY_XPLANEVERSION)
    String getXplaneVersion();

    @KeyByString(KEY_XPLANEVERSION)
    void setXplaneVersion(String version);

    @KeyByString(KEY_AUTOCONFIGURE)
    boolean isAutoconfigure();

    @KeyByString(KEY_BROADCASTSUBNET)
    boolean isBroadcastSubnet();

    @KeyByString(KEY_BROADCASTSUBNET)
    void setBroadcastSubnet(boolean broadcastSubnet);

    @KeyByString(KEY_SIMULATORADDRESS)
    @DefaultValue(R.string.localhost)
    String getSimulatorAddress();

    @KeyByString(KEY_SIMULATORADDRESS)
    void setSimulatorAddress(String simulatorAddress);

    @KeyByString(KEY_RECEIVEPORT)
    @DefaultValue(R.string.default_port)
    String getReceivePort();

    @KeyByString(KEY_RECEIVEPORT)
    void setReceivePort(String receivePort);

    @KeyByString(KEY_UDPFORWARD)
    boolean isUdpForward();

    @KeyByString(KEY_FORWARDADDRESS)
    @DefaultValue(R.string.localhost)
    String getForwardAddress();

    @KeyByString(KEY_FORWARDADDRESS)
    void setForwardAddress(String forwardAddress);

    @KeyByString(KEY_EASYVFR)
    boolean isEasyVfr();

    @KeyByString(KEY_SELECTEDTAB)
    String getSelectedTab();

    @KeyByString(KEY_SELECTEDTAB)
    void setSelectedTab(String selectedTab);
}
