package com.coffeeshot.swarm.render;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.coffeeshot.swarm.ProfileRecorder;

/**
 * A generic Canvas rendering Thread. Delegates to a Renderer instance to do the
 * actual drawing.
 */
class CanvasThread extends Thread
{
  public boolean mPaused;
  public Runnable mEvent;

  private boolean mDone;
  private boolean mHasFocus;
  private boolean mHasSurface;
  private boolean mContextLost;
  private int mWidth;
  private int mHeight;
  private Renderer mRenderer;
  private SurfaceHolder mSurfaceHolder;

  private boolean mSizeChanged = true;

  CanvasThread(SurfaceHolder holder, Renderer renderer)
  {
    super();
    mDone = false;
    mWidth = 0;
    mHeight = 0;
    mRenderer = renderer;
    mSurfaceHolder = holder;
    setName("CanvasThread");
  }

  @Override
  public void run()
  {

    boolean tellRendererSurfaceChanged = true;

    /*
     * This is our main activity thread's loop, we go until asked to quit.
     */
    final ProfileRecorder profiler = ProfileRecorder.sSingleton;
    while (!mDone)
    {
      profiler.start(ProfileRecorder.PROFILE_FRAME);
      /*
       * Update the asynchronous state (window size)
       */
      int w;
      int h;
      synchronized (this)
      {
        // If the user has set a runnable to run in this thread,
        // execute it and record the amount of time it takes to
        // run.
        if (mEvent != null)
        {
          profiler.start(ProfileRecorder.PROFILE_SIM);
          mEvent.run();
          profiler.stop(ProfileRecorder.PROFILE_SIM);
        }

        if (needToWait())
        {
          while (needToWait())
          {
            try
            {
              wait();
            }
            catch (InterruptedException e)
            {

            }
          }
        }
        if (mDone)
        {
          break;
        }
        tellRendererSurfaceChanged = mSizeChanged;
        w = mWidth;
        h = mHeight;
        mSizeChanged = false;
      }

      if (tellRendererSurfaceChanged)
      {
        mRenderer.sizeChanged(w, h);
        tellRendererSurfaceChanged = false;
      }

      if ((w > 0) && (h > 0))
      {
        // Get ready to draw.
        // We record both lockCanvas() and unlockCanvasAndPost()
        // as part of "page flip" time because either may block
        // until the previous frame is complete.
        profiler.start(ProfileRecorder.PROFILE_PAGE_FLIP);
        Canvas canvas = mSurfaceHolder.lockCanvas();
        profiler.start(ProfileRecorder.PROFILE_PAGE_FLIP);
        if (canvas != null)
        {
          // Draw a frame!
          profiler.start(ProfileRecorder.PROFILE_DRAW);
          mRenderer.drawFrame(canvas);
          profiler.stop(ProfileRecorder.PROFILE_DRAW);

          profiler.start(ProfileRecorder.PROFILE_PAGE_FLIP);
          mSurfaceHolder.unlockCanvasAndPost(canvas);
          profiler.stop(ProfileRecorder.PROFILE_PAGE_FLIP);

        }
      }
      profiler.stop(ProfileRecorder.PROFILE_FRAME);
      profiler.endFrame();
    }
  }

  private boolean needToWait()
  {
    return (mPaused || (!mHasFocus) || (!mHasSurface) || mContextLost)
        && (!mDone);
  }

  public void surfaceCreated()
  {
    synchronized (this)
    {
      mHasSurface = true;
      mContextLost = false;
      notify();
    }
  }

  public void surfaceDestroyed()
  {
    synchronized (this)
    {
      mHasSurface = false;
      notify();
    }
  }

  public void onPause()
  {
    synchronized (this)
    {
      mPaused = true;
    }
  }

  public void onResume()
  {
    synchronized (this)
    {
      mPaused = false;
      notify();
    }
  }

  public void onWindowFocusChanged(boolean hasFocus)
  {
    synchronized (this)
    {
      mHasFocus = hasFocus;
      if (mHasFocus == true)
      {
        notify();
      }
    }
  }

  public void onWindowResize(int w, int h)
  {
    synchronized (this)
    {
      mWidth = w;
      mHeight = h;
      mSizeChanged = true;
    }
  }

  public void requestExitAndWait()
  {
    // don't call this from CanvasThread thread or it is a guaranteed
    // deadlock!
    synchronized (this)
    {
      mDone = true;
      notify();
    }
    try
    {
      join();
    }
    catch (InterruptedException ex)
    {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Queue an "event" to be run on the rendering thread.
   *
   * @param r
   *          the runnable to be run on the rendering thread.
   */
  public void setEvent(Runnable r)
  {
    synchronized (this)
    {
      mEvent = r;
    }
  }

  public void clearEvent()
  {
    synchronized (this)
    {
      mEvent = null;
    }
  }

}
