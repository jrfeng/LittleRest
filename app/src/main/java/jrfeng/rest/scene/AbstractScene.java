package jrfeng.rest.scene;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.transition.AutoTransition;
import androidx.transition.Scene;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

public abstract class AbstractScene implements Transition.TransitionListener {
    private ViewGroup mSceneRoot;
    private Context mContext;

    private Scene mScene;
    private Transition mTransition;

    public AbstractScene(@NonNull ViewGroup sceneRoot, @NonNull Context context) {
        mSceneRoot = sceneRoot;
        mContext = context;
        mScene = onCreateScene(sceneRoot, context);
        mTransition = onCreateTransition();
    }

    @NonNull
    protected abstract  Scene onCreateScene(@NonNull ViewGroup sceneRoot, @NonNull Context context);

    @NonNull
    protected Transition onCreateTransition() {
        return new AutoTransition();
    }

    protected final ViewGroup getSceneRoot() {
        return mSceneRoot;
    }

    protected final View getLayoutRoot() {
        return mSceneRoot.getRootView();
    }

    protected final Context getContext() {
        return mContext;
    }

    public final void go() {
        go(mTransition);
    }

    public final void go(@NonNull Transition transition) {
        transition.addListener(this);
        TransitionManager.go(mScene, transition);
    }

    @Override
    public void onTransitionStart(@NonNull Transition transition) {

    }

    @Override
    public void onTransitionEnd(@NonNull Transition transition) {

    }

    @Override
    public void onTransitionCancel(@NonNull Transition transition) {

    }

    @Override
    public void onTransitionPause(@NonNull Transition transition) {

    }

    @Override
    public void onTransitionResume(@NonNull Transition transition) {

    }
}
