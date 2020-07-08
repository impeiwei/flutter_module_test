package com.example.fluttermodule.custom;

import android.content.Context;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterJNI;
import io.flutter.embedding.engine.loader.FlutterLoader;
import io.flutter.embedding.engine.renderer.FlutterRenderer;

public class XFlutterEngine extends FlutterEngine {

  private FlutterRenderer xFlutterRenderer;

  public XFlutterEngine(Context context) {
    this(context, null);
  }

  public XFlutterEngine(Context context, String[] dartVmArgs) {
    this(context, FlutterLoader.getInstance(), new FlutterJNI(), dartVmArgs, true);
  }

  public XFlutterEngine(Context context, FlutterLoader flutterLoader, FlutterJNI flutterJNI) {
    this(context, flutterLoader, flutterJNI, null, true);
  }

  public XFlutterEngine(Context context, FlutterLoader flutterLoader, FlutterJNI flutterJNI, String[] dartVmArgs, boolean automaticallyRegisterPlugins) {
    super(context, flutterLoader, flutterJNI, dartVmArgs, automaticallyRegisterPlugins);
    xFlutterRenderer = new XFlutterRenderer(flutterJNI);
  }

  @Override
  public FlutterRenderer getRenderer() {
    return xFlutterRenderer;
  }

  public void refresh() {
    getLifecycleChannel().appIsInactive();
  }
}
