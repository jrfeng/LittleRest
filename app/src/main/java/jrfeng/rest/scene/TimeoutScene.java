package jrfeng.rest.scene;

import android.content.Context;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.transition.Scene;
import androidx.transition.Transition;
import jrfeng.rest.AppApplication;
import jrfeng.rest.R;
import jrfeng.rest.widget.ClockView;

public class TimeoutScene extends AbstractScene {
    private static final String TAG = "TimeoutScene";

    private Context mContext;

    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;

    private TextView tvMessage;
    private TextView tvTimeLabel;
    private Button btnOK;

    private View.OnClickListener mOkButtonClickListener;

    public TimeoutScene(@NonNull ViewGroup sceneRoot,
                        @NonNull Context context,
                        @NonNull View.OnClickListener listener) {
        super(sceneRoot, context);
        mContext = context;
        mOkButtonClickListener = listener;
    }

    @NonNull
    @Override
    protected Scene onCreateScene(@NonNull ViewGroup sceneRoot, @NonNull Context context) {
        return Scene.getSceneForLayout(sceneRoot, R.layout.scene_timeout, context);
    }

    @Override
    public void onTransitionStart(@NonNull Transition transition) {
        findViews(getSceneRoot());
        initViews();
        addListener();
    }

    private void findViews(View rootView) {
        tvMessage = rootView.findViewById(R.id.tvMessage);
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

    /**
     * 响铃，如果铃声的音量太小，则还会振动。
     */
    public void startRingMaybeVibrate() {
//        lightUpScreen();
        mMediaPlayer = new MediaPlayer();
        try {
            // 获取系统铃声 Uri
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            mMediaPlayer.setDataSource(mContext, alarmUri);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                    mp.start();
                }
            });
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }

        if (shouldVibrate()) {
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

    // 如果闹钟音量太小，则返回 true，其他情况下返回 false
    private boolean shouldVibrate() {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int volume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);

            Log.d(TAG, "Max    : " + maxVolume);
            Log.d(TAG, "Current: " + volume);

            return volume < (maxVolume * 0.3);
        }
        return false;
    }
}
