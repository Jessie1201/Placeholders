package pdp.placeholders;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;

import java.util.concurrent.TimeUnit;


public class ShowNotificationJob extends JobService {
    JobParameters params = null;

    public static void startNotification(Context context){
        ComponentName jobcomponent = new ComponentName(context,ShowNotificationJob.class);
        JobInfo.Builder jobInfo = new JobInfo.Builder(123123,jobcomponent);
        jobInfo.setPeriodic(TimeUnit.HOURS.toMillis(12));
        jobInfo.setPersisted(true);
        jobInfo.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);
        JobInfo bultjob = jobInfo.build();
        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(bultjob);



    }

    @Override
    public boolean onStartJob(JobParameters params) {
        this.params = params;
        new myNotificationThread().start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public class myNotificationThread extends Thread {
        myNotificationThread(){
            //startNotification(getApplicationContext());
            //FirebaseHelper.getArrayList(getApplicationContext(),null);
            //startNotification(getApplicationContext());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
