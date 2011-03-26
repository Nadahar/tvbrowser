/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.core.Settings;
import util.ui.Localizer;
import util.ui.OrderChooser;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Channel;
import devplugin.SettingsItem;
import devplugin.SettingsTab;

/**
 * Channel settings for the program showing in tray.
 * 
 * @author René Mach
 *
 */
public class TrayProgramsChannelsSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer = TrayBaseSettingsTab.mLocalizer;
  
  private JCheckBox mUseUserChannels;
  private OrderChooser mChannelOCh;
  private static boolean mTrayIsEnabled = Settings.propTrayIsEnabled.getBoolean();
  private JLabel mSeparator1;
  private JSlider mChannelWidth;
  
  private String mHelpLinkText;
  
  private JEditorPane mHelpLabel;
  
  private static TrayProgramsChannelsSettingsTab mInstance;
  private static boolean mNow = Settings.propTrayNowProgramsEnabled.getBoolean(),
                         mSoon = Settings.propTraySoonProgramsEnabled.getBoolean(),
                         mOnTime = Settings.propTrayOnTimeProgramsEnabled.getBoolean();
  
  public JPanel createSettingsPanel() {
    mInstance = this;
    
    PanelBuilder builder = new PanelBuilder(new FormLayout(
        "5dlu,pref,2dlu,default,5dlu,pref,fill:default:grow,5dlu",
        "pref,5dlu,pref,10dlu,pref,5dlu,pref,10dlu,fill:default:grow,5dlu,pref"));
    builder.setDefaultDialogBorder();
    CellConstraints cc = new CellConstraints();
    try {
   mChannelWidth = new JSlider(SwingConstants.HORIZONTAL, 50, 150, Settings.propTrayChannelWidth.getInt());
    }catch(Exception e){e.printStackTrace();}
    
    mUseUserChannels = new JCheckBox(mLocalizer.msg("userChannels","Use user defined channels"),Settings.propTrayUseSpecialChannels.getBoolean());
    mUseUserChannels.setToolTipText(mLocalizer.msg("userChannelsToolTip","<html>If you select this you can choose the channels that will be used for<br><b>Programs at...</b> and <b>Now/Soon running programs</b>.<br>If this isn't selected the first 10 channels in default order will be used.</html>"));
    
    mChannelOCh = new OrderChooser(
        Settings.propTraySpecialChannels.getChannelArray(),
        Settings.propSubscribedChannels.getChannelArray(), true);
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("help","The Tray is deactivated. To activate these settings activate the option <b>Tray activated</b> in the <a href=\"#link\">Tray Base settings</a>."),new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.TRAY);
        }
      }
    });
    
    mHelpLinkText = mHelpLabel.getText();
    mHelpLabel.setFont(mUseUserChannels.getFont());
    
    builder.addSeparator(mLocalizer.msg("channelColumnWidth","Column with for channel name"), cc.xyw(1, 1, 8));
    builder.add(mChannelWidth, cc.xy(2,3));
    final JLabel valueLabel = builder.addLabel(String.valueOf(mChannelWidth.getValue()), cc.xy(4,3));
    valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    Dimension dim = valueLabel.getPreferredSize();
    valueLabel.setPreferredSize(new Dimension(Sizes.dialogUnitXAsPixel(20, builder.getPanel()), dim.height));

    mChannelWidth.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        valueLabel.setText(String.valueOf(mChannelWidth.getValue()));
      }
    });
    
    JButton reset = new JButton(Localizer.getLocalization(Localizer.I18N_DEFAULT));
    reset.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mChannelWidth.setValue(Settings.propTrayChannelWidth.getDefault());
      }
    });
    
    builder.add(reset, cc.xy(6,3));
    
    JPanel c = (JPanel) builder.addSeparator(mLocalizer.msg(
        "channelsSeparator",
        "Which channels should be used for these displays?"), cc.xyw(1, 5, 8));
    builder.add(mUseUserChannels, cc.xyw(2,7,7));
    builder.add(mChannelOCh, cc.xyw(2, 9,7));
    builder.add(mHelpLabel, cc.xyw(1, 11, 8));
        
    mSeparator1 = (JLabel)c.getComponent(0);
    
    setEnabled(true);
    
    mUseUserChannels.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setEnabled(false);
      }
    });
    
    return builder.getPanel();
  }
  
  private String createHtml(Font font,String text) {
    return "<html><div style=\"color:#000000;font-family:"+ font.getName() +"; font-size:"+font.getSize()+";\">"+text+"</div></html>";
  }

  private void setEnabled(boolean trayStateChange) {
    if(!mTrayIsEnabled) {
      mHelpLabel.setVisible(true);
      mHelpLabel.setText(createHtml(mHelpLabel.getFont(),mHelpLinkText));
    }
    else if(!mNow && !mSoon && !mOnTime) {
      mHelpLabel.setVisible(true);
      mHelpLabel.setText(createHtml(mHelpLabel.getFont(),mLocalizer.msg("helpPrograms","<html>These settings are used only by the Now, Soon and At... programs. Enable at least one of that to enable these settings.</html>")));
    } else {
      mHelpLabel.setVisible(false);
    }
    
    if(trayStateChange) {
      mSeparator1.setEnabled(mTrayIsEnabled);
    }
    
    mUseUserChannels.setEnabled(mTrayIsEnabled && (mNow || mSoon || mOnTime));
    mChannelOCh.setEnabled(mTrayIsEnabled && mUseUserChannels.isSelected() && (mNow || mSoon || mOnTime));
  }
  
  public void saveSettings() {
    Settings.propTrayUseSpecialChannels.setBoolean(mUseUserChannels
        .isSelected());
    
    Object[] order = mChannelOCh.getOrder();
    Channel[] ch = new Channel[order.length];

    if(!mUseUserChannels.isSelected()) {
      order = Settings.propSubscribedChannels.getChannelArray();
      ch = new Channel[order.length > 10 ? 10 : order.length];
    }

    for (int i = 0; i < ch.length; i++) {
      ch[i] = (Channel) order[i];
    }
    
    if (order != null) {
      Settings.propTraySpecialChannels.setChannelArray(ch);
    }
    
    if (mChannelWidth != null) {
      Settings.propTrayChannelWidth.setInt(mChannelWidth.getValue());
    }
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return Localizer.getLocalization(Localizer.I18N_CHANNELS);
  }

  protected static void setTrayIsEnabled(boolean value) {
    mTrayIsEnabled = value;
    if(mInstance != null) {
      mInstance.setEnabled(true);
    }
  }
  
  protected static void setNowIsEnabled(boolean value) {
    mNow = value;
    if(mInstance != null) {
      mInstance.setEnabled(false);
    }
  }
  
  protected static void setSoonIsEnabled(boolean value) {
    mSoon = value;
    if(mInstance != null) {
      mInstance.setEnabled(false);
    }
  }

  protected static void setOnTimeIsEnabled(boolean value) {
    mOnTime = value;
    if(mInstance != null) {
      mInstance.setEnabled(false);
    }
  }
}
