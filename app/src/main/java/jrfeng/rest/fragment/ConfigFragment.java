package jrfeng.rest.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
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
import jrfeng.rest.widget.RulerView;

public class ConfigFragment extends Fragment {
    public static final String TAG = "jrfeng.rest.fragment:ConfigFragment";

    private AppApplication mApplication;
    private SharedPreferences mCountdownPreferences;

    private TextView tvTimeLabel;
    private RulerView mTimeSelector;
    private ImageButton btnStart;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = AppApplication.getInstance();
        mCountdownPreferences = mApplication.getCountdownPreferences();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = LayoutInflater.from(getContext())
                .inflate(R.layout.fragment_config, container, false);

        findViews(layout);
        initViews();

        return layout;
    }

    private void findViews(View layout) {
        tvTimeLabel = layout.findViewById(R.id.tvTimeLabel);
        mTimeSelector = layout.findViewById(R.id.timeSelector);
        btnStart = layout.findViewById(R.id.btnStart);
    }

    private void initViews() {
        tvTimeLabel.setTypeface(mApplication.getTypeface());
        int countdownMinutes = mApplication.getCountdownMinute();
        setTimeLabelMinute(countdownMinutes);

        mTimeSelector.setProgress(countdownMinutes);
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
                int minute = mCountdownPreferences.getInt(
                        AppApplication.KEY_COUNTDOWN_MINUTE,
                        AppApplication.DEFAULT_COUNTDOWN_MINUTE);
                if (minute > 0) {
                    startCountdown(minute);
                }
            }
        });
    }

    private void startCountdown(int minute) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.startCountdown(minute);
            startCountdownFragment(mainActivity.getSupportFragmentManager());
        }
    }

    private void startCountdownFragment(FragmentManager fm) {
        fm.beginTransaction()
                .setCustomAnimations(R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out)
                .replace(R.id.fragmentContainer, new CountdownFragment(), CountdownFragment.TAG)
                .addToBackStack("")
                .commit();
    }

    private void setTimeLabelMinute(int minute) {
        String timeLabel = tvTimeLabel.getText().toString();
        tvTimeLabel.setText(timeLabel.replaceFirst("[0-9]+", String.valueOf(minute)));
    }
}
