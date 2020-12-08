package com.sd.whereareyou.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.sd.whereareyou.R;
import com.sd.whereareyou.adapter.CustomAdapter;
import com.sd.whereareyou.models.Chat;
import com.sd.whereareyou.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class OldChatActivity extends AppCompatActivity {

    private static final String TAG = OldChatActivity.class.getName();
    private DatabaseConfiguration dbConfig;
    private Database chatsDb;
    private ListView listView;
    private List<Chat> chatsList;
    private CustomAdapter adapter;
    private String friendUID, friendUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_old_chat);
        initialiseStuffs();
    }

    private void initialiseStuffs() {
        dbConfig = new DatabaseConfiguration(this);
        listView = findViewById(R.id.listviewOldChat);
        chatsList = new ArrayList<>();

        chatsList.add(new Chat("message for testing", true));
        chatsList.add(new Chat("message for testing", false));
        adapter = new CustomAdapter(this, chatsList);
        listView.setAdapter(adapter);
        Intent intent = getIntent();
        friendUID = intent.getStringExtra(Constants.FRIEND_UID);
        friendUserName = intent.getStringExtra(Constants.FRIEND_USER_NAME);

        getSupportActionBar().setTitle(friendUserName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        try {
            chatsDb = new Database(friendUID, dbConfig);

        } catch (CouchbaseLiteException e) {
            Log.d(TAG, "initialiseStuffs(): " + e.getMessage());
            e.printStackTrace();
        }


    }

    private void loadOldChats() throws CouchbaseLiteException {

        Query query = QueryBuilder.select(SelectResult.property(Constants.CHAT_TYPE), SelectResult.property(Constants.MESSAGE))
                .from(DataSource.database(chatsDb));
        ResultSet resultSet = query.execute();

        for (Result result : resultSet) {

            try {
                //get message from database
                String msg = result.getString(Constants.MESSAGE);
                JSONObject jsonMsg = new JSONObject(msg);
                String chatMessage = jsonMsg.getString(Constants.MESSAGE);
                if (result.getString(Constants.CHAT_TYPE).equals(Constants.SEND)) {
                    //sort message to right
                    Chat chat = new Chat(chatMessage, true);
                    chatsList.add(chat);
                    adapter.notifyDataSetChanged();
                } else if (result.getString(Constants.CHAT_TYPE).equals(Constants.RECEIVE)) {
                    //sort message to left
                    Chat chat = new Chat(chatMessage, false);
                    chatsList.add(chat);
                    adapter.notifyDataSetChanged();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            loadOldChats();
        } catch (CouchbaseLiteException e) {
            Log.d(TAG, "onStart(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}