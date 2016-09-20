package com.coffeeshot.swarm.mover;

import android.os.SystemClock;

import com.coffeeshot.swarm.render.Renderable;
import com.coffeeshot.swarm.render.GridCell;

import java.util.ArrayList;

/**
 * A runnable that updates the position of each sprite on the screen every
 * frame.
 */
public abstract class Mover implements Runnable
{
  protected ArrayList<? extends Renderable> mRenderables;
  protected int mLeft;
  protected int mTop;
  protected int mRight;
  protected int mBottom;
  protected Object mPauseLock = new Object();
  protected boolean mPaused;
  protected GridCell[][] mGrid;
  protected int mNumCols;
  protected int mNumRows;
  protected long mLastTime;

  public abstract void run();

  public void setRenderables(ArrayList<? extends Renderable> renderables)
  {
    mRenderables = renderables;
  }

  public void setGrid(GridCell[][] grid, int numCols, int numRows)
  {
    mGrid = grid;
    mNumCols = numCols;
    mNumRows = numRows;
  }

  public void setViewSize(int left, int top, int right, int bottom)
  {
    mLeft = left;
    mTop = top;
    mRight = right;
    mBottom = bottom;
  }

  public boolean getIsPaused()
  {
    return mPaused;
  }

  public void syncTime()
  {
    synchronized (mPauseLock)
    {
      mLastTime = SystemClock.uptimeMillis();
      mPauseLock.notifyAll();
    }
  }

  public void setPause(boolean paused)
  {
    mPaused = paused;
    if (!mPaused)
    {
      syncTime();
    }
  }
}
