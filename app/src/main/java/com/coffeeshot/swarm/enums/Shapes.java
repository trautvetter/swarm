package com.coffeeshot.swarm.enums;

public enum Shapes
{
  LINE("Line"),
  RECTANGLE("Rectangle");

  private String mString;

  Shapes(String string)
  {
    mString = string;
  }

  public String getString()
  {
    return mString;
  }
}
