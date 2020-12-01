package com.sd.whereareyou.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.sd.whereareyou.R;
import com.sd.whereareyou.ui.ChatterActivity;
import com.sd.whereareyou.ui.HomeActivity;
import com.sd.whereareyou.ui.UserDiscovery;


public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private UserDiscovery userDiscoveryActivity;
    private ChatterActivity chatterActivity;
    private HomeActivity homeActivity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, UserDiscovery userDiscoveryActivity) {
        wifiP2pManager = manager;
        this.channel = channel;
        this.userDiscoveryActivity = userDiscoveryActivity;
        this.chatterActivity = null;
        this.homeActivity = null;
    }

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, ChatterActivity chatterActivity) {
        wifiP2pManager = manager;
        this.channel = channel;
        this.userDiscoveryActivity = null;
        this.chatterActivity = chatterActivity;
        this.homeActivity = null;
    }

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel, HomeActivity homeActivity) {
        wifiP2pManager = manager;
        this.channel = channel;
        this.userDiscoveryActivity = null;
        this.chatterActivity = null;
        this.homeActivity = homeActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            // Indicate that Wifi is off. To use service please turn it on.
            if (state != WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, context.getResources().getString(R.string.WiFi_Off), Toast.LENGTH_SHORT).show();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Notify to appropriate activity that available peers list has changed.
            if (wifiP2pManager != null) {

                if (userDiscoveryActivity != null) {

                }
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Notify to appropriate activity that peers to peer connection is changed.
            if (wifiP2pManager != null) {
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (userDiscoveryActivity != null) {
                    if (networkInfo.isConnected()) {
                        wifiP2pManager.requestConnectionInfo(channel, (WifiP2pManager.ConnectionInfoListener) userDiscoveryActivity);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.disconnected), Toast.LENGTH_SHORT).show();
                    }
                } else if (chatterActivity != null) {
                    if (networkInfo.isConnected()) {
                        wifiP2pManager.requestConnectionInfo(channel, (WifiP2pManager.ConnectionInfoListener) chatterActivity);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.disconnected), Toast.LENGTH_SHORT).show();
                    }
                } else if (homeActivity != null) {
                    if (networkInfo.isConnected()) {
                        // wifiP2pManager.requestConnectionInfo(channel, (WifiP2pManager.ConnectionInfoListener) homeActivity);
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.disconnected), Toast.LENGTH_SHORT).show();
                    }
                }
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            //Indicates this device's configuration details have changed.
        }
    }
}