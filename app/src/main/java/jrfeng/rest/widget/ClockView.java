package jrfeng.rest.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.ViewUtils;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.OnLifecycleEvent;
import jrfeng.anim.AnimUtil;
import jrfeng.rest.R;
import jrfeng.rest.widget.util.ViewUtil;

public class ClockView extends View implements LifecycleObserver, CountdownTimer {
    private static final String TAG = "ClockView";

    private static final int DEFAULT_WIDTH_DP = 120/*dp*/;
    private static final int DEFAULT_HEIGHT_DP = 120/*dp*/;

    private static final int DEFAULT_PANEL_STROKE_WIDTH = 4/*dp*/;
    private static final int DEFAULT_HOUR_HAND_WIDTH = 4/*dp*/;
    private static final int DEFAULT_MINUTE_HAND_WIDTH = 4/*dp*/;
    private static final int DEFAULT_SECOND_HAND_WIDTH = 2/*dp*/;

    private static final int DEFAULT_PANEL_STROKE_COLOR = Color.parseColor("#4d4d4d");
    private static final int DEFAULT_PANEL_FILL_COLOR = Color.TRANSPARENT;
    private static final int DEFAULT_HOUR_HAND_COLOR = Color.parseColor("#3a3a3a");
    private static final int DEFAULT_MINUTE_HAND_COLOR = DEFAULT_PANEL_STROKE_COLOR;
    private static final int DEFAULT_SECOND_HAND_COLOR = DEFAULT_PANEL_STROKE_COLOR;
    private static final int DEFAULT_COUNTDOWN_BAR_COLOR = Color.WHITE;

    private static final int DEFAULT_FLASH_COLOR = Color.parseColor("#FF8800");

    private int mFlashColor;
    private boolean mEnabledFlash;
    private ValueAnimator mFlashAnimator;

    private Handler mHandler;

    private Context mContext;
    private boolean mShowing;

    private int mDefaultWidth;
    private int mDefaultHeight;

    private int mPanelStrokeWidth;
    private int mHourHandWidth;
    private int mMinuteHandWidth;
    private int mSecondHandWidth;
    private int mCountdownBarWidth;

    private int mPanelStrokeColor;
    private int mPanelFillColor;
    private int mHourHandColor;
    private int mMinuteHandColor;
    private int mSecondHandColor;
    private int mCountdownBarColor;

    private float mHourHandRotateDegrees;
    private float mMinuteHandRotateDegrees;
    private float mSecondHandRotateDegrees;

    private boolean mShouldUpdateHourAndMinuteHand;

    private int mCountdownSeconds;    // 单位：秒
    private boolean mCountdownRunning;
    private float mRemainderDegrees;
    private RectF mRect = new RectF();
    @Nullable
    private OnTimeoutListener mTimeoutListener;

    private Calendar mCalendar = Calendar.getInstance();
    private Timer mTimer;
    private boolean mTimerRunning;

    private boolean mShowSecondHand;

    private Paint mPaint;

    public ClockView(Context context) {
        this(context, null);
    }

    public ClockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());

        if (context instanceof LifecycleOwner) {
            LifecycleOwner lifecycleOwner = (LifecycleOwner) context;
            lifecycleOwner.getLifecycle().addObserver(this);
        }

        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        mDefaultWidth = ViewUtil.dpToPx(mContext, DEFAULT_WIDTH_DP);
        mDefaultHeight = ViewUtil.dpToPx(mContext, DEFAULT_HEIGHT_DP);

        mPanelStrokeWidth = ViewUtil.dpToPx(mContext, DEFAULT_PANEL_STROKE_WIDTH);
        mPanelStrokeColor = DEFAULT_PANEL_STROKE_COLOR;
        mPanelFillColor = DEFAULT_PANEL_FILL_COLOR;

        mHourHandWidth = ViewUtil.dpToPx(mContext, DEFAULT_HOUR_HAND_WIDTH);
        mHourHandColor = DEFAULT_HOUR_HAND_COLOR;

        mMinuteHandWidth = ViewUtil.dpToPx(mContext, DEFAULT_MINUTE_HAND_WIDTH);
        mMinuteHandColor = DEFAULT_MINUTE_HAND_COLOR;

        mSecondHandWidth = ViewUtil.dpToPx(mContext, DEFAULT_SECOND_HAND_WIDTH);
        mSecondHandColor = DEFAULT_SECOND_HAND_COLOR;

        mCountdownBarColor = DEFAULT_COUNTDOWN_BAR_COLOR;

        mFlashColor = DEFAULT_FLASH_COLOR;

        if (attrs != null) {
            TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.ClockView);

            mPanelStrokeWidth = typedArray.getDimensionPixelSize(R.styleable.ClockView_panelStrokeWidth, mPanelStrokeWidth);
            mPanelStrokeColor = typedArray.getColor(R.styleable.ClockView_panelStrokeColor, mPanelStrokeColor);
            mPanelFillColor = typedArray.getColor(R.styleable.ClockView_panelFillColor, mPanelFillColor);

            mHourHandWidth = typedArray.getDimensionPixelSize(R.styleable.ClockView_hourHandWidth, mHourHandWidth);
            mHourHandColor = typedArray.getColor(R.styleable.ClockView_hourHandColor, mHourHandColor);

            mMinuteHandWidth = typedArray.getDimensionPixelSize(R.styleable.ClockView_minuteHandWidth, mMinuteHandWidth);
            mMinuteHandColor = typedArray.getColor(R.styleable.ClockView_minuteHandColor, mMinuteHandColor);

            mSecondHandWidth = typedArray.getDimensionPixelSize(R.styleable.ClockView_secondHandWidth, mSecondHandWidth);
            mSecondHandColor = typedArray.getColor(R.styleable.ClockView_secondHandColor, mSecondHandColor);

            mCountdownBarWidth = mPanelStrokeWidth;
            mCountdownBarColor = typedArray.getColor(R.styleable.ClockView_countdownBarColor, mCountdownBarColor);

            mFlashColor = typedArray.getColor(R.styleable.ClockView_flashColor, mFlashColor);
            mEnabledFlash = typedArray.getBoolean(R.styleable.ClockView_enableFlash, false);

            mShowSecondHand = typedArray.getBoolean(R.styleable.ClockView_showSecondHand, false);
            typedArray.recycle();
        }

        mShouldUpdateHourAndMinuteHand = true;
        updateClockPanel(System.currentTimeMillis());

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
    }

    private void updateClockPanel(long mills) {
        mCalendar.setTimeInMillis(mills);

        int second = mCalendar.get(Calendar.SECOND);
        mSecondHandRotateDegrees = 6 * second;

        if (mCountdownRunning) {
            mRemainderDegrees -= 0.1;// (6.0 / 60)
            mCountdownSeconds -= 1;
            checkTimeout();
        }

        if (mShouldUpdateHourAndMinuteHand) {
            int minute = mCalendar.get(Calendar.MINUTE);
            int hour = mCalendar.get(Calendar.HOUR);
            hour = hour > 12 ? hour - 12 : hour;

            double minuteElapsePercent = minute / 60.0;

            mMinuteHandRotateDegrees = 6 * minute;
            mHourHandRotateDegrees = (30 * hour) + (float) (30 * minuteElapsePercent);
        }

        mShouldUpdateHourAndMinuteHand = (second == 59);
    }

    private void checkTimeout() {
        if (isTimeout()) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifyTimeout();
                }
            });
        }
    }

    private boolean isTimeout() {
        return mCountdownSeconds < 1;
    }

    private void notifyTimeout() {
        mCountdownRunning = false;
        if (mTimeoutListener != null) {
            mTimeoutListener.timeout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = ViewUtil.measureSize(widthMeasureSpec, mDefaultWidth);
        int measureHeight = ViewUtil.measureSize(heightMeasureSpec, mDefaultHeight);
        setMeasuredDimension(measureWidth, measureHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = (float) (getWidth() / 2.0);
        float centerY = (float) (getHeight() / 2.0);

        float radius = (float) ((Math.min(getWidth(), getHeight()) / 2.0));

        // 绘制表盘填充
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mPanelFillColor);
        canvas.drawCircle(centerX, centerY, radius, mPaint);

        // 绘制表盘描边
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mPanelStrokeWidth);
        mPaint.setColor(mPanelStrokeColor);
        float strokeRadius = radius - (float) (mPanelStrokeWidth / 2.0);
        canvas.drawCircle(centerX, centerY, strokeRadius, mPaint);

        float startX = (float) (getWidth() / 2.0);
        float endY = (float) (getHeight() / 2.0);

        // 绘制时针
        mPaint.setStrokeWidth(mHourHandWidth);
        mPaint.setColor(mHourHandColor);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        float hourHandLength = (float) (radius * 0.5);
        float hourHandStartY = radius - hourHandLength;
        canvas.save();
        canvas.rotate(mHourHandRotateDegrees, centerX, centerY);
        canvas.drawLine(startX, hourHandStartY, startX/*endX = startX*/, endY, mPaint);
        canvas.restore();

        // 绘制分针
        mPaint.setStrokeWidth(mMinuteHandWidth);
        mPaint.setColor(mMinuteHandColor);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        float minuteHandLength = (float) (radius * 0.7);
        float minuteHandStartY = radius - minuteHandLength;
        canvas.save();
        canvas.rotate(mMinuteHandRotateDegrees, centerX, centerY);
        canvas.drawLine(startX, minuteHandStartY, startX/*endX = startX*/, endY, mPaint);
        canvas.restore();

        if (mShowSecondHand) {
            // 绘制秒针
            mPaint.setStrokeWidth(mSecondHandWidth);
            mPaint.setColor(mSecondHandColor);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            float secondHandLength = (float) (radius * 0.8);
            float secondHandStartY = radius - secondHandLength;
            float endOffset = (float) (radius * 0.15);
            canvas.save();
            canvas.rotate(mSecondHandRotateDegrees, centerX, centerY);
            canvas.drawLine(startX, secondHandStartY, startX/*endX = startX*/, endY + endOffset, mPaint);
            canvas.restore();
        }

        if (mCountdownRunning) {
            // 绘制倒计时进度条
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(mCountdownBarWidth);
            mPaint.setColor(mCountdownBarColor);

            float arcRadius = (float) (radius - mCountdownBarWidth / 2.0);

            mRect.left = centerX - arcRadius;
            mRect.top = centerY - arcRadius;
            mRect.right = centerX + arcRadius;
            mRect.bottom = centerY + arcRadius;

            int startDegree = -90;
            canvas.drawArc(mRect, startDegree, mRemainderDegrees, false, mPaint);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        mShowing = visibility != INVISIBLE && visibility != GONE;
    }

    /**
     * 启动倒计时。大于等于 1 秒，小于等于 {@link Integer#MAX_VALUE}
     *
     * @param seconds  倒计时的秒数。应该大于等于 1 秒。
     * @param listener 倒计时监听器。当倒计时完成时，该监听器会被回调。该值可为 null。
     */
    public void startCountdown(int seconds,
                               @Nullable OnTimeoutListener listener) {
        mCountdownRunning = true;
        mTimeoutListener = listener;

        if (seconds < 1) {
            seconds = 1;
        }

        mCountdownSeconds = seconds;

        ValueAnimator animator = AnimUtil.ofFloat(1, (float) (Math.min(seconds, 3600) * 0.1))
                .interpolator(new DecelerateInterpolator(1.2F))
                .duration(500)
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mRemainderDegrees = (float) animation.getAnimatedValue();
                        postInvalidate();
                    }
                })
                .addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mRemainderDegrees = (float) (mCountdownSeconds * 0.1);
                    }
                })
                .build();

        animator.start();
    }

    @Override
    public boolean isCountdownRunning() {
        return mCountdownRunning;
    }

    public void cancelCountdown() {
        if (!mCountdownRunning) {
            return;
        }

        float start = mRemainderDegrees;

        ValueAnimator animator = AnimUtil.ofFloat(start, 0.0F)
                .interpolator(new AccelerateInterpolator())
                .duration(500)
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mRemainderDegrees = (float) animation.getAnimatedValue();
                        postInvalidate();
                    }
                })
                .addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mCountdownRunning = false;
                        postInvalidate();
                    }
                })
                .build();

        animator.start();
    }

    public int getRemainderCountdown() {
        return mCountdownSeconds;
    }

    /**
     * 启动 ClockView。
     * ClockView 实现了 {@link androidx.lifecycle.LifecycleObserver} 接口，具有生命周
     * 期感知功能。如果附加到的 Activity 是个 {@link androidx.lifecycle.LifecycleOwner}，
     * 那么 ClockView 会在它的 onStart() 生命周期中自动启动，不需要手动启动。不过，如果附加到
     * 的 Activity 不是个 {@link androidx.lifecycle.LifecycleOwner}，那么你需要手动调用该
     * 方法来启动 ClockView，并且还需要在 Activity 销毁时负责调用 {@link #cancel()} 方法来
     * 关闭 ClockView。
     *
     * @see #cancel() 关闭 ClockView
     */
    public void start() {
        mShouldUpdateHourAndMinuteHand = true;
        updateClockPanel(System.currentTimeMillis());

        if (mTimer != null) {
            mTimer.cancel();
        }

        mTimer = new Timer("ClockView", true);
        Calendar calendar = Calendar.getInstance();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateClockPanel(System.currentTimeMillis());
                if (mShowing) {
                    postInvalidate();
                }
            }
        }, 1000 - calendar.get(Calendar.MILLISECOND), 1000);
    }

    /**
     * 关闭 ClockView。
     * ClockView 实现了 {@link androidx.lifecycle.LifecycleObserver} 接口，具有生命周期感
     * 知功能。如果附加到的 Activity 是个 {@link androidx.lifecycle.LifecycleOwner}，那么
     * ClockView 会在它的 onDestroy() 生命周期中自动退出，不需要手动退出。不过，如果附加到的
     * Activity 不是个 {@link androidx.lifecycle.LifecycleOwner}，那么你需要在 Activity 的
     * onDestroy() 生命周期中手动调用该方法来退出 ClockView，并且还需要在 Activity 启动时负责
     * 调用 {@link #start()} 方法来启动 ClockView。
     *
     * @see #start() 启动 ClockView
     */
    public void cancel() {
        mTimer.cancel();
        mTimerRunning = false;
        mCountdownSeconds = 0;
        mRemainderDegrees = 0;
    }

    public void showSecondHand(boolean show) {
        if (show) {
            animShowSecondHand();
        } else {
            animHideSecondHand();
        }
    }

    public boolean isShowSecondHand() {
        return mShowSecondHand;
    }

    private void animShowSecondHand() {
        int animDuration = 1000;

        int from = mSecondHandColor & 0x00FFFFFF;
        int to = mSecondHandColor | 0xFF000000;

        ValueAnimator alphaAnimator = AnimUtil.ofInt(from, to)
                .duration(animDuration)
                .evaluator(new ArgbEvaluator())
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mSecondHandColor = (int) animation.getAnimatedValue();
                        postInvalidate();
                    }
                })
                .build();

        // 由于动画消耗了一部分时间，因此需要多加一个刻度（也就是 6 度）对动画补偿，否则在动画结束后
        // 可能会出现指针一次连续跳两个刻度的情况。
        float rotateTo = mSecondHandRotateDegrees + 6;
        ValueAnimator rotateAnimator = AnimUtil.ofFloat(0, rotateTo)
                .duration(animDuration)
                .interpolator(new DecelerateInterpolator(1.5F))
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mSecondHandRotateDegrees = (float) animation.getAnimatedValue();
                        postInvalidate();
                    }
                })
                .build();

        mShowSecondHand = true;
        alphaAnimator.start();
        rotateAnimator.start();
    }

    private void animHideSecondHand() {
        int from = mSecondHandColor;
        int to = mSecondHandColor & 0x00FFFFFF;

        ValueAnimator animator = AnimUtil.ofInt(from, to)
                .duration(300)
                .evaluator(new ArgbEvaluator())
                .interpolator(new AccelerateInterpolator())
                .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mSecondHandColor = (int) animation.getAnimatedValue();
                        postInvalidate();
                    }
                })
                .addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mShowSecondHand = false;
                    }
                })
                .build();

        animator.start();
    }

    private void startFlash() {
        setKeepScreenOn(true);
        int flashStartColor = mPanelStrokeColor;
        if (mFlashAnimator == null) {
            mFlashAnimator = AnimUtil.ofInt(flashStartColor, mFlashColor)
                    .duration(200)
                    .evaluator(new ArgbEvaluator())
                    .repeatMode(ValueAnimator.REVERSE)
                    .repeatCount(-1)
                    .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            int color = (int) animation.getAnimatedValue();
                            mPanelStrokeColor = color;
                            mHourHandColor = color;
                            mMinuteHandColor = color;
                            postInvalidate();
                        }
                    })
                    .build();
        }

        mFlashAnimator.start();
    }

    private void endFlash() {
        setKeepScreenOn(false);
        if (mFlashAnimator != null && mFlashAnimator.isRunning()) {
            mFlashAnimator.cancel();
        }
    }

    public void enableFlash(boolean enable) {
        mEnabledFlash = enable;
        if (enable) {
            startFlash();
        } else {
            endFlash();
        }
    }

    /**
     * Activity 生命周期感知方法。
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        if (!mTimerRunning) {
            start();
        }

        if (mEnabledFlash) {
            startFlash();
        }
    }

    /**
     * Activity 生命周期感知方法。
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        mShowing = true;
    }

    /**
     * Activity 生命周期感知方法。
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        mShowing = false;
        endFlash();
    }

    /**
     * Activity 生命周期感知方法。
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        cancel();
    }
}
