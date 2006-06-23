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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TimeZone;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
    
    Language[] languages = createAllLangagues();
    
    mSettingsPn.add(new JLabel(mLocalizer.msg("language", "Language:")), cc.xy(2,3));
    mSettingsPn.add(mLanguageCB = new JComboBox(languages), cc.xy(4,3));
    
    String lan = Settings.propLanguage.getString();
    String country = Settings.propCountry.getString();
    
    boolean fitWithCountry = false;
    
    for (int i = 0; i < languages.length; i++) {
      if (languages[i].getId().equalsIgnoreCase(lan)) {
        
        if ((languages[i].getCountry() != null) && (languages[i].getCountry().equalsIgnoreCase(country))) {
          mLanguageCB.setSelectedIndex(i);
          fitWithCountry = true;
        } else if (!fitWithCountry) {
          mLanguageCB.setSelectedIndex(i);
        }
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
   * Create a list of available Languages from the Jar-File.
   * 
   * It tries to find all tvbrowser/tvbrowser_*.properties and creates a List
   * of available Languages
   * 
   * If the function fails, it returns "EN", "EN-US", "DE", "SV" as Fallback
   * 
   * @return List of Languages
   */
  private Language[] createAllLangagues() {
    Language[] languages = new Language[] { new Language("en","us"), new Language("en"), new Language("de"), new Language("sv") };
    
    try {
      ArrayList<Language> langArray = new ArrayList<Language>();

      langArray.add(new Language("en"));
      
      JarFile file = new JarFile(new File("tvbrowser.jar"));
      
      Enumeration<JarEntry> entries = file.entries();
      
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name = entry.getName();
        if (name.startsWith("tvbrowser/tvbrowser_") && (name.lastIndexOf(".properties") > 0)) {
          name = name.substring(20, name.lastIndexOf(".properties"));
          
          String[] split = name.split("_");

          if (split.length >= 3) {
            Language lang = new Language(split[0], split[1], split[2]);
            langArray.add(lang);
          } else if (split.length == 2) {
            Language lang = new Language(split[0], split[1]);
            langArray.add(lang);
          } else {
            Language lang = new Language(split[0]);
            langArray.add(lang);
          }
        }
      }
      
      languages = langArray.toArray(new Language[langArray.size()]);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    Arrays.sort(languages, new Comparator<Language>() {
      public int compare(Language o1, Language o2) {
        return o1.toString().compareTo(o2.toString());
      }
    });
    
    return languages;
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Language lan = (Language) mLanguageCB.getSelectedItem();
    Settings.propLanguage.setString(lan.getId());
    Settings.propCountry.setString(lan.getCountry());
    
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
    private String mId = "";
    private String mCountry = "";
    private String mVariant = "";
    private String mName = null;
    
    public Language(String id) {
      mId = id;
    }

    public Language(String id, String country) {
      mId = id;
      mCountry = country;
    }

    public Language(String id, String country, String variant) {
      mId = id;
      mCountry = country;
      mVariant = variant;
    }

    public String toString() {
      if (mName == null) {
        mName = new Locale(mId, mCountry, mVariant).getDisplayName();
      }
      
      return mName;
    }

    public String getId() {
      return mId;
    }
    
    public String getCountry() {
      return mCountry;
    }
  }  
}