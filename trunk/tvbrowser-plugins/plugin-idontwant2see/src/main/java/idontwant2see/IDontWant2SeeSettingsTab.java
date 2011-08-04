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
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import util.ui.DefaultProgramImportanceSelectionPanel;
import util.ui.Localizer;
import util.ui.ScrollableJPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * Settings tab for I don't want to see plugin.
 * 
 * @author René Mach
 */
public class IDontWant2SeeSettingsTab implements SettingsTab {
  private JCheckBox mAutoSwitchToMyFilter;
  private JRadioButton mSimpleContextMenu;
  private JRadioButton mCascadedContextMenu;
  private ExclusionTablePanel mExclusionPanel;
  private Localizer mLocalizer = IDontWant2See.mLocalizer;
  private DefaultProgramImportanceSelectionPanel mProgramImportancePanel;
  private IDontWant2SeeSettings mSettings;
  
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
            "default,10dlu,default,5dlu,default,5dlu,default,5dlu,fill:default:grow,10dlu,fill:default:grow"),
            new ScrollableJPanel());

    final PanelBuilder pb2 = new PanelBuilder(new FormLayout(
        "default,2dlu,default", "default,1dlu,default,default"));

    mSimpleContextMenu = new JRadioButton(mLocalizer.msg(
        "settings.menu.simple", "Direct in the context menu:"), mSettings
        .isSimpleMenu());
    mSimpleContextMenu.setHorizontalTextPosition(JRadioButton.RIGHT);

    mCascadedContextMenu = new JRadioButton(mLocalizer.msg(
        "settings.menu.cascaded", "In a sub menu:"), !mSettings.isSimpleMenu());
    mCascadedContextMenu.setHorizontalTextPosition(JRadioButton.RIGHT);
    mCascadedContextMenu.setVerticalAlignment(JRadioButton.TOP);

    final ButtonGroup bg = new ButtonGroup();

    bg.add(mSimpleContextMenu);
    bg.add(mCascadedContextMenu);

    pb2.add(mSimpleContextMenu, cc.xy(1, 1));
    pb2.addLabel("-"
        + mLocalizer.msg("name", "I don't want to see!")
        + " ("
        + mLocalizer.msg("menu.completeCaseSensitive",
            "Instant exclusion with title") + ")", cc.xy(3, 1));

    pb2.add(mCascadedContextMenu, cc.xy(1, 3));
    pb2.addLabel("-"
        + mLocalizer.msg("menu.completeCaseSensitive",
            "Instant exclusion with title"), cc.xy(3, 3));
    pb2.addLabel(
        "-" + mLocalizer.msg("menu.userEntered", "User entered value"), cc.xy(
            3, 4));

    mAutoSwitchToMyFilter = new JCheckBox(mLocalizer.msg("settings.autoFilter",
        "Automatically activate filter on adding/removing"), mSettings
        .isSwitchToMyFilter());

    pb.add(mAutoSwitchToMyFilter, cc.xyw(2, 1, 3));
    pb.addSeparator(mLocalizer.msg("settings.contextMenu", "Context menu"), cc
        .xyw(1, 3, 5));
    pb.add(pb2.getPanel(), cc.xyw(2, 5, 3));
    pb.addSeparator(mLocalizer.msg("settings.search", "Search"), cc
        .xyw(1, 7, 5));
    pb.add(mExclusionPanel = new ExclusionTablePanel(mSettings), cc.xyw(2, 9, 3));

    mProgramImportancePanel = DefaultProgramImportanceSelectionPanel.createPanel(mSettings.getProgramImportance(),true,false);

    pb.add(mProgramImportancePanel, cc.xyw(2,11,3));
    
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
    
    mExclusionPanel.saveSettings(mSettings);
  }
}
