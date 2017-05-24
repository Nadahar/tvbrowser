/*
 * IDontWant2See - Plugin for TV-Browser
 * Copyright (C) 2008 René Mach
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package idontwant2see;


import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import util.ui.DefaultProgramImportanceSelectionPanel;
import util.ui.Localizer;
import util.ui.ScrollableJPanel;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import compat.VersionCompat;
import devplugin.SettingsTab;

/**
 * Settings tab for I don't want to see plugin.
 * 
 * @author René Mach
 */
public class IDontWant2SeeSettingsTab implements SettingsTab {
  private JCheckBox mAutoSwitchToMyFilter;
  private JCheckBox mAutoCaseSensitive;
  
  private JRadioButton mSimpleContextMenu;
  private JRadioButton mCascadedContextMenu;
  private ExclusionTablePanel mExclusionPanel;
  private Localizer mLocalizer = IDontWant2See.mLocalizer;
  private DefaultProgramImportanceSelectionPanel mProgramImportancePanel;
  private IDontWant2SeeSettings mSettings;
  
  private JTextField mUserName;
  private JPasswordField mUserPassword;
  
  /**
   * Create an instance of this class.
   * 
   * @param settings The current settings for the I don't want to see plugin.
   */
  public IDontWant2SeeSettingsTab(IDontWant2SeeSettings settings) {
    mSettings = settings;
  }

  public JPanel createSettingsPanel() {
    final CellConstraints cc = new CellConstraints();
    final PanelBuilder pb = new PanelBuilder(
        new FormLayout("5dlu,default,0dlu:grow,default,5dlu",
            "default,2dlu,default,10dlu,default,5dlu,default,1dlu,default,7dlu,default,10dlu,default,5dlu,default,5dlu,default,5dlu,fill:default:grow,10dlu,fill:default:grow"),
            new ScrollableJPanel());

    final FormLayout layout = new FormLayout("default,2dlu,0dlu:grow", "default,1dlu,default,default");
    
    final PanelBuilder pb2 = new PanelBuilder(layout);

    mUserName = new JTextField(mSettings.getUserName());
    mUserPassword = new JPasswordField(mSettings.getPassword());
        
    String msg = mLocalizer.msg("settings.menu.simple", "Direct in the context menu:");
    
    if(VersionCompat.isAtLeastTvBrowser4()) {
      msg = IDontWant2See.removeEnd(msg, ":");
    }
    
    mSimpleContextMenu = new JRadioButton(msg, mSettings.isSimpleMenu());
    mSimpleContextMenu.setHorizontalTextPosition(JRadioButton.RIGHT);

    msg = mLocalizer.msg("settings.menu.cascaded", "In a sub menu:");
    
    if(VersionCompat.isAtLeastTvBrowser4()) {
      msg = IDontWant2See.removeEnd(msg, ":");
    }
    
    mCascadedContextMenu = new JRadioButton(msg, !mSettings.isSimpleMenu());
    mCascadedContextMenu.setHorizontalTextPosition(JRadioButton.RIGHT);
    mCascadedContextMenu.setVerticalAlignment(JRadioButton.TOP);

    final ButtonGroup bg = new ButtonGroup();

    bg.add(mSimpleContextMenu);
    bg.add(mCascadedContextMenu);

    int y = 1;
    
    pb2.add(mSimpleContextMenu, cc.xy(1, y));
    
    if(!VersionCompat.isAtLeastTvBrowser4()) {
      pb2.addLabel("-"
          + mLocalizer.msg("name", "I don't want to see!")
          + " ("
          + mLocalizer.msg("menu.completeCaseSensitive",
              "Instant exclusion with title") + ")", cc.xy(3, y));
    }
    
    y += 2;
    
    pb2.add(mCascadedContextMenu, cc.xy(1, y));
    
    if(!VersionCompat.isAtLeastTvBrowser4()) {
      pb2.addLabel("-"
          + mLocalizer.msg("menu.completeCaseSensitive",
              "Instant exclusion with title"), cc.xy(3, y++));
      pb2.addLabel(
          "-" + mLocalizer.msg("menu.userEntered", "User entered value"), cc.xy(
              3, y));
    }
    else {
      layout.removeRow(y+1);
    }

    mAutoSwitchToMyFilter = new JCheckBox(mLocalizer.msg("settings.autoFilter",
        "Automatically activate filter on adding/removing"), mSettings
        .isSwitchToMyFilter());
    
    mAutoCaseSensitive = new JCheckBox(mLocalizer.msg("settings.autoCaseSensitive", "Case sensitive by default"),
        mSettings.isDefaultCaseSensitive());
    
    y = 1;

    pb.add(mAutoSwitchToMyFilter, cc.xyw(2, y++, 3));
    pb.add(mAutoCaseSensitive, cc.xyw(2,++y,3));
    
    y += 2;
    
    pb.addSeparator(mLocalizer.msg("settings.synchronization","Android synchronization"), cc.xyw(1, y, 5));
    
    y += 2;
    
    pb.addLabel(mLocalizer.msg("settings.userName","User name:") + " ", cc.xy(2, y));
    pb.add(mUserName, cc.xy(3,y));
    
    y += 2;
    
    pb.addLabel(mLocalizer.msg("settings.passWord","Password:") + " ", cc.xy(2, y));
    pb.add(mUserPassword, cc.xy(3,y));
    
    y += 2;
    
    pb.add(UiUtilities.createHtmlHelpTextArea("The Android robot is reproduced or modified from work created and shared by Google and used according to terms described in the Creative Commons 3.0 Attribution License."), CC.xyw(2, y, 4));
    
    y += 2;
    
    pb.addSeparator(mLocalizer.msg("settings.contextMenu", "Context menu"), cc
        .xyw(1, y, 5));
    
    y += 2;
    
    pb.add(pb2.getPanel(), cc.xyw(2, y, 3));
    
    y += 2;
    
    pb.addSeparator(mLocalizer.msg("settings.search", "Search"), cc
        .xyw(1, y, 5));
    
    y += 2;
    
    pb.add(mExclusionPanel = new ExclusionTablePanel(mSettings), cc.xyw(2, y, 3));

    mProgramImportancePanel = DefaultProgramImportanceSelectionPanel.createPanel(mSettings.getProgramImportance(),true,false);

    y += 2;
    
    pb.add(mProgramImportancePanel, cc.xyw(2,y,3));
    
    final JPanel p = new JPanel(new FormLayout("0dlu,0dlu:grow",
        "5dlu,fill:default:grow"));
    
    JScrollPane scrollPane = new JScrollPane(pb.getPanel());
    scrollPane.setBorder(null);
    scrollPane.setViewportBorder(null);
    
    p.add(scrollPane, cc.xy(2, 2));

    return p;
  }

  public Icon getIcon() {
    return IDontWant2See.getInstance().createImageIcon("apps", "idontwant2see", 16);
  }

  public String getTitle() {
    return null;
  }

  public void saveSettings() {
    mSettings.setSimpleMenu(mSimpleContextMenu.isSelected());
    mSettings.setSwitchToMyFilter(mAutoSwitchToMyFilter.isSelected());
    mSettings.setProgramImportance(mProgramImportancePanel.getSelectedImportance());
    mSettings.setUserName(mUserName.getText());
    mSettings.setPassword(new String(mUserPassword.getPassword()));
    mSettings.setDefaultCaseSensitive(mAutoCaseSensitive.isSelected());
    
    mExclusionPanel.saveSettings(mSettings);
  }
}
