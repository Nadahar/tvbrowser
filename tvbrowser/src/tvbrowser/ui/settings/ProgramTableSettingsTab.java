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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Calendar;
import java.util.Date;

import javax.swing.*;

import tvbrowser.core.Settings;
import util.ui.TabLayout;
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

  private JButton mDefaultBtn;
  private JTextField mBackgroundEdgeTF, mBackgroundEarlyTF, mBackgroundMiddayTF,
    mBackgroundAfternoonTF, mBackgroundEveningTF;
    
  private JSpinner mStartOfDayTimeSp, mEndOfDayTimeSp;
  private JSlider mColWidthSl;
  

  
  
  /**
   * Creates a new instance of ProgramTableSettingsTab.
   */
  public ProgramTableSettingsTab() {
  }
  
  
  
  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource();
    if (source == mDefaultBtn) {
      mColWidthSl.setValue(200);
    }
  }
  
  
  
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    String msg;
    JPanel p1;
    
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
    p1 = new JPanel(new TabLayout(3));
    msg = mLocalizer.msg("tableBackground", "Table background");
    p1.setBorder(BorderFactory.createTitledBorder(msg));
    main.add(p1);

    p1.add(new JLabel(mLocalizer.msg("edge", "Edge")));
    mBackgroundEdgeTF = new JTextField(Settings.getTableBackgroundEdge(), 15);
    p1.add(mBackgroundEdgeTF);
    p1.add(createBrowseButton(mBackgroundEdgeTF));

    p1.add(new JLabel(mLocalizer.msg("early", "Early")));
    mBackgroundEarlyTF = new JTextField(Settings.getTableBackgroundEarly(), 15);
    p1.add(mBackgroundEarlyTF);
    p1.add(createBrowseButton(mBackgroundEarlyTF));

    p1.add(new JLabel(mLocalizer.msg("midday", "Midday")));
    mBackgroundMiddayTF = new JTextField(Settings.getTableBackgroundMidday(), 15);
    p1.add(mBackgroundMiddayTF);
    p1.add(createBrowseButton(mBackgroundMiddayTF));

    p1.add(new JLabel(mLocalizer.msg("afternoon", "Afternoon")));
    mBackgroundAfternoonTF = new JTextField(Settings.getTableBackgroundAfternoon(), 15);
    p1.add(mBackgroundAfternoonTF);
    p1.add(createBrowseButton(mBackgroundAfternoonTF));

    p1.add(new JLabel(mLocalizer.msg("evening", "Evening")));
    mBackgroundEveningTF = new JTextField(Settings.getTableBackgroundEvening(), 15);
    p1.add(mBackgroundEveningTF);
    p1.add(createBrowseButton(mBackgroundEveningTF));
    
    // column width
    JPanel colWidthPn=new JPanel(new BorderLayout());
    
    colWidthPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("columnwidth","column width")));
    mColWidthSl=new JSlider(SwingConstants.HORIZONTAL,0,300,Settings.getColumnWidth());
    colWidthPn.add(mColWidthSl,BorderLayout.WEST);
    mColWidthSl.setPreferredSize(new Dimension(300,15));
    
    mDefaultBtn=new JButton(mLocalizer.msg("default","default"));
    mDefaultBtn.addActionListener(this);
    colWidthPn.add(mDefaultBtn,BorderLayout.EAST);
    
    main.add(colWidthPn);
    
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


  private JButton createBrowseButton(final JTextField tf) {
    JButton bt = new JButton(mLocalizer.msg("change", "Change"));
    bt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        JFileChooser fileChooser=new JFileChooser();
        String[] extArr = { ".jpg", ".jpeg", ".gif", ".png"};
        fileChooser.setFileFilter(new util.ui.ExtensionFileFilter(extArr, ".jpg, .gif, png"));
        fileChooser.showOpenDialog(mSettingsPn);
        File selection = fileChooser.getSelectedFile();
        if (selection != null) {
          tf.setText(selection.getAbsolutePath());
        }
      }
    });
    
    Dimension size = bt.getPreferredSize();
    size.height = tf.getPreferredSize().height;
    bt.setPreferredSize(size);
    
    return bt;
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
    
    Settings.setTableBackgroundEdge(mBackgroundEdgeTF.getText());
    Settings.setTableBackgroundEarly(mBackgroundEarlyTF.getText());
    Settings.setTableBackgroundMidday(mBackgroundMiddayTF.getText());
    Settings.setTableBackgroundAfternoon(mBackgroundAfternoonTF.getText());
    Settings.setTableBackgroundEvening(mBackgroundEveningTF.getText());
    
    Settings.setColumnWidth(mColWidthSl.getValue());
    
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

