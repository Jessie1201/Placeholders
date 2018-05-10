package pdp.placeholders;

/**
 This class gets items from the database and writes (getArrayList) to the database (saveArrayList)
 Further reading https://firebase.google.com/docs/database/android/read-and-write
 */
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.Nullable;


public class FirebaseHelper {

    static void getArrayList(@Nullable final Context context, @Nullable final Class LaunchActivity){
        final String BOXES = "Boxes";
        final String userlist = "userlist";
        final String serverUpdate = "lastupdate";
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        try{
            final String userId = UserItems.getUserid();
            if (userId!=null){
                mDatabase.child(userId).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                try{
                                    // Get user list'
                                    ArrayList<String> list1;
                                    list1 = (ArrayList<String>) dataSnapshot.child(userlist).getValue();
                                    int serverVersion = Integer.valueOf(dataSnapshot.child(serverUpdate).getValue().toString());
                                    int a=UserItems.getLastUpdate();
                                    if (a <=serverVersion) {
                                        UserItems.lastUpdate=serverVersion;
                                        UserItems.eaten=Integer.valueOf(dataSnapshot.child("eaten").getValue().toString());
                                        //UserItems.thrownout =(ArrayList<String>)dataSnapshot.child("thrownout").getValue();
                                        UserItems.setList(list1);
                                        HashMap<String, User.Box> FBoxes = new HashMap<>();
                                        for (DataSnapshot postSnapshot : dataSnapshot.child(BOXES).getChildren()) {
                                            String boxid = postSnapshot.getKey();
                                            String boxexpiration = postSnapshot.child("expiration").getValue().toString();
                                            String boxitem = postSnapshot.child("itemname").getValue().toString();
                                            String boxupdate = postSnapshot.child("updatevalue").getValue().toString();
                                            User.Box newbox = new User.Box(boxitem,boxexpiration,boxupdate);
                                            FBoxes.put(boxid,newbox);

                                         }
                                        if(FBoxes.size()>0){UserItems.setBoxes(FBoxes);}
                                        if(LaunchActivity!=null && context!=null){
                                            Intent intent =new Intent(context, LaunchActivity);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            context.startActivity(intent);
                                            ((Activity)context).finish();
                                        }else if(LaunchActivity==null && context!=null){
                                            //case where i want to launch the notification
                                            ShowNotificationAlarm.createNoitification(context);
                                        }

                                    } else {saveArrayList();}
                                }catch (Exception e){
                                    Exception a = e;

                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w("does it matter", "getUser:onCancelled", databaseError.toException());
                            }
                        }
                );
            }
        } catch (NullPointerException e) { Log.e( "getarraylistNullpointer","nullpointer in getarraylist"); }
        // [END single_value_read]
    }

    static void saveArrayList(){
        String BOXES = "Boxes";
        String userlist = "userlist";
        String serverUpdate = "lastupdate";
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users");
        if(UserItems.getUserid()!=null){
            if (UserItems.getInstance().getBoxes()==null){
                User.Box beta = new User.Box("item","2018-10-01","value");
                UserItems.addBox("savebox",beta);
            }
            User user = new User(UserItems.getUserid(),
                    UserItems.getUsername(), UserItems.getList(), UserItems.getInstance().getLastUpdate(),
                    UserItems.getInstance().getBoxes());
            String key1 = UserItems.getUserid();
            mDatabase.child(key1).setValue(user);
        }
    }
}
