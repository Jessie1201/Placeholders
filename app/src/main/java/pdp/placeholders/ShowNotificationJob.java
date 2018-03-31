package pdp.placeholders;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Valtteri on 6.3.2018.
 */

public class ShowNotificationJob extends JobService {

    NotificationCompat.Builder notification;
    private static final int notificationID = 3478916;

    @Override
    public boolean onStartJob(JobParameters params) {
        new backgroundtask(this).execute();
        return false;

    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public class backgroundtask extends AsyncTask<Void,Void,String>{
        List<String> listDataHeader=new ArrayList<>();
        HashMap<String, List<String>> listHash= new HashMap<>();

        private Context context;
        public backgroundtask(Context context){
            this.context= context;
        }
        @Override
        protected String doInBackground(Void... voids) {
            SharedPreferences sprefs = getSharedPreferences("userprefs",MODE_PRIVATE);
            if (sprefs.contains("username")) {
                UserItems.setUsername(sprefs.getString("username", null));
                UserItems.setUserId(sprefs.getString("userid", null));
                FirebaseHelper.getArrayList(null, null);
                ExpandableListAdapter listAdapter = new ExpandableListAdapter(context, listDataHeader, listHash);
                listAdapter.initData(UserItems.getList());
                listHash = listAdapter.listHashMap;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            int notificationID = 3478916;
            if(listHash.size()>0) {
                List<String> expired = listHash.get("Expired!");
                for (String s1 : expired) {
                    String[] txt1 = s1.split(UserItems.DELIMLIST);
                    if (txt1[0] != "No expired items, Good Job!") {
                        NotificationCompat.Builder notification;
                        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                            NotificationChannel mChannel = new NotificationChannel("id", "name", NotificationManager.IMPORTANCE_DEFAULT);
                            nm.createNotificationChannel(mChannel);
                        }
                        notification = new NotificationCompat.Builder(context);
                        notification.setSmallIcon(R.mipmap.ic_launcher);
                        notification.setTicker(txt1[0]+" has expired!");
                        notification.setContentTitle(txt1[0] + " is expired!");
                        notification.setContentText("");
                        Intent intent = new Intent(context, MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        notification.setContentIntent(pendingIntent);
                        //ate it button
                        Intent ateit = new Intent(getApplicationContext(), ItemadditionActivity.class);
                        PendingIntent button1 = PendingIntent.getActivity(getApplicationContext(), 0, ateit, PendingIntent.FLAG_UPDATE_CURRENT);
                        notification.addAction(R.drawable.button1, "I ate it", button1);
                        // throw out button
                        Intent throwout = new Intent(getApplicationContext(), LoginActivity.class);
                        PendingIntent button2 = PendingIntent.getActivity(getApplicationContext(), 0, throwout, PendingIntent.FLAG_UPDATE_CURRENT);
                        notification.addAction(R.drawable.button1, "throw out", button2);
                        nm.notify(notificationID, notification.build());
                        notificationID =+1;
                    }
                }
                List<String> shrtterm = listHash.get("Short-term");

            }

        }

    }
}

