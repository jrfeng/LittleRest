package jrfeng.rest.scene;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.transition.Fade;
import androidx.transition.Scene;
import androidx.transition.Transition;
import jrfeng.anim.AnimUtil;
import jrfeng.rest.AppApplication;
import jrfeng.rest.R;
import jrfeng.rest.activity.TimeoutActivity;
import jrfeng.rest.widget.ClockView;
import jrfeng.rest.widget.CountdownTimer;
import jrfeng.rest.widget.TextCountdownView;

public class MainActivityCountdownScene extends AbstractScene {
    private AppApplication mApplication;
    private Typeface mNotoSansThin;

    private int mClockViewWidthAndHeight;

    private ClockView mClockView;
    private TextCountdownView mTextCountdownView;
    private TextView tvTimeLabel;
    private ImageButton btnCancel;

    private PowerManager.WakeLock mWakeLock;

    public MainActivityCountdownScene(@NonNull ViewGroup sceneRoot, @NonNull Context context) {
        super(sceneRoot, context);

        mApplication = AppApplication.getInstance();
        mNotoSansThin = mApplication.getTypeface();

        mClockViewWidthAndHeight = context.getResources().getDimensionPixelSize(R.dimen.sceneCountdownClockWidth);

        initWakeLock(context);
    }

    private void initWakeLock(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "jrfeng.rest.scene:CountdownScene");
        }
    }

    private void acquireWakeLock(long ms) {
        if (mWakeLock != null) {
            mWakeLock.acquire(ms);
        }
    }

    private void releaseWakeLock() {
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }

    @NonNull
    @Override
    protected Scene onCreateScene(@NonNull ViewGroup sceneRoot, @NonNull Context context) {
        return Scene.getSceneForLayout(sceneRoot, R.layout.activity_main_scene_countdown, context);
    }

    @NonNull
    @Override
    protected Transition onCreateTransition() {
        return new Fade();
    }

    @Override
    public void onTransitionStart(@NonNull Transition transition) {
        findViews();
        initViews();
        addListener();
        startCountdown();
    }

    private void findViews() {
        // 由于 ClockView 不属于 SceneRoot，因此需要通过 LayoutRoot 来查找它
        mClockView = getLayoutRoot().findViewById(R.id.clockView);

        ViewGroup sceneRoot = getSceneRoot();
        mTextCountdownView = sceneRoot.findViewById(R.id.textCountdown);
        tvTimeLabel = sceneRoot.findViewById(R.id.tvTimeLabel2);
        btnCancel = sceneRoot.findViewById(R.id.btnAction);
    }

    private void initViews() {
        mTextCountdownView.setTypeface(mNotoSansThin);
        tvTimeLabel.setTypeface(mNotoSansThin);

        setTimeLabelMinute(mApplication.getCountdownMinute());
    }

    private void addListener() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

    private void startCountdown() {
        mApplication.setCountdownRunning(true);
        int seconds = mApplication.getCountdownMinute() * 60;
        acquireWakeLock(seconds * 1000 + 60_000);
        mTextCountdownView.startCountdown(seconds, null);
        mTextCountdownView.setKeepScreenOn(true);
        mClockView.showSecondHand(true);
        mClockView.startCountdown(seconds, new CountdownTimer.OnTimeoutListener() {
            @Override
            public void timeout() {
                cancel();
                startTimeoutActivity();
            }
        });

        int from = mClockView.getLayoutParams().width;
        ValueAnimator animator = AnimUtil.ofInt(from, mClockViewWidthAndHeight)
                .duration(300)
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        ViewGroup.LayoutParams layoutParams = mClockView.getLayoutParams();
                        // 由于布局中 ClockView 的长宽值是一个样的，为了方便起见这里只从对单个属性创建动画
                        // 然后同时应用到宽和高属性上即可。
                        int value = (int) animation.getAnimatedValue();
                        layoutParams.width = value;
                        layoutParams.height = value;
                        mClockView.requestLayout();
                    }
                })
                .build();
        animator.start();
    }

    private void setTimeLabelMinute(int minute) {
        String timeLabel = tvTimeLabel.getText().toString();
        tvTimeLabel.setText(timeLabel.replaceFirst("[0-9]+", String.valueOf(minute)));
    }


    private void startTimeoutActivity() {
        Context context = getContext();
        Intent intent = new Intent(context, TimeoutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(intent);
    }

    private void cancel() {
        releaseWakeLock();
        mApplication.setCountdownRunning(false);
        mTextCountdownView.cancelCountdown();
        mTextCountdownView.setKeepScreenOn(false);
        new MainActivityConfigScene(getSceneRoot(), getContext()).go();
    }


}
