package tvbrowser.ui.finder.calendar;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToggleButton;

import tvbrowser.core.DateListener;
import tvbrowser.core.TvDataBase;
import tvbrowser.ui.finder.AbstractDateSelector;
import tvbrowser.ui.finder.DateSelector;
import devplugin.Date;
import devplugin.ProgressMonitor;

public class CalendarPanel extends AbstractDateSelector implements
    DateSelector, ProgressMonitor {

  private static final int COLUMNS = 7;
  private static final int ROWS = 6;

  private JComponent[][] components = new JComponent[COLUMNS][ROWS];

  public DateListener mDateChangedListener;

  public CalendarPanel() {
    rebuildControls();
    addMouseListener(this);
  }

  public JComponent getComponent() {
    return this;
  }

  protected void rebuildControls() {
    removeAll();
    setLayout(new GridLayout(ROWS, COLUMNS));
    Date date = getFirstDate();
    while (date.getDayOfWeek() != Calendar.MONDAY) {
      date = date.addDays(-1);
    }
    Date weekday = date;
    for (int i = 0; i < COLUMNS; i++) {
      components[i][0] = new JLabel(new SimpleDateFormat("E").format(weekday
          .getCalendar().getTime()));
      add(components[i][0]);
      weekday = weekday.addDays(1);
    }
    for (int y = 1; y < ROWS; y++) {
      for (int x = 0; x < COLUMNS; x++) {
        ExtendedButton currentButton = new ExtendedButton();
        components[x][y] = currentButton;
        currentButton.setSelectedDate(date);
        currentButton.setEnabled(isValidDate(date));
        add(components[x][y]);
        date = date.addDays(1);
        currentButton.addActionListener(currentButton);
        currentButton.addMouseListener(this);
      }
    }
  }

  public DateListener getDateChangedListener() {
    return mDateChangedListener;
  }

  public void setDateListener(final DateListener dateChangedListener) {
    mDateChangedListener = dateChangedListener;
  }

  public void markDate(final Date d) {
    markDate(d, null);
  }

  public void markDate(final Date d, final Runnable callback) {
    setCurrentDate(d);
    if (mDateChangedListener == null) {
      return;
    }
    for (int y = 1; y < ROWS; y++) {
      for (int x = 0; x < COLUMNS; x++) {
        if (components[x][y] instanceof ExtendedButton) {
          final ExtendedButton button = (ExtendedButton) components[x][y];
          button.setSelected(button.getSelectedDate().equals(d));
        }
      }
    }

    Thread thread = new Thread("Finder") {
      public void run() {
        mDateChangedListener.dateChanged(d, CalendarPanel.this, callback);
      }
    };
    thread.start();
  }

  public void updateItems() {
    for (int y = 1; y < ROWS; y++) {
      for (int x = 0; x < COLUMNS; x++) {
        final ExtendedButton currentButton = (ExtendedButton) components[x][y];
        currentButton.setEnabled(TvDataBase.getInstance().dataAvailable(
            currentButton.getSelectedDate()));
      }
    }
  }

  public void setMaximum(int maximum) {
  }

  public void setValue(int value) {
  }

  public void setMessage(String msg) {
  }

  private class ExtendedButton extends JToggleButton implements ActionListener {

    private Date mSelectedDate = null;

    private ExtendedButton() {
      // remove the left and right border of the standard button layout
      setMargin(new Insets(1, -10, 1, -10));

      Font font = getFont();
      font = font.deriveFont(font.getSize() / 2);
      setFont(font);
      Dimension size = getMinimumSize();
      setMinimumSize(new Dimension(((int) size.getWidth() / 2), ((int) size
          .getHeight() / 2)));
    }

    public Date getSelectedDate() {
      return mSelectedDate;
    }

    public void setSelectedDate(final Date selectedDate) {
      this.mSelectedDate = selectedDate;
      String value = Integer.toString(selectedDate.getDayOfMonth());
      if (value.length() == 1) {
        value = "0" + value;
      }
      setText(value);
    }

    public void actionPerformed(final ActionEvent e) {
      markDate(mSelectedDate);
    }
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

}
