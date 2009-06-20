package de.misi.tvbrowser.data;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import de.misi.tvbrowser.Utility;
import de.misi.tvbrowser.widgets.TVBrowserGridView;

public class Broadcast {

   public int mId;
   public String mTitle;
   private int mStartValue;
   public int mLength;
   private long mStartInMillis;
   private long mEndInMillis;
   private long mReminderId;

   public Broadcast(int id, String title, long currentDate, long nextDate, long startDate, int startTime, long endDate, int endTime) {
      mId = id;
      mTitle = title;
      recalcIntegerDateValues(currentDate, nextDate, startDate, startTime, endDate, endTime);
   }

   private void recalcIntegerDateValues(long currentDate, long nextDate, long startDate, int startTime, long endDate, int endTime) {
      mStartInMillis = Utility.getTimeInMillis(startDate, startTime);
      mEndInMillis = Utility.getTimeInMillis(endDate, endTime);
      mStartValue = startTime;
      mLength = endTime - startTime;
      if (mStartInMillis < currentDate && currentDate < mEndInMillis) {
         mStartValue = 0;
         mLength = (int) ((mEndInMillis - currentDate) / (60 * 1000));
      }
      if (mStartInMillis < nextDate && nextDate <= mEndInMillis) {
         mLength = (int) ((nextDate - mStartInMillis) / (60 * 1000));
      }
   }

   public void draw(Canvas canvas, TVBrowserGridView.DrawConfiguration drawConfiguration, int y, int offsetX, long nowInMillis, int nowx, boolean isSelected) {
      int x = mStartValue * TVBrowserGridView.MINUTE_WIDTH;
      Paint textPaint = drawConfiguration.textPaint;
      Rect rect = new Rect(x - offsetX, y, x + mLength * TVBrowserGridView.MINUTE_WIDTH - offsetX, y + TVBrowserGridView.TEXTHEIGHT + 2 * TVBrowserGridView.HORIZONTAL_GAP);
      if (mEndInMillis <= nowInMillis) {
         textPaint = drawConfiguration.oldTextPaint;
         canvas.drawRect(rect, drawConfiguration.oldBackgroundPaint);
      } else if (mStartInMillis < nowInMillis && mEndInMillis > nowInMillis) {
         int oldRight = rect.right;
         canvas.drawRect(rect, drawConfiguration.currentEventNewTime);
         rect.right = nowx - offsetX;
         canvas.drawRect(rect, drawConfiguration.currentEventOldTime);
         rect.right = oldRight;
      }
      canvas.drawRect(rect, isSelected ? drawConfiguration.selectedBorderPaint : drawConfiguration.borderPaint);
      int titlestop = textPaint.breakText(mTitle, true, mLength * TVBrowserGridView.MINUTE_WIDTH - 2 * TVBrowserGridView.VERTICAL_GAP, null);
      canvas.drawText(mTitle, 0, titlestop, x - offsetX + TVBrowserGridView.VERTICAL_GAP, y + TVBrowserGridView.TEXTHEIGHT + TVBrowserGridView.HORIZONTAL_GAP, textPaint);
   }

   public boolean isBetween(int visibleStartX, int visibleEndX) {
      int endX = mStartValue + mLength;
      return (mStartValue <= visibleStartX && endX > visibleStartX) ||
             (mStartValue >= visibleStartX && endX <= visibleEndX) ||
             (mStartValue < visibleEndX && endX >= visibleEndX);
   }

   public boolean containsPosition(int position) {
      return mStartValue <= position && position <= mStartValue + mLength;
   }

   public long getReminderId() {
      if (mReminderId == 0)
         mReminderId = DataLoader.getReminderId(mId);
      return mReminderId;
   }
}
