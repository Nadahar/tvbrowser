package clockplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * The settings tab for the ClockPlugin. License: GPL
 * 
 * @author René Mach
 */
public class ClockSettingsTab implements SettingsTab, ActionListener {

  private JSpinner mTime, mFontSize;
  private JCheckBox mBox, mShowBorder, mTitleClock, mMove;
  private JLabel mLabel;

  /** The localizer for this class. */
  public static util.ui.Localizer mLocalizer;

  /**
   * The default contructor of this class.
   * 
   */
  public ClockSettingsTab() {
    mLocalizer = util.ui.Localizer.getLocalizerFor(ClockSettingsTab.class);
  }

  public JPanel createSettingsPanel() {
    PanelBuilder pb = new PanelBuilder(new FormLayout(
        "5dlu,pref,3dlu,pref,pref:grow,10dlu",
        "5dlu,pref,pref,pref,pref,5dlu,pref,2dlu,pref,"
            + "pref,10dlu,pref,pref"));
    CellConstraints cc = new CellConstraints();

    mMove = new JCheckBox(mLocalizer.msg("moveonscreen",
        "Move clock on screen with TV-Browser"));
    mMove.setSelected(ClockPlugin.getInstance().getMoveOnScreen());

    mShowBorder = new JCheckBox(mLocalizer.msg("clockborder",
        "Clock with border"));
    mShowBorder.setSelected(ClockPlugin.getInstance().getShowBorder());

    mTitleClock = new JCheckBox(mLocalizer.msg("titlebar",
        "Clock in the title bar"));
    mTitleClock.setSelected(ClockPlugin.getInstance().getTitleBarClock());

    mBox = new JCheckBox(mLocalizer.msg("forever", "Show clock forever"));
    mBox.setSelected(ClockPlugin.getInstance().getShowForever());
    mBox.addActionListener(this);

    mTime = new JSpinner();
    mTime.setModel(new SpinnerNumberModel(ClockPlugin.getInstance()
        .getTimeValue(), 5, 30, 1));

    mFontSize = new JSpinner();
    mFontSize.setModel(new SpinnerNumberModel(ClockPlugin.getInstance()
        .getFontValue(), 10, 30, 1));
    
    pb.add(mMove, cc.xyw(2, 2, 4));
    pb.add(mShowBorder, cc.xyw(2, 3, 4));
    pb.add(mTitleClock, cc.xyw(2, 4, 4));
    pb.add(mBox, cc.xyw(2, 5, 4));
    mLabel = pb.addLabel(mLocalizer.msg("desc",
        "Duration of showing the clock in seconds")
        + ":", cc.xy(2, 7));
    pb.add(mTime, cc.xy(4, 7));
    pb.addLabel(mLocalizer.msg("fsize", "Font size of the clock") + ":", cc.xy(
        2, 9));
    pb.add(mFontSize, cc.xy(4, 9));
    pb.addLabel(mLocalizer.msg("info1",
        "To move the clock on screen click it left"), cc.xyw(2, 12, 4));
    pb.addLabel(mLocalizer.msg("info2",
        "and move the mouse with pressed left button."), cc.xyw(2, 13, 4));

    if (mBox.isSelected()) {
      mTime.setEnabled(false);
      mLabel.setEnabled(false);
    }
    
    return pb.getPanel();
  }

  public void saveSettings() {
    ClockPlugin.getInstance().storeTimeValue(
        ((Integer) mTime.getValue()).intValue());
    ClockPlugin.getInstance().setFontValue(
        ((Integer) mFontSize.getValue()).intValue());
    ClockPlugin.getInstance().setShowForever(mBox.isSelected());
    ClockPlugin.getInstance().setShowBorder(mShowBorder.isSelected());
    ClockPlugin.getInstance().setMoveOnScreen(mMove.isSelected());
    ClockPlugin.getInstance().setTitleBarClock(mTitleClock.isSelected());
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return null;
  }

  public void actionPerformed(ActionEvent e) {
    mTime.setEnabled(!mBox.isSelected());
    mLabel.setEnabled(!mBox.isSelected());
  }

}
