package com.sd.whereareyou.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sd.whereareyou.R;
import com.sd.whereareyou.models.PeerInfo;
import com.sd.whereareyou.utils.Constants;
import com.sd.whereareyou.utils.WiFiDirectBroadcastReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import static com.sd.whereareyou.utils.Constants.IS_GROUP_OWNER;
import static com.sd.whereareyou.utils.Constants.PEER;
import static com.sd.whereareyou.utils.Constants.SERVICE_INSTANCE;
import static com.sd.whereareyou.utils.Constants.UID;
import static com.sd.whereareyou.utils.Constants.USER_NAME;

public class UserDiscovery extends AppCompatActivity implements WifiP2pManager.ConnectionInfoListener {


    private static final String TAG = UserDiscovery.class.getSimpleName();
    private static final int WIFI_ENABLE_CODE = 132;
    private final Map<String, String> deviceToUIDMap = new HashMap<String, String>();
    private final Map<String, String> deviceToUsernameMap = new HashMap<String, String>();
    private ListView listView;
    private TextView tvSearchStatus, tvWifiStatus;
    private Button btnWifiStart;
    private Toolbar toolbar;
    private WifiManager wifiManager;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    private List<PeerInfo> peerInfoList;
    private List<String> peerNameList;
    private WifiP2pDnsSdServiceRequest serviceRequest;
    private PeerInfo selectedPeerInfo;
    private ProgressBar progressBar;
    private ArrayAdapter<String> adapter;
    private String myUserName;
    private String myUID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_discovery);
        initialiseUiElements();
        initialiseObjects();
        removeGroup();
        progressBar.setVisibility(View.VISIBLE);
        discoverPeers();


    }

    // Check if group is already created, if yes, remove it
    private void removeGroup() {
        wifiP2pManager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
            @Override
            public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
                if (wifiP2pGroup != null) {
                    wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(UserDiscovery.this, getResources().getString(R.string.group_removed), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int i) {
                            Toast.makeText(UserDiscovery.this, getResources().getString(R.string.failed_to_remove_group), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });
    }


    //Discover available neerby peers
    private void discoverPeers() {

        peerInfoList.clear();
        peerNameList.clear();

        WifiP2pManager.DnsSdServiceResponseListener dnsSdServiceResponseListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
                if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
                    if (deviceToUIDMap.containsKey(srcDevice.deviceAddress)) {
                        PeerInfo peerInfo = new PeerInfo();
                        peerInfo.setDeviceName(srcDevice.deviceName);
                        peerInfo.setDeviceAddress(srcDevice.deviceAddress);
                        peerInfo.setInstanceName(instanceName);
                        peerInfo.setServiceRegistrationType(registrationType);

                        peerInfo.setUid(deviceToUIDMap.get(srcDevice.deviceAddress));
                        peerInfo.setUsername(deviceToUsernameMap.get(srcDevice.deviceAddress));
                        peerInfoList.add(peerInfo);
                        peerNameList.add(srcDevice.deviceName + "(" + deviceToUIDMap.get(srcDevice.deviceAddress) + ")");

                    }
                    Log.d("deviceToUUID", deviceToUIDMap.toString());
                    Log.d("deviceToUsername", deviceToUsernameMap.toString());
                }
            }
        };

        WifiP2pManager.DnsSdTxtRecordListener dnsSdTxtRecordListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                deviceToUIDMap.put(srcDevice.deviceAddress, txtRecordMap.get(Constants.UID));
                deviceToUsernameMap.put(srcDevice.deviceAddress, txtRecordMap.get(Constants.USER_NAME));
                Log.d("User Discovery UUID", srcDevice.deviceName + " is " + txtRecordMap.get(Constants.UID));
            }
        };

        wifiP2pManager.setDnsSdResponseListeners(channel, dnsSdServiceResponseListener, dnsSdTxtRecordListener);

        serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        //Add service request
        wifiP2pManager.addServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(int reason) {

            }
        });
        //Discover Service
        wifiP2pManager.discoverServices(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                tvSearchStatus.setText(getResources().getString(R.string.searching));
            }

            @Override
            public void onFailure(int error) {
                tvSearchStatus.setText(getString(R.string.searching_failed));
                progressBar.setVisibility(View.GONE);
                if (error == WifiP2pManager.P2P_UNSUPPORTED) {
                    Log.d(TAG, "onFailure(): Wi-Fi P2P isn't supported on the device running the app.");
                    Toast.makeText(UserDiscovery.this, getResources().getString(R.string.p2p_not_supported), Toast.LENGTH_SHORT).show();

                } else if (error == WifiP2pManager.ERROR) {
                    Toast.makeText(UserDiscovery.this, getResources().getString(R.string.p2p_error), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure(): The operation failed due to an internal error.");
                } else if (error == WifiP2pManager.BUSY) {
                    Toast.makeText(UserDiscovery.this, getResources().getString(R.string.p2p_bussy), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure(): The system is too busy to process the request.");
                }
            }
        });

    }

    private void initialiseObjects() {

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        broadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this);


        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        peerInfoList = new ArrayList<>();
        peerNameList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, peerNameList);
        listView.setAdapter(adapter);
    }

    private void initialiseUiElements() {
        listView = findViewById(R.id.listViewUserDiscovery);
        progressBar = findViewById(R.id.progressBarUserDiscovery);
        tvSearchStatus = findViewById(R.id.tvSearchStatusUserDiscovery);
        tvWifiStatus = findViewById(R.id.tvWifiStatusUserDiscovery);
        btnWifiStart = findViewById(R.id.btnWifiStartUserDiscovery);
        btnWifiStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onWifiStart();
            }
        });
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.near_by_user));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Intent intent = getIntent();
        myUserName = intent.getStringExtra(USER_NAME);
        myUID = intent.getStringExtra(UID);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Find the correct peer and start Chatter Activity with that peer
                selectedPeerInfo = peerInfoList.get(position);
                Intent intent = new Intent(UserDiscovery.this, ChatterActivity.class);
                intent.putExtra(PEER, selectedPeerInfo);
                intent.putExtra(Constants.DEVICE_TYPE, Constants.INIT_CONNECTION);
                intent.putExtra(USER_NAME, myUserName);
                intent.putExtra(UID, myUID);
                startActivity(intent);
                finish();
            }
        });
    }

    // Start ChatterActivity if a connection is received instead of creating
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {
        if (p2pInfo.groupFormed) {

            Intent intent = new Intent(this, ChatterActivity.class);
            PeerInfo peerInfo = new PeerInfo();
            intent.putExtra(PEER, peerInfo);
            intent.putExtra(Constants.DEVICE_TYPE, Constants.RECEIVE_CONNECTION);

            if (p2pInfo.isGroupOwner) {
                intent.putExtra(IS_GROUP_OWNER, true);
            } else {
                peerInfo.setGroupOwnerAddress(p2pInfo.groupOwnerAddress);
                intent.putExtra(IS_GROUP_OWNER, false);
            }

            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);
        if (!wifiManager.isWifiEnabled()) {
            onWifiOff();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }


    public void onWifiOff() {
        tvWifiStatus.setVisibility(View.VISIBLE);
        btnWifiStart.setVisibility(View.VISIBLE);
        listView.setVisibility(View.GONE);
    }

    public void onWifiStart() {

        if (!wifiManager.isWifiEnabled()) {
            Intent wifiIntent = new Intent(Settings.Panel.ACTION_WIFI);
            startActivityForResult(wifiIntent, WIFI_ENABLE_CODE);
        }

        tvWifiStatus.setVisibility(View.GONE);
        btnWifiStart.setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
    }
}