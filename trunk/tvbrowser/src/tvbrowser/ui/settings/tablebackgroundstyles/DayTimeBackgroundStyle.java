package tvbrowser.ui.settings.tablebackgroundstyles;

import util.ui.TabLayout;

import javax.swing.*;


import tvbrowser.core.Settings;
import tvbrowser.ui.settings.ProgramTableSettingsTab;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 30.04.2005
 * Time: 17:48:23
 */
public class DayTimeBackgroundStyle implements TableBackgroundStyle {

  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(DayTimeBackgroundStyle.class);

  private JTextField mTimeOfDayEdgeTF, mTimeOfDayEarlyTF, mTimeOfDayMiddayTF,
                     mTimeOfDayAfternoonTF, mTimeOfDayEveningTF;

  private JPanel mContent;

  public DayTimeBackgroundStyle() {

  }

  public boolean hasContent() {
    return true;
  }

  public JPanel createSettingsContent() {
    mContent = new JPanel(new TabLayout(3));

    mContent.add(new JLabel(mLocalizer.msg("timeOfDay.edge", "Edge")));
    mTimeOfDayEdgeTF = new JTextField(Settings.propTimeOfDayBackgroundEdge.getString(), 25);
    mContent.add(mTimeOfDayEdgeTF);
    mContent.add(ProgramTableSettingsTab.createBrowseButton(mContent, mTimeOfDayEdgeTF));

    mContent.add(new JLabel(mLocalizer.msg("timeOfDay.early", "Early")));
    mTimeOfDayEarlyTF = new JTextField(Settings.propTimeOfDayBackgroundEarly.getString(), 25);
    mContent.add(mTimeOfDayEarlyTF);
    mContent.add(ProgramTableSettingsTab.createBrowseButton(mContent, mTimeOfDayEarlyTF));

    mContent.add(new JLabel(mLocalizer.msg("timeOfDay.midday", "Midday")));
    mTimeOfDayMiddayTF = new JTextField(Settings.propTimeOfDayBackgroundMidday.getString(), 25);
    mContent.add(mTimeOfDayMiddayTF);
    mContent.add(ProgramTableSettingsTab.createBrowseButton(mContent, mTimeOfDayMiddayTF));

    mContent.add(new JLabel(mLocalizer.msg("timeOfDay.afternoon", "Afternoon")));
    mTimeOfDayAfternoonTF = new JTextField(Settings.propTimeOfDayBackgroundAfternoon.getString(), 25);
    mContent.add(mTimeOfDayAfternoonTF);
    mContent.add(ProgramTableSettingsTab.createBrowseButton(mContent, mTimeOfDayAfternoonTF));

    mContent.add(new JLabel(mLocalizer.msg("timeOfDay.evening", "Evening")));
    mTimeOfDayEveningTF = new JTextField(Settings.propTimeOfDayBackgroundEvening.getString(), 25);
    mContent.add(mTimeOfDayEveningTF);
    mContent.add(ProgramTableSettingsTab.createBrowseButton(mContent, mTimeOfDayEveningTF));

    return mContent;
  }

  public void storeSettings() {
    if (mContent == null) {
      return;
    }
    Settings.propTimeOfDayBackgroundEdge.setString(mTimeOfDayEdgeTF.getText());
    Settings.propTimeOfDayBackgroundEarly.setString(mTimeOfDayEarlyTF.getText());
    Settings.propTimeOfDayBackgroundMidday.setString(mTimeOfDayMiddayTF.getText());
    Settings.propTimeOfDayBackgroundAfternoon.setString(mTimeOfDayAfternoonTF.getText());
    Settings.propTimeOfDayBackgroundEvening.setString(mTimeOfDayEveningTF.getText());
  }

  public String getName() {
    return mLocalizer.msg("style","Day time");
  }


  public String toString() {
    return getName();
  }

  public String getSettingsString() {
    return "timeOfDay";
  }

}
