/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.settings;

import java.io.File;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Calendar;
import java.util.Date;

import tvbrowser.core.Settings;
import tvbrowser.TVBrowser;
import util.exc.*;
import util.ui.*;

import devplugin.SettingsTab;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class TVBrowserSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(TVBrowserSettingsTab.class);
  
  private JPanel mSettingsPn;
  
  private JComboBox lfComboBox;
  private JTextField skinTextField;
  private JCheckBox skinCheckBox;

  private JCheckBox mTimeCheck, updateCheck, settingsCheck;
  private JRadioButton textOnlyRadio, picOnlyRadio, textAndPicRadio;
  private TimePanel mEarlyTimePn, mMorningTimePn, mMiddayTimePn, mEveningTimePn;
  private JLabel mEarlyLb, mMorningLb, mMiddayLb, mEveningLb;

  class LookAndFeelObj {
    private UIManager.LookAndFeelInfo info;
    public LookAndFeelObj(UIManager.LookAndFeelInfo info) {
      this.info=info;
    }
    public String toString() {
      return info.getName();
    }
    public String getLFClassName() {
      return info.getClassName();
    }
  }


  public TVBrowserSettingsTab()  {
  }

  

  private LookAndFeelObj[] getLookAndFeelObjs() {
    UIManager.LookAndFeelInfo[] info=UIManager.getInstalledLookAndFeels();
    LookAndFeelObj[] result=new LookAndFeelObj[info.length];
    for (int i=0;i<info.length;i++) {
      result[i]=new LookAndFeelObj(info[i]);
    }

    return result;
  }
  
  
 
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    String msg;
    JPanel p1, p2;
    
    mSettingsPn = new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
    JPanel main = new JPanel(new TabLayout(1));
    mSettingsPn.add(main, BorderLayout.NORTH);

    // Look and feel
    JPanel lfPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
    main.add(lfPanel);
    
    msg = mLocalizer.msg("lookAndFeel", "Aussehen");
    lfPanel.add(new JLabel(msg));

    LookAndFeelObj[] obj=getLookAndFeelObjs();
    lfComboBox=new JComboBox(obj);
    String lf=Settings.getLookAndFeel();
    for (int i=0;i<obj.length;i++) {
      if (obj[i].getLFClassName().equals(lf)) {
        lfComboBox.setSelectedItem(obj[i]);
      }
    }
    lfPanel.add(lfComboBox);

    // Background
    JPanel skinPanel = new JPanel(new BorderLayout(5, 0));
    main.add(skinPanel);
    msg = mLocalizer.msg("background", "Background");
    skinCheckBox = new JCheckBox(msg);
    skinTextField = new JTextField(Settings.getApplicationSkin());
    msg = mLocalizer.msg("change", "Change");
    final JButton skinChooseBtn = new JButton(msg);
    skinPanel.add(skinCheckBox,BorderLayout.WEST);
    skinPanel.add(skinTextField,BorderLayout.CENTER);
    skinPanel.add(skinChooseBtn,BorderLayout.EAST);
    skinCheckBox.setSelected(Settings.useApplicationSkin());
    skinChooseBtn.setEnabled(Settings.useApplicationSkin());
    skinTextField.setEnabled(Settings.useApplicationSkin());

    skinCheckBox.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        skinChooseBtn.setEnabled(skinCheckBox.isSelected());
        skinTextField.setEnabled(skinCheckBox.isSelected());
      }
    }
    );

    skinChooseBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JFileChooser fileChooser=new JFileChooser();
        fileChooser.showOpenDialog(mSettingsPn);
        File f=fileChooser.getSelectedFile();
        if (f!=null) {
          skinTextField.setText(f.getAbsolutePath());
        }
      }
    });

    // buttons panel
    JPanel toolBarPanel=new JPanel();
    toolBarPanel.setLayout(new BoxLayout(toolBarPanel,BoxLayout.Y_AXIS));
    JPanel buttonPanel=new JPanel(new GridLayout(1,0));
    main.add(toolBarPanel);
    msg = mLocalizer.msg("buttons", "Buttons");
    toolBarPanel.setBorder(BorderFactory.createTitledBorder(msg));

    JPanel visibleBtnsPanel=new JPanel(new BorderLayout());
    JPanel panel3=new JPanel(new BorderLayout());
    msg = mLocalizer.msg("showButtons", "Show buttons");
    panel3.setBorder(BorderFactory.createTitledBorder(msg));
    panel3.setLayout(new BoxLayout(panel3,BoxLayout.Y_AXIS));

    mTimeCheck = new JCheckBox(mLocalizer.msg("buttons.time", "Time buttons"));
    updateCheck = new JCheckBox(mLocalizer.msg("buttons.update", "Update"));
    settingsCheck = new JCheckBox(mLocalizer.msg("buttons.settings", "Settings"));
    panel3.add(mTimeCheck);
    panel3.add(updateCheck);
    panel3.add(settingsCheck);

    mTimeCheck.setSelected(Settings.isTimeBtnVisible());
    updateCheck.setSelected(Settings.isUpdateBtnVisible());
    settingsCheck.setSelected(Settings.isPreferencesBtnVisible());
    
    visibleBtnsPanel.add(panel3,BorderLayout.NORTH);

    JPanel labelBtnsPanel=new JPanel(new BorderLayout());
    JPanel panel4=new JPanel(new BorderLayout());
    msg = mLocalizer.msg("label", "Label");
    panel4.setBorder(BorderFactory.createTitledBorder(msg));
    panel4.setLayout(new BoxLayout(panel4,BoxLayout.Y_AXIS));
    textOnlyRadio = new JRadioButton(mLocalizer.msg("textOnly", "Text only"));
    picOnlyRadio = new JRadioButton(mLocalizer.msg("imagesOnly", "Images only"));
    textAndPicRadio = new JRadioButton(mLocalizer.msg("textAndImages", "Text and images"));
    ButtonGroup labelBtnsGroup=new ButtonGroup();
    labelBtnsGroup.add(textOnlyRadio);
    labelBtnsGroup.add(picOnlyRadio);
    labelBtnsGroup.add(textAndPicRadio);

    if (Settings.getButtonSettings()==Settings.TEXT_ONLY) {
      textOnlyRadio.setSelected(true);
    }else if (Settings.getButtonSettings()==Settings.ICON_ONLY) {
      picOnlyRadio.setSelected(true);
    }else {
      textAndPicRadio.setSelected(true);
    }

    panel4.add(textOnlyRadio);
    panel4.add(picOnlyRadio);
    panel4.add(textAndPicRadio);
    labelBtnsPanel.add(panel4,BorderLayout.NORTH);

    buttonPanel.add(visibleBtnsPanel);
    buttonPanel.add(labelBtnsPanel);

    // time buttons

    JPanel timeButtonsPn=new JPanel(new GridLayout(2,4));
    timeButtonsPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("buttons.time", "Time buttons")));
    
    String timePattern = mLocalizer.msg("timePattern", "hh:mm a");
    
    mEarlyTimePn = new TimePanel(Settings.getEarlyTime());
    mMorningTimePn=new TimePanel(Settings.getMorningTime());
    mMiddayTimePn=new TimePanel(Settings.getMiddayTime());
    mEveningTimePn=new TimePanel(Settings.getEveningTime());
    
    mEarlyLb=new JLabel(TVBrowser.mLocalizer.msg("button.early","Early")+":");
    timeButtonsPn.add(mEarlyLb);
    timeButtonsPn.add(mEarlyTimePn);
    
    mMorningLb=new JLabel(TVBrowser.mLocalizer.msg("button.morning","Morning")+":");
    timeButtonsPn.add(mMorningLb);
    timeButtonsPn.add(mMorningTimePn);  
    
    mMiddayLb=new JLabel(TVBrowser.mLocalizer.msg("button.midday","Midday")+":");
    timeButtonsPn.add(mMiddayLb);
    timeButtonsPn.add(mMiddayTimePn);
    
    mEveningLb=new JLabel(TVBrowser.mLocalizer.msg("button.evening","Evening")+":");
    timeButtonsPn.add(mEveningLb);
    timeButtonsPn.add(mEveningTimePn);

    toolBarPanel.add(buttonPanel);

    toolBarPanel.add(timeButtonsPn);


    mTimeCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        enableTimeButtons(mTimeCheck.isSelected());
      }
    });
    
    enableTimeButtons(mTimeCheck.isSelected());
    
    return mSettingsPn;
  }

  private void enableTimeButtons(boolean val) {
    boolean b=mTimeCheck.isSelected();
    mEarlyTimePn.setEnabled(b);
    mMorningTimePn.setEnabled(b);
    mMiddayTimePn.setEnabled(b);
    mEveningTimePn.setEnabled(b);
    mEarlyLb.setEnabled(b);
    mMorningLb.setEnabled(b);
    mMiddayLb.setEnabled(b);
    mEveningLb.setEnabled(b);
  }
  
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    LookAndFeelObj obj=(LookAndFeelObj)lfComboBox.getSelectedItem();
    try {
      UIManager.setLookAndFeel(obj.getLFClassName());
      Settings.setLookAndFeel(obj.getLFClassName());
    }
    catch (Exception exc) {
      String msg = mLocalizer.msg("error.1", "Unable to set look and feel.", exc);
      ErrorHandler.handle(msg, exc);
    }

    Settings.setUseApplicationSkin(this.skinCheckBox.isSelected());
    Settings.setApplicationSkin(skinTextField.getText());

    Settings.setTimeBtnVisible(mTimeCheck.isSelected());
    Settings.setUpdateBtnVisible(updateCheck.isSelected());
    Settings.setPreferencesBtnVisible(settingsCheck.isSelected());
    
    if (textOnlyRadio.isSelected()) {
      Settings.setButtonSettings(Settings.TEXT_ONLY);
    } else if (picOnlyRadio.isSelected()) {
      Settings.setButtonSettings(Settings.ICON_ONLY);
    } else {
      Settings.setButtonSettings(Settings.TEXT_AND_ICON);
    }
    
    Settings.setEarlyTime(mEarlyTimePn.getTime());
    Settings.setMorningTime(mMorningTimePn.getTime());
    Settings.setMiddayTime(mMiddayTimePn.getTime());
    Settings.setEveningTime(mEveningTimePn.getTime());
       
  }

  
  
  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    
    return new ImageIcon("imgs/TVBrowser16.png");
    
    
  }
  
  
  
  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("tvBrowser", "TV-Browser");
  }


class TimePanel extends JPanel {
  
  
  private JSpinner mTimeSp;
  public TimePanel(int minutes) {
    setLayout(new BorderLayout());
    
//  String timePattern = mLocalizer.msg("timePattern", "hh:mm a");
    String timePattern="hh:mm a";
    
    
    mTimeSp = new JSpinner(new SpinnerDateModel());
    mTimeSp.setEditor(new JSpinner.DateEditor(mTimeSp, timePattern));
    mTimeSp.setBorder(null);
   
    add(mTimeSp,BorderLayout.WEST);
    setTime(minutes);
  }
  
  public void setTime(int minutes) {
    Calendar cal=Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
    cal.set(Calendar.MINUTE, minutes % 60);
    mTimeSp.setValue(cal.getTime());   
  }
  
  public int getTime() {
    
    Date time= (Date) mTimeSp.getValue();
    Calendar cal=Calendar.getInstance();
    cal.setTime(time);
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }
  
  public void setEnabled(boolean val) {
    mTimeSp.setEnabled(val);
  }
}
  
}



