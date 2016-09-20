package com.coffeeshot.swarm.render;

import android.graphics.Point;

import com.coffeeshot.swarm.enums.CellStatus;

public class GridCell
{
  // Actual location on the screen
  public Point screen = new Point();
  // This cell's coordinate on the grid
  public Point grid = new Point();
  // Occupation status
  public CellStatus status = CellStatus.VACANT;

  public void setScreenLoc(int x, int y)
  {
    screen.x = x;
    screen.y = y;
  }

  public void setGridLoc(int x, int y)
  {
    grid.x = x;
    grid.y = y;
  }
}
