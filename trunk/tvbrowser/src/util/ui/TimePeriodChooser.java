package util.ui;

import java.awt.FlowLayout;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

import tvbrowser.core.Settings;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TimePeriodChooser extends JPanel {

  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TimePeriodChooser.class);

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
    FlowLayout layout = new FlowLayout(FlowLayout.LEFT, 0, 0);
    
    JPanel content = new JPanel(new FormLayout("pref, 3dlu, pref, 3dlu, pref, 3dlu, pref", "pref"));
    
    mTimeFromSp = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(mTimeFromSp, Settings.getTimePattern());
    mTimeFromSp.setEditor(dateEditor);
    CaretPositionCorrector.createCorrector(dateEditor.getTextField(), new char[] {':'}, -1);
    
    mTimeToSp = new JSpinner(new SpinnerDateModel());
    dateEditor = new JSpinner.DateEditor(mTimeToSp, Settings.getTimePattern());
    mTimeToSp.setEditor(dateEditor);
    CaretPositionCorrector.createCorrector(dateEditor.getTextField(), new char[] {':'}, -1);
    
    CellConstraints cc = new CellConstraints();

    content.add(mLabel1 = new JLabel(mLocalizer.msg("between", "between")), cc.xy(1, 1));
    content.add(mTimeFromSp, cc.xy(3, 1));
    content.add(mLabel2 = new JLabel(mLocalizer.msg("and", "and")), cc.xy(5, 1));
    content.add(mTimeToSp, cc.xy(7, 1));

    if (from >= 0) {
      setFromTime(from);
    }
    if (to >= 0) {
      setToTime(to);
    }

    if (alignment == ALIGN_LEFT) {
      layout.setAlignment(FlowLayout.LEFT);
    } else if (alignment == ALIGN_RIGHT) {
      layout.setAlignment(FlowLayout.RIGHT);
    } else {
      layout.setAlignment(FlowLayout.CENTER);
    }
    setLayout(layout);
    add(content);
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
    cal.set(Calendar.HOUR_OF_DAY, time / 60);
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
