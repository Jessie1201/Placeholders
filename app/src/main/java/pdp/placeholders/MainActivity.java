package pdp.placeholders;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Combines for Does the same thing as the other main activity, Has less useless buttons */
public class MainActivity extends Activity {
    private DatabaseReference mDatabase;
    private ExpandableListView itemlist;
    private ExpandableListAdapter listAdapter;
    private List<String> listDataHeader;
    private HashMap<String, List<String>> listHash;
    public static final int TAG_NAME = ExpandableListAdapter.TAG_NAME;
    public static final int TAG_DATE = ExpandableListAdapter.TAG_DATE;
    public static final int TAG_DELETE = ExpandableListAdapter.TAG_DELETE;
    public static final int TAG_DISPOSE = ExpandableListAdapter.TAG_DISPOSE;
    public static final int TAG_EDIT = ExpandableListAdapter.TAG_EDIT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {}
        mDatabase = FirebaseDatabase.getInstance().getReference("users");
        if (YourSingleton.getInstance().getArray().size()>=1){
            saveArrayList();
        }
        getArrayList();
        setContentView(R.layout.activity_main);
        if(YourSingleton.getUserid()==null){
            Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(loginIntent);
        }
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
    protected void onStart() {
        super.onStart();
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
        itemlist = (ExpandableListView)findViewById(R.id.expndlist);
        //itemlist.setVisibility(View.GONE);
        //itemlist.setVisibility(View.VISIBLE);
        getArrayList();
        itemlist.setChildDivider(getResources().getDrawable(R.color.colorPrimaryDark));
        initData();
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
                    btnItemDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Object item = listAdapter.getChild(groupPosition, childPosition);
                            YourSingleton.removeFromArray(item);
                            btnItemDelete.setText("Gone");
                            btnItemDelete.setBackgroundColor(getResources().getColor(R.color.colorOGreen));
                            btnItemDelete.setVisibility(View.GONE);
                            btnItemDispose.setVisibility(View.GONE);
                            btnItemEdit.setVisibility(View.GONE);
                            itemName.setVisibility(View.GONE);
                            itemDate.setVisibility(View.GONE);

                            //refreshFragment();
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

    public void saveArrayList(){
        if(YourSingleton.getUserid()!=null){
            User user = new User(YourSingleton.getUserid(),
                    YourSingleton.getUsername(), YourSingleton.getInstance().getArray());
            String key1 = YourSingleton.getUserid();
            mDatabase.child(key1).setValue(user);
        }
    }
    private void initData(){
        listDataHeader = new ArrayList<>();
        listHash = new HashMap<>();
        listDataHeader.add("Short term products");
        listDataHeader.add("Medium term products");
        listDataHeader.add("Long term products");
        List<String> itmlist = YourSingleton.getInstance().getArray(); //creates list of items
        List<String> shrtTerm = new ArrayList<>(); //creates different sections
        List<String> mdmTerm = new ArrayList<>();
        List<String> lngTerm = new ArrayList<>();
        for(String item : itmlist){  //goes through list and adds them to appropriate section
            String[] txt1 = YourSingleton.getItemInfo(item);
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

        listHash.put(listDataHeader.get(0),shrtTerm);
        listHash.put(listDataHeader.get(1),mdmTerm);
        listHash.put(listDataHeader.get(2),lngTerm);

    }

    public void getArrayList(){
        try{
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
                        } else {
                            YourSingleton.getInstance().replaceArray(list1);
                            saveArrayList();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w("does it matter", "getUser:onCancelled", databaseError.toException());
                    }
                }
        );}
        catch (NullPointerException e) { saveArrayList();

        }
        // [END single_value_read]
    }
}
