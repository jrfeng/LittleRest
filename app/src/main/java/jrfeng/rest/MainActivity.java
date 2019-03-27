package jrfeng.rest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.transition.AutoTransition;
import androidx.transition.Scene;
import androidx.transition.Transition;
import androidx.transition.TransitionListenerAdapter;
import androidx.transition.TransitionManager;
import jrfeng.rest.widget.ClockView;
import jrfeng.rest.widget.RulerView;
import jrfeng.rest.widget.TextCountdownView;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private static final String KEY_COUNTDOWN_MINUTE = "mCountdownMinute";

    private Typeface mNotoSans;
    private ClockView mClockView;
    private RulerView mRulerView;
    private TextCountdownView mCountdownView;
    private TextView mText;
    private ImageButton mBtnStart;
    private ImageButton mBtnCancel;

    private Scene sceneNormal;
    private Scene sceneCountdown;

    private SharedPreferences mPreferences;
    private int mCountdownMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = getPreferences(MODE_PRIVATE);
        mCountdownMinute = mPreferences.getInt(KEY_COUNTDOWN_MINUTE, 30);

        mNotoSans = Typeface.createFromAsset(getAssets(), "NotoSansSC-Thin-custom.ttf");

        initView();

        ViewGroup root = findViewById(android.R.id.content);
        sceneNormal = Scene.getSceneForLayout(root, R.layout.activity_main, this);
        sceneCountdown = Scene.getSceneForLayout(root, R.layout.activity_main2, this);
    }

    private void initView() {
        mClockView = findViewById(R.id.clockView);
        mRulerView = findViewById(R.id.rulerView);
        mText = findViewById(R.id.text);
        mBtnStart = findViewById(R.id.btnStart);

        mText.setTypeface(mNotoSans);
        mText.setText(mCountdownMinute + "分钟");
        mBtnStart.setOnClickListener(MainActivity.this);

        mRulerView.setProgress(mCountdownMinute);
        mRulerView.setOnProgressUpdateListener(new RulerView.OnProgressUpdateListener() {
            @Override
            public void onProgressUpdate(int progress) {
                mCountdownMinute = progress;
                mText.setText(mCountdownMinute + "分钟");
                mPreferences.edit()
                        .putInt(KEY_COUNTDOWN_MINUTE, progress)
                        .apply();
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnStart:
                if (mCountdownMinute > 0) {
                    startCountdown();
                }
                break;
            case R.id.btnCancel:
                cancelCountdown();
                break;
        }
    }

    private void startCountdown() {
        AutoTransition transition = new AutoTransition();
        transition.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                mClockView = findViewById(R.id.clockView);
                mText = findViewById(R.id.text2);
                mText.setTypeface(mNotoSans);
                mText.setText("共 " + mCountdownMinute + " 分钟");
                mCountdownView = findViewById(R.id.textCountdown);
                mCountdownView.setTypeface(mNotoSans);

                mBtnCancel = findViewById(R.id.btnCancel);
                mBtnCancel.setOnClickListener(MainActivity.this);
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                int seconds = mCountdownMinute * 60;

                mClockView.startCountdown(seconds, null);
                mClockView.showSecondHand(true);
                mCountdownView.startCountdown(seconds, new TextCountdownView.OnTimeoutListener() {
                    @Override
                    public void timeout() {
                        Log.d(TAG, "Timeout!");
                    }
                });
            }
        });

        TransitionManager.go(sceneCountdown, transition);
    }

    private void cancelCountdown() {
        mClockView.cancelCountdown();
        if (mCountdownView != null) {
            mCountdownView.cancelCountdown();
        }

        Transition transition = new AutoTransition();
        transition.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {
                initView();
                mClockView.showSecondHand(false);
            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                initView();
                mClockView.showSecondHand(false);
            }
        });
        TransitionManager.go(sceneNormal, transition);
    }
}
