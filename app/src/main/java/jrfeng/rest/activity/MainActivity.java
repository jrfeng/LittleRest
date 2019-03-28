package jrfeng.rest.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import jrfeng.anim.AnimUtil;
import jrfeng.rest.R;
import jrfeng.rest.fragment.ConfigFragment;
import jrfeng.rest.fragment.CountdownFragment;
import jrfeng.rest.widget.ClockView;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ClockView mClockView;

    private int mSceneConfig_ClockViewWidth;
    private int mSceneConfig_ClockViewHeight;

    private int mSceneCountdown_ClockViewWidth;
    private int mSceneCountdown_ClockViewHeight;

    private boolean mCountdownRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mClockView = findViewById(R.id.clockView);

        Resources resources = getResources();
        mSceneConfig_ClockViewWidth = resources.getDimensionPixelSize(R.dimen.sceneConfigClockWidth);
        mSceneConfig_ClockViewHeight = resources.getDimensionPixelSize(R.dimen.sceneConfigClockHeight);
        mSceneCountdown_ClockViewWidth = resources.getDimensionPixelSize(R.dimen.sceneCountdownClockWidth);
        mSceneCountdown_ClockViewHeight = resources.getDimensionPixelSize(R.dimen.sceneCountdownClockHeight);


        showConfigFragment();
    }

    private void showConfigFragment() {
        ViewGroup.LayoutParams layoutParams = mClockView.getLayoutParams();
        layoutParams.width = mSceneConfig_ClockViewWidth;
        layoutParams.height = mSceneConfig_ClockViewHeight;
        mClockView.requestLayout();
        mClockView.showSecondHand(false);

        if (fragmentNotFilled(ConfigFragment.TAG)) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, new ConfigFragment(), ConfigFragment.TAG)
                    .commit();
        }
    }

    private void showCountdownFragment() {
        ViewGroup.LayoutParams layoutParams = mClockView.getLayoutParams();
        layoutParams.width = mSceneCountdown_ClockViewWidth;
        layoutParams.height = mSceneCountdown_ClockViewHeight;
        mClockView.requestLayout();
        mClockView.showSecondHand(true);

        if (fragmentNotFilled(CountdownFragment.TAG)) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, new CountdownFragment(), CountdownFragment.TAG)
                    .commit();
        }
    }

    private boolean fragmentNotFilled(String tag) {
        FragmentManager fm = getSupportFragmentManager();
        return fm.findFragmentByTag(tag) == null;
    }

    public void startCountdown(int minute) {
        mCountdownRunning = true;
        mClockView.showSecondHand(true);
        mClockView.setKeepScreenOn(true);
        mClockView.startCountdown(minute * 60, null);

        ValueAnimator animator = createClockViewChangeBoundsAnim(
                mSceneConfig_ClockViewWidth,
                mSceneCountdown_ClockViewWidth);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setStartDelay(100);
        animator.start();
    }

    public void cancelCountdown() {
        mCountdownRunning = false;
        mClockView.showSecondHand(false);
        mClockView.setKeepScreenOn(false);
        mClockView.cancelCountdown();

        ValueAnimator animator = createClockViewChangeBoundsAnim(
                mSceneCountdown_ClockViewWidth,
                mSceneConfig_ClockViewWidth);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    private ValueAnimator createClockViewChangeBoundsAnim(int from, int to) {
        return AnimUtil.ofInt(from, to)
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
    }

    @Override
    public void onBackPressed() {
        if (mCountdownRunning) {
            backToHome();
        } else {
            super.onBackPressed();
        }
    }

    private void backToHome() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
