package tfm.uniovi.pirateseas.view.activities;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentCallbacks2;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;

import tfm.uniovi.pirateseas.controller.audio.MusicManager;

public class App extends Application implements ActivityLifecycleCallbacks {

    private int activityReferences = 0;
    private boolean isActivityChangingConfigurations = false;
    MemoryBoss mMemoryBoss;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        mMemoryBoss = new MemoryBoss();
        registerComponentCallbacks(mMemoryBoss);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (++activityReferences == 1 && !isActivityChangingConfigurations) {
            // App enters foreground
            Log.d(App.class.getCanonicalName(), "onActivityStarted -> Foreground");
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        isActivityChangingConfigurations = activity.isChangingConfigurations();
        if (--activityReferences == 0 && !isActivityChangingConfigurations) {
            // App enters background
            Log.d(App.class.getCanonicalName(), "onActivityStopped -> Background");
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    class MemoryBoss implements ComponentCallbacks2 {
        @Override
        public void onConfigurationChanged(Configuration configuration) {

        }

        @Override
        public void onLowMemory() {

        }

        @Override
        public void onTrimMemory(int level) {
            if(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN == level){
                // Background
                Log.d(MemoryBoss.class.getCanonicalName(), "onTrimMemory -> Background");
                MusicManager.getInstance().pauseBackgroundMusic();
            }
        }
    }
}
