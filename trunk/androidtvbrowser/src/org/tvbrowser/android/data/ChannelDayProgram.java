package org.tvbrowser.android.data;

import java.util.ArrayList;

import org.tvbrowser.android.widgets.TVBrowserGridView;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class ChannelDayProgram {

  private int mChannelId;
  private String mChannelName;
  private ArrayList<Program> mPrograms;

  protected ChannelDayProgram(int id, String name) {
    mChannelId = id;
    mChannelName = name;
  }

  protected void loadPrograms(long timeInMillis) {
    mPrograms = DataLoader.loadProgramsFromDatabase(timeInMillis, mChannelId);
  }

  public void drawChannelNames(Canvas canvas, TVBrowserGridView.DrawConfiguration drawConfiguration, int y) {
    canvas.drawRect(new Rect(0, y, TVBrowserGridView.CHANNELNAME_SIZE, y + TVBrowserGridView.TEXTHEIGHT + 2
        * TVBrowserGridView.HORIZONTAL_GAP), drawConfiguration.headerBackground);
    Paint textPaint = drawConfiguration.headerTextColor;
    int titlestop = textPaint.breakText(mChannelName, true, TVBrowserGridView.CHANNELNAME_SIZE - 2
        * TVBrowserGridView.VERTICAL_GAP, null);
    canvas.drawText(mChannelName, 0, titlestop, TVBrowserGridView.VERTICAL_GAP, y + TVBrowserGridView.TEXTHEIGHT
        + TVBrowserGridView.HORIZONTAL_GAP, textPaint);
  }

  public void drawBroadcasts(Canvas canvas, TVBrowserGridView.DrawConfiguration drawConfiguration, int y,
      int visibleStartX, int visibleEndX, long nowInMillis, int nowx, int offsetX, Program selectBroadcast) {
    if (mPrograms != null) {
      for (Program broadcast : mPrograms) {
        if (broadcast.isBetween(visibleStartX, visibleEndX)) {
          broadcast.draw(canvas, drawConfiguration, y, offsetX, nowInMillis, nowx, broadcast == selectBroadcast);
        }
      }
    }
  }

  public Program getBroadcastByPosition(int position) {
    if (mPrograms != null) {
      for (Program broadcast : mPrograms) {
        if (broadcast.containsPosition(position)) {
          return broadcast;
        }
      }
    }
    return null;
  }

  public String getChannelName() {
    return mChannelName;
  }
}
