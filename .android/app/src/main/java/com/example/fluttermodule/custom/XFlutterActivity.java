// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package com.example.fluttermodule.custom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import io.flutter.Log;
import io.flutter.embedding.android.DrawableSplashScreen;
//import io.flutter.embedding.android.FlutterActivityAndFragmentDelegate;
import com.example.fluttermodule.custom.FlutterActivityLaunchConfigs.BackgroundMode;
import io.flutter.embedding.android.FlutterFragment;
//import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.android.SplashScreen;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterShellArgs;
import io.flutter.embedding.engine.plugins.activity.ActivityControlSurface;
import io.flutter.plugin.platform.PlatformPlugin;
import io.flutter.view.FlutterMain;

import static com.example.fluttermodule.custom.FlutterActivityLaunchConfigs.DART_ENTRYPOINT_META_DATA_KEY;
import static com.example.fluttermodule.custom.FlutterActivityLaunchConfigs.DEFAULT_DART_ENTRYPOINT;
import static com.example.fluttermodule.custom.FlutterActivityLaunchConfigs.DEFAULT_INITIAL_ROUTE;
import static com.example.fluttermodule.custom.FlutterActivityLaunchConfigs.EXTRA_BACKGROUND_MODE;
import static com.example.fluttermodule.custom.FlutterActivityLaunchConfigs.EXTRA_CACHED_ENGINE_ID;
import static com.example.fluttermodule.custom.FlutterActivityLaunchConfigs.EXTRA_DESTROY_ENGINE_WITH_ACTIVITY;
import static com.example.fluttermodule.custom.FlutterActivityLaunchConfigs.EXTRA_INITIAL_ROUTE;
import static com.example.fluttermodule.custom.FlutterActivityLaunchConfigs.INITIAL_ROUTE_META_DATA_KEY;
import static com.example.fluttermodule.custom.FlutterActivityLaunchConfigs.NORMAL_THEME_META_DATA_KEY;
import static com.example.fluttermodule.custom.FlutterActivityLaunchConfigs.SPLASH_SCREEN_META_DATA_KEY;

// A number of methods in this class have the same implementation as FlutterFragmentActivity. These
// methods are duplicated for readability purposes. Be sure to replicate any change in this class in
// FlutterFragmentActivity, too.
public class XFlutterActivity extends Activity
    implements XFlutterActivityAndFragmentDelegate.Host,
    LifecycleOwner {
  private static final String TAG = "FlutterActivity";


  // Delegate that runs all lifecycle and OS hook logic that is common between
  // FlutterActivity and FlutterFragment. See the FlutterActivityAndFragmentDelegate
  // implementation for details about why it exists.
  @VisibleForTesting
  protected XFlutterActivityAndFragmentDelegate delegate;

  @NonNull
  private LifecycleRegistry lifecycle;

  public XFlutterActivity() {
    lifecycle = new LifecycleRegistry(this);
  }

  /**
   * This method exists so that JVM tests can ensure that a delegate exists without
   * putting this Activity through any lifecycle events, because JVM tests cannot handle
   * executing any lifecycle methods, at the time of writing this.
   * <p>
   * The testing infrastructure should be upgraded to make FlutterActivity tests easy to
   * write while exercising real lifecycle methods. At such a time, this method should be
   * removed.
   */
  // TODO(mattcarroll): remove this when tests allow for it (https://github.com/flutter/flutter/issues/43798)
  @VisibleForTesting
  /* package */ void setDelegate(@NonNull XFlutterActivityAndFragmentDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    switchLaunchThemeForNormalTheme();

    super.onCreate(savedInstanceState);

    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

    delegate = new XFlutterActivityAndFragmentDelegate(this);
    delegate.onAttach(this);
    delegate.onActivityCreated(savedInstanceState);

    configureWindowForTransparency();
    setContentView(createFlutterView());
    configureStatusBarForFullscreenFlutterExperience();
  }

  private void switchLaunchThemeForNormalTheme() {
    try {
      ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA);
      if (activityInfo.metaData != null) {
        int normalThemeRID = activityInfo.metaData.getInt(NORMAL_THEME_META_DATA_KEY, -1);
        if (normalThemeRID != -1) {
          setTheme(normalThemeRID);
        }
      } else {
        Log.d(TAG, "Using the launch theme as normal theme.");
      }
    } catch (PackageManager.NameNotFoundException exception) {
      Log.e(TAG, "Could not read meta-data for FlutterActivity. Using the launch theme as normal theme.");
    }
  }

  @Nullable
  @Override
  public SplashScreen provideSplashScreen() {
    Drawable manifestSplashDrawable = getSplashScreenFromManifest();
    if (manifestSplashDrawable != null) {
      return new DrawableSplashScreen(manifestSplashDrawable);
    } else {
      return null;
    }
  }

  /**
   * Returns a {@link Drawable} to be used as a splash screen as requested by meta-data in the
   * {@code AndroidManifest.xml} file, or null if no such splash screen is requested.
   * <p>
   * See {@link FlutterActivityLaunchConfigs#SPLASH_SCREEN_META_DATA_KEY} for the meta-data key to
   * be used in a manifest file.
   */
  @Nullable
  @SuppressWarnings("deprecation")
  private Drawable getSplashScreenFromManifest() {
    try {
      ActivityInfo activityInfo = getPackageManager().getActivityInfo(
          getComponentName(),
          PackageManager.GET_META_DATA
      );
      Bundle metadata = activityInfo.metaData;
      int splashScreenId = metadata != null ? metadata.getInt(SPLASH_SCREEN_META_DATA_KEY) : 0;
      return splashScreenId != 0
          ? Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP
            ? getResources().getDrawable(splashScreenId, getTheme())
            : getResources().getDrawable(splashScreenId)
          : null;
    } catch (PackageManager.NameNotFoundException e) {
      // This is never expected to happen.
      return null;
    }
  }

  /**
   * Sets this {@code Activity}'s {@code Window} background to be transparent, and hides the status
   * bar, if this {@code Activity}'s desired {@link BackgroundMode} is {@link BackgroundMode#transparent}.
   * <p>
   * For {@code Activity} transparency to work as expected, the theme applied to this {@code Activity}
   * must include {@code <item name="android:windowIsTranslucent">true</item>}.
   */
  private void configureWindowForTransparency() {
    BackgroundMode backgroundMode = getBackgroundMode();
    if (backgroundMode == BackgroundMode.transparent) {
      getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
      getWindow().setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
      );
    }
  }

  @NonNull
  private View createFlutterView() {
    return delegate.onCreateView(
        null /* inflater */,
        null /* container */,
        null /* savedInstanceState */);
  }

  private void configureStatusBarForFullscreenFlutterExperience() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      Window window = getWindow();
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      window.setStatusBarColor(0x40000000);
      window.getDecorView().setSystemUiVisibility(PlatformPlugin.DEFAULT_SYSTEM_UI);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START);
    delegate.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
    delegate.onResume();
  }

  @Override
  public void onPostResume() {
    super.onPostResume();
    delegate.onPostResume();
  }

  @Override
  protected void onPause() {
    delegate.onPause();
    super.onPause();
    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
  }

  @Override
  protected void onStop() {
    super.onStop();
    delegate.onStop();
    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    delegate.onSaveInstanceState(outState);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    delegate.onDestroyView();
    delegate.onDetach();
    lifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    delegate.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  protected void onNewIntent(@NonNull Intent intent) {
    // TODO(mattcarroll): change G3 lint rule that forces us to call super
    super.onNewIntent(intent);
    delegate.onNewIntent(intent);
  }

  @Override
  public void onBackPressed() {
    delegate.onBackPressed();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    delegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
  }

  @Override
  public void onUserLeaveHint() {
    delegate.onUserLeaveHint();
  }

  @Override
  public void onTrimMemory(int level) {
    super.onTrimMemory(level);
    delegate.onTrimMemory(level);
  }

  /**
   * {@link XFlutterActivityAndFragmentDelegate.Host} method that is used by
   * {@link XFlutterActivityAndFragmentDelegate} to obtain a {@code Context} reference as
   * needed.
   */
  @Override
  @NonNull
  public Context getContext() {
    return this;
  }

  /**
   * {@link XFlutterActivityAndFragmentDelegate.Host} method that is used by
   * {@link XFlutterActivityAndFragmentDelegate} to obtain an {@code Activity} reference as
   * needed. This reference is used by the delegate to instantiate a {@link FlutterView},
   * a {@link PlatformPlugin}, and to determine if the {@code Activity} is changing
   * configurations.
   */
  @Override
  @NonNull
  public Activity getActivity() {
    return this;
  }

  /**
   * {@link XFlutterActivityAndFragmentDelegate.Host} method that is used by
   * {@link XFlutterActivityAndFragmentDelegate} to obtain a {@code Lifecycle} reference as
   * needed. This reference is used by the delegate to provide Flutter plugins with access
   * to lifecycle events.
   */
  @Override
  @NonNull
  public Lifecycle getLifecycle() {
    return lifecycle;
  }

  /**
   * {@link XFlutterActivityAndFragmentDelegate.Host} method that is used by
   * {@link XFlutterActivityAndFragmentDelegate} to obtain Flutter shell arguments when
   * initializing Flutter.
   */
  @NonNull
  @Override
  public FlutterShellArgs getFlutterShellArgs() {
    return FlutterShellArgs.fromIntent(getIntent());
  }

  /**
   * Returns the ID of a statically cached {@link FlutterEngine} to use within this
   * {@code FlutterActivity}, or {@code null} if this {@code FlutterActivity} does not want to
   * use a cached {@link FlutterEngine}.
   */
  @Override
  @Nullable
  public String getCachedEngineId() {
    return getIntent().getStringExtra(EXTRA_CACHED_ENGINE_ID);
  }

  /**
   * Returns false if the {@link FlutterEngine} backing this {@code FlutterActivity} should
   * outlive this {@code FlutterActivity}, or true to be destroyed when the {@code FlutterActivity}
   * is destroyed.
   * <p>
   * The default value is {@code true} in cases where {@code FlutterActivity} created its own
   * {@link FlutterEngine}, and {@code false} in cases where a cached {@link FlutterEngine} was
   * provided.
   */
  @Override
  public boolean shouldDestroyEngineWithHost() {
    boolean explicitDestructionRequested = getIntent().getBooleanExtra(EXTRA_DESTROY_ENGINE_WITH_ACTIVITY, false);
    if (getCachedEngineId() != null || delegate.isFlutterEngineFromHost()) {
      // Only destroy a cached engine if explicitly requested by app developer.
      return explicitDestructionRequested;
    } else {
      // If this Activity created the FlutterEngine, destroy it by default unless
      // explicitly requested not to.
      return getIntent().getBooleanExtra(EXTRA_DESTROY_ENGINE_WITH_ACTIVITY, true);
    }
  }

  /**
   * The Dart entrypoint that will be executed as soon as the Dart snapshot is loaded.
   * <p>
   * This preference can be controlled by setting a {@code <meta-data>} called
   * {@link FlutterActivityLaunchConfigs#DART_ENTRYPOINT_META_DATA_KEY} within the Android manifest
   * definition for this {@code FlutterActivity}.
   * <p>
   * Subclasses may override this method to directly control the Dart entrypoint.
   */
  @NonNull
  public String getDartEntrypointFunctionName() {
    try {
      ActivityInfo activityInfo = getPackageManager().getActivityInfo(
          getComponentName(),
          PackageManager.GET_META_DATA
      );
      Bundle metadata = activityInfo.metaData;
      String desiredDartEntrypoint = metadata != null ? metadata.getString(DART_ENTRYPOINT_META_DATA_KEY) : null;
      return desiredDartEntrypoint != null ? desiredDartEntrypoint : DEFAULT_DART_ENTRYPOINT;
    } catch (PackageManager.NameNotFoundException e) {
      return DEFAULT_DART_ENTRYPOINT;
    }
  }

  /**
   * The initial route that a Flutter app will render upon loading and executing its Dart code.
   * <p>
   * This preference can be controlled with 2 methods:
   * <ol>
   *   <li>Pass a boolean as {@link FlutterActivityLaunchConfigs#EXTRA_INITIAL_ROUTE} with the
   *     launching {@code Intent}, or</li>
   *   <li>Set a {@code <meta-data>} called
   *     {@link FlutterActivityLaunchConfigs#INITIAL_ROUTE_META_DATA_KEY} for this {@code Activity}
   *     in the Android manifest.</li>
   * </ol>
   * If both preferences are set, the {@code Intent} preference takes priority.
   * <p>
   * The reason that a {@code <meta-data>} preference is supported is because this {@code Activity}
   * might be the very first {@code Activity} launched, which means the developer won't have
   * control over the incoming {@code Intent}.
   * <p>
   * Subclasses may override this method to directly control the initial route.
   */
  @NonNull
  public String getInitialRoute() {
    if (getIntent().hasExtra(EXTRA_INITIAL_ROUTE)) {
      return getIntent().getStringExtra(EXTRA_INITIAL_ROUTE);
    }

    try {
      ActivityInfo activityInfo = getPackageManager().getActivityInfo(
          getComponentName(),
          PackageManager.GET_META_DATA
      );
      Bundle metadata = activityInfo.metaData;
      String desiredInitialRoute = metadata != null ? metadata.getString(INITIAL_ROUTE_META_DATA_KEY) : null;
      return desiredInitialRoute != null ? desiredInitialRoute : DEFAULT_INITIAL_ROUTE;
    } catch (PackageManager.NameNotFoundException e) {
      return DEFAULT_INITIAL_ROUTE;
    }
  }

  /**
   * The path to the bundle that contains this Flutter app's resources, e.g., Dart code snapshots.
   * <p>
   * When this {@code FlutterActivity} is run by Flutter tooling and a data String is included
   * in the launching {@code Intent}, that data String is interpreted as an app bundle path.
   * <p>
   * By default, the app bundle path is obtained from {@link FlutterMain#findAppBundlePath()}.
   * <p>
   * Subclasses may override this method to return a custom app bundle path.
   */
  @NonNull
  public String getAppBundlePath() {
    // If this Activity was launched from tooling, and the incoming Intent contains
    // a custom app bundle path, return that path.
    // TODO(mattcarroll): determine if we should have an explicit FlutterTestActivity instead of conflating.
    if (isDebuggable() && Intent.ACTION_RUN.equals(getIntent().getAction())) {
      String appBundlePath = getIntent().getDataString();
      if (appBundlePath != null) {
        return appBundlePath;
      }
    }

    // Return the default app bundle path.
    // TODO(mattcarroll): move app bundle resolution into an appropriately named class.
    return FlutterMain.findAppBundlePath();
  }

  /**
   * Returns true if Flutter is running in "debug mode", and false otherwise.
   * <p>
   * Debug mode allows Flutter to operate with hot reload and hot restart. Release mode does not.
   */
  private boolean isDebuggable() {
    return (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
  }

  /**
   * {@link XFlutterActivityAndFragmentDelegate.Host} method that is used by
   * {@link XFlutterActivityAndFragmentDelegate} to obtain the desired {@link FlutterView.RenderMode}
   * that should be used when instantiating a {@link FlutterView}.
   */
  @NonNull
  @Override
  public FlutterView.RenderMode getRenderMode() {
    return getBackgroundMode() == BackgroundMode.opaque
        ? FlutterView.RenderMode.surface
        : FlutterView.RenderMode.texture;
  }

  /**
   * {@link XFlutterActivityAndFragmentDelegate.Host} method that is used by
   * {@link XFlutterActivityAndFragmentDelegate} to obtain the desired
   * {@link FlutterView.TransparencyMode} that should be used when instantiating a
   * {@link FlutterView}.
   */
  @NonNull
  @Override
  public FlutterView.TransparencyMode getTransparencyMode() {
    return getBackgroundMode() == BackgroundMode.opaque
        ? FlutterView.TransparencyMode.opaque
        : FlutterView.TransparencyMode.transparent;
  }

  /**
   * The desired window background mode of this {@code Activity}, which defaults to
   * {@link BackgroundMode#opaque}.
   */
  @NonNull
  protected BackgroundMode getBackgroundMode() {
    if (getIntent().hasExtra(EXTRA_BACKGROUND_MODE)) {
      return BackgroundMode.valueOf(getIntent().getStringExtra(EXTRA_BACKGROUND_MODE));
    } else {
      return BackgroundMode.opaque;
    }
  }

  /**
   * Hook for subclasses to easily provide a custom {@link FlutterEngine}.
   * <p>
   * This hook is where a cached {@link FlutterEngine} should be provided, if a cached
   * {@link FlutterEngine} is desired.
   */
  @Nullable
  @Override
  public FlutterEngine provideFlutterEngine(@NonNull Context context) {
    // No-op. Hook for subclasses.
    return null;
  }

  /**
   * Hook for subclasses to obtain a reference to the {@link FlutterEngine} that is owned
   * by this {@code FlutterActivity}.
   */
  @Nullable
  protected FlutterEngine getFlutterEngine() {
    return delegate.getFlutterEngine();
  }

  @Nullable
  @Override
  public PlatformPlugin providePlatformPlugin(@Nullable Activity activity, @NonNull FlutterEngine flutterEngine) {
    if (activity != null) {
      return new PlatformPlugin(getActivity(), flutterEngine.getPlatformChannel());
    } else {
      return null;
    }
  }

  /**
   * Hook for subclasses to easily configure a {@code FlutterEngine}, e.g., register
   * plugins.
   * <p>
   * This method is called after {@link #provideFlutterEngine(Context)}.
   */
  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    // No-op. Hook for subclasses.
  }

  /**
   * Hook for the host to cleanup references that were established in
   * {@link #configureFlutterEngine(FlutterEngine)} before the host is destroyed or detached.
   * <p>
   * This method is called in {@link #onDestroy()}.
   */
  @Override
  public void cleanUpFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    // No-op. Hook for subclasses.
  }

  /**
   * Hook for subclasses to control whether or not the {@link FlutterFragment} within this
   * {@code Activity} automatically attaches its {@link FlutterEngine} to this {@code Activity}.
   * <p>
   * This property is controlled with a protected method instead of an {@code Intent} argument because
   * the only situation where changing this value would help, is a situation in which
   * {@code FlutterActivity} is being subclassed to utilize a custom and/or cached {@link FlutterEngine}.
   * <p>
   * Defaults to {@code true}.
   * <p>
   * Control surfaces are used to provide Android resources and lifecycle events to
   * plugins that are attached to the {@link FlutterEngine}. If {@code shouldAttachEngineToActivity}
   * is true then this {@code FlutterActivity} will connect its {@link FlutterEngine} to itself,
   * along with any plugins that are registered with that {@link FlutterEngine}. This allows
   * plugins to access the {@code Activity}, as well as receive {@code Activity}-specific calls,
   * e.g., {@link Activity#onNewIntent(Intent)}. If {@code shouldAttachEngineToActivity} is false,
   * then this {@code FlutterActivity} will not automatically manage the connection between its
   * {@link FlutterEngine} and itself. In this case, plugins will not be offered a reference to
   * an {@code Activity} or its OS hooks.
   * <p>
   * Returning false from this method does not preclude a {@link FlutterEngine} from being
   * attaching to a {@code FlutterActivity} - it just prevents the attachment from happening
   * automatically. A developer can choose to subclass {@code FlutterActivity} and then
   * invoke {@link ActivityControlSurface#attachToActivity(Activity, Lifecycle)}
   * and {@link ActivityControlSurface#detachFromActivity()} at the desired times.
   * <p>
   * One reason that a developer might choose to manually manage the relationship between the
   * {@code Activity} and {@link FlutterEngine} is if the developer wants to move the
   * {@link FlutterEngine} somewhere else. For example, a developer might want the
   * {@link FlutterEngine} to outlive this {@code FlutterActivity} so that it can be used
   * later in a different {@code Activity}. To accomplish this, the {@link FlutterEngine} may
   * need to be disconnected from this {@code FluttterActivity} at an unusual time, preventing
   * this {@code FlutterActivity} from correctly managing the relationship between the
   * {@link FlutterEngine} and itself.
   */
  @Override
  public boolean shouldAttachEngineToActivity() {
    return true;
  }

  @Override
  public void onFlutterUiDisplayed() {
    // Notifies Android that we're fully drawn so that performance metrics can be collected by
    // Flutter performance tests.
    // This was supported in KitKat (API 19), but has a bug around requiring
    // permissions. See https://github.com/flutter/flutter/issues/46172
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      reportFullyDrawn();
    }
  }

  @Override
  public void onFlutterUiNoLongerDisplayed() {
    // no-op
  }

}
