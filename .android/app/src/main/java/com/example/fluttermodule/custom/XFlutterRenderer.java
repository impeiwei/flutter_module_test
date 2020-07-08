package com.example.fluttermodule.custom;

import android.view.Surface;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.FlutterJNI;
import io.flutter.embedding.engine.renderer.FlutterRenderer;
import io.flutter.embedding.engine.renderer.FlutterUiDisplayListener;

public class XFlutterRenderer extends FlutterRenderer {

  private FlutterJNI xFlutterJNI;
  private Surface xSurface;
  private boolean isDisplayingFlutterUi = false;

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
  public void startRenderingToSurface(Surface surface) {
    if (this.xSurface != null) {
      stopRenderingToSurface();
    }
    this.xSurface = surface;
    xFlutterJNI.onSurfaceCreated(surface);
  }

  @Override
  public void surfaceChanged(int width, int height) {
    xFlutterJNI.onSurfaceChanged(width, height);
  }

  @Override
  public void stopRenderingToSurface() {
    xFlutterJNI.onSurfaceDestroyed();

    xSurface = null;

    if (isDisplayingFlutterUi) {
      flutterUiDisplayListener.onFlutterUiNoLongerDisplayed();
    }

    isDisplayingFlutterUi = false;
  }

  public void refresh() {

  }


}
