package jrfeng.rest.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;

import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class TextCountdownView extends AppCompatTextView implements CountdownTimer {
    private int mSeconds;
    private int mMinutes;

    private Handler mHandler;

    private boolean mCountdownRunning;

    private Timer mTimer;
    private OnTimeoutListener mListener;

    private Runnable mUpdateTextAction = new Runnable() {
        @Override
        public void run() {
            String text = String.format(Locale.ENGLISH, "%02d", mMinutes)
                    + ":"
                    + String.format(Locale.ENGLISH, "%02d", mSeconds);
            setText(text);
        }
    };

    public TextCountdownView(Context context) {
        super(context);
        mHandler = new Handler(Looper.getMainLooper());

        init();
    }

    public TextCountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TextCountdownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        setText("00:00");
    }

    public void startCountdown(int seconds, @Nullable OnTimeoutListener listener) {
        if (seconds < 1) {
//            notifyTimeout();
            return;
        }

        mListener = listener;

        mMinutes = seconds / 60;
        mSeconds = seconds % 60;

        if (mTimer != null) {
            mTimer.cancel();
        }

        mCountdownRunning = true;
        Calendar calendar = Calendar.getInstance();
        mTimer = new Timer(true);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mSeconds < 1 && mMinutes > 0) {
                    mMinutes -= 1;
                    mSeconds = 59;
                } else {
                    mSeconds -= 1;
                }

                if (isTimeout()) {
                    notifyTimeout();
                }

                post(mUpdateTextAction);
            }
        }, 1000 - calendar.get(Calendar.MILLISECOND), 1000);
    }

    private boolean isTimeout() {
        return mSeconds < 1 && mMinutes < 1;
    }

    @Override
    public boolean isCountdownRunning() {
        return mCountdownRunning;
    }

    public void cancelCountdown() {
        mCountdownRunning = false;
        mMinutes = 0;
        mSeconds = 0;
        post(mUpdateTextAction);
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    private void notifyTimeout() {
        mTimer.cancel();
        mCountdownRunning = false;
        if (mListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mListener.timeout();
                }
            });
        }
    }
}
