package com.example.fluttermodule.host;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.example.fluttermodule.custom.XFlutterEngine;

import java.util.HashSet;
import java.util.Set;

import io.flutter.app.FlutterApplication;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.embedding.engine.dart.DartExecutor;

public class MyApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();

    // Instantiate a FlutterEngine.
    FlutterEngine flutterEngine = new XFlutterEngine(this);

    flutterEngine.getNavigationChannel().setInitialRoute("/cache");

    // Start executing Dart code to pre-warm the FlutterEngine.
    flutterEngine.getDartExecutor().executeDartEntrypoint(
      DartExecutor.DartEntrypoint.createDefault()
    );

    // Cache the FlutterEngine to be used by FlutterActivity.
    FlutterEngineCache
      .getInstance()
      .put("my_engine_id", flutterEngine);

    registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

      private Set<Activity> mCacheActivity = new HashSet();

      @Override
      public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (activity instanceof CacheFlutterActivity) {
          mCacheActivity.add(activity);
        }
      }

      @Override
      public void onActivityStarted(Activity activity) {

      }

      @Override
      public void onActivityResumed(Activity activity) {

      }

      @Override
      public void onActivityPaused(Activity activity) {

      }

      @Override
      public void onActivityStopped(Activity activity) {
        if (activity instanceof CacheFlutterActivity && !mCacheActivity.isEmpty()) {
          XFlutterEngine engine = (XFlutterEngine)FlutterEngineCache.getInstance().get("my_engine_id");
          engine.refresh();
        }
      }

      @Override
      public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

      }

      @Override
      public void onActivityDestroyed(Activity activity) {
        if (activity instanceof CacheFlutterActivity) {
          mCacheActivity.remove(activity);
        }
        if (!mCacheActivity.isEmpty()) {
          XFlutterEngine engine = (XFlutterEngine)FlutterEngineCache.getInstance().get("my_engine_id");
          engine.refresh();
        }
      }
    });
  }
}
