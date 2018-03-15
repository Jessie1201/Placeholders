package pdp.placeholders;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Valtteri on 6.3.2018.
 */

public class ShowNotificationJob extends JobService {

    NotificationCompat.Builder notification;
    private static final int notificationID = 3478916;

    @Override
    public boolean onStartJob(JobParameters params) {
        notification = new NotificationCompat.Builder(this);
        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setTicker("Expiring food!");
        notification.setContentTitle("Food is expiring");
        notification.setContentText("Check your food storage!");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(notificationID,notification.build());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;

    }

    public static class backgroundtask extends AsyncTask<Void,Void,String>{

        @Override
        protected String doInBackground(Void... voids) {

            return null;
        }
    }
}

