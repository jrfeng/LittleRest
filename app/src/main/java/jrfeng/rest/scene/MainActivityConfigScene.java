package jrfeng.rest.scene;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Typeface;
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
import jrfeng.rest.widget.ClockView;
import jrfeng.rest.widget.RulerView;

public class MainActivityConfigScene extends AbstractScene {
    private AppApplication mApplication;
    private Typeface mNotoSansThin;

    private int mClockViewWidthAndHeight;

    private ClockView mClockView;
    private TextView tvTimeLabel;
    private RulerView mTimeSelector;
    private ImageButton btnStart;

    public MainActivityConfigScene(ViewGroup sceneRoot, Context context) {
        super(sceneRoot, context);

        mApplication = AppApplication.getInstance();
        mNotoSansThin = mApplication.getTypeface();
        mClockViewWidthAndHeight = context.getResources().getDimensionPixelSize(R.dimen.sceneConfigClockWidth);
    }

    @NonNull
    @Override
    protected Scene onCreateScene(@NonNull ViewGroup sceneRoot, @NonNull Context context) {
        return Scene.getSceneForLayout(sceneRoot, R.layout.activity_main_scene_config, context);
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
        clockViewCancelCountdown();
    }

    private void findViews() {
        // 由于 ClockView 不属于 SceneRoot，因此需要通过 RootView 来查找它
        mClockView = getLayoutRoot().findViewById(R.id.clockView);

        ViewGroup sceneRoot = getSceneRoot();
        tvTimeLabel = sceneRoot.findViewById(R.id.tvTimeLabel);
        mTimeSelector = sceneRoot.findViewById(R.id.timeSelector);
        btnStart = sceneRoot.findViewById(R.id.btnAction);
    }

    private void initViews() {
        tvTimeLabel.setTypeface(mNotoSansThin);

        int minute = mApplication.getCountdownMinute();
        setTimeLabelMinute(minute);
        mTimeSelector.setProgress(minute);
    }

    private void addListener() {
        mTimeSelector.setOnProgressUpdateListener(new RulerView.OnProgressUpdateListener() {
            @Override
            public void onProgressUpdate(int progress) {
                setTimeLabelMinute(progress);
                mApplication.saveCountdownMinute(progress);
            }
        });

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mApplication.getCountdownMinute() > 0) {
                    new MainActivityCountdownScene(getSceneRoot(), getContext()).go();
                }
            }
        });
    }

    private void setTimeLabelMinute(int minute) {
        String timeLabel = tvTimeLabel.getText().toString();
        tvTimeLabel.setText(timeLabel.replaceFirst("[0-9]+", String.valueOf(minute)));
    }

    private void clockViewCancelCountdown() {
        mClockView.showSecondHand(false);
        mClockView.cancelCountdown();

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
}
