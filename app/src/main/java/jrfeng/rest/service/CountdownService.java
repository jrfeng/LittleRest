package jrfeng.rest.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import jrfeng.rest.R;
import jrfeng.rest.activity.MainActivity;
import jrfeng.rest.activity.TimeoutActivity;

public class CountdownService extends Service {
    public static final String KEY_START_MSEC = "jrfeng.rest.service.CountdownService:StartMs";
    public static final String KEY_COUNTDOWN_MINUTES = "jrfeng.rest.service.CountdownService:CountDownMs";

    private static final int ID_FOREGROUND = 1;
    private static final String ID_NOTIFICATION_CHANNEL = "CountDownService";

    private static final String ACTION_TIMEOUT = "jrfeng.rest.service.action.ACTION_TIMEOUT";

    private AlarmManager mAlarmManager;

    private PendingIntent mTimeoutBroadcast;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotificationBuilder;

    private Timer mTimer;
    private int mElapseSeconds;
    private boolean mCancelled;

    private boolean mRunning;

    private BroadcastReceiver mTimeoutReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        mTimeoutReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("App", "TimeoutReceiver OK");
                TimeoutActivity.start(context);
                stopSelf();
            }
        };

        registerReceiver(mTimeoutReceiver, new IntentFilter(ACTION_TIMEOUT));

        Intent timeoutBroadcastIntent = new Intent(ACTION_TIMEOUT);
        mTimeoutBroadcast = PendingIntent.getBroadcast(
                this,
                0,
                timeoutBroadcastIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        PendingIntent startMainActivity = PendingIntent.getActivity(this,
                0,
                mainActivityIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotificationBuilder = new NotificationCompat.Builder(this, ID_NOTIFICATION_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentIntent(startMainActivity)
                .setOnlyAlertOnce(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mRunning) {
            mRunning = true;
            startCountdown(intent);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        cancelCountdown();
    }

    private void startCountdown(Intent intent) {
        long startAt = intent.getLongExtra(KEY_START_MSEC, System.currentTimeMillis());
        int minutes = intent.getIntExtra(KEY_COUNTDOWN_MINUTES, 0);
        int ms = minutes * 60_000;

        if (ms < 6000) {
            throw new IllegalArgumentException("倒计时时长必须大于等于 1 分钟");
        }

        startCountdown(startAt, ms);
        startForeground(minutes);
    }

    private void startCountdown(long startAt, int ms) {
        mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, startAt + ms, mTimeoutBroadcast);
    }

    private void cancelCountdown() {
        unregisterReceiver(mTimeoutReceiver);
        mAlarmManager.cancel(mTimeoutBroadcast);
        mTimer.cancel();
        mCancelled = true;
        stopForeground();
    }

    private void startForeground(int minutes) {
        final int seconds = minutes * 60;

        String contentText = getResources().getString(R.string.defaultText_Countdown_TimeLabel);
        contentText = contentText.replaceAll("\\d", String.valueOf(minutes))
                .toLowerCase();

        Notification notification = mNotificationBuilder
                .setProgress(seconds, 0, false)
                .setContentText(contentText)
                .build();

        startForeground(ID_FOREGROUND, notification);

        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mNotificationBuilder.setProgress(
                        seconds,
                        mElapseSeconds++,
                        false);

                if (!mCancelled) {
                    mNotificationManager.notify(ID_FOREGROUND, mNotificationBuilder.build());
                }
            }
        }, 0, 1000);
    }

    private void stopForeground() {
        stopForeground(true);
        mNotificationManager.cancel(ID_FOREGROUND);
    }
}
