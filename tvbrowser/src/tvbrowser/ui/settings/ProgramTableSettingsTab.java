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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import util.ui.TabLayout;
import java.util.Calendar;
import java.util.Date;

import tvbrowser.core.Settings;
import tvbrowser.ui.SkinPanel;

import devplugin.SettingsTab;


/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class ProgramTableSettingsTab implements SettingsTab, ActionListener {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ProgramTableSettingsTab.class);
  
  private JPanel mSettingsPn;
  
  private JComboBox mProgramArrangementCB;
  private JRadioButton mBlankRB, mWallpaperRB, mColumnsRB;

  private JLabel mBackgroundLb;
  private JButton mBackgroundBt;
  private JTextField mBackgroundTF;
  
  private JSpinner mStartOfDayTimeSp, mEndOfDayTimeSp;
  

  
  
  /**
   * Creates a new instance of ProgramTableSettingsTab.
   */
  public ProgramTableSettingsTab() {
  }
  
  
  
  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource();
    if (source == mBlankRB) {
      if (mBlankRB.isSelected()) {
        mBackgroundLb.setEnabled(false);
        mBackgroundBt.setEnabled(false);
        mBackgroundTF.setEnabled(false);
      }
    } else if (source == mWallpaperRB || source == mColumnsRB) {
      if (mWallpaperRB.isSelected() || mColumnsRB.isSelected()) {
        mBackgroundLb.setEnabled(true);
        mBackgroundBt.setEnabled(true);
        mBackgroundTF.setEnabled(true);
      }
    }
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

    // program table arrangement
    p1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
    main.add(p1);
    
    p1.add(new JLabel(mLocalizer.msg("programArrangement", "Program arrangement")));
    String[] arrangementArr = {
      mLocalizer.msg("compact", "Compact"),
      mLocalizer.msg("timeSynchronous", "Time synchronous")
    };
    mProgramArrangementCB = new JComboBox(arrangementArr);
    if (Settings.getTableLayout() == Settings.TABLE_LAYOUT_COMPACT) {
      mProgramArrangementCB.setSelectedIndex(0);
    } else {
      mProgramArrangementCB.setSelectedIndex(1);
    }
    p1.add(mProgramArrangementCB);
    
    // program table background panel
    p1 = new JPanel(new TabLayout(1));
    msg = mLocalizer.msg("tableBackground", "Table background");
    p1.setBorder(BorderFactory.createTitledBorder(msg));
    main.add(p1);
    
    // program table background style
    p2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
    p1.add(p2);
    
    p2.add(new JLabel(mLocalizer.msg("alignment", "Alignment")));

    ButtonGroup tablePanelBtnGroup = new ButtonGroup();

    mBlankRB = new JRadioButton(mLocalizer.msg("blank", "Blank"));
    mBlankRB.setSelected(Settings.getTableBGMode() == SkinPanel.NONE);
    mBlankRB.addActionListener(this);
    tablePanelBtnGroup.add(mBlankRB);
    p2.add(mBlankRB);

    mWallpaperRB = new JRadioButton(mLocalizer.msg("wallpaper", "Wallpaper"));
    mWallpaperRB.setSelected(Settings.getTableBGMode() == SkinPanel.WALLPAPER);
    mWallpaperRB.addActionListener(this);
    tablePanelBtnGroup.add(mWallpaperRB);
    p2.add(mWallpaperRB);

    mColumnsRB = new JRadioButton(mLocalizer.msg("columns", "Columns"));
    mColumnsRB.setSelected(Settings.getTableBGMode() == SkinPanel.COLUMNS);
    mColumnsRB.addActionListener(this);
    tablePanelBtnGroup.add(mColumnsRB);
    p2.add(mColumnsRB);

    // program table background image
    p2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
    p1.add(p2);

    mBackgroundLb = new JLabel(mLocalizer.msg("background", "Background"));
    p2.add(mBackgroundLb);
    
    mBackgroundTF = new JTextField(Settings.getTableSkin(), 15);
    p2.add(mBackgroundTF);

    mBackgroundBt = new JButton(mLocalizer.msg("change", "Change"));
    mBackgroundBt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JFileChooser fileChooser=new JFileChooser();
        fileChooser.showOpenDialog(mSettingsPn);
        File selection = fileChooser.getSelectedFile();
        if (selection != null) {
          mBackgroundTF.setText(selection.getAbsolutePath());
        }
      }
    });
    p2.add(mBackgroundBt);
    
    
    // day range
    
    JPanel pn=new JPanel(new GridLayout(2,2,0,3));
    pn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("range","Range"))); 
  
    pn.add(new JLabel(mLocalizer.msg("startOfDay","Start of day")));    
    JPanel panel1=new JPanel(new BorderLayout(7,0));
    
    String timePattern = mLocalizer.msg("timePattern", "hh:mm a");
    
    mStartOfDayTimeSp = new JSpinner(new SpinnerDateModel());
    mStartOfDayTimeSp.setEditor(new JSpinner.DateEditor(mStartOfDayTimeSp, timePattern));
    mStartOfDayTimeSp.setBorder(null);
    
    
    panel1.add(mStartOfDayTimeSp,BorderLayout.WEST);
    panel1.add(new JLabel("("+mLocalizer.msg("today","today")+")"));
    pn.add(panel1);
    
    pn.add(new JLabel(mLocalizer.msg("endOfDay","End of day")));
    panel1=new JPanel(new BorderLayout(7,0));
    
    mEndOfDayTimeSp = new JSpinner(new SpinnerDateModel());
    mEndOfDayTimeSp.setEditor(new JSpinner.DateEditor(mEndOfDayTimeSp, timePattern));
    mEndOfDayTimeSp.setBorder(null);
    
    panel1.add(mEndOfDayTimeSp,BorderLayout.WEST);
    panel1.add(new JLabel("("+mLocalizer.msg("nextDay","next day")+")"));
    pn.add(panel1);
    
    int minutes;
    Calendar cal = Calendar.getInstance();
    minutes=Settings.getProgramTableStartOfDay();    
    cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
    cal.set(Calendar.MINUTE, minutes % 60);
    mStartOfDayTimeSp.setValue(cal.getTime());
    
    minutes=Settings.getProgramTableEndOfDay();    
    cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
    cal.set(Calendar.MINUTE, minutes % 60);
    mEndOfDayTimeSp.setValue(cal.getTime());
    
    main.add(pn);

    return mSettingsPn;
  }

  
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    if (mProgramArrangementCB.getSelectedIndex() == 0) {
      Settings.setTableLayout(Settings.TABLE_LAYOUT_COMPACT);
    } else {
      Settings.setTableLayout(Settings.TABLE_LAYOUT_TIME_SYNCHRONOUS);
    }
    
    Settings.setTableSkin(mBackgroundTF.getText());
    if (mWallpaperRB.isSelected()) {
      Settings.setTableBGMode(SkinPanel.WALLPAPER);
    } else if (mColumnsRB.isSelected()) {
      Settings.setTableBGMode(SkinPanel.COLUMNS);
    } else {
      Settings.setTableBGMode(SkinPanel.NONE);
    }
    
    
    Calendar cal=Calendar.getInstance();
    Date startTime = (Date) mStartOfDayTimeSp.getValue();
    cal.setTime(startTime);
    int minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    Settings.setProgramTableStartOfDay(minutes);
    
    Date endTime = (Date) mEndOfDayTimeSp.getValue();
    cal.setTime(endTime);
    minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    Settings.setProgramTableEndOfDay(minutes);
    
    
  }

  
  
  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return null;
  }
  
  
  
  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("programTable", "Program table");
  }

}

