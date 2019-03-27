package jrfeng.rest.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import jrfeng.anim.AnimUtil;
import jrfeng.rest.R;

public class RulerView extends View {
    private static final String TAG = "RulerView";

    private static final int DEFAULT_WIDTH_DP = 320/*dp*/;
    private static final int DEFAULT_HEIGHT_DP = 160/*dp*/;
    private static final int DEFAULT_SCALE_LINE_WIDTH_DP = 1/*dp*/;

    private static final int DEFAULT_SCALE_10_LENGTH = 24/*dp*/;    // 十分度刻度的默认长度
    private static final int DEFAULT_SCALE_5_LENGTH = 16/*dp*/;     // 五分度刻度的默认长度
    private static final int DEFAULT_SCALE_1_LENGTH = 10/*dp*/;     // 一分度刻度的默认长度

    private static final int DEFAULT_SCALE_LINE_COLOR = Color.parseColor("#FFFFFF");

    private static final int DEFAULT_INDICATOR_RES_ID = R.mipmap.ic_indicator;

    private static final int DEFAULT_TEXT_SIZE = 16/*dp*/;          // 为了避免文字缩放问题，使用 dp 作为文字单位

    private static final int CENTER_Y_OFFSET = 8/*dp*/;

    private int mDefaultWidth;
    private int mDefaultHeight;

    private int mScaleLineWidth;
    private int mScale10Length;     // 十分度刻度的长度
    private int mScale5Length;      // 五分度刻度的长度
    private int mScale1Length;      // 一分度刻度的长度

    private int mScaleLineColor;
//    private int mScaleLineSelectedColor;

    private int mCenterYOffset;
    private int mTextSize;

    private String[] mText = {"0", "10", "20", "30", "40", "50", "60"};

    private Bitmap mIndicatorBitmap;
    private int mProgress;

    private int mCenterX;
    private int mCenterY;

    @Nullable
    private OnProgressUpdateListener mListener;

    private Context mContext;

    private RectF mRectF;
    private Rect mRect;
    private Paint mPaint;

    public RulerView(Context context) {
        this(context, null);
    }

    public RulerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
        parseAttrs(attrs);
    }

    private void init() {
        mDefaultWidth = dpToPx(DEFAULT_WIDTH_DP);
        mDefaultHeight = dpToPx(DEFAULT_HEIGHT_DP);

        mScaleLineWidth = dpToPx(DEFAULT_SCALE_LINE_WIDTH_DP);
        mScale10Length = dpToPx(DEFAULT_SCALE_10_LENGTH);
        mScale5Length = dpToPx(DEFAULT_SCALE_5_LENGTH);
        mScale1Length = dpToPx(DEFAULT_SCALE_1_LENGTH);

        mScaleLineColor = DEFAULT_SCALE_LINE_COLOR;

        mCenterYOffset = dpToPx(CENTER_Y_OFFSET);
        mTextSize = dpToPx(DEFAULT_TEXT_SIZE);

        mIndicatorBitmap = getBitmapFromResId(DEFAULT_INDICATOR_RES_ID);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mScaleLineSelectedColor = getResources().getColor(R.color.colorAccent, mContext.getTheme());
//        } else {
//            mScaleLineSelectedColor = getResources().getColor(R.color.colorAccent);
//        }

        mRectF = new RectF();
        mRect = new Rect();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
    }

    private Bitmap getBitmapFromResId(int resId) {
        BitmapDrawable bitmapDrawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bitmapDrawable = (BitmapDrawable) getResources().getDrawable(resId, mContext.getTheme());
        } else {
            bitmapDrawable = (BitmapDrawable) getResources().getDrawable(resId);
        }
        return bitmapDrawable.getBitmap();
    }

    private void parseAttrs(@Nullable AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.RulerView);

        mScaleLineWidth = typedArray.getDimensionPixelSize(R.styleable.RulerView_scaleLineWidth, mScaleLineWidth);
        mScaleLineColor = typedArray.getColor(R.styleable.RulerView_scaleLineColor, mScaleLineColor);
//        mScaleLineSelectedColor = typedArray.getColor(R.styleable.RulerView_scaleLineSelectedColor, mScaleLineSelectedColor);
        int resId = typedArray.getResourceId(R.styleable.RulerView_indicatorDrawable, DEFAULT_INDICATOR_RES_ID);
        if (resId != DEFAULT_INDICATOR_RES_ID) {
            mIndicatorBitmap = getBitmapFromResId(resId);
        }

        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measureWidth = measureSize(widthMeasureSpec, mDefaultWidth);
        int measureHeight = measureSize(heightMeasureSpec, mDefaultHeight);
        setMeasuredDimension(measureWidth, measureHeight);
    }

    private int measureSize(int spec, int defaultValue) {
        int mode = MeasureSpec.getMode(spec);
        int size = MeasureSpec.getSize(spec);

        int result = 0;
        switch (mode) {
            case MeasureSpec.EXACTLY:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(size, defaultValue);
                break;
            case MeasureSpec.UNSPECIFIED:
                result = defaultValue;
        }

        return result;
    }

    private int dpToPx(float dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                mContext.getResources().getDisplayMetrics()
        ));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int scaleLineHalfWidth = (int) Math.round(mScaleLineWidth / 2.0);

        mCenterX = getWidth() / 2;
        mCenterY = getWidth() + scaleLineHalfWidth;

        mPaint.setColor(mScaleLineColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mScaleLineWidth);

        // 绘制刻度
        // 以垂直中线位置作为参考（0 度位置）
        int startY = mScaleLineWidth / 2;
        for (int i = 0; i < 61; i++) {
            int rotateDegrees = i - 30;

//            if (i == mProgress) {
//                mPaint.setColor(mScaleLineSelectedColor);
//            } else {
//                mPaint.setColor(mScaleLineColor);
//            }

            if (i % 10 == 0) {
                // 绘制十分度刻度
                canvas.save();
                canvas.rotate(rotateDegrees, mCenterX, mCenterY);
                canvas.drawLine(mCenterX, startY, mCenterX, startY + mScale10Length, mPaint);
                // 绘制文字
                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setTextSize(mTextSize);
                String text = mText[i / 10];
                mPaint.getTextBounds(text, 0, text.length(), mRect);
                float x = (float) (mCenterX - (mRect.width() / 2.0));
                float y = mScale10Length + mCenterYOffset + mRect.height();
                canvas.drawText(text, x, y, mPaint);
                canvas.restore();
            } else if (i % 5 == 0) {
                // 绘制五分度刻度
                canvas.save();
                canvas.rotate(rotateDegrees, mCenterX, mCenterY);
                canvas.drawLine(mCenterX, startY, mCenterX, startY + mScale5Length, mPaint);
                canvas.restore();
            } else {
                // 绘制一分度刻度
                canvas.save();
                canvas.rotate(rotateDegrees, mCenterX, mCenterY);
                canvas.drawLine(mCenterX, startY, mCenterX, startY + mScale1Length, mPaint);
                canvas.restore();
            }
        }

        // 绘制圆弧
        mPaint.setColor(mScaleLineColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mScaleLineWidth);
        int radius = getWidth();
        mRectF.left = mCenterX - radius;
        mRectF.top = mCenterY - radius;
        mRectF.right = mCenterX + radius;
        mRectF.bottom = mCenterY + radius;
        canvas.drawArc(mRectF, -120, 60, false, mPaint);

        // 绘制圆弧进度条
//        mPaint.setColor(mScaleLineSelectedColor);
//        canvas.drawArc(mRectF, -120, mProgress, false, mPaint);

        // 绘制指示器
        canvas.save();
        canvas.rotate(mProgress - 30, mCenterX, mCenterY);
        float left = (float) (mCenterX - mIndicatorBitmap.getWidth() / 2.0);
        float top = mScale10Length + 2 * mCenterYOffset/*2 个 offset*/ + mRect.height()/*文本高度*/;
        canvas.drawBitmap(mIndicatorBitmap, left, top, mPaint);
        canvas.restore();
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                animToProgress(event);
                return true;
            case MotionEvent.ACTION_MOVE:
                updateProgress(event);
                return true;
        }

        return performClick();
    }

    public void setProgress(int progress) {
        if (progress < 0) {
            mProgress = 0;
        } else if (progress > 60) {
            mProgress = 60;
        } else {
            mProgress = progress;
        }
    }

    private void updateProgress(MotionEvent event) {
        mProgress = getScanProgress(event);
        if (mListener != null) {
            mListener.onProgressUpdate(mProgress);
        }
        postInvalidate();
    }

    private void animToProgress(MotionEvent event) {
        int from = mProgress;
        int to = getScanProgress(event);

        if (Math.abs(to - from) < 5) {
            mProgress = to;
            postInvalidate();
            notifyProgressUpdate();
        } else {
            ValueAnimator animator = AnimUtil.ofInt(from, to)
                    .duration(200)
                    .interpolator(new LinearInterpolator())
                    .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            mProgress = (int) animation.getAnimatedValue();
                            postInvalidate();
                        }
                    })
                    .addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            notifyProgressUpdate();
                        }
                    })
                    .build();

            animator.start();
        }

    }

    private void notifyProgressUpdate() {
        if (mListener != null) {
            mListener.onProgressUpdate(mProgress);
        }
    }

    private int getScanProgress(MotionEvent event) {
        /*
         * 计算步骤：
         * 第一步：计算出触摸点到中心点的斜率。斜率计算公式：tank = (y1 - y2)/(x1 - x2)
         * 第二步：根据斜率计算出角度。公式：角度 k = atan(tank) * 180 / pi
         * */
        float x = event.getX();
        float y = event.getY();

        double tanK = (y - mCenterY) / (x - mCenterX);
        double degree = Math.atan(tanK) * 180 / Math.PI;

        double scanProgress;
        // 最大值为 60，最小值为 0
        if (degree < 0) {
            scanProgress = Math.min(60, 30 + 90 - Math.abs(degree));
        } else {
            scanProgress = Math.max(0, Math.round(degree) - 60);
        }

        return (int) Math.round(scanProgress);
    }

    /**
     * 设置进度条更新监听器。
     *
     * @param listener 进度监听监听器。当用户滑动进度条时，该监听器会收到通知。敢参数可为 null。
     */
    public void setOnProgressUpdateListener(@Nullable OnProgressUpdateListener listener) {
        mListener = listener;
    }

    /**
     * 事件监听器。用来监听 RulerView 的进度更新事件。
     */
    public interface OnProgressUpdateListener {
        void onProgressUpdate(int progress);
    }
}
