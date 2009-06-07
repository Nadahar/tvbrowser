package de.misi.tvbrowser.data;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import de.misi.tvbrowser.widgets.TVBrowserGridView;

import java.util.Calendar;

public class Broadcast {

   private static Calendar startDate = Calendar.getInstance();
   private static Calendar endDate = Calendar.getInstance();

   public int id;
   public String title;
   private int startValue;
   public int length;
   private long startInMillis;
   private long endInMillis;

   public Broadcast(int id, String title, Calendar date, String start, String end, boolean firstBroadcastOfDay) {
      this.id = id;
      this.title = title;
      recalcIntegerDateValues(date, start, end, firstBroadcastOfDay);
   }

   private void recalcIntegerDateValues(Calendar date, String start, String end, boolean firstBroadcastOfDay) {
      boolean startTimeAfterEndTime = start.compareTo(end) > 0;
      initCalendar(startDate, date);
      int startMinutes = getMinutes(start);
      int endMinutes = getMinutes(end);
      startDate.add(Calendar.MINUTE, startMinutes);
      initCalendar(endDate, date);
      endDate.add(Calendar.MINUTE, endMinutes);
      startValue = startMinutes;
      length = endMinutes - startMinutes;
      if (firstBroadcastOfDay) {
         if (startTimeAfterEndTime) {
            length = 24 * 60 - startValue;
            startValue = 0;
            startDate.add(Calendar.DAY_OF_MONTH, -1);
         }
      } else {
         if (startTimeAfterEndTime) {
            length = 24 * 60 - startValue;
            initCalendar(endDate, date);
            endDate.add(Calendar.MINUTE, startMinutes);
            endDate.add(Calendar.MINUTE, length);
         }
      }
      startInMillis = startDate.getTimeInMillis();
      endInMillis = endDate.getTimeInMillis();
   }

   private void initCalendar(Calendar calendar, Calendar date) {
      calendar.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
   }

   private int getMinutes(String start) {
      return Integer.parseInt(start.substring(0, 2)) * 60 + Integer.parseInt(start.substring(3, 5));
   }

   public int getId() {
      return id;
   }

   public void draw(Canvas canvas, TVBrowserGridView.DrawConfiguration drawConfiguration, int y, int offsetX, long nowInMillis, int nowx, boolean isSelected) {
      int x = startValue * TVBrowserGridView.MINUTE_WIDTH;
      Paint textPaint = drawConfiguration.textPaint;
      Rect rect = new Rect(x - offsetX, y, x + length * TVBrowserGridView.MINUTE_WIDTH - offsetX, y + TVBrowserGridView.TEXTHEIGHT + 2 * TVBrowserGridView.HORIZONTAL_GAP);
      if (endInMillis <= nowInMillis) {
         textPaint = drawConfiguration.oldTextPaint;
         canvas.drawRect(rect, drawConfiguration.oldBackgroundPaint);
      } else if (startInMillis < nowInMillis && endInMillis > nowInMillis) {
         int oldRight = rect.right;
         canvas.drawRect(rect, drawConfiguration.currentEventNewTime);
         rect.right = nowx - offsetX;
         canvas.drawRect(rect, drawConfiguration.currentEventOldTime);
         rect.right = oldRight;
      }
      canvas.drawRect(rect, isSelected ? drawConfiguration.selectedBorderPaint : drawConfiguration.borderPaint);
      int titlestop = textPaint.breakText(title, true, length * TVBrowserGridView.MINUTE_WIDTH - 2 * TVBrowserGridView.VERTICAL_GAP, null);
      canvas.drawText(title, 0, titlestop, x - offsetX + TVBrowserGridView.VERTICAL_GAP, y + TVBrowserGridView.TEXTHEIGHT + TVBrowserGridView.HORIZONTAL_GAP, textPaint);
   }

   public boolean isBetween(int visibleStartX, int visibleEndX) {
      int endX = startValue + length;
      return (startValue <= visibleStartX && endX > visibleStartX) ||
              (startValue >= visibleStartX && endX <= visibleEndX) ||
              (startValue < visibleEndX && endX >= visibleEndX);
   }

   public boolean containsPosition(int position) {
      return startValue <= position && position <= startValue + length;
   }
}
