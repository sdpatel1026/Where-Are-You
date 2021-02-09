package com.sd.whereareyou.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.sd.whereareyou.R;
import com.sd.whereareyou.adapter.FriendListAdapter;
import com.sd.whereareyou.models.FriendBlock;
import com.sd.whereareyou.utils.Constants;
import com.sd.whereareyou.utils.PermissionHelper;
import com.sd.whereareyou.utils.WiFiDirectBroadcastReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.sd.whereareyou.utils.Constants.APP_NAME;
import static com.sd.whereareyou.utils.Constants.AVAILABLE;
import static com.sd.whereareyou.utils.Constants.FRIENDS_LIST_DB;
import static com.sd.whereareyou.utils.Constants.FRIEND_UID;
import static com.sd.whereareyou.utils.Constants.FRIEND_USER_NAME;
import static com.sd.whereareyou.utils.Constants.SERVICE_INSTANCE;
import static com.sd.whereareyou.utils.Constants.SERVICE_REG_TYPE;
import static com.sd.whereareyou.utils.Constants.UID;
import static com.sd.whereareyou.utils.Constants.USER_NAME;
import static com.sd.whereareyou.utils.Constants.VISIBLE;
import static com.sd.whereareyou.utils.Constants.WHERE_ARE_YOU;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = "HomeActivity";
    private static final int WIFI_ENABLE_CODE = 131;
    private String userDocId;
    private List<FriendBlock> friendBlockList;
    private RecyclerView recyclerView;
    private FriendListAdapter friendListAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private TextView tvNavUID, tvNavUsername, tvWifiStatus;
    private Button btnWifiStart;
    private FloatingActionButton fab;
    private WifiManager wifiManager;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private DatabaseConfiguration dbConfig;
    private Database friendListDb;
    private String myUID;
    private String myUsername;
    private String friendUsername, friendUID;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        PermissionHelper.checkAndRequestRequiredPermission(getApplicationContext());
        Intent intent = getIntent();
        userDocId = intent.getStringExtra(Constants.USER_DOC_ID);
        myUsername = intent.getStringExtra(Constants.USER_NAME);
        myUID = intent.getStringExtra(UID);
        intialiseUiElements();
        initialiseObjects();


    }

    public void registerLocalService() {

        final Map<String, String> record = new HashMap<String, String>();
        record.put(AVAILABLE, VISIBLE);
        record.put(APP_NAME, WHERE_ARE_YOU);
        record.put(UID, myUID);
        record.put(USER_NAME, myUsername);

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE, record);

        wifiP2pManager.addLocalService(channel, service, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess(): Services added Successfully");
            }

            @Override
            public void onFailure(int error) {

                if (error == WifiP2pManager.P2P_UNSUPPORTED) {
                    Toast.makeText(HomeActivity.this, getResources().getString(R.string.p2p_not_supported), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure(): Wi-Fi P2P isn't supported on the device running the app.");

                } else if (error == WifiP2pManager.ERROR) {
                    Log.d(TAG, "onFailure(): The operation failed due to an internal error.");
                } else if (error == WifiP2pManager.BUSY) {
                    Log.d(TAG, "onFailure(): The system is too busy to process the request.");
                }
                //Toast.makeText(HomeActivity.this, getResources().getString(R.string.failed_to_add_service), Toast.LENGTH_SHORT).show();

            }
        });
    }
    private void getOldChatsFromDB() throws CouchbaseLiteException {

        friendBlockList.clear();
        Query query = QueryBuilder.select(SelectResult.property(FRIEND_UID), SelectResult.property(FRIEND_USER_NAME))
                .from(DataSource.database(friendListDb));
        ResultSet resultSet = query.execute();
        List<Result> results = resultSet.allResults();

        for (Result result : results) {
            friendUID = result.getString(FRIEND_UID);
            friendUsername = result.getString(FRIEND_USER_NAME);
            friendBlockList.add(new FriendBlock(friendUsername, friendUID, "3:20 pm"));
        }
        friendBlockList.add(new FriendBlock("sagar", "123", "3:20 pm"));
        friendBlockList.add(new FriendBlock("sagar", "123", "3:20 pm"));
        friendBlockList.add(new FriendBlock("sagar", "123", "3:20 pm"));
        friendListAdapter.notifyDataSetChanged();


    }

    private void initialiseObjects() {
        dbConfig = new DatabaseConfiguration(this);
        try {
            friendListDb = new Database(FRIENDS_LIST_DB, dbConfig);
        } catch (CouchbaseLiteException e) {
            Log.d(TAG, "initialiseObjects(): " + e.getMessage());
            e.printStackTrace();
        }


        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        //Create broadcast_receiver object and filter
        broadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        friendBlockList = new ArrayList<>();
        friendListAdapter = new FriendListAdapter(friendBlockList);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(friendListAdapter);
        friendListAdapter.setOnItemClickListener(new FriendListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String fname = friendBlockList.get(position).getUserName();
                String fuid = friendBlockList.get(position).getUID();
                Intent oldChatActivityIntent = new Intent(HomeActivity.this, OldChatActivity.class);
                oldChatActivityIntent.putExtra(FRIEND_USER_NAME, fname);
                oldChatActivityIntent.putExtra(FRIEND_UID, fuid);
                startActivity(oldChatActivityIntent);
            }
        });


    }

    private void intialiseUiElements() {
        recyclerView = findViewById(R.id.recyclerViewContentHome);
        toolbar = findViewById(R.id.toolbarHome);
        drawerLayout = findViewById(R.id.drawerLayoutHome);
        navigationView = findViewById(R.id.navViewHome);
        tvNavUsername = findViewById(R.id.navHeaderUsername);
        tvWifiStatus = findViewById(R.id.tvWifiStatusHome);
        btnWifiStart = findViewById(R.id.btnWifiStartHome);
        btnWifiStart.setOnClickListener(this);
        fab = findViewById(R.id.fabHome);


        fab.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.chats));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();


        tvNavUID = navigationView.getHeaderView(0).findViewById(R.id.navHeaderUID);
        tvNavUID.setText(myUID);

        tvNavUsername = navigationView.getHeaderView(0).findViewById(R.id.navHeaderUsername);
        tvNavUsername.setText(myUsername);
        Log.d(TAG, "intialiseUiElements: DOCID-UI" + userDocId);


    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.fabHome:
                startUserDiscoveryActivity();
                break;
            case R.id.btnWifiStartHome:
                onWifiStart();
                break;
        }

    }

    public void onWifiStart() {
        if (!wifiManager.isWifiEnabled()) {
            Intent wifiIntent = new Intent(Settings.Panel.ACTION_WIFI);
            startActivityForResult(wifiIntent, WIFI_ENABLE_CODE);

        }

        tvWifiStatus.setVisibility(View.GONE);
        btnWifiStart.setVisibility(View.GONE);
    }

    private void startUserDiscoveryActivity() {

        Intent userDiscoveryIntent = new Intent(this, UserDiscovery.class);
        userDiscoveryIntent.putExtra(USER_NAME, myUsername);
        userDiscoveryIntent.putExtra(UID, myUID);
        startActivity(userDiscoveryIntent);
    }

    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //list menu in navigation drawer
        if (id == R.id.navLogout) {
            try {
                // Get the database (and create it if it doesn’t exist).
                Database userDatabase = new Database(Constants.USERS_LIST_DB, dbConfig);
                Log.d(TAG, "onNavigationItemSelected(): DOCIDLogOut  " + userDocId);
                if (userDocId != null && !userDocId.isEmpty()) {
                    MutableDocument userDoc = userDatabase.getDocument(userDocId).toMutable();
                    //set this user is logged out to check when on initiateActivity
                    userDoc.setBoolean(Constants.IS_LOG_IN, false);
                    userDatabase.save(userDoc);
                    Intent signInIntent = new Intent(HomeActivity.this, SignInActivity.class);
                    startActivity(signInIntent);
                    finish();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.something_wrong_msg), Toast.LENGTH_SHORT).show();
                }

            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }

        }


        drawerLayout.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            getOldChatsFromDB();
        } catch (CouchbaseLiteException e) {
            Log.d(TAG, "onStart(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onWifiOff() {

        tvWifiStatus.setVisibility(View.VISIBLE);
        btnWifiStart.setVisibility(View.VISIBLE);
    }
}