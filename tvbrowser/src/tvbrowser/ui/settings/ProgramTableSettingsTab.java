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

  private JComboBox mBackgroundStyleCB;
  private JPanel mBackgoundPanel;
  
  private JPanel mOneImagePanel;
  private JTextField mOneImageBackgroundTF;

  private JPanel mTimeBlockPanel;
  private JSpinner mTimeBlockSizeSp;
  private JTextField mTimeBlockBackground1TF, mTimeBlockBackground2TF;
  private JCheckBox mTimeBlockShowWestChB;
  private JLabel mTimeBlockWestImage1Lb, mTimeBlockWestImage2Lb;
  private JTextField mTimeBlockWestImage1TF, mTimeBlockWestImage2TF;
  private JButton mTimeBlockWestImage1Bt, mTimeBlockWestImage2Bt;

  private JPanel mTimeOfDayPanel;
  private JTextField mTimeOfDayEdgeTF, mTimeOfDayEarlyTF, mTimeOfDayMiddayTF,
    mTimeOfDayAfternoonTF, mTimeOfDayEveningTF;

  private JSlider mColWidthSl;
  private JButton mDefaultBtn;

  private JSpinner mStartOfDayTimeSp, mEndOfDayTimeSp;
  
  
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
    if (Settings.propTableLayout.getString().equals("compact")) {
      mProgramArrangementCB.setSelectedIndex(0);
    } else {
      mProgramArrangementCB.setSelectedIndex(1);
    }
    p1.add(mProgramArrangementCB);
    
    
    // column width
       JPanel colWidthPn=new JPanel(new BorderLayout());
    
       colWidthPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("columnwidth","column width")));
       mColWidthSl=new JSlider(SwingConstants.HORIZONTAL, 50, 300, Settings.propColumnWidth.getInt());

       colWidthPn.add(mColWidthSl,BorderLayout.WEST);
       mColWidthSl.setPreferredSize(new Dimension(250,25));
    
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
    
       panel1.add(mStartOfDayTimeSp,BorderLayout.WEST);
       panel1.add(new JLabel("("+mLocalizer.msg("today","today")+")"));
       pn.add(panel1);
    
       pn.add(new JLabel(mLocalizer.msg("endOfDay","End of day")));
       panel1=new JPanel(new BorderLayout(7,0));
    
       mEndOfDayTimeSp = new JSpinner(new SpinnerDateModel());
       mEndOfDayTimeSp.setEditor(new JSpinner.DateEditor(mEndOfDayTimeSp, timePattern));
    
       panel1.add(mEndOfDayTimeSp,BorderLayout.WEST);
       panel1.add(new JLabel("("+mLocalizer.msg("nextDay","next day")+")"));
       pn.add(panel1);
    
       int minutes;
       Calendar cal = Calendar.getInstance();
       minutes = Settings.propProgramTableStartOfDay.getInt();    
       cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
       cal.set(Calendar.MINUTE, minutes % 60);
       mStartOfDayTimeSp.setValue(cal.getTime());
    
       minutes = Settings.propProgramTableEndOfDay.getInt();    
       cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
       cal.set(Calendar.MINUTE, minutes % 60);
       mEndOfDayTimeSp.setValue(cal.getTime());
    
       main.add(pn);
    
    
    
    
    
    
    
    
    
    // program table background panel
    p1 = new JPanel(new BorderLayout());
    msg = mLocalizer.msg("tableBackground", "Table background");
    p1.setBorder(BorderFactory.createTitledBorder(msg));
    main.add(p1);
    
    p2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
    p1.add(p2, BorderLayout.NORTH);
    
    msg = mLocalizer.msg("tableBackgroundStyle", "Table background style");
    p2.add(new JLabel(msg));

    String[] msgArr = {
      mLocalizer.msg("style.white", "White"),
      mLocalizer.msg("style.oneImage", "One image"),
      mLocalizer.msg("style.timeBlock", "Time block"),
      mLocalizer.msg("style.timeOfDay", "Time of day"),
    };
    mBackgroundStyleCB = new JComboBox(msgArr);
    String style = Settings.propTableBackgroundStyle.getString();
    if (style.equals("white")) {
      mBackgroundStyleCB.setSelectedIndex(0);
    }
    if (style.equals("oneImage")) {
      mBackgroundStyleCB.setSelectedIndex(1);
    }
    if (style.equals("timeBlock")) {
      mBackgroundStyleCB.setSelectedIndex(2);
    }
    if (style.equals("timeOfDay")) {
      mBackgroundStyleCB.setSelectedIndex(3);
    }
    mBackgroundStyleCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        handleBackgroundStyle();
      }
    });
    p2.add(mBackgroundStyleCB);
    
    mBackgoundPanel = new JPanel(new BorderLayout());
    p1.add(mBackgoundPanel, BorderLayout.CENTER);
    
    // one image background
    mOneImagePanel = new JPanel(new TabLayout(3));

    msg = mLocalizer.msg("oneImage.image", "Image");
    mOneImagePanel.add(new JLabel(msg));
    mOneImageBackgroundTF = new JTextField(Settings.propOneImageBackground.getString(), 15);
    mOneImagePanel.add(mOneImageBackgroundTF);
    mOneImagePanel.add(createBrowseButton(mOneImageBackgroundTF));
    
    // time block background
    mTimeBlockPanel = new JPanel(new TabLayout(1));

    p1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
    mTimeBlockPanel.add(p1);
    msg = mLocalizer.msg("timeBlock.blockSize", "Block size");
    p1.add(new JLabel(msg));
    mTimeBlockSizeSp = new JSpinner(new SpinnerNumberModel(Settings.propTimeBlockSize.getInt(), 1, 23, 1));
    //mTimeBlockSizeSp.setValue(new Integer(Settings.propTimeBlockSize.getInt()));
    p1.add(mTimeBlockSizeSp);
    msg = mLocalizer.msg("timeBlock.hours", "hours");
    p1.add(new JLabel(msg));

    p1 = new JPanel(new TabLayout(3));
    mTimeBlockPanel.add(p1);
    
    msg = mLocalizer.msg("timeBlock.background1", "Image 1");
    p1.add(new JLabel(msg));
    mTimeBlockBackground1TF = new JTextField(Settings.propTimeBlockBackground1.getString(), 15);
    p1.add(mTimeBlockBackground1TF);
    p1.add(createBrowseButton(mTimeBlockBackground1TF));
    
    msg = mLocalizer.msg("timeBlock.background2", "Image 2");
    p1.add(new JLabel(msg));
    mTimeBlockBackground2TF = new JTextField(Settings.propTimeBlockBackground2.getString(), 15);
    p1.add(mTimeBlockBackground2TF);
    p1.add(createBrowseButton(mTimeBlockBackground2TF));
    
    msg = mLocalizer.msg("timeBlock.showWest", "Show left border");
    mTimeBlockShowWestChB = new JCheckBox(msg, Settings.propTimeBlockShowWest.getBoolean());
    mTimeBlockShowWestChB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        handleTimeBlockShowWest();
      }
    });
    mTimeBlockPanel.add(mTimeBlockShowWestChB);

    p1 = new JPanel(new TabLayout(3));
    mTimeBlockPanel.add(p1);

    msg = mLocalizer.msg("timeBlock.west1", "Border image 1");
    mTimeBlockWestImage1Lb = new JLabel(msg);
    p1.add(mTimeBlockWestImage1Lb);
    mTimeBlockWestImage1TF = new JTextField(Settings.propTimeBlockWestImage1.getString(), 15);
    p1.add(mTimeBlockWestImage1TF);
    mTimeBlockWestImage1Bt = createBrowseButton(mTimeBlockWestImage1TF);
    p1.add(mTimeBlockWestImage1Bt);

    msg = mLocalizer.msg("timeBlock.west2", "Border image 2");
    mTimeBlockWestImage2Lb = new JLabel(msg);
    p1.add(mTimeBlockWestImage2Lb);
    mTimeBlockWestImage2TF = new JTextField(Settings.propTimeBlockWestImage2.getString(), 15);
    p1.add(mTimeBlockWestImage2TF);
    mTimeBlockWestImage2Bt = createBrowseButton(mTimeBlockWestImage2TF);
    p1.add(mTimeBlockWestImage2Bt);

    // time of day background
    mTimeOfDayPanel = new JPanel(new TabLayout(3));

    mTimeOfDayPanel.add(new JLabel(mLocalizer.msg("timeOfDay.edge", "Edge")));
    mTimeOfDayEdgeTF = new JTextField(Settings.propTimeOfDayBackgroundEdge.getString(), 15);
    mTimeOfDayPanel.add(mTimeOfDayEdgeTF);
    mTimeOfDayPanel.add(createBrowseButton(mTimeOfDayEdgeTF));

    mTimeOfDayPanel.add(new JLabel(mLocalizer.msg("timeOfDay.early", "Early")));
    mTimeOfDayEarlyTF = new JTextField(Settings.propTimeOfDayBackgroundEarly.getString(), 15);
    mTimeOfDayPanel.add(mTimeOfDayEarlyTF);
    mTimeOfDayPanel.add(createBrowseButton(mTimeOfDayEarlyTF));

    mTimeOfDayPanel.add(new JLabel(mLocalizer.msg("timeOfDay.midday", "Midday")));
    mTimeOfDayMiddayTF = new JTextField(Settings.propTimeOfDayBackgroundMidday.getString(), 15);
    mTimeOfDayPanel.add(mTimeOfDayMiddayTF);
    mTimeOfDayPanel.add(createBrowseButton(mTimeOfDayMiddayTF));

    mTimeOfDayPanel.add(new JLabel(mLocalizer.msg("timeOfDay.afternoon", "Afternoon")));
    mTimeOfDayAfternoonTF = new JTextField(Settings.propTimeOfDayBackgroundAfternoon.getString(), 15);
    mTimeOfDayPanel.add(mTimeOfDayAfternoonTF);
    mTimeOfDayPanel.add(createBrowseButton(mTimeOfDayAfternoonTF));

    mTimeOfDayPanel.add(new JLabel(mLocalizer.msg("timeOfDay.evening", "Evening")));
    mTimeOfDayEveningTF = new JTextField(Settings.propTimeOfDayBackgroundEvening.getString(), 15);
    mTimeOfDayPanel.add(mTimeOfDayEveningTF);
    mTimeOfDayPanel.add(createBrowseButton(mTimeOfDayEveningTF));
    
   

    handleBackgroundStyle();
    handleTimeBlockShowWest();

    return mSettingsPn;
  }


  private void handleBackgroundStyle() {
    JPanel newPn;
    switch (mBackgroundStyleCB.getSelectedIndex()) {
      case 0:  newPn = null; break;            // white
      case 1:  newPn = mOneImagePanel; break;  // oneimage
      case 3: newPn = mTimeOfDayPanel; break; // timeofday
      default:  newPn = mTimeBlockPanel; break; // timeblock
    }
    
    mBackgoundPanel.removeAll();
    if (newPn != null) {
      mBackgoundPanel.add(newPn, BorderLayout.CENTER);
    }
    mBackgoundPanel.revalidate();
    mBackgoundPanel.repaint();
  }


  private void handleTimeBlockShowWest() {
    boolean enabled = mTimeBlockShowWestChB.isSelected();
    
    mTimeBlockWestImage1Lb.setEnabled(enabled);
    mTimeBlockWestImage1TF.setEnabled(enabled);
    mTimeBlockWestImage1Bt.setEnabled(enabled);
    mTimeBlockWestImage2Lb.setEnabled(enabled);
    mTimeBlockWestImage2TF.setEnabled(enabled);
    mTimeBlockWestImage2Bt.setEnabled(enabled);
  }


  private JButton createBrowseButton(final JTextField tf) {
    JButton bt = new JButton(mLocalizer.msg("change", "Change"));
    bt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        File file = new File(tf.getText());
        JFileChooser fileChooser = new JFileChooser(file.getParent());
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
      Settings.propTableLayout.setString("compact");
    } else {
      Settings.propTableLayout.setString("timeSynchronous");
    }
    
    String backgroundStyle;
    switch (mBackgroundStyleCB.getSelectedIndex()) {
      case 0:  backgroundStyle = "white"; break;
      case 1:  backgroundStyle = "oneImage"; break;
      case 2:  backgroundStyle = "timeBlock"; break;
      default: backgroundStyle = "timeOfDay"; break;
    }
    Settings.propTableBackgroundStyle.setString(backgroundStyle);
    
    Settings.propOneImageBackground.setString(mOneImageBackgroundTF.getText());
    
    Integer blockSize = (Integer) mTimeBlockSizeSp.getValue();
    Settings.propTimeBlockSize.setInt(blockSize.intValue());
    Settings.propTimeBlockBackground1.setString(mTimeBlockBackground1TF.getText());
    Settings.propTimeBlockBackground2.setString(mTimeBlockBackground2TF.getText());
    Settings.propTimeBlockShowWest.setBoolean(mTimeBlockShowWestChB.isSelected());
    Settings.propTimeBlockWestImage1.setString(mTimeBlockWestImage1TF.getText());
    Settings.propTimeBlockWestImage2.setString(mTimeBlockWestImage2TF.getText());

    Settings.propTimeOfDayBackgroundEdge.setString(mTimeOfDayEdgeTF.getText());
    Settings.propTimeOfDayBackgroundEarly.setString(mTimeOfDayEarlyTF.getText());
    Settings.propTimeOfDayBackgroundMidday.setString(mTimeOfDayMiddayTF.getText());
    Settings.propTimeOfDayBackgroundAfternoon.setString(mTimeOfDayAfternoonTF.getText());
    Settings.propTimeOfDayBackgroundEvening.setString(mTimeOfDayEveningTF.getText());
    
    Settings.propColumnWidth.setInt(mColWidthSl.getValue());
    
    Calendar cal=Calendar.getInstance();
    Date startTime = (Date) mStartOfDayTimeSp.getValue();
    cal.setTime(startTime);
    int minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    Settings.propProgramTableStartOfDay.setInt(minutes);
    
    Date endTime = (Date) mEndOfDayTimeSp.getValue();
    cal.setTime(endTime);
    minutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    Settings.propProgramTableEndOfDay.setInt(minutes);
    
    
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

