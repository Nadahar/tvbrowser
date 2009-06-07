package de.misi.tvbrowser;

import de.misi.tvbrowser.widgets.TVBrowserGridView;

import java.util.Calendar;

public class Utility {

   public static int getX(Calendar now) {
      return getX(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));
   }

   public static int getX(int hour, int minute) {
      return (hour * 60 + minute) * TVBrowserGridView.MINUTE_WIDTH;
   }
}
