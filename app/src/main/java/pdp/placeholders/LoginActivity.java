package pdp.placeholders;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.apache.http.protocol.HTTP;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: Add authentication to database.
     */
    private static final String TAG = "EmailPassword";
    private EditText mEmailView;
    private EditText mPasswordView;
    private ProgressBar mProgressView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Checks if there are any saved users on the phone
        SharedPreferences sprefs = getSharedPreferences("userprefs",MODE_PRIVATE);
        if(sprefs.contains("username")){
            UserItems.setUsername(sprefs.getString("username",null));
            UserItems.setUserId(sprefs.getString("userid",null));
            ArrayList<String> list1 = new ArrayList<>(sprefs.getStringSet("userlist",null));
            FirebaseHelper.getArrayList(this,MainActivity.class);
        }

        if(UserItems.getUserid()!=null){
            Intent mIntent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(mIntent);
            finish();
        }
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mAuth = FirebaseAuth.getInstance();

        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mProgressView = findViewById(R.id.login_progress);

        mEmailView.setText("thenew@new.com");
        mPasswordView.setText("123456");
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    final String email = mEmailView.getText().toString();
                    final String password = mPasswordView.getText().toString();
                    signIn(email, password);
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = mEmailView.getText().toString();
                String password = mPasswordView.getText().toString();
                Toast.makeText(LoginActivity.this, "Authenticating", Toast.LENGTH_SHORT).show();

                signIn(email, password);

            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailView.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError("Required.");
            valid = false;
        } else {
            mEmailView.setError(null);
        }

        String password = mPasswordView.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError("Required.");
            valid = false;
        } else {
            mPasswordView.setError(null);
        }

        return valid;
    }

    private void createAccount(String email, String password){
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {return;}
        mProgressView.setVisibility(View.VISIBLE);

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this,"Creating account",Toast.LENGTH_SHORT).show();
                            UserItems.setUserId(user.getUid());
                            UserItems.setUsername(mEmailView.getText().toString());
                            UserItems.getInstance().addToList("remove this after adding new item;/;2018-01-10;/;2018-01-10");
                            FirebaseHelper.saveArrayList();
                            FirebaseHelper.getArrayList(LoginActivity.this,MainActivity.class);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "createUserWithEmail failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END create_user_with_email]
    }

    private void signIn(String email, String password){
        mProgressView.setVisibility(View.VISIBLE);
        if(isEmailValid(email) && isPasswordValid(password)) {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                UserItems.setUserId(user.getUid());
                                UserItems.setUsername(mEmailView.getText().toString());
                                mProgressView.setVisibility(View.GONE);
                                FirebaseHelper.getArrayList(LoginActivity.this,MainActivity.class);

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                signupDialog();
                                mProgressView.setVisibility(View.GONE);
                            }
                        }
                    });
        } else {
            Toast.makeText(LoginActivity.this, "Email and/or password not valid",
                    Toast.LENGTH_SHORT).show();
            mProgressView.setVisibility(View.GONE);
        }
    }
    private void signupDialog(){
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder
                .setMessage(R.string.signUpPrompt)
                .setPositiveButton(R.string.signUpAccept, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        createAccount(email, password);
                    }
                })
                .setNegativeButton(R.string.signUpDeny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
        builder.create().show();
    }




    private boolean isEmailValid(String email) {
        // Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        // Replace this with your own logic
        return password.length() >= 0;
    }




}

