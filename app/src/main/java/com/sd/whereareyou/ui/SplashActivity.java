package com.sd.whereareyou.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.DataSource;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.couchbase.lite.Document;
import com.couchbase.lite.Expression;
import com.couchbase.lite.MutableDocument;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryBuilder;
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.sd.whereareyou.R;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import static com.sd.whereareyou.utils.Constants.IS_LOG_IN;
import static com.sd.whereareyou.utils.Constants.UID;
import static com.sd.whereareyou.utils.Constants.USERS_LIST_DB;
import static com.sd.whereareyou.utils.Constants.USER_DOC_ID;
import static com.sd.whereareyou.utils.Constants.USER_NAME;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";


    private Intent intent;
    private String myUID;
    private String myUserName;
    private String userDocId;


    private Handler handler;
    private DatabaseConfiguration dbConfig;
    private Database userDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Initialise Dtaabase
        initialiseDataBase();


        //check is user login or not?
        Boolean isUserLogin = false;
        try {
            isUserLogin = checkIsUserLogin();
        } catch (CouchbaseLiteException e) {
            Log.d(TAG, "onCreate(): " + e.getMessage());
            e.printStackTrace();
        }

        //route's user to appropriate activity.
        if (isUserLogin) {
            intent = new Intent(this, HomeActivity.class);
            intent.putExtra(UID, myUID);
            intent.putExtra(USER_NAME, myUserName);
            intent.putExtra(USER_DOC_ID, userDocId);
        } else {
            intent = new Intent(this, SignInActivity.class);
        }
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(intent);
                finish();
            }
        }, 2000);

    }

    private Boolean checkIsUserLogin() throws CouchbaseLiteException {

        Query query = QueryBuilder
                .select(SelectResult.property(USER_DOC_ID))
                .from(DataSource.database(userDB))

                .where(Expression.property(IS_LOG_IN).equalTo(Expression.string("true")));
        ResultSet resultSet = query.execute();
        int size = resultSet.allResults().size();
        List<Result> results = resultSet.allResults();
        //only one user login
        if (size == 1) {
            userDocId = results.get(0).getString(USER_DOC_ID);
            Document userDoc = userDB.getDocument(userDocId);
            myUserName = userDoc.getString(USER_NAME);
            myUID = userDoc.getString(UID);
            return true;

        } else if (size > 1) {
            //In case there is any bug that multiple users log in, forced to log them out.
            for (Result result : results) {
                userDocId = result.getString(USER_DOC_ID);
                MutableDocument mutableDocumentUserDoc = userDB.getDocument(userDocId).toMutable();
                mutableDocumentUserDoc.setString(IS_LOG_IN, "false");
                userDB.save(mutableDocumentUserDoc);
            }
        }
        return false;

    }

    private void initialiseDataBase() {


        dbConfig = new DatabaseConfiguration(getApplicationContext());
        try {
            userDB = new Database(USERS_LIST_DB, dbConfig);
        } catch (CouchbaseLiteException e) {
            Log.d(TAG, "initialiseDataBase(): " + e.getMessage());
            e.printStackTrace();
        }
    }
}