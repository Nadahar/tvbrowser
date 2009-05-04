package tvbrowser.ui.finder;

import tvbrowser.core.DateListener;

import javax.swing.*;

import devplugin.Date;

public interface DateSelector {
  void setDateListener(DateListener dateChangedListener);
  JComponent getComponent();

  void updateContent();

  void markDate(Date d);

  void markPreviousDate();

  void markNextDate();

  void markNextWeek();

  void markPreviousWeek();

  void markDate(Date d, Runnable callback);

  void updateItems();

  Date getSelectedDate();
}
