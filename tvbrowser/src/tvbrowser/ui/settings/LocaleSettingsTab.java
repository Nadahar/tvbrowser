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
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

import tvbrowser.core.PluginLoader;
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
  
  private JTextArea mInfoArea;
  
  private static boolean mSomethingChanged = false;
  
  private static int mStartLanguageIndex;
  private static int mStartTimeZoneIndex;
  private static boolean mTwelveHourFormatIsSelected;
  
  /**
   * Creates a new instance of ProxySettingsTab.
   */
  public LocaleSettingsTab() {
  }

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    mSettingsPn = new JPanel(new FormLayout("5dlu, pref, 3dlu, default, fill:3dlu:grow, 3dlu", 
        "pref, 5dlu, pref, 10dlu, pref, 5dlu, pref, 10dlu, pref, 5dlu, pref, 2dlu, pref, fill:3dlu:grow, pref"));
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);
    
    CellConstraints cc = new CellConstraints();
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("titleLanguage", "Locale")), cc.xyw(1,1,5));
    
    mSettingsPn.add(new JLabel(mLocalizer.msg("language", "Language:")), cc.xy(2,3));
    mSettingsPn.add(mLanguageCB = new JComboBox(mLocalizer.getAllAvailableLocales()), cc.xy(4,3));
    
    mLanguageCB.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent(list, ((Locale)value).getDisplayName(), index, isSelected, cellHasFocus);
      }
    });
    
    String lan = Settings.propLanguage.getString();
    String country = Settings.propCountry.getString();
    String variant = Settings.propVariant.getString();
    
    Locale loc = new Locale(lan, country, variant);
    mLanguageCB.setSelectedItem(loc);
    
    // time zone data may not be accessible, therefore use try-catch everywhere
    String[] zoneIds = new String[0];
    try {
      zoneIds = TimeZone.getAvailableIDs();
      Arrays.sort(zoneIds);
    } catch (Exception e) {
      e.printStackTrace();
    }
    mTimezoneCB = new JComboBox(zoneIds);
    String zone = Settings.propTimezone.getString();
    if (zone == null) {
      try {
        zone = TimeZone.getDefault().getID();
      } catch (Exception e) {
        e.printStackTrace();
      }
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
    
    mInfoArea = UiUtilities.createHelpTextArea(mLocalizer.msg("restartNote", "Please Restart"));
    mInfoArea.setForeground(Color.RED);
    mInfoArea.setVisible(mSomethingChanged);
    
    if(!mSomethingChanged) {
      mStartLanguageIndex = mLanguageCB.getSelectedIndex();
      mStartTimeZoneIndex = mTimezoneCB.getSelectedIndex();
      mTwelveHourFormatIsSelected = mTwelveHourFormat.isSelected();
    }    
    
    ItemListener itemListener= new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mInfoArea.setVisible(mLanguageCB.getSelectedIndex() != mStartLanguageIndex ||
            mTimezoneCB.getSelectedIndex() != mStartTimeZoneIndex ||
            (mTwelveHourFormatIsSelected && !mTwelveHourFormat.isSelected() || 
                !mTwelveHourFormatIsSelected && !mTwentyfourHourFormat.isSelected()));
      }
    };

    mLanguageCB.addItemListener(itemListener);
    mTimezoneCB.addItemListener(itemListener);
    mTwelveHourFormat.addItemListener(itemListener);
    mTwentyfourHourFormat.addItemListener(itemListener);
    
    mSettingsPn.add(mInfoArea, cc.xyw(1, 15, 5));
    
    return mSettingsPn;
  }


  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Locale loc = (Locale) mLanguageCB.getSelectedItem();
    
    Settings.propLanguage.setString(loc.getLanguage());
    Settings.propCountry.setString(loc.getCountry());
    Settings.propVariant.setString(loc.getVariant());
    
    try {
      Settings.propTimezone.setString((String) mTimezoneCB.getSelectedItem());
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    Settings.propTwelveHourFormat.setBoolean(mTwelveHourFormat.isSelected());
    
    mSomethingChanged = mInfoArea.isVisible();
    
    // remove all plugin proxies as their cached plugin description needs to adapt to the new locale
    if (mSomethingChanged) {
      PluginLoader.getInstance().deleteAllPluginProxies();
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
    return mLocalizer.msg("locale", "Locale");
  }

}