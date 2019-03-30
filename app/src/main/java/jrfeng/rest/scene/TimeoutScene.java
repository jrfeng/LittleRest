package jrfeng.rest.scene;

import android.content.Context;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.transition.Scene;
import androidx.transition.Transition;
import jrfeng.rest.AppApplication;
import jrfeng.rest.R;
import jrfeng.rest.widget.ClockView;

public class TimeoutScene extends AbstractScene {
    private Context mContext;

    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;

    private TextView tvMessage;
    private ClockView mClockView;
    private TextView tvTimeLabel;
    private Button btnOK;

    private View.OnClickListener mOkButtonClickListener;

    private PowerManager.WakeLock mLightUpScreenWakeLock;

    public TimeoutScene(@NonNull ViewGroup sceneRoot,
                        @NonNull Context context,
                        @NonNull LifecycleOwner lifecycleOwner,
                        @NonNull View.OnClickListener listener) {
        super(sceneRoot, context);
        mContext = context;
        lifecycleOwner.getLifecycle().addObserver(this);
        mOkButtonClickListener = listener;
    }

    @NonNull
    @Override
    protected Scene onCreateScene(@NonNull ViewGroup sceneRoot, @NonNull Context context) {
        return Scene.getSceneForLayout(sceneRoot, R.layout.fragment_timeout, context);
    }

    @Override
    public void onTransitionStart(@NonNull Transition transition) {
        findViews(getSceneRoot());
        initViews();
        addListener();
    }

    @Override
    public void onTransitionEnd(@NonNull Transition transition) {
        mClockView.beginFlash();
        mClockView.setKeepScreenOn(true);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        startRingMaybeVibrate();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        mClockView.endFlash();
        releaseWakeLock();
        mClockView.setKeepScreenOn(false);
    }

    private void findViews(View rootView) {
        tvMessage = rootView.findViewById(R.id.tvMessage);
        mClockView = rootView.findViewById(R.id.clockView);
        tvTimeLabel = rootView.findViewById(R.id.tvTimeTable);
        btnOK = rootView.findViewById(R.id.btnOk);
    }

    private void initViews() {
        Typeface typeface = AppApplication.getInstance().getTypeface();
        tvMessage.setTypeface(typeface);
        tvTimeLabel.setTypeface(typeface);
        btnOK.setTypeface(typeface);

        setTimeLabelMinute(AppApplication.getInstance().getCountdownMinute());
    }

    private void addListener() {
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOkButtonClickListener.onClick(v);
                stopRingAndVibrate();
            }
        });
    }

    private void setTimeLabelMinute(int minute) {
        String timeLabel = tvTimeLabel.getText().toString();
        tvTimeLabel.setText(timeLabel.replaceFirst("[0-9]+", String.valueOf(minute)));
    }

    private void startRingMaybeVibrate() {
        lightUpScreen();
        mMediaPlayer = MediaPlayer.create(mContext, R.raw.default_ring);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
        if (isLowVolume()) {
            startVibrate();
        }
    }

    /**
     * 停止铃声和振动
     */
    public void stopRingAndVibrate() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            stopVibrate();
        }
    }

    private void startVibrate() {
        mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator != null) {
            mVibrator.vibrate(new long[]{800, 400}, 0);
        }
    }

    private void stopVibrate() {
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

    private boolean isLowVolume() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            return volume < (maxVolume * 0.2);
        }
        return false;
    }

    private void lightUpScreen() {
        if (mLightUpScreenWakeLock == null || !mLightUpScreenWakeLock.isHeld()) {
            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            if (pm != null) {
                mLightUpScreenWakeLock = pm.newWakeLock(
                        PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                                | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                        "jrfeng.rest.activity:lightUpScreen");
                mLightUpScreenWakeLock.acquire(300_000);    // 唤醒屏幕 5 分钟
            }
        }
    }

    private void releaseWakeLock() {
        if (mLightUpScreenWakeLock != null && mLightUpScreenWakeLock.isHeld()) {
            mLightUpScreenWakeLock.release();
            mLightUpScreenWakeLock = null;
        }
    }
}
