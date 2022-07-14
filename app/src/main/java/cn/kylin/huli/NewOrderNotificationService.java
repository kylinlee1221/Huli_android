package cn.kylin.huli;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class NewOrderNotificationService extends Service {
    public static final String NOTIFICATION_CHANNEL_ID="10001";
    private final static String default_notification_channel_id="default";
    Timer timer;
    TimerTask timerTask;
    String TAG="Timers";
    int Your_X_SECS=5;
    public NewOrderNotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){
        super.onStartCommand(intent,flags,startId);
        createNotification();
        return START_STICKY;
    }
    private void createNotification(){
        NotificationManager mNotificationManager=(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        long[] pattern1 = {0, 100, 1000, 300, 200, 100, 500, 200, 100};
        NotificationCompat.Builder mBuilder=new NotificationCompat.Builder(getApplicationContext(),default_notification_channel_id);
        Uri sound= Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/"+R.raw.ordercomes);

        Intent resultIntent=new Intent(this,OrderMarketActivity.class);
        TaskStackBuilder stackBuilder=TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent=stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setContentTitle(getResources().getString(R.string.app_name));
        mBuilder.setContentText(getResources().getString(R.string.new_order_notification));
        mBuilder.setTicker(getResources().getString(R.string.new_order_notification));
        //mBuilder.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE);
        //mBuilder.setVibrate(pattern1);
        //mBuilder.setSound(sound);
        mBuilder.setSmallIcon(R.drawable.notification);
        mBuilder.setAutoCancel(true);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            int importance=NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel=new NotificationChannel(NOTIFICATION_CHANNEL_ID,"NOTIFICATION_CHANNEL_NAME",importance);
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel);
        }
        assert mNotificationManager!=null;
        mNotificationManager.notify((int)System.currentTimeMillis(),mBuilder.build());
    }
}