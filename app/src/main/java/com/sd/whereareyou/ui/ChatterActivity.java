package com.sd.whereareyou.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sd.whereareyou.R;
import com.sd.whereareyou.adapter.CustomAdapter;
import com.sd.whereareyou.models.Chat;
import com.sd.whereareyou.models.PeerInfo;
import com.sd.whereareyou.utils.ClientClass;
import com.sd.whereareyou.utils.Constants;
import com.sd.whereareyou.utils.OnCreateSendReceiveListener;
import com.sd.whereareyou.utils.SendReceive;
import com.sd.whereareyou.utils.ServerClass;
import com.sd.whereareyou.utils.WiFiDirectBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import static com.sd.whereareyou.utils.Constants.CHAT_TYPE;
import static com.sd.whereareyou.utils.Constants.DEVICE_TYPE;
import static com.sd.whereareyou.utils.Constants.FRIENDS_LIST_DB;
import static com.sd.whereareyou.utils.Constants.FRIEND_DOC_ID;
import static com.sd.whereareyou.utils.Constants.FRIEND_UID;
import static com.sd.whereareyou.utils.Constants.FRIEND_USER_NAME;
import static com.sd.whereareyou.utils.Constants.INIT_CONNECTION;
import static com.sd.whereareyou.utils.Constants.IS_GROUP_OWNER;
import static com.sd.whereareyou.utils.Constants.MESSAGE;
import static com.sd.whereareyou.utils.Constants.MESSAGE_READ;
import static com.sd.whereareyou.utils.Constants.RECEIVE;
import static com.sd.whereareyou.utils.Constants.RECEIVE_CONNECTION;
import static com.sd.whereareyou.utils.Constants.SEND;
import static com.sd.whereareyou.utils.Constants.UID;
import static com.sd.whereareyou.utils.Constants.USER_NAME;

public class ChatterActivity extends AppCompatActivity implements View.OnClickListener, WifiP2pManager.ConnectionInfoListener, OnCreateSendReceiveListener {

    private static final String TAG = ChatterActivity.class.getName();

    private static final String TYPE = "type";
    private static final String META = "meta";
    private static final String CHAT = "chat";
    private static final String LAT = "latitude";
    private static final String LONG = "longitude";
    private static final String INDEX = "index";


    private FloatingActionButton btnSend;
    private FloatingActionButton btnAr;
    private EditText edtMsgBox;
    private ListView listViewMsg;

    private Handler handler;
    private PeerInfo peerInfo;
    private WifiManager wifiManager;
    private WifiP2pManager wifiP2pManager;
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;
    private SendReceive sendReceive;
    private ServerClass serverClass;
    private ClientClass clientClass;
    private String deviceType;
    private InetAddress groupOwnerAddress;


    private String friendDocID, friendUID, friendUsername;
    private String myUsername;
    private String myUID;
    private Database friendListDb;
    private Database chatsDb;
    private DatabaseConfiguration dbConfig;

    private List msgList = new ArrayList<Chat>();
    private CustomAdapter adapter;
    private Vibrator vibrator;

    private String friendLong = null;
    private String friendLat = null;
    private Boolean metaSent = false;


    private LocationManager locationManager;
    private Double myLat = null;
    private Double myLong = null;
    private Location myLocation;

    private Boolean isGroupOwner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatter);

        initialiseUiElements();
        initialiseObjects();
        createHandler();
        Intent intent = getIntent();
        peerInfo = (PeerInfo) intent.getSerializableExtra(Constants.PEER);
        deviceType = intent.getStringExtra(DEVICE_TYPE);
        myUsername = intent.getStringExtra(USER_NAME);
        myUID = intent.getStringExtra(UID);

        if (deviceType.equals(INIT_CONNECTION)) {
            initiateConnection(INIT_CONNECTION, peerInfo);
            friendUID = peerInfo.getUid();
            friendUsername = peerInfo.getUsername();
        } else if (deviceType.equals(RECEIVE_CONNECTION)) {
            isGroupOwner = intent.getBooleanExtra(IS_GROUP_OWNER, false);
            initiateConnection(RECEIVE_CONNECTION, peerInfo);
        }

    }


    private void initiateConnection(String connectionType, PeerInfo peerInfo) {
        if (connectionType.equals(INIT_CONNECTION)) {
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = peerInfo.getDeviceAddress();
            config.wps.setup = WpsInfo.PBC;

            wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_request_successful), Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_request_unsuccessful), Toast.LENGTH_SHORT).show();

                }
            });
        } else if (connectionType.equals(RECEIVE_CONNECTION)) {
               /*if(isGroupOwner)
               {
                   serverClass = new ServerClass(handler);
                   serverClass.setOnCreateSendReceiveListener(this);
                   serverClass.start();


               }
               else
               {
                   groupOwnerAddress = peerInfo.getGroupOwnerAddress();
                   clientClass = new ClientClass(groupOwnerAddress,handler);
                    clientClass.setOnCreateSendReceiveListener(this);
                   clientClass.start();

               }*/
        }
    }

    private void initialiseObjects() {
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        broadcastReceiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        adapter = new CustomAdapter(ChatterActivity.this, msgList);
        listViewMsg.setAdapter(adapter);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        dbConfig = new DatabaseConfiguration(this);
        try {
            friendListDb = new Database(FRIENDS_LIST_DB, dbConfig);
        } catch (CouchbaseLiteException e) {
            Log.d(TAG, "initialiseObjects(): " + e.getMessage());
            e.printStackTrace();
        }
        createHandler();

    }

    private void createHandler() {

        handler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msgObj) {

                switch (msgObj.what) {
                    case MESSAGE_READ:
                        processReceivedMessage(msgObj);
                        break;
                }
                return true;
            }
        });
    }

    private void processReceivedMessage(Message msgobj) {

        byte[] readBuff = (byte[]) msgobj.obj;
        String tempMsg = new String(readBuff, 0, msgobj.arg1);
        try {
            JSONObject parsedMessage = new JSONObject(tempMsg);
            if (parsedMessage.get(TYPE).equals(META)) {
                friendUsername = parsedMessage.getString(USER_NAME);
                friendUID = parsedMessage.getString(UID);
                getSupportActionBar().setTitle(friendUsername);
                storeFriendToDB(friendUsername, friendUID);


            } else if (parsedMessage.get(TYPE).equals(CHAT)) {
                String message = parsedMessage.getString(MESSAGE);
                friendLong = parsedMessage.getString(LONG);
                friendLat = parsedMessage.getString(LAT);
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                Chat chat = new Chat(message, false);
                msgList.add(chat);
                adapter.notifyDataSetChanged();
                saveMessageToDB(parsedMessage.toString(), false);


            }
        } catch (JSONException | CouchbaseLiteException e) {
            Log.d(TAG, "processReceivedMessage(): " + e.getMessage());
            e.printStackTrace();
        }

    }

    //If this is a new friend, creates a new doc to store this new friend in friendListDb Database
    private void storeFriendToDB(String friendUsername, String friendUID) {

        MutableDocument friendDoc;
        try {
            //check if this friendUUID is in friend list
            Query query = QueryBuilder.select(SelectResult.property(FRIEND_UID))
                    .from(DataSource.database(friendListDb))
                    .where(Expression.property(FRIEND_UID).equalTo(Expression.string(friendUID)));
            ResultSet rs = query.execute();


            if (rs.allResults().size() == 0) {
                friendDoc = new MutableDocument();
                friendDocID = friendDoc.getId();
                friendDoc.setString(FRIEND_DOC_ID, friendDocID);
                friendDoc.setString(FRIEND_UID, friendUID);
                friendDoc.setString(FRIEND_USER_NAME, friendUsername);
                friendListDb.save(friendDoc);
                Log.d(TAG, "storeFriendToDB(): new  friend added");
            }
        } catch (CouchbaseLiteException e) {
            Log.d(TAG, "storeFriendToDB(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initialiseUiElements() {

        btnSend = (FloatingActionButton) findViewById(R.id.fabSendChatter);
        edtMsgBox = (EditText) findViewById(R.id.edtMsgBoxChatter);
        listViewMsg = (ListView) findViewById(R.id.listViewChatter);
        btnAr = (FloatingActionButton) findViewById(R.id.fabArChatter);

        btnSend.setOnClickListener(this);
        btnAr.setOnClickListener(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fabSendChatter:
                // send message
                sendMessage();

                break;
            case R.id.fabArChatter:
                // start AR activity
                break;
        }
    }

    private void sendMessage() {
        String message = edtMsgBox.getText().toString();
        if (message.isEmpty()) {
            Toast.makeText(this, getResources().getString(R.string.empty_message), Toast.LENGTH_SHORT).show();
            return;
        }


        //Send identity to verify connection
        try {
            String metadata = createJSONMeta(myUID, myUsername);
            sendReceive.write(metadata.getBytes());
            metaSent = true;
        } catch (JSONException e) {
            Toast.makeText(this, getResources().getString(R.string.something_wrong_msg), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "sendMessage(): " + e.getMessage());
            e.printStackTrace();
        }


        try {
            // String packetToSend = createJSONChat(message, myLat.toString(), myLong.toString());
            String packetToSend = createJSONChat(message, "30.3827", "31.0932");

            //Send message to peer
            sendReceive.write(packetToSend.getBytes());
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            //save message in database
            saveMessageToDB(packetToSend, true);
            // Display the text message on listview
            Chat chat = new Chat(message, true);
            msgList.add(chat);
            adapter.notifyDataSetChanged();
            edtMsgBox.setText("");


        } catch (JSONException | CouchbaseLiteException e) {
            Log.d(TAG, "sendMessage(): " + e.getMessage());
            e.printStackTrace();
        }


    }

    private void saveMessageToDB(String message, Boolean isSend) throws CouchbaseLiteException {
        //create new or open exist chat db by using freindUID
        chatsDb = new Database(friendUID, dbConfig);
        //Every message will be a new doc
        MutableDocument messageDoc = new MutableDocument();
        int lastIndex = (int) chatsDb.getCount();
        //lastIndex can be last index
        //such as when count is 0, means there is no previous message
        //and the current message will be at index 0 to new chat db.
        if (isSend) {
            messageDoc.setString(CHAT_TYPE, SEND);
        } else {
            messageDoc.setString(CHAT_TYPE, RECEIVE);
        }
        messageDoc.setInt(INDEX, lastIndex);
        messageDoc.setString(MESSAGE, message);
        chatsDb.save(messageDoc);


    }


    private String createJSONMeta(String uid, String username) throws JSONException {
        try {
            JSONObject meta = new JSONObject();
            meta.put(TYPE, META);
            meta.put(UID, uid);
            meta.put(USER_NAME, username);
            Log.d(TAG, "createJSONMeta: " + meta.toString());
            return meta.toString();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private String createJSONChat(String msg, String lat, String lon) throws JSONException {
        try {
            JSONObject chat = new JSONObject();
            chat.put(TYPE, CHAT);
            chat.put(LAT, lat);
            chat.put(LONG, lon);
            chat.put(MESSAGE, msg);
            return chat.toString();
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            Log.d(TAG, "onConnectionInfoAvailable(): server");
            if (serverClass == null) {
                serverClass = new ServerClass(handler);
                serverClass.setOnCreateSendReceiveListener(this);
                serverClass.start();

            }
            getSupportActionBar().setTitle(getResources().getString(R.string.new_device));
        } else if (wifiP2pInfo.groupFormed) {
            Log.d(TAG, "onConnectionInfoAvailable(): client");
            if (clientClass == null) {
                clientClass = new ClientClass(groupOwnerAddress, handler);
                clientClass.setOnCreateSendReceiveListener(this);
                clientClass.start();

            }
            getSupportActionBar().setTitle(getResources().getString(R.string.group_owner) + groupOwnerAddress.toString());
        }
    }

    @Override
    public void onCreateSendReceive(SendReceive sendReceive) {
        this.sendReceive = sendReceive;
    }
}