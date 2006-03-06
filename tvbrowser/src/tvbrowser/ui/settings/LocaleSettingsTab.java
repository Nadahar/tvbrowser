/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourcceforge.net)
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
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Locale Settings
 */
public class LocaleSettingsTab implements devplugin.SettingsTab {
  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(LocaleSettingsTab.class);

  private JPanel mSettingsPn;

  private JComboBox mLanguageCB, mTimezoneCB;

  private JCheckBox mOSTimezoneCb;

  private JLabel mTimezoneLB;
  
  /**
   * Creates a new instance of ProxySettingsTab.
   */
  public LocaleSettingsTab() {
  }

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    mSettingsPn = new JPanel(new FormLayout("5dlu, pref, 3dlu, pref, fill:3dlu:grow, 3dlu", "pref, 5dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);
    
    CellConstraints cc = new CellConstraints();
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("title", "Locale")), cc.xyw(1,1,5));
    
    Language[] languages = new Language[] { new Language("en"), new Language("de"), new Language("sv") };
    
    mSettingsPn.add(new JLabel(mLocalizer.msg("language", "Language:")), cc.xy(2, 3));
    mSettingsPn.add(mLanguageCB = new JComboBox(languages), cc.xy(4, 3));

    String lan = Settings.propLanguage.getString();
    for (int i = 0; i < languages.length; i++) {
      if (languages[i].getId().equalsIgnoreCase(lan)) {
        mLanguageCB.setSelectedIndex(i);
        break;
      }
    }

    String[] zoneIds = TimeZone.getAvailableIDs();
    mTimezoneCB = new JComboBox(zoneIds);
    String zone = Settings.propTimezone.getString();
    if (zone == null) {
      zone = TimeZone.getDefault().getID();
    }
    for (int i = 0; i < zoneIds.length; i++) {
      if (zoneIds[i].equals(zone)) {
        mTimezoneCB.setSelectedIndex(i);
        break;
      }
    }

    mOSTimezoneCb = new JCheckBox(mLocalizer.msg("useSystemTimezone", "use timezone provided by OS"));
    mOSTimezoneCb.setSelected(Settings.propTimezone.getString() == null);
    mTimezoneLB = new JLabel(mLocalizer.msg("timezone", "Timezone:"));

    mTimezoneCB.setEnabled(!mOSTimezoneCb.isSelected());
    mTimezoneLB.setEnabled(!mOSTimezoneCb.isSelected());

    mOSTimezoneCb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mTimezoneCB.setEnabled(!mOSTimezoneCb.isSelected());
        mTimezoneLB.setEnabled(!mOSTimezoneCb.isSelected());
      }
    });

    mSettingsPn.add(mOSTimezoneCb, cc.xyw(2, 5, 4));
    mSettingsPn.add(mTimezoneLB, cc.xy(2, 7));
    mSettingsPn.add(mTimezoneCB, cc.xy(4, 7));
    
    return mSettingsPn;
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Language lan = (Language) mLanguageCB.getSelectedItem();
    Settings.propLanguage.setString(lan.getId());

    if (mOSTimezoneCb.isSelected()) {
      Settings.propTimezone.setString(null);
    } else {
      Settings.propTimezone.setString((String) mTimezoneCB.getSelectedItem());
    }
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("apps", "preferences-desktop-locale", 16);
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("title", "Locale");
  }


  class Language {
    private String mId;

    public Language(String id) {
      mId = id;
    }

    public String toString() {
      return new Locale(mId).getDisplayLanguage();
    }

    public String getId() {
      return mId;
    }
  }  
}