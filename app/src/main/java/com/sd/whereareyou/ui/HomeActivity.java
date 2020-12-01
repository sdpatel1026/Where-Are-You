package com.sd.whereareyou.ui;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.sd.whereareyou.utils.Constants.FRIENDS_LIST_DB;
import static com.sd.whereareyou.utils.Constants.FRIEND_UID;
import static com.sd.whereareyou.utils.Constants.FRIEND_USER_NAME;
import static com.sd.whereareyou.utils.Constants.UID;
import static com.sd.whereareyou.utils.Constants.USER_NAME;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {


    private static final String TAG = HomeActivity.class.getName();
    Date time;
    private List<FriendBlock> friendBlockList;
    private RecyclerView recyclerView;
    private FriendListAdapter friendListAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private TextView tvNavUID, tvNavUsername;
    private FloatingActionButton fab;
    private WifiManager wifiManager;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private DatabaseConfiguration dbConfig;
    private Database friendListDb;
    private String myUID;
    private String myUsername;
    private String friendUsername, friendUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        intialiseUiElements();
        initialiseObjects();

    }


    private void getOldChatsFromDB() throws CouchbaseLiteException {

        friendBlockList.clear();
        Query query = QueryBuilder.select(SelectResult.property(FRIEND_UID), SelectResult.property(FRIEND_USER_NAME))
                .from(DataSource.database(friendListDb));
        ResultSet resultSet = query.execute();
        List<Result> results = resultSet.allResults();
        int size = results.size();
        for (int i = 0; i < size; i++) {
            friendUID = results.get(i).getString(FRIEND_UID);
            friendUsername = results.get(i).getString(FRIEND_USER_NAME);
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

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);

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
                Intent intent = new Intent(HomeActivity.this, OldChatActivity.class);
                intent.putExtra(FRIEND_USER_NAME, fname);
                intent.putExtra(FRIEND_UID, fuid);
                startActivity(intent);
            }
        });


    }

    private void intialiseUiElements() {
        recyclerView = findViewById(R.id.recyclerViewContentHome);
        toolbar = findViewById(R.id.toolbarHome);
        drawerLayout = findViewById(R.id.drawerLayoutHome);
        navigationView = findViewById(R.id.navViewHome);
        tvNavUsername = findViewById(R.id.navHeaderUsername);
        fab = findViewById(R.id.fabHome);
        fab.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.chats));
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Intent intent = getIntent();
        myUsername = intent.getStringExtra(Constants.USER_NAME);
        myUID = intent.getStringExtra(intent.getStringExtra(Constants.UID));
        tvNavUID = navigationView.getHeaderView(0).findViewById(R.id.navHeaderUID);
        tvNavUID.setText(myUID);
        tvNavUsername = navigationView.getHeaderView(0).findViewById(R.id.navHeaderUsername);
        tvNavUsername.setText(myUsername);


    }

    @Override
    public void onClick(View view) {

        Intent intent = new Intent(this, UserDiscovery.class);
        intent.putExtra(USER_NAME, myUsername);
        intent.putExtra(UID, myUID);
        startActivity(intent);
    }

    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        //list menu in navigation drawer
        if (id == R.id.navLogout) {
            try {
                // Get the database (and create it if it doesn’t exist).
                Database userDatabase = new Database(Constants.USERS_LIST_DB, dbConfig);
                Intent intent = getIntent();
                String userDocId = intent.getStringExtra(Constants.USER_DOC_ID);
                MutableDocument userDoc = userDatabase.getDocument(userDocId).toMutable();
                //set this user is logged out to check when on initiateActivity

                userDoc.setBoolean(Constants.IS_LOG_IN, false);
                userDatabase.save(userDoc);
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(HomeActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
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
}