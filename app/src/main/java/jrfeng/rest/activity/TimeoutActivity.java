package jrfeng.rest.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import jrfeng.rest.R;
import jrfeng.rest.scene.TimeoutScene;

public class TimeoutActivity extends AppCompatActivity {
    private TimeoutScene mTimeoutScene;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeout);

        ViewGroup container = findViewById(R.id.container);
        mTimeoutScene = new TimeoutScene(container,
                this,
                this,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });
        mTimeoutScene.go();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mTimeoutScene.stopRingAndVibrate();
        }
        return super.onKeyDown(keyCode, event);
    }

    public static void start(Context context) {
        Intent intent = new Intent(context, TimeoutActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
