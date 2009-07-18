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
import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import util.ui.FontChooserPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FontsSettingsTab implements devplugin.SettingsTab {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(FontsSettingsTab.class);

  private JCheckBox mUseDefaultFontsCB;

  private JCheckBox mEnableAntialiasingCB;

  private FontChooserPanel mTitleFontPanel, mInfoFontPanel, mChannelNameFontPanel, mTimeFontPanel;

  private JLabel mTimeFontLabel;

  private JLabel mChannelNameFontLabel;

  private JLabel mInfoFontLabel;

  private JLabel mTitleFontLabel;

  public JPanel createSettingsPanel() {
    PanelBuilder mainPanel = new PanelBuilder(new FormLayout("5dlu, 10dlu, pref, 3dlu, pref, fill:3dlu:grow", 
        "pref, 5dlu, pref, 10dlu, pref, 5dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));
    mainPanel.setBorder(Borders.DIALOG_BORDER);
    
    CellConstraints cc = new CellConstraints();
    
    mainPanel.addSeparator(mLocalizer.msg("Fonts", "Fonts"), cc.xyw(1,1,6));
    
    mEnableAntialiasingCB = new JCheckBox(mLocalizer.msg("EnableAntialiasing", "Enable antialiasing"));
    mEnableAntialiasingCB.setSelected(Settings.propEnableAntialiasing.getBoolean());

    mainPanel.add(mEnableAntialiasingCB, cc.xyw(2,3, 4));

    mainPanel.addSeparator(mLocalizer.msg("UserDefinedFonts", "Userdefined Fonts"), cc.xyw(1,5,6));

    mUseDefaultFontsCB = new JCheckBox(mLocalizer.msg("UseDefaultFonts", "Use default fonts"));
    mUseDefaultFontsCB.setSelected(Settings.propUseDefaultFonts.getBoolean());
    
    mainPanel.add(mUseDefaultFontsCB, cc.xyw(2,7, 4));
        
    mTitleFontLabel = new JLabel(mLocalizer.msg("ProgramTitle", "Program title"));
    mainPanel.add(mTitleFontLabel, cc.xy(3,9));
    mTitleFontPanel = new FontChooserPanel(Settings.propProgramTitleFont.getFont());
    mainPanel.add(mTitleFontPanel, cc.xy(5,9));

    mInfoFontLabel = new JLabel(mLocalizer.msg("ProgramInfo", "Program information"));
    mainPanel.add(mInfoFontLabel, cc.xy(3,11));
    mInfoFontPanel = new FontChooserPanel(Settings.propProgramInfoFont.getFont());
    mainPanel.add(mInfoFontPanel, cc.xy(5,11));

    mChannelNameFontLabel = new JLabel(mLocalizer.msg("ChannelNames", "Channel name"));
    mainPanel.add(mChannelNameFontLabel, cc.xy(3,13));
    mChannelNameFontPanel = new FontChooserPanel(Settings.propChannelNameFont.getFont());
    mainPanel.add(mChannelNameFontPanel, cc.xy(5,13));

    mTimeFontLabel = new JLabel(mLocalizer.msg("Time", "Time"));
    mainPanel.add(mTimeFontLabel, cc.xy(3,15));
    mTimeFontPanel = new FontChooserPanel(Settings.propProgramTimeFont.getFont());
    mainPanel.add(mTimeFontPanel, cc.xy(5,15));

    mUseDefaultFontsCB.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        enableFontFields(!mUseDefaultFontsCB.isSelected());
      }
    });
    
    enableFontFields(!mUseDefaultFontsCB.isSelected());
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
  }
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Settings.propProgramTitleFont.setFont(mTitleFontPanel.getChosenFont());
    Settings.propProgramInfoFont.setFont(mInfoFontPanel.getChosenFont());
    Settings.propChannelNameFont.setFont(mChannelNameFontPanel.getChosenFont());
    Settings.propProgramTimeFont.setFont(mTimeFontPanel.getChosenFont());
    Settings.propUseDefaultFonts.setBoolean(mUseDefaultFontsCB.isSelected());
    Settings.propEnableAntialiasing.setBoolean(mEnableAntialiasingCB.isSelected());
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