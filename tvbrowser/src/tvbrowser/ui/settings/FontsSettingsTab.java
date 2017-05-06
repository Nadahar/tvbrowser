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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import util.ui.FontChooserPanel;

public class FontsSettingsTab implements devplugin.SettingsTab {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(FontsSettingsTab.class);

  private JCheckBox mUseUserDefindedFontsCB;

  private JCheckBox mEnableAntialiasingCB;

  private FontChooserPanel mTitleFontPanel, mInfoFontPanel, mChannelNameFontPanel, mTimeFontPanel;

  private JLabel mTimeFontLabel;

  private JLabel mChannelNameFontLabel;

  private JLabel mInfoFontLabel;

  private JLabel mTitleFontLabel;
  
  private JLabel mTextLineGapLabel;
  private JComboBox<String> mTextLineGap;

  public JPanel createSettingsPanel() {
    PanelBuilder mainPanel = new PanelBuilder(new FormLayout("5dlu, 10dlu, default, 3dlu, default, fill:3dlu:grow",
        "default, 5dlu, default, default, 3dlu, default, 13dlu, default, 3dlu, default," +
        " 3dlu, default, 3dlu, default"));
    mainPanel.border(Borders.DIALOG);
    
    int y = 1;
    
    mainPanel.addSeparator(mLocalizer.msg("Fonts", "Fonts"), CC.xyw(1,y++,6));
    
    mEnableAntialiasingCB = new JCheckBox(mLocalizer.msg("EnableAntialiasing", "Enable antialiasing"));
    mEnableAntialiasingCB.setSelected(Settings.propEnableAntialiasing.getBoolean());

    mainPanel.add(mEnableAntialiasingCB, CC.xyw(2,++y,4));

    mUseUserDefindedFontsCB = new JCheckBox(mLocalizer.msg("UserDefinedFonts", "Use userdefined fonts"));
    mUseUserDefindedFontsCB.setSelected(!Settings.propUseDefaultFonts.getBoolean());
    
    mainPanel.add(mUseUserDefindedFontsCB, CC.xyw(2,++y,4));

    y += 2;
    
    mChannelNameFontLabel = new JLabel(mLocalizer.msg("ChannelNames", "Channel name"));
    mainPanel.add(mChannelNameFontLabel, CC.xy(3,y));
    mChannelNameFontPanel = new FontChooserPanel(Settings.propChannelNameFont.getFont());
    mainPanel.add(mChannelNameFontPanel, CC.xy(5,y));
    
    y += 2;
    
    mTitleFontLabel = new JLabel(mLocalizer.msg("ProgramTitle", "Program title"));
    mainPanel.add(mTitleFontLabel, CC.xy(3,y));
    mTitleFontPanel = new FontChooserPanel(Settings.propProgramTitleFont.getFont());
    mainPanel.add(mTitleFontPanel, CC.xy(5,y));

    y += 2;
    
    mInfoFontLabel = new JLabel(mLocalizer.msg("ProgramInfo", "Program information"));
    mainPanel.add(mInfoFontLabel, CC.xy(3,y));
    mInfoFontPanel = new FontChooserPanel(Settings.propProgramInfoFont.getFont());
    mainPanel.add(mInfoFontPanel, CC.xy(5,y));
    
    y += 2;
    
    mTimeFontLabel = new JLabel(mLocalizer.msg("Time", "Time"));
    mainPanel.add(mTimeFontLabel, CC.xy(3,y));
    mTimeFontPanel = new FontChooserPanel(Settings.propProgramTimeFont.getFont());
    mainPanel.add(mTimeFontPanel, CC.xy(5,y));
    
    y += 2;
        
    mTextLineGapLabel = new JLabel(mLocalizer.msg("lineSpacing", "Line spacing:"));
    mainPanel.add(mTextLineGapLabel, CC.xy(3,y));
    mTextLineGap = new JComboBox<>(new String[] {
        mLocalizer.msg("lineSpacing.singleLine", "Single line"),
        mLocalizer.msg("lineSpacing.oneAndAQuaterLine", "1.25 line"),
        mLocalizer.msg("lineSpacing.oneAndAHalfLine", "1.5 line"),
        mLocalizer.msg("lineSpacing.oneAndThreeQuaterLine", "1.75 line"),
        mLocalizer.msg("lineSpacing.doubleLine", "Double line")
        });
    mTextLineGap.setSelectedIndex(Settings.propProgramTextLineGap.getInt());
    mainPanel.add(mTextLineGap, CC.xy(5,y));
    
    mUseUserDefindedFontsCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        enableFontFields(mUseUserDefindedFontsCB.isSelected());
      }
    });
    
    enableFontFields(mUseUserDefindedFontsCB.isSelected());
    return mainPanel.getPanel();
  }

  private void enableFontFields(boolean enable) {
    mTitleFontLabel.setEnabled(enable);
    mTitleFontPanel.setEnabled(enable);
    mInfoFontLabel.setEnabled(enable);
    mInfoFontPanel.setEnabled(enable);
    mChannelNameFontLabel.setEnabled(enable);
    mChannelNameFontPanel.setEnabled(enable);
    mTimeFontLabel.setEnabled(enable);
    mTimeFontPanel.setEnabled(enable);
    mTextLineGapLabel.setEnabled(enable);
    mTextLineGap.setEnabled(enable);
  }
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Settings.propProgramTitleFont.setFont(mTitleFontPanel.getChosenFont());
    Settings.propProgramInfoFont.setFont(mInfoFontPanel.getChosenFont());
    Settings.propChannelNameFont.setFont(mChannelNameFontPanel.getChosenFont());
    Settings.propProgramTimeFont.setFont(mTimeFontPanel.getChosenFont());
    Settings.propUseDefaultFonts.setBoolean(!mUseUserDefindedFontsCB.isSelected());
    Settings.propEnableAntialiasing.setBoolean(mEnableAntialiasingCB.isSelected());
    Settings.propProgramTextLineGap.setInt(mTextLineGap.getSelectedIndex());
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("apps", "preferences-desktop-font", 16);
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("Fonts", "Fonts");
  }

}