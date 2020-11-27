package com.sd.whereareyou.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
import com.sd.whereareyou.R;

import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import static com.sd.whereareyou.utils.Constants.IS_LOG_IN;
import static com.sd.whereareyou.utils.Constants.PASSWORD;
import static com.sd.whereareyou.utils.Constants.UID;
import static com.sd.whereareyou.utils.Constants.USERS_LIST_DB;
import static com.sd.whereareyou.utils.Constants.USER_DOC_ID;
import static com.sd.whereareyou.utils.Constants.USER_NAME;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SignUpActivity.class.getName() ;
    private Button btnSignUp;
    private AppCompatEditText edtUserName,edtPassword;
    private TextView tvSignIn;
    private DatabaseConfiguration dbConfig;
    private Database userListDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        initialiseUiElements();
        initialiesDataBase();
    }

    private void initialiesDataBase()
    {
        dbConfig = new DatabaseConfiguration(this);
        try {
            userListDb = new Database(USERS_LIST_DB, dbConfig);
        } catch (CouchbaseLiteException e) {
            displayMsg(e.getMessage());
            Log.d(TAG, "onCreate(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void displayMsg(String msg) {

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void initialiseUiElements() {
        tvSignIn = findViewById(R.id.tvSignIn);
        edtUserName = findViewById(R.id.edtUserNameSignUp);
        edtPassword = findViewById(R.id.edtPasswordSignUp);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(this);
        tvSignIn.setOnClickListener(this);
    }

    private void moveToSignInActivity()
    {
        Intent intent = new Intent(this,SignInActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.tvSignIn:
                moveToSignInActivity(); 
                break;
            case R.id.btnSignUp:
                signUpUser();
                break;
        }
    }

    private void signUpUser() {

        String username = edtUserName.getText().toString().toUpperCase();
        String password = edtPassword.getText().toString();
        if (username.isEmpty()) {
            displayMsg(getResources().getString(R.string.username_required));
            return;
        }
        if (password.isEmpty()) {
            displayMsg(getResources().getString(R.string.password_required));
            return;
        }
        if (password.length() < 6) {
            displayMsg(getResources().getString(R.string.password_length_msg));
            return;
        }
        Query query = QueryBuilder.select(SelectResult.property(USER_NAME))
                .from(DataSource.database(userListDb))
                .where(Expression.property(USER_NAME).equalTo(Expression.property(username)));
        ResultSet resultSet = null;
        try {
            resultSet = query.execute();
        } catch (CouchbaseLiteException e) {
            Log.d(TAG, "signUpUser(): " + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (resultSet.allResults().size() != 0) {
            displayMsg(getResources().getString(R.string.user_name_is_used));
            edtUserName.setText("");
            return;
        }
        String ID = generateUID(username);


        MutableDocument userDocument = new MutableDocument();
        String userDocId = userDocument.getId();
        userDocument.setString(USER_DOC_ID, userDocId);
        userDocument.setString(USER_NAME, username);
        userDocument.setString(PASSWORD, password);
        userDocument.setString(UID, ID);
        userDocument.setString(IS_LOG_IN, "true");
        try {
            userListDb.save(userDocument);
        } catch (CouchbaseLiteException e) {
            Log.d(TAG, "signUpUser(): " + e.getMessage());
            e.printStackTrace();
        }
        if (userListDb.getDocument(userDocId) != null) {

            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra(USER_DOC_ID, userDocId);
            intent.putExtra(UID, ID);
            intent.putExtra(USER_NAME, username);
            startActivity(intent);
            finish();
        } else {
            displayMsg(getResources().getString(R.string.sign_up_failed));
        }

    }

    //generate Unique ID for each user
    private String generateUID(String username) {
        Random random = new Random();
        int randomNum = random.nextInt(1000000);
        randomNum = random.nextInt(1000000000);
        return (username + randomNum);
    }
}