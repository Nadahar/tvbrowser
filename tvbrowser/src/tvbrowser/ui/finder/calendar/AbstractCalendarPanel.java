package tvbrowser.ui.finder.calendar;

import java.awt.event.MouseEvent;

import javax.swing.JComponent;

import tvbrowser.core.DateListener;
import tvbrowser.ui.finder.AbstractDateSelector;
import tvbrowser.ui.finder.DateSelector;
import devplugin.Date;
import devplugin.ProgressMonitor;

public abstract class AbstractCalendarPanel extends AbstractDateSelector
    implements DateSelector, ProgressMonitor {

  protected DateListener mDateChangedListener;

  public JComponent getComponent() {
    return this;
  }
  public DateListener getDateChangedListener() {
    return mDateChangedListener;
  }

  public void setDateListener(final DateListener dateChangedListener) {
    mDateChangedListener = dateChangedListener;
  }

  public void markDate(final Date date) {
    markDate(date, null);
  }

  public void setMaximum(int maximum) {
  }

  public void setValue(int value) {
  }

  public void setMessage(String msg) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

}
