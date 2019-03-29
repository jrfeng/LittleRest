package jrfeng.rest.activity;

import androidx.appcompat.app.AppCompatActivity;
import jrfeng.rest.AppApplication;
import jrfeng.rest.R;
import jrfeng.rest.scene.MainActivityConfigScene;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ViewGroup sceneRoot = findViewById(R.id.sceneRoot);
        MainActivityConfigScene configScene = new MainActivityConfigScene(sceneRoot, this);
        configScene.go();
    }

    @Override
    public void onBackPressed() {
        if (AppApplication.getInstance().isCountdownRunning()) {
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
