package pdp.placeholders;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Valtteri on 6.3.2018.
 */

public class ShowNotificationAlarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        setMyAlarm(context);
        SharedPreferences sprefs = context.getSharedPreferences("userprefs",Context.MODE_PRIVATE);
        if (sprefs.contains("username")) {
            UserItems.setUsername(sprefs.getString("username", null));
            UserItems.setUserId(sprefs.getString("userid", null));
            ShowNotificationJob.startNotification(context);
            FirebaseHelper.getArrayList(context, null);
        }
    }

    private void setMyAlarm(Context context) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MINUTE,15);
        cal.set(Calendar.HOUR_OF_DAY,19);
        if(cal.before(Calendar.getInstance())){
        //    cal.add(Calendar.HOUR,12);
        }
        Intent notintent = new Intent(context, ShowNotificationAlarm.class);
        PendingIntent pendintent = PendingIntent.getBroadcast(context,0,notintent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager)context.getSystemService(ALARM_SERVICE);
        assert alarm != null;
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,cal.getTimeInMillis(),AlarmManager.INTERVAL_HALF_DAY,pendintent);
    }

    public static void createNoitification(Context context){
        int notificationID = 300994;
        for (String s1 : UserItems.getList()) {
            String[] txt1 = s1.split(UserItems.DELIMLIST);
            if(UserItems.countDays(txt1[1])<4){
                notificationID = notificationID +1;
                NotificationCompat.Builder notification;
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
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
                Intent ateit = new Intent(context.getApplicationContext(), ItemadditionActivity.class);
                PendingIntent button1 = PendingIntent.getActivity(context.getApplicationContext(), 0, ateit, PendingIntent.FLAG_UPDATE_CURRENT);
                notification.addAction(R.drawable.button1, "I ate it", button1);
                // throw out button
                Intent throwout = new Intent(context.getApplicationContext(), LoginActivity.class);
                PendingIntent button2 = PendingIntent.getActivity(context.getApplicationContext(), 0, throwout, PendingIntent.FLAG_UPDATE_CURRENT);
                notification.addAction(R.drawable.button1, "throw out", button2);
                nm.notify(notificationID, notification.build());
                notificationID =+1;
            }
        }
    }
}

