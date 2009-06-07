package de.misi.tvbrowser.data;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Paint;
import de.misi.tvbrowser.widgets.TVBrowserGridView;

import java.util.ArrayList;
import java.util.Calendar;

public class Channel {

   public int id;
   public String name;
   private ArrayList<Broadcast> broadcasts;

   public Channel() {
   }

   public Channel(int id, String name) {
      this.id = id;
      this.name = name;
   }

   public void loadBroadcasts(Calendar date) {
      broadcasts = DataLoader.loadBroadcastsFromDatabase(date, id);
   }

   public void drawChannelNames(Canvas canvas, TVBrowserGridView.DrawConfiguration drawConfiguration, int y) {
      canvas.drawRect(new Rect(0, y, TVBrowserGridView.CHANNELNAME_SIZE, y + TVBrowserGridView.TEXTHEIGHT + 2 * TVBrowserGridView.HORIZONTAL_GAP),
              drawConfiguration.headerBackground);
      Paint textPaint = drawConfiguration.headerTextColor;
      int titlestop = textPaint.breakText(name, true, TVBrowserGridView.CHANNELNAME_SIZE - 2 * TVBrowserGridView.VERTICAL_GAP, null);
      canvas.drawText(name, 0, titlestop, TVBrowserGridView.VERTICAL_GAP, y + TVBrowserGridView.TEXTHEIGHT + TVBrowserGridView.HORIZONTAL_GAP, textPaint);
   }

   public void drawBroadcasts(Canvas canvas, TVBrowserGridView.DrawConfiguration drawConfiguration, int y, int visibleStartX, int visibleEndX, long nowInMillis, int nowx, int offsetX, Broadcast selectBroadcast) {
      if (broadcasts != null) {
         for (Broadcast broadcast : broadcasts) {
            if (broadcast.isBetween(visibleStartX, visibleEndX))
               broadcast.draw(canvas, drawConfiguration, y, offsetX, nowInMillis, nowx, broadcast == selectBroadcast);
         }
      }
   }

   public Broadcast getBroadcastByPosition(int position) {
      if (broadcasts != null)
         for (Broadcast broadcast : broadcasts)
            if (broadcast.containsPosition(position))
               return broadcast;
      return null;
   }
}
