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
import com.couchbase.lite.Result;
import com.couchbase.lite.ResultSet;
import com.couchbase.lite.SelectResult;
import com.sd.whereareyou.R;
import com.sd.whereareyou.utils.Constants;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import static com.sd.whereareyou.utils.Constants.IS_LOG_IN;
import static com.sd.whereareyou.utils.Constants.PASSWORD;
import static com.sd.whereareyou.utils.Constants.UID;
import static com.sd.whereareyou.utils.Constants.USERS_LIST_DB;
import static com.sd.whereareyou.utils.Constants.USER_DOC_ID;
import static com.sd.whereareyou.utils.Constants.USER_NAME;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SignInActivity.class.getName();
    private AppCompatEditText edtUserName, edtPassword;
    private Button btnSignIn;
    private TextView tvForgot, tvSignUp;
    private DatabaseConfiguration dbConfig;
    private Database userListDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        initialiesUiElements();
        initialiesDataBase();


    }

    private void initialiesDataBase() {

        dbConfig = new DatabaseConfiguration(this);
        try {
            userListDb = new Database(USERS_LIST_DB, dbConfig);
        } catch (CouchbaseLiteException e) {
            displayMsg(e.getMessage());
            Log.d(TAG, "onCreate(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initialiesUiElements() {
        edtUserName = findViewById(R.id.edtUserName);
        edtPassword = findViewById(R.id.edtPassword);
        tvForgot = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvSignUp.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);

    }

    private void displayMsg(String errMsg) {
        Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tvSignUp:
                moveToSignUpActivity();
                break;
            case R.id.btnSignIn:
                signInUser();
                break;

        }
    }

    private void moveToSignUpActivity() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    private void signInUser() {
        String userName = edtUserName.getText().toString().toUpperCase().trim();
        String password = edtPassword.getText().toString().trim();
        Query query = QueryBuilder.select(SelectResult.property(Constants.USER_NAME))
                .from(DataSource.database(userListDb))
                .where(Expression.property(Constants.USER_NAME).equalTo(Expression.string(userName)));
        ResultSet resultSet = null;
        try {
            resultSet = query.execute();
        } catch (CouchbaseLiteException e) {
            Log.d(TAG, "onClick: ()" + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (resultSet.allResults().size() == 1) {
            query = QueryBuilder
                    .select(SelectResult.property(USER_DOC_ID))
                    .from(DataSource.database(userListDb))
                    .where(Expression.property(USER_NAME).equalTo(Expression.string(userName)));
            try {
                resultSet = query.execute();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
                return;
            }
            Result result = resultSet.allResults().get(0);
            String userDocId = result.getString(USER_DOC_ID);
            MutableDocument userDocument = userListDb.getDocument(userDocId).toMutable();
            if (userDocument.getString(PASSWORD).equals(password)) {
                // byte[] imageInByte = userDocument.getBlob(IMAGE).getContent();
                String UUID = userDocument.getString(UID);
                userDocument.setBoolean(IS_LOG_IN, true);
                try {
                    userListDb.save(userDocument);
                    Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                    // intent.putExtra(IMAGE, imageInByte);
                    intent.putExtra(UID, UUID);
                    intent.putExtra(USER_DOC_ID, userDocId);
                    intent.putExtra(USER_NAME, userName);
                    startActivity(intent);
                    finish();
                } catch (CouchbaseLiteException e) {
                    Toast.makeText(this, getResources().getString(R.string.something_wrong_msg), Toast.LENGTH_SHORT).show();
                    userDocument.setBoolean(IS_LOG_IN, false);
                    Log.d(TAG, "onClick: ()" + e.getMessage());
                    e.printStackTrace();
                    return;
                }


            } else {
                displayMsg(getResources().getString(R.string.invalid_password));
            }
        } else {
            displayMsg(getResources().getString(R.string.user_not_exist));
        }

    }
}