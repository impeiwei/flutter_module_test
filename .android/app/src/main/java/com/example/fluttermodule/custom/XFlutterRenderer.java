package com.example.fluttermodule.custom;

import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;
import java.util.Stack;
import java.util.function.Consumer;

import io.flutter.embedding.engine.FlutterJNI;
import io.flutter.embedding.engine.renderer.FlutterRenderer;
import io.flutter.embedding.engine.renderer.FlutterUiDisplayListener;

public class XFlutterRenderer extends FlutterRenderer {

  private FlutterJNI xFlutterJNI;
  private Surface xSurface;
  private boolean isDisplayingFlutterUi = false;
  private Stack<Surface> cacheSurfaces = new Stack<>();

  @NonNull
  private final FlutterUiDisplayListener flutterUiDisplayListener = new FlutterUiDisplayListener() {
    @Override
    public void onFlutterUiDisplayed() {
      isDisplayingFlutterUi = true;
    }

    @Override
    public void onFlutterUiNoLongerDisplayed() {
      isDisplayingFlutterUi = false;
    }
  };

  public XFlutterRenderer(FlutterJNI flutterJNI) {
    super(flutterJNI);
    this.xFlutterJNI = flutterJNI;
  }

  @Override
  public void startRenderingToSurface(@NonNull Surface surface) {
    if (this.xSurface != null) {
      stopRenderingToSurface();
    }
    this.xSurface = surface;
    // 记录当前
    cacheSurfaces.add(this.xSurface);
    xFlutterJNI.onSurfaceCreated(surface);
  }

  @Override
  public void surfaceChanged(int width, int height) {
    xFlutterJNI.onSurfaceChanged(width, height);
  }

  @Override
  public void stopRenderingToSurface() {
    print("begin stopRenderingToSurface");
    xFlutterJNI.onSurfaceDestroyed();
    print("after stopRenderingToSurface");
    xSurface = null;

    if (isDisplayingFlutterUi) {
      flutterUiDisplayListener.onFlutterUiNoLongerDisplayed();
    }

    isDisplayingFlutterUi = false;
  }

  public boolean isDisplayingFlutterUi() {
    return isDisplayingFlutterUi;
  }

  public void addIsDisplayingFlutterUiListener(@NonNull FlutterUiDisplayListener listener) {
    xFlutterJNI.addIsDisplayingFlutterUiListener(listener);

    if (isDisplayingFlutterUi) {
      listener.onFlutterUiDisplayed();
    }
  }

  public void removeIsDisplayingFlutterUiListener(@NonNull FlutterUiDisplayListener listener) {
    xFlutterJNI.removeIsDisplayingFlutterUiListener(listener);
  }

  void print(String timeline){
    ListIterator<Surface> iterator = cacheSurfaces.listIterator();
    while (iterator.hasNext()) {
      Surface surface = iterator.next();
      Log.i("XFlutterRenderer", timeline + " ===> " + surface + " : " +surface.isValid());
    }
  }


}
