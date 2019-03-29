package jrfeng.rest;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Typeface;

public class AppApplication extends Application {
    public static final String KEY_COUNTDOWN_MINUTE = "jrfeng.rest:CountDownMinute";

    public static final int DEFAULT_COUNTDOWN_MINUTE = 30;

    private static AppApplication mInstance;
    private Typeface mNotoSansThin;
    private SharedPreferences mCountdownPreferences;

    private boolean mCountdownRunning;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        mNotoSansThin = Typeface.createFromAsset(getAssets(), "NotoSansSC-Thin-custom.ttf");
        mCountdownPreferences = getSharedPreferences("countdown_config", MODE_PRIVATE);
    }

    public static AppApplication getInstance() {
        return mInstance;
    }

    public Typeface getTypeface() {
        return mNotoSansThin;
    }

    public SharedPreferences getCountdownPreferences() {
        return mCountdownPreferences;
    }

    public void saveCountdownMinute(int minute) {
        mCountdownPreferences.edit()
                .putInt(AppApplication.KEY_COUNTDOWN_MINUTE, minute)
                .apply();
    }

    public int getCountdownMinute() {
        return mCountdownPreferences.getInt(
                AppApplication.KEY_COUNTDOWN_MINUTE,
                AppApplication.DEFAULT_COUNTDOWN_MINUTE);
    }

    public boolean isCountdownRunning() {
        return mCountdownRunning;
    }

    public void setCountdownRunning(boolean countdownRunning) {
        mCountdownRunning = countdownRunning;
    }
}
