package com.coffeeshot.swarm.mover;

import android.graphics.Color;
import android.os.SystemClock;

import com.coffeeshot.swarm.SpriteGridActivity;
import com.coffeeshot.swarm.enums.CellStatus;
import com.coffeeshot.swarm.render.GridCell;
import com.coffeeshot.swarm.render.SquareThing;
import com.coffeeshot.swarm.util.Utils;

import java.util.ArrayList;
import java.util.Random;

/**
 * A simple runnable that updates the position of each sprite on the screen
 * every frame by moving them to adjacent cells on in a grid.
 */
public class ShuffleMover extends Mover
{
  private long       mLastTime;
  boolean            allowSquaresToOverlapOnMove = true;

  static final float COEFFICIENT_OF_RESTITUTION  = 0.75f;
  static final float SPEED_OF_GRAVITY            = 150.0f;
  static final long  JUMBLE_EVERYTHING_DELAY     = 15 * 1000;
  static final long  WAIT_BEFORE_MOVE_DELAY      = 500;
  static final float MAX_VELOCITY                = 8000.0f;

  public void run()
  {
    // Perform a single simulation step.
    if (mRenderables != null)
    {
      final long time = SystemClock.uptimeMillis();
      final long timeDelta = time - mLastTime;
      final float timeDeltaSeconds = mLastTime > 0.0f ? timeDelta / 1000.0f : 0.0f;
      mLastTime = time;

      for (int n = 0; n < mRenderables.size(); n++)
      {
        SquareThing p = (SquareThing) mRenderables.get(n); // ArrayOutofBounds
                                                              // if 0 is empty
        boolean move = true;
        // Check if the object has reached its next destination
        if (hasReachedNextDest(p))
        {
          /* Object has reached its next destination */
          if (p.nextMoveTime == 0)
          {
            p.nextMoveTime = time + WAIT_BEFORE_MOVE_DELAY;
          }

          if (p.nextMoveTime > 0 && time > p.nextMoveTime)
          {
            p.nextMoveTime = 0;
            move = true;

            // current grid becomes the previous grid
            {
              // Utils.robteelog("-----");
              // Utils.robteelog("" + object.previousGridCell.status);
              // Utils.robteelog("" +
              // mGrid[object.previousGridCell.grid.x][object.previousGridCell.grid.y].status);
              // This WORKS
              if (!allowSquaresToOverlapOnMove)
              {
                mGrid[p.previousGridCell.grid.x][p.previousGridCell.grid.y].status = CellStatus.VACANT;
              }
              // Utils.robteelog("" + object.previousGridCell.status);
              // Utils.robteelog("" +
              // mGrid[object.previousGridCell.grid.x][object.previousGridCell.grid.y].status);
              // Utils.robteelog("-----");
            }
            // This DOESN'T WORK
            // object.previousGridCell.status = CellStatus.VACANT;

            if (Utils.GRID_OVERLAY_DEBUG)
            {
              SpriteGridActivity.drawOnCanvas(
                  p.previousGridCell.screen.x + 20, p.previousGridCell.screen.y + 20, Color.GREEN);
            }
            iterate(p);
          }
          else
          {
            move = false;
          }
        }
        else
        {
          move = true;
        }

        if (move)
        {
          // Move.
          // x
          if (p.x < p.gridCell.screen.x)
          {
            p.x = Math.min(p.gridCell.screen.x, (p.x + (p.velocityX * timeDeltaSeconds)));
          }
          else
          {
            p.x = Math.max(p.gridCell.screen.x, (p.x + (-p.velocityX * timeDeltaSeconds)));
          }
          // y
          if (p.y < p.gridCell.screen.y)
          {
            p.y = Math.min(p.gridCell.screen.y, (p.y + (p.velocityY * timeDeltaSeconds)));
          }
          else
          {
            p.y = Math.max(p.gridCell.screen.y, (p.y + (-p.velocityY * timeDeltaSeconds)));
          }

        }
      }
    }
  }

  private void iterate(SquareThing p)
  {
    p.previousGridCell.status = CellStatus.VACANT;
    // if (mDiscoMode)
    // {
    // Random c = new Random();
    // ThingColors thingColor =
    // ThingColors.fromOrdinal((c.nextInt(ThingColors.values().length)));
    // p.thingColor = thingColor;
    // }
    ArrayList<GridCell> destinations = new ArrayList<GridCell>();
    int xx;
    int yy = 0;
    if (Utils.ITERATE_DEBUG)
    {
      Utils.robteelog("This SquareThing is in mGrid["
          + p.gridCell.grid.x + "][" + p.gridCell.grid.y + "]");
    }
    for (xx = p.gridCell.grid.x - 1; xx <= p.gridCell.grid.x + 1; ++xx)
    {
      for (yy = p.gridCell.grid.y - 1; yy <= p.gridCell.grid.y + 1; ++yy)
      {
        // Check not out of bounds
        if (((xx >= 0) && (xx < mNumCols)) &&
            ((yy >= 0) && (yy < mNumRows)))
        {
          // Check not self
          GridCell gcc = mGrid[xx][yy];
          if (!(gcc.equals(p.gridCell)))
          {
            if (Utils.ITERATE_DEBUG)
            {
              String status = gcc.status.toString();
              Utils.robteelog("mGrid[" + xx + "][" + yy + "] is " + status);
            }
            if (gcc.status == CellStatus.VACANT) // TODO: check for color of
                                                 // cell etc. instead of status
            {
              // add this GridCell to pool of possible move destinations if it
              // is not a diagonal move (both x and y are different)
              if (!(xx != p.gridCell.grid.x && yy != p.gridCell.grid.y))
              {
                gcc.grid.x = xx;
                gcc.grid.y = yy;
                if (Utils.GRID_OVERLAY_DEBUG)
                {
                  SpriteGridActivity.drawOnCanvas(
                      gcc.screen.x + 45,
                      gcc.screen.y + 20, Color.YELLOW);
                }
                destinations.add(gcc);
              }
            }
          }
        }
      }
    }

    // move to a random destination cell
    if (destinations.size() > 0)
    {
      Random d = new Random();
      d.setSeed(mLastTime);
      int dd = d.nextInt(destinations.size());
      GridCell dest = destinations.get(dd);

      if (allowSquaresToOverlapOnMove)
      {
        if (Utils.GRID_OVERLAY_DEBUG)
        {
          SpriteGridActivity.drawOnCanvas(p.gridCell.screen.x + 20, p.gridCell.screen.y + 20, Color.RED);
        }
        // THIS WORKS
        // Update status of _old_ GridCell
        // Leave status set to VACANT to allow other squares to move
        // immediately into this cell
        p.gridCell.status = CellStatus.VACANT;
        p.copyGridCellintoPreviousGridCell(CellStatus.VACANT);
      }
      else
      {
        // THIS WORK ?
        // p.gridCell.status = CellStatus.OCCUPIED;
        if (Utils.GRID_OVERLAY_DEBUG)
        {
          SpriteGridActivity.drawOnCanvas(p.gridCell.screen.x + 20, p.gridCell.screen.y + 20, Color.RED);
        }
        // the grid will be free'd after the square has moved
        p.copyGridCellintoPreviousGridCell(CellStatus.OCCUPIED);
      }
      // destination (next) grid becomes the current one
      p.gridCell.grid.x = dest.grid.x;
      p.gridCell.grid.y = dest.grid.y;

      // set the dest into it
      dest.status = CellStatus.OCCUPIED; // destination cell is now occupied
      p.gridCell = dest;
      if (Utils.ITERATE_DEBUG)
      {
        Utils.robteelog("Moving SquareThing to"
            + "mGrid[" + dest.grid.x + "][" + dest.grid.y + "]\n##########\n");
      }

    }
    else
    {
      // Utils.robteelog("Cannot move this SquareThing\n##########\n");
    }
  }

  private boolean hasReachedNextDest(SquareThing o)
  {
    if (o.gridCell.screen.x == 0.0f || o.gridCell.screen.y == 0.0f)
    {
      return false;
    }
    else if (o.x == o.gridCell.screen.x && o.y == o.gridCell.screen.y)
    {
      return true;
    }
    else
    {
      return false;
    }
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
