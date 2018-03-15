package pdp.placeholders;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private TextView UserNameText;
    private HashMap<String, List<String>> listHash;
    public static final int TAG_NAME = ExpandableListAdapter.TAG_NAME;
    public static final int TAG_DATE = ExpandableListAdapter.TAG_DATE;
    public static final int TAG_DELETE = ExpandableListAdapter.TAG_DELETE;
    public static final int TAG_DISPOSE = ExpandableListAdapter.TAG_DISPOSE;
    public static final int TAG_EDIT = ExpandableListAdapter.TAG_EDIT;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //This part creates a "job" that is taken care of in the ShowNotificationJob
        ComponentName componentName = new ComponentName(this,ShowNotificationJob.class);
        final int notificationID = 3478916;
        JobInfo.Builder builder = new JobInfo.Builder(notificationID+1,componentName);
        builder.setPeriodic(TimeUnit.DAYS.toMillis(1));
        builder.setPersisted(true);
        JobScheduler jbSched; JobInfo jobInfo; jobInfo = builder.build();
        jbSched = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jbSched.schedule(jobInfo);
        
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
        //creates the floating button
        FloatingActionButton btnAddItem = (FloatingActionButton)findViewById(R.id.btnadditem2);
        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddItemActivity();
            }
        });
        refreshFragment();
        if(Build.VERSION.SDK_INT>=21){//creates color for status bar
            Window window =this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.colorPrimary));
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        FirebaseHelper.getArrayList(null,null);
        refreshFragment();
        if(UserItems.getUserid()==null){
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
        }
    }

    private void refreshFragment() {
        //Creates the list, and a button for each item
        itemlist = (ExpandableListView)findViewById(R.id.expndlist);
        itemlist.setChildDivider(getResources().getDrawable(R.color.colorPrimaryDark));
        initData(UserItems.getInstance().getList());
        listAdapter = new ExpandableListAdapter(getApplicationContext(), listDataHeader,listHash);
        itemlist.setAdapter(listAdapter);
        itemlist.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, final int groupPosition, final int childPosition, long id) {
                int id1 = 1000 * groupPosition + childPosition + 1000;
                final Button btnItemDelete = (Button) v.findViewWithTag(id1 + TAG_DELETE);
                final Button btnItemDispose = (Button) v.findViewWithTag(id1 + TAG_DISPOSE);
                final Button btnItemEdit = (Button) v.findViewWithTag(id1 + TAG_EDIT);
                final TextView itemName = (TextView) v.findViewWithTag(id1 + TAG_NAME);
                final TextView itemDate = (TextView) v.findViewWithTag(id1 + TAG_DATE);
                ViewGroup.LayoutParams params = itemlist.getLayoutParams();
                ViewGroup.LayoutParams params2 = btnItemDelete.getLayoutParams();
                if (btnItemDelete.getVisibility() == View.GONE || btnItemDelete.getVisibility() == View.INVISIBLE) {
                    btnItemDelete.setVisibility(View.VISIBLE);
                    btnItemDispose.setVisibility(View.VISIBLE);
                    btnItemEdit.setVisibility(View.VISIBLE);
                    btnItemDispose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            btnItemDispose.setBackgroundColor(getResources().getColor(R.color.colorOGreen));
                        }
                    });
                    btnItemDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Object item = listAdapter.getChild(groupPosition, childPosition);
                            UserItems.removeItem(item.toString());
                            btnItemDelete.setText("Gone");
                            btnItemDelete.setBackgroundColor(getResources().getColor(R.color.colorOGreen));
                            btnItemDelete.setVisibility(View.GONE);
                            btnItemDispose.setVisibility(View.GONE);
                            btnItemEdit.setVisibility(View.GONE);
                            itemName.setVisibility(View.GONE);
                            itemDate.setVisibility(View.GONE);

                        }
                    });
                    //params.height = a + params2.height;
                } else {
                    btnItemDelete.setVisibility(View.GONE);
                    btnItemDispose.setVisibility(View.GONE);
                    btnItemEdit.setVisibility(View.GONE);
                }
                return true;
            }
        });
    }

    private void startAddItemActivity() {
        Intent intent = new Intent(getApplicationContext(), ItemadditionActivity.class);
        startActivity(intent);
    }

    // TODO: 13.3.2018 create box layout and a new box activity
    // TODO: 13.3.2018 wifi credentials pass through


    private void initData(List<String> itmlist){
        //creates the sublists for the subgroups
        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();
        listDataHeader.add("Short term products");
        listDataHeader.add("Medium term products");
        listDataHeader.add("Long term products");
        listDataHeader.add("Boxes");

        List<String> shrtTerm = new ArrayList<>(); //creates different sections
        List<String> mdmTerm = new ArrayList<>();
        List<String> lngTerm = new ArrayList<>();
        List<String> boxTerm = new ArrayList<>();

        HashMap<String,User.Box> BoxesSorted =UserItems.getBoxes();
        Object[] array = BoxesSorted.keySet().toArray();
        for(Object item:array){boxTerm.add(item.toString()+" - "
                +BoxesSorted.get(item).itemname+UserItems.itemListDelimiter+BoxesSorted.get(item).expiration);}

        for(String item : itmlist){  //goes through list and adds them to appropriate section
            String[] txt1 = item.replace(UserItems.BoxDelimiter,UserItems.itemListDelimiter).split(UserItems.itemListDelimiter);
            String txtdate = txt1[1];
            try {
                DateFormat formatter = new SimpleDateFormat("yyy-MM-dd");
                Date txtDate1 = formatter.parse(txtdate);
                Date currentdate = Calendar.getInstance().getTime();
                long diff = (txtDate1.getTime() - currentdate.getTime()) / (1000 * 60 * 60 * 24);
                if (diff < 7) {shrtTerm.add(item);
                } else {
                    if (diff < 30) {mdmTerm.add(item);
                    } else {lngTerm.add(item);}
                }
            } catch (ParseException e) { e.printStackTrace();}
        }
        if (shrtTerm.size()==0)shrtTerm.add("No Items Yet");
        if (mdmTerm.size()==0)mdmTerm.add("No Items Yet");
        if (lngTerm.size()==0)lngTerm.add("No Items Yet");
        if (boxTerm.size()==0)boxTerm.add("No Items Yet");

        listHash.put(listDataHeader.get(0),shrtTerm);
        listHash.put(listDataHeader.get(1),mdmTerm);
        listHash.put(listDataHeader.get(2),lngTerm);
        listHash.put(listDataHeader.get(3),boxTerm);

    }

    @Override
    protected void onPause() {
        super.onPause();

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

}
