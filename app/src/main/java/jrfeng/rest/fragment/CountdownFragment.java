package jrfeng.rest.fragment;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import jrfeng.rest.AppApplication;
import jrfeng.rest.R;
import jrfeng.rest.activity.MainActivity;
import jrfeng.rest.widget.TextCountdownView;

public class CountdownFragment extends Fragment {
    public static final String TAG = "jrfeng.rest.fragment:CountdownFragment";

    private static final String DEBUG_TAG = "CountdownFragment";

    private TextCountdownView mTextCountdownView;
    private TextView tvTimeLabel;
    private ImageButton btnCancel;

    private int mCountdownMinute;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = AppApplication.getInstance().getCountdownPreferences();
        mCountdownMinute = preferences.getInt(
                AppApplication.KEY_COUNTDOWN_MINUTE,
                AppApplication.DEFAULT_COUNTDOWN_MINUTE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_countdown, container, false);

        Log.d(DEBUG_TAG, "onCreate, Bundle is Null: " + (savedInstanceState == null));

        findViews(layout);
        initViews();

        return layout;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(DEBUG_TAG, "onSaveInstanceState");
    }

    private void findViews(View layout) {
        mTextCountdownView = layout.findViewById(R.id.textCountdown);
        tvTimeLabel = layout.findViewById(R.id.tvTimeLabel);
        btnCancel = layout.findViewById(R.id.btnCancel);
    }

    private void initViews() {
        Typeface typeface = AppApplication.getInstance().getTypeface();
        mTextCountdownView.setTypeface(typeface);
        tvTimeLabel.setTypeface(typeface);

        mTextCountdownView.startCountdown(mCountdownMinute * 60, null);

        setTimeLabelMinute(mCountdownMinute);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isRemoving() && mTextCountdownView.isCountdownRunning()) {
            mTextCountdownView.cancelCountdown();
        }
    }

    private void setTimeLabelMinute(int minute) {
        String timeLabel = tvTimeLabel.getText().toString();
        tvTimeLabel.setText(timeLabel.replaceFirst("[0-9]+", String.valueOf(minute)));
    }

    private void cancel() {
        final MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    activity.cancelCountdown();
                    FragmentManager fm = activity.getSupportFragmentManager();
                    fm.popBackStack();
                }
            });
        }
    }
}
