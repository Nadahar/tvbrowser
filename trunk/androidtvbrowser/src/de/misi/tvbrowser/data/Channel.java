package de.misi.tvbrowser.data;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Paint;
import de.misi.tvbrowser.widgets.TVBrowserGridView;

import java.util.ArrayList;

public class Channel {

   private int mId;
   public String mName;
   private ArrayList<Broadcast> mBroadcasts;

   public Channel(int id, String name) {
      mId = id;
      mName = name;
   }

   public void loadBroadcasts(long timeInMillis) {
      mBroadcasts = DataLoader.loadBroadcastsFromDatabase(timeInMillis, mId);
   }

   public void drawChannelNames(Canvas canvas, TVBrowserGridView.DrawConfiguration drawConfiguration, int y) {
      canvas.drawRect(new Rect(0, y, TVBrowserGridView.CHANNELNAME_SIZE, y + TVBrowserGridView.TEXTHEIGHT + 2 * TVBrowserGridView.HORIZONTAL_GAP),
              drawConfiguration.headerBackground);
      Paint textPaint = drawConfiguration.headerTextColor;
      int titlestop = textPaint.breakText(mName, true, TVBrowserGridView.CHANNELNAME_SIZE - 2 * TVBrowserGridView.VERTICAL_GAP, null);
      canvas.drawText(mName, 0, titlestop, TVBrowserGridView.VERTICAL_GAP, y + TVBrowserGridView.TEXTHEIGHT + TVBrowserGridView.HORIZONTAL_GAP, textPaint);
   }

   public void drawBroadcasts(Canvas canvas, TVBrowserGridView.DrawConfiguration drawConfiguration, int y, int visibleStartX, int visibleEndX, long nowInMillis, int nowx, int offsetX, Broadcast selectBroadcast) {
      if (mBroadcasts != null) {
         for (Broadcast broadcast : mBroadcasts) {
            if (broadcast.isBetween(visibleStartX, visibleEndX))
               broadcast.draw(canvas, drawConfiguration, y, offsetX, nowInMillis, nowx, broadcast == selectBroadcast);
         }
      }
   }

   public Broadcast getBroadcastByPosition(int position) {
      if (mBroadcasts != null)
         for (Broadcast broadcast : mBroadcasts)
            if (broadcast.containsPosition(position))
               return broadcast;
      return null;
   }
}
