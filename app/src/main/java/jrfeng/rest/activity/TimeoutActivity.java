package jrfeng.rest.activity;

import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import jrfeng.rest.AppApplication;
import jrfeng.rest.R;

// TODO
public class TimeoutActivity extends AppCompatActivity {
    private MediaPlayer mMediaPlayer;

    private TextView tvMessage;
    private Button btnOK;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeout);

        findViews();
        initViews();
        playRing();
    }

    public void findViews() {
        tvMessage = findViewById(R.id.tvMessage);
        btnOK = findViewById(R.id.btnOk);
    }

    private void initViews() {
        Typeface typeface = AppApplication.getInstance().getTypeface();
        tvMessage.setTypeface(typeface);
        btnOK.setTypeface(typeface);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void playRing() {
        mMediaPlayer = MediaPlayer.create(this, R.raw.default_ring);
        mMediaPlayer.setLooping(true);
        mMediaPlayer.start();
    }

    private void stopRing() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isFinishing()) {
            stopRing();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
