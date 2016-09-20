package com.coffeeshot.swarm.render;

import android.graphics.Bitmap;

import com.coffeeshot.swarm.enums.CellStatus;
import com.coffeeshot.swarm.enums.ThingColors;

import java.util.Random;

public class SquareThing extends CanvasSprite
{
  public GridCell    gridCell;
  public ThingColors thingColor;
  public GridCell    previousGridCell;

  public SquareThing(Bitmap bitmap)
  {
    super(bitmap);
    // Superclass stuff
    width = bitmap.getWidth();
    height = bitmap.getHeight();
    x = 0;
    y = 0;
  }

  public SquareThing(GridCell cell, ThingColors col, Bitmap bitmap)
  {
    this(bitmap);
    x = cell.screen.x;
    y = cell.screen.y;
    gridCell = cell;
    previousGridCell = new GridCell();
    thingColor = col;
  }

  public void copyGridCellintoPreviousGridCell(CellStatus status)
  {
    previousGridCell.setGridLoc(gridCell.grid.x, gridCell.grid.y);
    previousGridCell.setScreenLoc(gridCell.screen.x, gridCell.screen.y);
    previousGridCell.status = status;
  }

  public void ditherVelocity()
  {
    // Vary velocity by +/- 10% so they don't all move at the same time
    Random r = new Random();
    int n = r.nextInt((int) (velocityX * 1.1 - velocityX * 0.9));
    velocityX += n;
    velocityY += n;
  }
}
