package jrfeng.rest.activity;

import android.content.Context;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import jrfeng.rest.AppApplication;
import jrfeng.rest.R;
import jrfeng.rest.widget.ClockView;

public class TimeoutActivity extends AppCompatActivity {
    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;

    private TextView tvMessage;
    private ClockView mClockView;
    private TextView tvTimeLabel;
    private Button btnOK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeout);

        findViews();
        initViews();
        playRing();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mClockView.beginFlash();
        mClockView.setKeepScreenOn(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mClockView.setKeepScreenOn(false);
        mClockView.endFlash();
        if (isFinishing()) {
            stopRing();
        }
    }

    public void findViews() {
        tvMessage = findViewById(R.id.tvMessage);
        mClockView = findViewById(R.id.clockView);
        tvTimeLabel = findViewById(R.id.tvTimeTable);
        btnOK = findViewById(R.id.btnOk);
    }

    private void initViews() {
        Typeface typeface = AppApplication.getInstance().getTypeface();
        tvMessage.setTypeface(typeface);
        tvTimeLabel.setTypeface(typeface);
        btnOK.setTypeface(typeface);

        setTimeLabelMinute(AppApplication.getInstance().getCountdownMinute());
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setTimeLabelMinute(int minute) {
        String timeLabel = tvTimeLabel.getText().toString();
        tvTimeLabel.setText(timeLabel.replaceFirst("[0-9]+", String.valueOf(minute)));
    }

    private void playRing() {
        mMediaPlayer = MediaPlayer.create(this, R.raw.default_ring);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
        if (isVolumeLow()) {
            startVibrate();
        }
    }

    private void stopRing() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
            stopVibrate();
        }
    }

    private void startVibrate() {
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator != null) {
            mVibrator.vibrate(new long[]{800, 400}, 0);
        }
    }

    private void stopVibrate() {
        if (mVibrator != null) {
            mVibrator.cancel();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            stopRing();
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean isVolumeLow() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

            Log.d("App", "Volume: " + volume);

            return volume < (maxVolume * 0.4);
        }
        return false;
    }
}
