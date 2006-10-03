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
 *     $Date: 2006-04-26 19:57:04 +0200 (Mi, 26 Apr 2006) $
 *   $Author: troggan $
 * $Revision: 2255 $
 */

package tvbrowser.ui.settings;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import tvbrowser.core.Settings;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsItem;
import devplugin.SettingsTab;

/**
 * Settings for the ChannelList on the right side of the Gui
 * 
 * @author bodum
 */
public class ChannelListSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannelListSettingsTab.class);

  private JPanel mSettingsPn;

  private JRadioButton mShowNameAndIcon;

  private JRadioButton mShowIcon;

  private JRadioButton mShowName;

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    FormLayout layout = new FormLayout("5dlu, pref, 3dlu, pref, 3dlu, pref, fill:pref:grow 3dlu", "");
    mSettingsPn = new JPanel(layout);
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);

    CellConstraints cc = new CellConstraints();

    // Miscellaneous *********************************************
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("5dlu"));
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("3dlu"));
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("3dlu"));
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("3dlu"));
    layout.appendRow(new RowSpec("pref"));
    layout.appendRow(new RowSpec("5dlu"));
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("channelIcons.Title", "Channel Icons")), cc.xyw(1,1,8));
    
    mShowNameAndIcon = new JRadioButton(mLocalizer.msg("channelIcons.showNameAndIcon", "Show Channel Name and Icon"));
    mShowIcon = new JRadioButton(mLocalizer.msg("channelIcons.showIcon", "Show only Channel Icon"));
    mShowName = new JRadioButton(mLocalizer.msg("channelIcons.showName", "Show only Channel Name"));
    
    ButtonGroup group = new ButtonGroup();
    group.add(mShowIcon);
    group.add(mShowNameAndIcon);
    group.add(mShowName);
    
    mSettingsPn.add(mShowNameAndIcon, cc.xyw(2,3,7));
    mSettingsPn.add(mShowIcon, cc.xyw(2,5,7));
    mSettingsPn.add(mShowName, cc.xyw(2,7,7));
    
    if (Settings.propShowChannelIconsInChannellist.getBoolean() &&
        Settings.propShowChannelNamesInChannellist.getBoolean()) {
      mShowNameAndIcon.setSelected(true);
    } else if (Settings.propShowChannelIconsInChannellist.getBoolean()) {
      mShowIcon.setSelected(true);
    } else {
      Settings.propShowChannelNamesInChannellist.setBoolean(true);
      mShowName.setSelected(true);
    }
    updateIconSelection();
    Settings.propEnableChannelIcons.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
          updateIconSelection();
      }
    });
    
    JEditorPane pane = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("channelIcons.help","To disable/enable Channel Icons globally, please look <a href=\"#link\">here</a>."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.LOOKANDFEEL);
        }
      }
    });
    
    mSettingsPn.add(pane, cc.xyw(2,9,7));
    

    return mSettingsPn;
  }

  private void updateIconSelection() {
    if (!Settings.propEnableChannelIcons.getBoolean()) {
      mShowNameAndIcon.setEnabled(false);
      mShowIcon.setEnabled(false);
      mShowName.setSelected(true);
    } else {
      mShowNameAndIcon.setEnabled(true);
      mShowIcon.setEnabled(true);
    }
  }
  
    /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    if(Settings.propEnableChannelIcons.getBoolean()) {
      Settings.propShowChannelIconsInChannellist.setBoolean(mShowNameAndIcon.isSelected() || mShowIcon.isSelected());
      Settings.propShowChannelNamesInChannellist.setBoolean(mShowNameAndIcon.isSelected() || mShowName.isSelected());      
    }
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
    return mLocalizer.msg("channelList", "Channel List");
  }
}