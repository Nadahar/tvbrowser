package util.ui;

import javax.swing.*;
import java.util.Date;
import java.util.Calendar;
import java.awt.*;

public class TimePeriodChooser extends JPanel {

   public static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TimePeriodChooser.class);

  public static final int ALIGN_LEFT = 0;
  public static final int ALIGN_RIGHT = 1;
  public static final int ALGIN_CENTER = 2;


  private JSpinner mTimeFromSp;
  private JSpinner mTimeToSp;
  private JLabel mLabel1, mLabel2;

  public TimePeriodChooser(int alignment) {
    this(-1, -1, alignment);
  }

  public TimePeriodChooser(int from, int to, int alignment) {
    super(new BorderLayout());

    String timePattern = mLocalizer.msg("timePattern","HH:mm");

    JPanel content = new JPanel();

    mTimeFromSp = new JSpinner(new SpinnerDateModel());
    mTimeFromSp.setEditor(new JSpinner.DateEditor(mTimeFromSp, timePattern));

    mTimeToSp = new JSpinner(new SpinnerDateModel());
    mTimeToSp.setEditor(new JSpinner.DateEditor(mTimeToSp, timePattern));

    content.add(mLabel1 = new JLabel(mLocalizer.msg("between","between")));
    content.add(mTimeFromSp);
    content.add(mLabel2 = new JLabel(mLocalizer.msg("and","and")));
    content.add(mTimeToSp);


    if (from >=0) {
      setFromTime(from);
    }
    if (to >=0) {
      setToTime(to);
    }

    if (alignment == ALIGN_LEFT) {
      add(content, BorderLayout.WEST);
    }
    else if (alignment == ALIGN_RIGHT) {
      add(content, BorderLayout.EAST);
    }
    else {
      add(content, BorderLayout.CENTER);
    }

  }


  public void setEnabled(boolean enabled) {
    mTimeFromSp.setEnabled(enabled);
    mTimeToSp.setEnabled(enabled);
    mLabel1.setEnabled(enabled);
    mLabel2.setEnabled(enabled);
  }

  private int getTime(JSpinner spinner) {
    Date fromTime = (Date) spinner.getValue();
    Calendar cal = Calendar.getInstance();
    cal.setTime(fromTime);
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }

  private void setTime(JSpinner spinner, int time) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, time/60);
    cal.set(Calendar.MINUTE, time % 60);
    spinner.setValue(cal.getTime());
  }

  public void setFromTime(int minutes) {
    setTime(mTimeFromSp, minutes);
  }

  public void setToTime(int minutes) {
    setTime(mTimeToSp, minutes);
  }


  public int getFromTime() {
    return getTime(mTimeFromSp);
  }

  public int getToTime() {
    return getTime(mTimeToSp);
  }

}
