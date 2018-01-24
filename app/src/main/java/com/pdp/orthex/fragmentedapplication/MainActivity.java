package com.pdp.orthex.fragmentedapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    ft.replace(R.id.fragment_frame, new HomeFragment()).commit();
                    return true;
                case R.id.navigation_dashboard:
                    startAddItemActivity();
                    return true;
                case R.id.navigation_notifications:
                    ft.replace(R.id.fragment_frame, new NotificationsFragment()).commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
        }
        setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        if(YourSingleton.getUserid()==null){
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
        }
        getArrayList();
        refreshFragment();
        if(Build.VERSION.SDK_INT>=21){//creates color for status bar
            Window window =this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));
        }
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFragment();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveArrayList();
    }


    private void refreshFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_frame, new HomeFragment());
        fragmentTransaction.commit();
    }

    private void startAddItemActivity() {
        Intent intent = new Intent(getApplicationContext(), ItemadditionActivity.class);
        startActivity(intent);
    }

    public void saveArrayList(){
        if(YourSingleton.getUserid()!=null){
            User user = new User(YourSingleton.getUserid(),
                    YourSingleton.getUsername(),YourSingleton.getInstance().getArray());
            String key1 = YourSingleton.getUserid();
            mDatabase.child(key1).setValue(user);
        }
    }

    public void getArrayList(){
        final String userId = YourSingleton.getUserid();
        mDatabase.child(userId).child("userlist").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user list
                        ArrayList<String> list1= new ArrayList<>();
                        for(DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                            list1.add((String) postSnapshot.getValue());
                        }
                        Object value = dataSnapshot.getValue();
                        // [START_EXCLUDE]
                        if (value == null) {
                            // User is null, error out
                            Log.e(userId, "User " + userId + " is unexpectedly null");
                            Toast.makeText(MainActivity.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                            ArrayList<String> e= new ArrayList<>();
                            saveArrayList();
                        } else {
                            YourSingleton.getInstance().replaceArray(list1);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("does it matter", "getUser:onCancelled", databaseError.toException());
                    }
                }
        );
        // [END single_value_read]
    }
}
