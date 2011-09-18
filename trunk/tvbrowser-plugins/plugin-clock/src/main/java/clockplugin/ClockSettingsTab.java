package clockplugin;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsTab;

/**
 * The settings tab for the ClockPlugin. License: GPL
 *
 * @author René Mach
 */
public class ClockSettingsTab implements SettingsTab, ActionListener {

  private JSpinner mTime, mFontSize;
  private JCheckBox mBox, mShowBorder, mTitleClock, mMove, mUsePersonaColors, mUseTransparency;
  private JLabel mLabel;

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ClockSettingsTab.class);

  public JPanel createSettingsPanel() {
    FormLayout layout = new FormLayout(
        "5dlu,pref,3dlu,pref,pref:grow,10dlu",
        "5dlu,pref,pref,pref,pref,5dlu,pref,2dlu,pref,"
            + "pref,10dlu,pref,pref");
    PanelBuilder pb = new PanelBuilder(layout);
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
    
    mUsePersonaColors = new JCheckBox(mLocalizer.msg("usePersonaColors","Use Colors of Persona"),ClockPlugin.getInstance().isUsingPersonaColors());
    mUseTransparency = new JCheckBox(mLocalizer.msg("useTransparency","Clock transparent"),ClockPlugin.getInstance().isUsingTransparentBackground());

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
    
    int y = 6;
    
    try {
      Class.forName("util.ui.persona.Persona");
      layout.insertRow(y,RowSpec.decode("default"));
      pb.add(mUsePersonaColors, cc.xyw(2, y++, 4));
    }catch(ClassNotFoundException e) {}
    
    boolean showTransparencySelection = false;
    
    GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    GraphicsConfiguration config = devices[0].getDefaultConfiguration();
    
    try {
      Class<?> awtUtilities = Class.forName("com.sun.awt.AWTUtilities");
      Method m = awtUtilities.getMethod("isTranslucencyCapable",new Class<?>[] {GraphicsConfiguration.class});
      
      showTransparencySelection = (Boolean)m.invoke(awtUtilities, new Object[] {config});      
    }catch(Exception e) {e.printStackTrace();
      try {
        Method m = config.getClass().getMethod("isTranslucencyCapable()",new Class<?>[] {GraphicsConfiguration.class});
        showTransparencySelection = (Boolean)m.invoke(config,new Object[0]);
      } catch (Exception e1) {e1.printStackTrace();}
    }
    
    if(showTransparencySelection) {
      layout.insertRow(y,RowSpec.decode("default"));
      pb.add(mUseTransparency, cc.xyw(2, y++, 4));
    }
    
    mLabel = pb.addLabel(mLocalizer.msg("desc",
        "Duration of showing the clock in seconds")
        + ":", cc.xy(2, ++y));
    pb.add(mTime, cc.xy(4, y++));
    pb.addLabel(mLocalizer.msg("fsize", "Font size of the clock") + ":", cc.xy(
        2, ++y));
    pb.add(mFontSize, cc.xy(4, y));
    y += 3;
    pb.addLabel(mLocalizer.msg("info1",
        "To move the clock on screen click it left"), cc.xyw(2, y++, 4));
    pb.addLabel(mLocalizer.msg("info2",
        "and move the mouse with pressed left button."), cc.xyw(2, y, 4));

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
    ClockPlugin.getInstance().setIsUsingPersonaColors(mUsePersonaColors.isSelected());
    ClockPlugin.getInstance().setTransparentBackground(mUseTransparency.isSelected());
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
