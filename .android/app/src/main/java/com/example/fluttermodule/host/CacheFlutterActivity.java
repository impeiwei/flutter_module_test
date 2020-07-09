package com.example.fluttermodule.host;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.fluttermodule.custom.XFlutterActivity;

//import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class CacheFlutterActivity extends XFlutterActivity {

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    GeneratedPluginRegistrant.registerWith(flutterEngine);
    new MethodChannel(flutterEngine.getDartExecutor(), "start_cache_activity").setMethodCallHandler(
            new MethodChannel.MethodCallHandler() {
              @Override
              public void onMethodCall(MethodCall call, MethodChannel.Result result) {
                if (call.method.equals("startActivity")) {
                  Intent intent = new Intent(CacheFlutterActivity.this, CacheFlutterActivity.class);
                  startActivity(intent);

                } else {
                  result.notImplemented();
                }
              }
            }
    );

  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  public FlutterEngine provideFlutterEngine(Context context) {
    return FlutterEngineCache
            .getInstance()
            .get("my_engine_id");
  }

  @Override
  public String getInitialRoute() {
    return "/cache";
  }

  @Override
  public void onBackPressed() {
    finish();
  }
}
