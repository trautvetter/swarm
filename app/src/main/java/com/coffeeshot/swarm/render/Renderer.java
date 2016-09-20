package com.coffeeshot.swarm.render;

import android.graphics.Canvas;

/** A generic renderer interface. */
public interface Renderer
{
  /**
   * Surface changed size. Called after the surface is created and whenever
   * the surface size changes. Set your viewport here.
   *
   * @param width
   * @param height
   */
  void sizeChanged(int width, int height);

  /**
   * Draw the current frame.
   *
   * @param canvas
   *          The target canvas to draw into.
   */
  void drawFrame(Canvas canvas);
}
