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

package tvbrowser.ui.settings.channel;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Calendar;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;

import tvbrowser.core.Settings;
import util.ui.CaretPositionCorrector;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;

/**
 * This dialog enables the user to change every setting in the channel appearance
 * 
 * @author bodum
 * @since 2.1
 */
public class ChannelConfigDlg extends JDialog implements ActionListener, WindowClosingIf {
  /** Localizer */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannelConfigDlg.class);
  /** Current Channel */
  private Channel mChannel;
  /** Close/OK Buttons */
  private JButton mCloseBt, mOKBt;
  /** The Correction-Time*/
  private JComboBox mCorrectionCB;

  /** File for Icon */
  private File mIconFile;

  /** Button to Change FileName */
  private JButton mChangeIcon;

  /** Channel-Name */
  private JTextField mChannelName;
  /** Channel-WebPage*/
  private JTextField mWebPage;
  
  /** Start time limit selection */
  private JSpinner mStartTimeLimit;
  /** End time limit selection */
  private JSpinner mEndTimeLimit;
  private JLabel mIconLabel;
  
  /**
   * Create the Dialog
   * @param parent Parent
   * @param channel Channel to show
   */
  public ChannelConfigDlg(JDialog parent, Channel channel) {
    super(parent, mLocalizer.msg("configChannel", "Configure Channel"), true);
    mChannel = channel;
    createDialog();
  }

  /**
   * Create the Dialog
   * @param parent Parent
   * @param channel Channel to show
   */
  public ChannelConfigDlg(JFrame parent, Channel channel) {
    super(parent,  mLocalizer.msg("configChannel", "Configure Channel"), true);
    mChannel = channel;
    createDialog();
  }

  /**
   * Create the GUI
   */
  private void createDialog() {
    JPanel panel = (JPanel) getContentPane();

    UiUtilities.registerForClosing(this);
    
    panel.setLayout(new FormLayout("default, 3dlu, fill:default:grow",
    "default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu, default, 5dlu, default, 3dlu, default, 3dlu:grow, default, 5dlu, default"));

    CellConstraints cc = new CellConstraints();

    panel.setBorder(Borders.DLU4_BORDER);

    // name
    panel.add(new JLabel(mLocalizer.msg("channelName", "Channel Name:")), cc.xy(1, 1));
    mChannelName = new JTextField(mChannel.getName());
    panel.add(mChannelName, cc.xy(3, 1));

    // provider
    panel.add(new JLabel(mLocalizer.msg("provider", "Provided by:")), cc.xy(1, 3));
    panel.add(new JLabel(ChannelUtil.getProviderName(mChannel)), cc.xy(3, 3));

    // logo
    panel.add(new JLabel(mLocalizer.msg("channelLogo", "Channel Logo:")), cc.xy(1, 5));
    if (mChannel.getUserIconFileName() != null) {
      mIconFile = new File(mChannel.getUserIconFileName());
    }
    
    mIconLabel = new JLabel(createUserIcon());
    mChangeIcon = new JButton(mLocalizer.msg("useIcon", "Select channel icon"));
    mChangeIcon.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        changeIcon();
      }
    });

    panel.add(mIconLabel, cc.xy(3, 5));
    panel.add(mChangeIcon, cc.xy(3, 7));

    // URL
    panel.add(new JLabel(mLocalizer.msg("webAddress", "Web Address:")), cc.xy(1, 9));
    mWebPage =new JTextField(mChannel.getWebpage());
    panel.add(mWebPage, cc.xy(3, 9));

    // time correction
    panel.add(new JLabel(mLocalizer.msg("time", "Time Correction:")), cc.xy(1, 11));
    mCorrectionCB = new JComboBox(new String[] { "-1:00", "0:00", "+1:00" });
    mCorrectionCB.setSelectedIndex(mChannel.getDayLightSavingTimeCorrection() + 1);
    panel.add(mCorrectionCB, cc.xy(3, 11));

    JTextArea txt = UiUtilities.createHelpTextArea(mLocalizer.msg("DLSTNote", ""));
    // Hack because of growing JTextArea in FormLayout
    txt.setMinimumSize(new Dimension(150, 20));
    panel.add(txt, cc.xy(3, 13));

    // time limitation
    panel.add(DefaultComponentFactory.getInstance().createLabel(mLocalizer.msg("timeLimits","Time limits:")), cc.xy(1,15));
    
    String timePattern = mLocalizer.msg("timePattern", "hh:mm a");
        
    mStartTimeLimit = new JSpinner(new SpinnerDateModel());
    mStartTimeLimit.setEditor(new JSpinner.DateEditor(mStartTimeLimit, timePattern));
    setTimeDate(mStartTimeLimit, mChannel.getStartTimeLimit());

    mEndTimeLimit = new JSpinner(new SpinnerDateModel());
    mEndTimeLimit.setEditor(new JSpinner.DateEditor(mEndTimeLimit, timePattern));
    setTimeDate(mEndTimeLimit, mChannel.getEndTimeLimit());
        
    ((JSpinner.DateEditor)mStartTimeLimit.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.LEFT);
    ((JSpinner.DateEditor)mEndTimeLimit.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.LEFT);
    
    CaretPositionCorrector.createCorrector(((JSpinner.DateEditor)mStartTimeLimit.getEditor()).getTextField(), new char[] {':'}, -1);
    CaretPositionCorrector.createCorrector(((JSpinner.DateEditor)mEndTimeLimit.getEditor()).getTextField(), new char[] {':'}, -1);
    
    PanelBuilder timeLimitPanel = new PanelBuilder(new FormLayout("default:grow,10dlu,default:grow","default,2dlu,default"));
    
    timeLimitPanel.addLabel(mLocalizer.msg("startTime","Start time:"), cc.xy(1,1));
    timeLimitPanel.addLabel(mLocalizer.msg("endTime","End time:"), cc.xy(3,1));
    timeLimitPanel.add(mStartTimeLimit, cc.xy(1,3));
    timeLimitPanel.add(mEndTimeLimit, cc.xy(3,3));
    
    panel.add(timeLimitPanel.getPanel(), cc.xy(3,15));
    
    JTextArea txt2 = UiUtilities.createHelpTextArea(mLocalizer.msg("DLSTNote", ""));
    // Hack because of growing JTextArea in FormLayout
    txt2.setMinimumSize(new Dimension(150, 20));
    panel.add(txt2, cc.xy(3, 17));
    
    // buttons
    ButtonBarBuilder builder = new ButtonBarBuilder();
    JButton defaultButton = new JButton(Localizer.getLocalization(Localizer.I18N_DEFAULT));
    
    defaultButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetToDefaults();
      }

    });
    
    builder.addGridded(defaultButton);
    builder.addRelatedGap();
    builder.addGlue();

    mOKBt = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    mOKBt.addActionListener(this);
    
    getRootPane().setDefaultButton(mOKBt);
    
    mCloseBt = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCloseBt.addActionListener(this);

    builder.addGriddedButtons(new JButton[] { mOKBt, mCloseBt });

    panel.add(new JSeparator(), cc.xyw(1, 19, 3));
    panel.add(builder.getPanel(), cc.xyw(1, 21, 3));
    
    pack();
    Settings.layoutWindow("channelConfig", this, new Dimension(420,350));
  }
  
  private void setTimeDate(JSpinner toSet, int time) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, time / 60);
    cal.set(Calendar.MINUTE, time % 60);
    
    toSet.setValue(cal.getTime());
  }
  
  private int getTimeInMinutes(JSpinner toGet) {
    Calendar cal = Calendar.getInstance();
    cal.setTime((java.util.Date)toGet.getValue());
    
    return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
  }

  /**
   * Reset the Channel to the Default-Values
   */
  private void resetToDefaults() {
    mWebPage.setText(mChannel.getDefaultWebPage());
    mChannelName.setText(mChannel.getDefaultName());
    mIconFile = null;
    mIconLabel.setIcon(createUserIcon());
    mCorrectionCB.setSelectedIndex(1);
    setTimeDate(mStartTimeLimit, 0);
    setTimeDate(mEndTimeLimit, 0);
  }

  /**
   * Create a User-Icon
   * @return Icon
   */
  private Icon createUserIcon() {
    Icon icon;
    if ((mIconFile != null) && (mIconFile.exists())) {
      Image img = ImageUtilities.createImage(mIconFile.getAbsolutePath());
      if (img != null) {
        icon = UiUtilities.createChannelIcon(new ImageIcon(img));
      } else {
        icon = UiUtilities.createChannelIcon(mChannel.getIcon());
      }
    } else {
      icon = UiUtilities.createChannelIcon(mChannel.getDefaultIcon());
    }

    return icon;
  }

  /**
   * Change the Icon
   */
  private void changeIcon() {
    JFileChooser fileChooser = new JFileChooser(mIconFile);
    String[] extArr = { ".jpg", ".jpeg", ".gif", ".png" };
    fileChooser.setFileFilter(new util.ui.ExtensionFileFilter(extArr, ".jpg, .gif, .png"));
    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      File selection = fileChooser.getSelectedFile();
      if (selection != null) {
        mIconFile = selection;
        mIconLabel.setIcon(createUserIcon());
      }
    }
  }

  /**
   * Center and Show the Dialog
   */
  public void centerAndShow() {
    UiUtilities.centerAndShow(this);
  }

  public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    if (o == mOKBt) {
      int correction = mCorrectionCB.getSelectedIndex() - 1;
      mChannel.setDayLightSavingTimeCorrection(correction);
      mChannel.useUserIcon(mIconFile != null);
      if (mIconFile != null) {
        mChannel.setUserIconFileName(mIconFile.getAbsolutePath());
      } else {
        mChannel.setUserIconFileName(null);
      }
      mChannel.setUserChannelName(mChannelName.getText());
      mChannel.setUserWebPage(mWebPage.getText());
      mChannel.setStartTimeLimit(getTimeInMinutes(mStartTimeLimit));
      mChannel.setEndTimeLimit(getTimeInMinutes(mEndTimeLimit));
      
      setVisible(false);
    } else if (o == mCloseBt) {
      setVisible(false);
    }

  }

  public void close() {
    setVisible(false);
  }

}