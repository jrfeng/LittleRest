package jrfeng.rest.scene;

import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.PowerManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.transition.Fade;
import androidx.transition.Scene;
import androidx.transition.Transition;
import jrfeng.anim.AnimUtil;
import jrfeng.rest.AppApplication;
import jrfeng.rest.R;
import jrfeng.rest.activity.MainActivity;
import jrfeng.rest.service.CountdownService;
import jrfeng.rest.widget.ClockView;
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

    private Intent mCountdownServiceIntent;

    private Context mContext;
    private BroadcastReceiver mScreenOffReceiver;

    public MainActivityCountdownScene(@NonNull ViewGroup sceneRoot, @NonNull Context context) {
        super(sceneRoot, context);
        mContext = context;

        mApplication = AppApplication.getInstance();
        mNotoSansThin = mApplication.getTypeface();

        mClockViewWidthAndHeight = context.getResources().getDimensionPixelSize(R.dimen.sceneCountdownClockWidth);

        initWakeLock(context);
        initScreenOffReceiver();
    }

    private void initWakeLock(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            mWakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "jrfeng.rest.scene:CountdownScene");
        }
    }

    private void initScreenOffReceiver() {
        mScreenOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                    context.startActivity(new Intent(context, MainActivity.class));
                }
            }
        };
    }

    private void registerScreenOffReceiver() {
        mContext.registerReceiver(mScreenOffReceiver,
                new IntentFilter(Intent.ACTION_SCREEN_OFF));
    }

    private void unregisterScreenOffReceiver() {
        mContext.unregisterReceiver(mScreenOffReceiver);
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
        registerScreenOffReceiver();
        mApplication.setCountdownRunning(true);
        int seconds = mApplication.getCountdownMinute() * 60;
        acquireWakeLock(seconds * 1000 + 60_000);
        mTextCountdownView.startCountdown(seconds, null);
        mTextCountdownView.setKeepScreenOn(true);
        mClockView.showSecondHand(true);
        mClockView.startCountdown(seconds, null);

        int from = mClockView.getLayoutParams().width;
        ValueAnimator animator = AnimUtil.ofInt(from, mClockViewWidthAndHeight)
                .duration(300)
                .interpolator(new DecelerateInterpolator())
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

        startCountdownService(mApplication.getCountdownMinute());
    }

    private void setTimeLabelMinute(int minute) {
        String timeLabel = tvTimeLabel.getText().toString();
        tvTimeLabel.setText(timeLabel.replaceFirst("[0-9]+", String.valueOf(minute)));
    }

    private void cancel() {
        unregisterScreenOffReceiver();
        releaseWakeLock();
        mApplication.setCountdownRunning(false);
        mTextCountdownView.cancelCountdown();
        mTextCountdownView.setKeepScreenOn(false);
        cancelCountdownService();
        new MainActivityConfigScene(getSceneRoot(), getContext()).go();
    }

    private void startCountdownService(int minutes) {
        mCountdownServiceIntent = new Intent(getContext(), CountdownService.class);
        mCountdownServiceIntent.putExtra(CountdownService.KEY_START_MSEC, System.currentTimeMillis());
        mCountdownServiceIntent.putExtra(CountdownService.KEY_COUNTDOWN_MINUTES, minutes);
        getContext().startService(mCountdownServiceIntent);
    }

    private void cancelCountdownService() {
        getContext().stopService(mCountdownServiceIntent);
    }
}
