package com.coffeeshot.swarm.enums;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;

import com.coffeeshot.swarm.R;
import com.coffeeshot.swarm.SpriteGridActivity;
import com.coffeeshot.swarm.util.Utils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public enum ThingColors
{
  BLUE("#0099CC", "#33B5E5"),
  RED("#CC0000", "#FF4444"),
  GREEN("#669900", "#99CC00"),
  PURPLE("#9933CC", "#AA66CC"),
  ORANGE("#FF8800", "#FFBB33"),
  ;

  private String mBorderColor;
  private String mFillColor;
  private int    mSize;
  private Bitmap mBitmap;

  ThingColors(String borderColor, String fillColor)
  {
    mBorderColor = borderColor;
    mFillColor = fillColor;
  }

  private static ThingColors[] allValues = values();

  public static ThingColors fromOrdinal(int n)
  {
    return allValues[n];
  }

  public Bitmap getBitmap(int size)
  {
    return createBitmap(size);
  }

  public static Bitmap convertToMutable(final Context context, final Bitmap imgIn)
  {
    final int width = imgIn.getWidth(), height = imgIn.getHeight();
    final Bitmap.Config type = imgIn.getConfig();
    File outputFile = null;
    final File outputDir = context.getCacheDir();
    try
    {
      outputFile = File.createTempFile(Long.toString(System.currentTimeMillis()), null, outputDir);
      outputFile.deleteOnExit();
      final RandomAccessFile randomAccessFile = new RandomAccessFile(outputFile, "rw");
      final FileChannel channel = randomAccessFile.getChannel();
      final MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, imgIn.getRowBytes() * height);
      imgIn.copyPixelsToBuffer(map);
      imgIn.recycle();
      final Bitmap result = Bitmap.createBitmap(width, height, type);
      map.position(0);
      result.copyPixelsFromBuffer(map);
      channel.close();
      randomAccessFile.close();
      outputFile.delete();
      return result;
    }
    catch (final Exception e)
    {
    }
    finally
    {
      if (outputFile != null)
      {
        outputFile.delete();
      }
    }
    return null;
  }

  public static Bitmap decodeMutableBitmapFromResourceId(final Context context, final int bitmapResId)
  {
    final BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
    {
      bitmapOptions.inMutable = true;
    }
    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), bitmapResId, bitmapOptions);
    if (!bitmap.isMutable())
    {
      bitmap = convertToMutable(context, bitmap);
    }
    return bitmap;
  }

  private Bitmap createBitmap(int size)
  {
    if (mSize == size && mBitmap != null)
    {
      return mBitmap;
    }
    mSize = size;
    Bitmap bitmap = null;
    // bitmap = Bitmap.createBitmap(size, size,
    // SpriteGridActivity.sBitmapOptions.inPreferredConfig);
    bitmap = decodeMutableBitmapFromResourceId(SpriteGridActivity.mContext, R.drawable.transparent_sprite);
    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();

    if (Utils.GRID_DEBUG)
    {
      paint.setAntiAlias(false);
      paint.setColor(Color.RED);
      paint.setStyle(Paint.Style.STROKE);
      canvas.drawRect(0, 0, size, size, paint);
    }

    paint.setAntiAlias(true);
    paint.setStyle(Paint.Style.FILL);
    // Make the squares fit nicely just inside the grid cells
    int fillOffset = 6;
    // border
    paint.setColor(Color.parseColor(mBorderColor));
    canvas.drawRect(
        0 + fillOffset,
        0 + fillOffset,
        size - fillOffset,
        size - fillOffset,
        paint);
    // fill
    fillOffset += 4;
    paint.setColor(Color.parseColor(mFillColor));
    canvas.drawRect(
        0 + fillOffset,
        0 + fillOffset,
        size - fillOffset,
        size - fillOffset,
        paint);

    /*
     * Note: cropping the bitmap reduces its size from 147456 bytes to 3600
     * bytes for my smallest size to 90000 bytes for my largest.
     */
    if (Utils.GRID_DEBUG)
    {
      Utils.robteelog("bitmap:        " + bitmap.getByteCount() + " bytes");
      Utils.robteelog("bitmap:        " + bitmap.getConfig());
    }
    Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, 0, 0, size, size);
    mBitmap = croppedBitmap;
    if (Utils.GRID_DEBUG)
    {
      Utils.robteelog("croppedBitmap: " + croppedBitmap.getByteCount() + " bytes");
      Utils.robteelog("croppedBitmap: " + bitmap.getConfig());
    }

    return mBitmap;
  }
}
