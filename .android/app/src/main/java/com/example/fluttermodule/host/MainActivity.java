package com.example.fluttermodule.host;

import android.content.Intent;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {

  @Override
  public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
    GeneratedPluginRegistrant.registerWith(flutterEngine);


    new MethodChannel(flutterEngine.getDartExecutor(), "start_activity").setMethodCallHandler(
            new MethodChannel.MethodCallHandler() {
              @Override
              public void onMethodCall(MethodCall call, MethodChannel.Result result) {
                if (call.method.equals("startActivity")) {
                  Intent intent = new Intent(MainActivity.this, CacheFlutterActivity.class);
                  startActivity(intent);

                } else {
                  result.notImplemented();
                }
              }
            }
    );

  }


}
