/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import tvbrowser.core.Settings;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.SettingsTab;

public class ChannellogosSettingsTab implements SettingsTab {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannellogosSettingsTab.class);

  private JPanel mSettingsPn;

  private JCheckBox mShowChannelIconsCb, mProgramtableChIconsCb, mChannellistChIconsCb, mShowChannelNamesCb;

  public JPanel createSettingsPanel() {
    FormLayout layout = new FormLayout("5dlu, 4dlu, fill:pref:grow, 5dlu", "");

    CellConstraints cc = new CellConstraints();
    mSettingsPn = new JPanel(layout);
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);

    layout.appendRow(new RowSpec("pref"));
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("channelLogos", "Channel Logos")), cc.xyw(1, 1, 4));

    mShowChannelIconsCb = new JCheckBox(mLocalizer.msg("show", "Senderlogso anzeigen"));
    mProgramtableChIconsCb = new JCheckBox(mLocalizer.msg("programtable", "Programmtabelle"));
    mChannellistChIconsCb = new JCheckBox(mLocalizer.msg("channellist", "Kanalliste"));

    mShowChannelNamesCb = new JCheckBox(mLocalizer.msg("showChannelName", "Show channel name"));

    mShowChannelIconsCb.setSelected(Settings.propEnableChannelIcons.getBoolean());
    mProgramtableChIconsCb.setSelected(Settings.propShowChannelIconsInProgramTable.getBoolean());
    mChannellistChIconsCb.setSelected(Settings.propShowChannelIconsInChannellist.getBoolean());
    mShowChannelNamesCb.setSelected(Settings.propShowChannelNames.getBoolean());

    mShowChannelIconsCb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        boolean enabled = mShowChannelIconsCb.isSelected();
        mProgramtableChIconsCb.setEnabled(enabled);
        mChannellistChIconsCb.setEnabled(enabled);
        mProgramtableChIconsCb.setSelected(enabled);
        mChannellistChIconsCb.setSelected(enabled);
        mShowChannelNamesCb.setEnabled(enabled);
        if (!enabled) {
          mShowChannelNamesCb.setSelected(true);
        }
      }
    });

    boolean enabled = mShowChannelIconsCb.isSelected();
    mProgramtableChIconsCb.setEnabled(enabled);
    mChannellistChIconsCb.setEnabled(enabled);
    mShowChannelNamesCb.setEnabled(enabled);
    if (!enabled) {
      mShowChannelNamesCb.setSelected(true);
    }

    mProgramtableChIconsCb.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
    mChannellistChIconsCb.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
    mShowChannelNamesCb.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

    layout.appendRow(new RowSpec("5dlu"));
    layout.appendRow(new RowSpec("pref"));
    mSettingsPn.add(mShowChannelIconsCb, cc.xyw(2, 3, 2));
    layout.appendRow(new RowSpec("3dlu"));
    layout.appendRow(new RowSpec("pref"));
    mSettingsPn.add(mProgramtableChIconsCb, cc.xy(3, 5));
    layout.appendRow(new RowSpec("3dlu"));
    layout.appendRow(new RowSpec("pref"));
    mSettingsPn.add(mChannellistChIconsCb, cc.xy(3, 7));
    layout.appendRow(new RowSpec("3dlu"));
    layout.appendRow(new RowSpec("pref"));
    mSettingsPn.add(mShowChannelNamesCb, cc.xy(3, 9));

    return mSettingsPn;
  }

  public void saveSettings() {
    boolean enableChannelIcons = mShowChannelIconsCb.isSelected();
//    Settings.propEnableChannelIcons.setBoolean(enableChannelIcons);
//    Settings.propShowChannelIconsInChannellist.setBoolean(mChannellistChIconsCb.isSelected());
//    Settings.propShowChannelIconsInProgramTable.setBoolean(mProgramtableChIconsCb.isSelected());
//    Settings.propShowChannelNames.setBoolean(mShowChannelNamesCb.isSelected());
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return mLocalizer.msg("channelLogos", "Channel Logos");
  }

}