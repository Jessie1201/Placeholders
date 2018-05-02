package pdp.placeholders;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

// This activity makes the List of items, and displays them
/**
 * Todo: Create set up box activity
 *
 */
public class MainActivity extends Activity {
    //This defines the view items that are in the layout
    private ExpandableListView itemlist;
    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listHash;
    private TextView UserNameText;
    ImageView noItemImage;
    public Switch expSwitch;
    TextView noItemText;
    private TextView yourfoodText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noItemText =findViewById(R.id.noItemsText);
        noItemImage=(ImageView)findViewById(R.id.noItemsImage);
        expSwitch = findViewById(R.id.switch1);
        yourfoodText = (TextView)findViewById(R.id.yourfoodTitle);

        //ShowNotificationJob.startNotification(getApplicationContext());

        // Adds a button to the top part
        UserNameText =(TextView)findViewById(R.id.UserNameText);
        UserNameText.setText(UserItems.getUsername());
        UserNameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Creates popup to ask if you actually want to log out
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder
                        .setMessage(R.string.logoutText)
                        .setPositiveButton(R.string.signUpAccept, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UserItems.clearUser();
                                Toast.makeText(MainActivity.this, "You have been logged out", Toast.LENGTH_SHORT).show();
                                Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(loginIntent);
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.signUpDeny, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                builder.create().show();
            }
        });
        switchlistener();
        //creates the floating button
        FloatingActionButton btnAddItem = (FloatingActionButton)findViewById(R.id.btnadditem2);
        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddItemActivity();
            }
        });
        refreshFragment();



        if(UserItems.getList().size()<1){
            itemlist.setVisibility(View.GONE);
            expSwitch.setVisibility(View.GONE);
            yourfoodText.setVisibility(View.GONE);
        }else{
            noItemImage.setVisibility(View.GONE); noItemText.setVisibility(View.GONE);
            expSwitch.setVisibility(View.VISIBLE);
            itemlist.setVisibility(View.VISIBLE);
            yourfoodText.setVisibility(View.VISIBLE);

        }
        if(Build.VERSION.SDK_INT>=21){//creates color for status bar
            Window window =this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));
        }
        ImageView boxAdd = (ImageView)findViewById(R.id.addBoxImage);
        boxAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiReceiver.helperWifi(MainActivity.this);
            }
        });
    }

    private void startNotification(Context context) {
        /*Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE,50);
        cal.set(Calendar.HOUR_OF_DAY,11);
        if(cal.before(Calendar.getInstance())){
        //    cal.add(Calendar.HOUR,12);
        }
        Intent notintent = new Intent(this, ShowNotificationAlarm.class);
        PendingIntent pendintent = PendingIntent.getBroadcast(this,0,notintent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
        assert alarm != null;

        // interval for alarm find alarm fix alarm
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),AlarmManager.INTERVAL_FIFTEEN_MINUTES/15,pendintent);
        */
        ComponentName jobcomponent = new ComponentName(this,ShowNotificationJob.class);
        JobInfo.Builder jobInfo = new JobInfo.Builder(123123,jobcomponent);
        jobInfo.setPeriodic(TimeUnit.MINUTES.toMillis(1));
        jobInfo.setPersisted(true);
        jobInfo.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        JobInfo bultjob = jobInfo.build();
        JobScheduler jobScheduler = (JobScheduler)getSystemService(context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(bultjob);





    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseHelper.getArrayList(null,null);
        listAdapter.notifyDataSetChanged();
        refreshFragment();

    }
    public void switchlistener(){

        expSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    //UserItems.expired = 0;
                    UserItems.expiring=true;
                }else {UserItems.expiring=false;}
                listAdapter.notifyDataSetChanged();
                if(UserItems.expired==0 && isChecked){
                    noItemImage.setImageResource(R.drawable.ic_illustration_no_food);
                    noItemImage.setVisibility(View.VISIBLE);
                    itemlist.setVisibility(View.GONE);
                    noItemText.setVisibility(View.VISIBLE);
                    noItemText.setText("All your food is fresh and in order!");
                }else {noItemImage.setVisibility(View.GONE);
                noItemText.setVisibility(View.GONE);
                itemlist.setVisibility(View.VISIBLE);}
            }

        });
    }

    private void refreshFragment() {
        //Creates the list, and a button for each item

        itemlist = (ExpandableListView)findViewById(R.id.expndlist);
        //itemlist.setChildDivider(getResources().getDrawable(R.color.colorPrimaryDark));
        listAdapter = new ExpandableListAdapter(MainActivity.this, listDataHeader,listHash);
        listAdapter.initData(UserItems.getInstance().getList());
        listAdapter.getListHashMap();
        itemlist.setAdapter(listAdapter);
    }

    private void startAddItemActivity() {
        Intent intent = new Intent(getApplicationContext(), ItemadditionActivity.class);
        intent.putExtra("StartCamera",true);
        startActivity(intent);
    }

    // TODO: 13.3.2018 create box layout and a new box activity
    // TODO: 13.3.2018 wifi credentials pass through

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseHelper.getArrayList(null,null);
        SharedPreferences.Editor sprefs = getSharedPreferences("userprefs", MODE_PRIVATE).edit();
        sprefs.putString("username",UserItems.getUsername());
        sprefs.putString("userid",UserItems.getUserid());
        if ( UserItems.username==null){
            sprefs.putStringSet("userlist",null);
            sprefs.putStringSet("boxlist", null);
        }else{
            Set<String> listset = new HashSet<String>(UserItems.getInstance().getList());
            sprefs.putStringSet("userlist",listset);
        }
        sprefs.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //ShowNotificationJob.startNotification(this);
        }
}
