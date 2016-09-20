/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.coffeeshot.swarm.render;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.coffeeshot.swarm.SpriteGridActivity;
import com.coffeeshot.swarm.mover.Mover;

/**
 * Implements a surface view which writes updates to the surface's canvas using
 * a separate rendering thread. This class is based heavily on GLSurfaceView.
 */
public class CanvasSurfaceView extends SurfaceView implements SurfaceHolder.Callback
{
  private SurfaceHolder mHolder;
  private CanvasThread mCanvasThread;
  private Context mContext;

  public CanvasSurfaceView(Context context)
  {
    super(context);
    mContext = context;
    init();
  }

  public CanvasSurfaceView(Context context, AttributeSet attrs)
  {
    super(context, attrs);
    init();
  }

  private void init()
  {
    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed
    mHolder = getHolder();
    mHolder.addCallback(this);
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    ((SpriteGridActivity) mContext).setBomb();
    return true;
  }

  public SurfaceHolder getSurfaceHolder()
  {
    return mHolder;
  }

  /** Sets the user's renderer and kicks off the rendering thread. */
  public void setRenderer(Renderer renderer)
  {
    mCanvasThread = new CanvasThread(mHolder, renderer);
    mCanvasThread.start();
  }

  public void surfaceCreated(SurfaceHolder holder)
  {
    mCanvasThread.surfaceCreated();
  }

  public void surfaceDestroyed(SurfaceHolder holder)
  {
    // Surface will be destroyed when we return
    mCanvasThread.surfaceDestroyed();
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
  {
    // Surface size or format has changed. This should not happen in this
    // example.
    mCanvasThread.onWindowResize(w, h);
  }

  /** Inform the view that the activity is paused. */
  public void onPause()
  {
    mCanvasThread.onPause();
  }

  /** Inform the view that the activity is resumed. */
  public void onResume()
  {
    mCanvasThread.onResume();
    Mover m = (Mover) mCanvasThread.mEvent;
    m.setPause(false);
  }

  /** Inform the view that the window focus has changed. */
  @Override
  public void onWindowFocusChanged(boolean hasFocus)
  {
    super.onWindowFocusChanged(hasFocus);
    mCanvasThread.onWindowFocusChanged(hasFocus);
  }

  /**
   * Set an "event" to be run on the rendering thread.
   * 
   * @param r
   *          the runnable to be run on the rendering thread.
   */
  public void setEvent(Runnable r)
  {
    mCanvasThread.setEvent(r);
  }

  /** Clears the runnable event, if any, from the rendering thread. */
  public void clearEvent()
  {
    mCanvasThread.clearEvent();
  }

  @Override
  protected void onDetachedFromWindow()
  {
    super.onDetachedFromWindow();
    mCanvasThread.requestExitAndWait();
  }

  protected void stopDrawing()
  {
    mCanvasThread.requestExitAndWait();
  }

  public boolean getIsPaused()
  {
    return mCanvasThread.mPaused;
  }



}
