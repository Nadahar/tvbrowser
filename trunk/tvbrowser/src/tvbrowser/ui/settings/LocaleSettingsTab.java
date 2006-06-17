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

import java.awt.Color;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import util.ui.UiUtilities;

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

  private JLabel mTimezoneLB;

  private JRadioButton mTwelveHourFormat;

  private JRadioButton mTwentyfourHourFormat;
  
  /**
   * Creates a new instance of ProxySettingsTab.
   */
  public LocaleSettingsTab() {
  }

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    mSettingsPn = new JPanel(new FormLayout("5dlu, pref, 3dlu, pref, fill:3dlu:grow, 3dlu", 
        "pref, 5dlu, pref, 3dlu, pref, 5dlu, pref, 3dlu, pref, 5dlu, pref, 3dlu, pref, fill:3dlu:grow, pref"));
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);
    
    CellConstraints cc = new CellConstraints();
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("titleLanguage", "Locale")), cc.xyw(1,1,5));
    
    Language[] languages = new Language[] { new Language("en"), new Language("de"), new Language("sv") };
    
    mSettingsPn.add(new JLabel(mLocalizer.msg("language", "Language:")), cc.xy(2,3));
    mSettingsPn.add(mLanguageCB = new JComboBox(languages), cc.xy(4,3));
    
    String lan = Settings.propLanguage.getString();
    for (int i = 0; i < languages.length; i++) {
      if (languages[i].getId().equalsIgnoreCase(lan)) {
        mLanguageCB.setSelectedIndex(i);
        break;
      }
    }

    String[] zoneIds = TimeZone.getAvailableIDs();
    Arrays.sort(zoneIds);
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

    mTimezoneLB = new JLabel(mLocalizer.msg("timezone", "Timezone:"));

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("titleTimezone", "Locale")), cc.xyw(1,5,5));
    
    mSettingsPn.add(mTimezoneLB, cc.xy(2,7));
    mSettingsPn.add(mTimezoneCB, cc.xy(4,7));

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("titleTimeFormat", "Time format")), cc.xyw(1,9,5));
    
    mSettingsPn.add(new JLabel(mLocalizer.msg("timeFormat", "Time format:")), cc.xy(2,11));
    
    mTwentyfourHourFormat = new JRadioButton(mLocalizer.msg("twentyFour", "24 hour format"));
    mTwelveHourFormat = new JRadioButton(mLocalizer.msg("twelve", "12 hour format"));
    ButtonGroup group = new ButtonGroup();
    group.add(mTwentyfourHourFormat);
    group.add(mTwelveHourFormat);
    
    mSettingsPn.add(mTwentyfourHourFormat, cc.xy(4, 11));
    mSettingsPn.add(mTwelveHourFormat, cc.xy(4, 13));
    
    if (Settings.propTwelveHourFormat.getBoolean()) {
      mTwelveHourFormat.setSelected(true);
    } else {
      mTwentyfourHourFormat.setSelected(true);
    }
    
    JTextArea area = UiUtilities.createHelpTextArea(mLocalizer.msg("restartNote", "Please Restart"));
    area.setForeground(Color.RED);
    
    mSettingsPn.add(area, cc.xyw(1, 15, 5));
    
    return mSettingsPn;
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Language lan = (Language) mLanguageCB.getSelectedItem();
    Settings.propLanguage.setString(lan.getId());

    if (mTimezoneCB.getSelectedItem().equals(TimeZone.getDefault().getID())) {
      Settings.propTimezone.setString(null);
    } else {
      Settings.propTimezone.setString((String) mTimezoneCB.getSelectedItem());
    }
    
    Settings.propTwelveHourFormat.setBoolean(mTwelveHourFormat.isSelected());
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
    return mLocalizer.msg("locale", "Locale");
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