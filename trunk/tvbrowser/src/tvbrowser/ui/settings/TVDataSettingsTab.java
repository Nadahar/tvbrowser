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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.PeriodItem;
import util.ui.TabLayout;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class TVDataSettingsTab implements devplugin.SettingsTab {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TVDataSettingsTab.class);

  private static final String[] AUTO_DOWNLOAD_MSG_ARR = new String[] {
      mLocalizer.msg("autoDownload.daily", "Once a day"),
      mLocalizer.msg("autoDownload.every3days", "Every three days"), mLocalizer.msg("autoDownload.weekly", "Weekly")

  };

  private JPanel mSettingsPn;

  private JComboBox mAutoDownloadCB;

  private JCheckBox mAutoDownloadCb;

  private JComboBox mAutoDownloadPeriodCB;

  private JRadioButton mDonotAskBeforeDownloadRB;

  private JRadioButton mAskBeforeDownloadRB;

  private JComboBox mLanguageCB, mTimezoneCB;

  private JCheckBox mOSTimezoneCb;

  private JCheckBox mShowSplashChB, mMinimizeAfterStartUpChB;

  public TVDataSettingsTab() {
  }

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    mSettingsPn = new JPanel(new BorderLayout());
    mSettingsPn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

    JPanel onStartupPn = new JPanel();
    onStartupPn.setLayout(new BoxLayout(onStartupPn, BoxLayout.Y_AXIS));
    onStartupPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("autoDownload", "Download automatically")));

    JPanel autoDownloadPn = new JPanel(new GridLayout(0, 2, 0, 7));

    onStartupPn.add(autoDownloadPn);

    mSettingsPn.add(content, BorderLayout.NORTH);

    mAutoDownloadCb = new JCheckBox(mLocalizer.msg("onStartUp", "On startup"));
    autoDownloadPn.add(mAutoDownloadCb);

    mAutoDownloadCB = new JComboBox(AUTO_DOWNLOAD_MSG_ARR);
    String dlType = Settings.propAutoDownloadType.getString();
    if (dlType.equals("daily")) {
      mAutoDownloadCB.setSelectedIndex(0);
    } else if (dlType.equals("every3days")) {
      mAutoDownloadCB.setSelectedIndex(1);
    } else if (dlType.equals("weekly")) {
      mAutoDownloadCB.setSelectedIndex(2);
    }

    mAutoDownloadCb.setSelected(!dlType.equals("never"));

    autoDownloadPn.add(mAutoDownloadCB);

    JPanel askBeforeDLPanel = new JPanel();
    askBeforeDLPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

    askBeforeDLPanel.setLayout(new BoxLayout(askBeforeDLPanel, BoxLayout.Y_AXIS));

    mAskBeforeDownloadRB = new JRadioButton(mLocalizer.msg("autoDownload.ask", "Ask before downloading"));
    mDonotAskBeforeDownloadRB = new JRadioButton(mLocalizer.msg("autoDownload.dontask", "Don't ask, download for"));

    ButtonGroup buttonGroup = new ButtonGroup();
    buttonGroup.add(mAskBeforeDownloadRB);
    buttonGroup.add(mDonotAskBeforeDownloadRB);

    if (Settings.propAskForAutoDownload.getBoolean()) {
      mAskBeforeDownloadRB.setSelected(true);
    } else {
      mDonotAskBeforeDownloadRB.setSelected(true);
    }

    JPanel pn1 = new JPanel(new BorderLayout());
    JPanel pn2 = new JPanel(new BorderLayout());
    JPanel pn3 = new JPanel(new BorderLayout());

    pn1.add(mAskBeforeDownloadRB, BorderLayout.WEST);

    pn2.add(mDonotAskBeforeDownloadRB, BorderLayout.WEST);
    pn2.add(pn3, BorderLayout.CENTER);
    mAutoDownloadPeriodCB = new JComboBox(PeriodItem.PERIOD_ARR);
    pn3.add(mAutoDownloadPeriodCB, BorderLayout.WEST);

    int autoDLPeriod = Settings.propAutoDownloadPeriod.getInt();
    PeriodItem pi = new PeriodItem(autoDLPeriod);
    mAutoDownloadPeriodCB.setSelectedItem(pi);

    askBeforeDLPanel.add(pn1);
    askBeforeDLPanel.add(pn2);

    onStartupPn.add(askBeforeDLPanel);

    mAutoDownloadCb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setAutoDownloadEnabled(mAutoDownloadCb.isSelected());
      }
    });

    setAutoDownloadEnabled(mAutoDownloadCb.isSelected());

    content.add(onStartupPn);

    JPanel localePn = new JPanel(new BorderLayout());
    localePn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("locale", "Locale")));
    FormLayout formLayout = new FormLayout("default, 6dlu, default", "default,3dlu,default,3dlu,default,1dlu,default");
    JPanel formPn = new JPanel(formLayout);
    localePn.add(formPn, BorderLayout.CENTER);
    CellConstraints c = new CellConstraints();

    Language[] languages = new Language[] { new Language("en"), new Language("de"), new Language("sv") };
    formPn.add(new JLabel(mLocalizer.msg("language", "Language:")), c.xy(1, 1));
    formPn.add(mLanguageCB = new JComboBox(languages), c.xy(3, 1));

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
    for (int i = 0; i < zoneIds.length; i++) {
      if (zoneIds[i].equals(zone)) {
        mTimezoneCB.setSelectedIndex(i);
        break;
      }
    }

    mOSTimezoneCb = new JCheckBox(mLocalizer.msg("useSystemTimezone", "use timezone provided by OS"));
    mOSTimezoneCb.setSelected(Settings.propTimezone.getString() == null);
    mTimezoneCB.setEnabled(!mOSTimezoneCb.isSelected());

    mOSTimezoneCb.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mTimezoneCB.setEnabled(!mOSTimezoneCb.isSelected());
      }
    });

    formPn.add(mOSTimezoneCb, c.xyw(1, 3, 3));
    formPn.add(new JLabel(mLocalizer.msg("timezone", "Timezone:")), c.xy(1, 5));
    formPn.add(mTimezoneCB, c.xy(3, 5));
    JLabel lb = new JLabel(mLocalizer.msg("restartNote",
        "Diese Einstellungen werden erst nach dem Neustart von TV-Browser wirksam."));
    lb.setFont(new Font("Dialog", Font.PLAIN, 9));
    localePn.add(lb, BorderLayout.SOUTH);
    content.add(localePn);

    JPanel morePn = new JPanel(new TabLayout(1));
    morePn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("more", "More")));
    content.add(morePn);

    boolean checked = Settings.propSplashShow.getBoolean();
    mShowSplashChB = new JCheckBox(mLocalizer.msg("showSplashScreen", "Show splash screen during start up"), checked);
    morePn.add(mShowSplashChB);

    checked = Settings.propMinimizeAfterStartup.getBoolean();
    mMinimizeAfterStartUpChB = new JCheckBox(mLocalizer.msg("minimizeAfterStartup",
        "Minimize main window after start up"), checked);
    morePn.add(mMinimizeAfterStartUpChB);

    return mSettingsPn;
  }

  public void setAutoDownloadEnabled(boolean enabled) {
    mAskBeforeDownloadRB.setEnabled(enabled);
    mDonotAskBeforeDownloadRB.setEnabled(enabled);
    mAutoDownloadCB.setEnabled(enabled);
    mAutoDownloadPeriodCB.setEnabled(enabled);
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {

    int inx = mAutoDownloadCB.getSelectedIndex();

    if (!mAutoDownloadCb.isSelected()) {
      Settings.propAutoDownloadType.setString("never");
    } else if (inx == 0) {
      Settings.propAutoDownloadType.setString("daily");
    } else if (inx == 1) {
      Settings.propAutoDownloadType.setString("every3days");
    } else if (inx == 2) {
      Settings.propAutoDownloadType.setString("weekly");
    }

    Settings.propAskForAutoDownload.setBoolean(mAskBeforeDownloadRB.isSelected());

    PeriodItem periodItem = (PeriodItem) mAutoDownloadPeriodCB.getSelectedItem();
    Settings.propAutoDownloadPeriod.setInt(periodItem.getDays());

    Language lan = (Language) mLanguageCB.getSelectedItem();
    Settings.propLanguage.setString(lan.getId());

    if (mOSTimezoneCb.isSelected()) {
      Settings.propTimezone.setString(null);
    } else {
      Settings.propTimezone.setString((String) mTimezoneCB.getSelectedItem());
    }

    Settings.propMinimizeAfterStartup.setBoolean(mMinimizeAfterStartUpChB.isSelected());
    Settings.propSplashShow.setBoolean(mShowSplashChB.isSelected());
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
    return mLocalizer.msg("others", "others");
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
