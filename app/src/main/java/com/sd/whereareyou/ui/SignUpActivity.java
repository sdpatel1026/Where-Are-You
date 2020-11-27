package com.sd.whereareyou.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.DatabaseConfiguration;
import com.sd.whereareyou.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import static com.sd.whereareyou.utils.Constants.USERS_LIST_DB;

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

        String username = edtUserName.getText().toString();
        String password = edtPassword.getText().toString();
        if (username.isEmpty()) {
            displayMsg(getResources().getString(R.string.username_required));
            return;
        }
        if (password.isEmpty()) {
            displayMsg(getResources().getString(R.string.password_required));
            return;
        }
    }
}