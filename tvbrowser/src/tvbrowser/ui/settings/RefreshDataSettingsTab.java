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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.ui.mainframe.PeriodItem;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class RefreshDataSettingsTab implements devplugin.SettingsTab {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(RefreshDataSettingsTab.class);

  private static final String[] AUTO_DOWNLOAD_MSG_ARR = new String[] {
      mLocalizer.msg("autoDownload.daily", "Once a day"),
      mLocalizer.msg("autoDownload.every3days", "Every three days"), mLocalizer.msg("autoDownload.weekly", "Weekly")

  };

  private JPanel mSettingsPn;

  private JComboBox mAutoDownloadCombo;

  private JCheckBox mAutoDownloadCheck;

  private JComboBox mAutoDownloadPeriodCB;

  private JRadioButton mAskBeforeDownloadRadio;

  private JRadioButton mAskTimeRadio;

  private JLabel mHowOften;

  private JCheckBox mDateCheck;

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    mSettingsPn = new JPanel(new FormLayout("5dlu, pref, 3dlu, pref, fill:3dlu:grow, 3dlu",
        "pref, 5dlu, pref, 3dlu, pref, 5dlu, pref"));
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);

    CellConstraints cc = new CellConstraints();

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("title", "Startup")), cc.xyw(
        1, 1, 5));

    mAutoDownloadCheck = new JCheckBox(mLocalizer.msg("onStartUp", "On startup"));

    mSettingsPn.add(mAutoDownloadCheck, cc.xy(2, 3));

    mAutoDownloadCombo = new JComboBox(AUTO_DOWNLOAD_MSG_ARR);
    String dlType = Settings.propAutoDownloadType.getString();
    if (dlType.equals("daily")) {
      mAutoDownloadCombo.setSelectedIndex(0);
    } else if (dlType.equals("every3days")) {
      mAutoDownloadCombo.setSelectedIndex(1);
    } else if (dlType.equals("weekly")) {
      mAutoDownloadCombo.setSelectedIndex(2);
    }

    JPanel panel = new JPanel(new FormLayout("10dlu, pref, 3dlu, pref", "pref, 3dlu, pref, 3dlu, pref"));

    mAutoDownloadCheck.setSelected(!dlType.equals("never"));

    mHowOften = new JLabel(mLocalizer.msg("autoDownload.howOften", "How often?"));
    panel.add(mHowOften, cc.xy(2, 1));
    panel.add(mAutoDownloadCombo, cc.xy(4, 1));

    mAskBeforeDownloadRadio = new JRadioButton(mLocalizer.msg("autoDownload.ask", "Ask before downloading"));
    mAutoDownloadPeriodCB = new JComboBox(PeriodItem.PERIOD_ARR);

    int autoDLPeriod = Settings.propAutoDownloadPeriod.getInt();
    PeriodItem pi = new PeriodItem(autoDLPeriod);
    mAutoDownloadPeriodCB.setSelectedItem(pi);

    mAutoDownloadCheck.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setAutoDownloadEnabled(mAutoDownloadCheck.isSelected());
      }
    });

    mAskBeforeDownloadRadio.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setAutoDownloadEnabled(mAutoDownloadCheck.isSelected());
      };
    });

    panel.add(mAskBeforeDownloadRadio, cc.xyw(2, 3, 3));

    mAskTimeRadio = new JRadioButton(mLocalizer.msg("autoDownload.duration", "Automatically refresh for"));
    panel.add(mAskTimeRadio, cc.xy(2, 5));
    panel.add(mAutoDownloadPeriodCB, cc.xy(4, 5));

    mAskTimeRadio.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setAutoDownloadEnabled(mAskTimeRadio.isSelected());
      };
    });

    ButtonGroup group = new ButtonGroup();
    group.add(mAskBeforeDownloadRadio);
    group.add(mAskTimeRadio);

    mAskBeforeDownloadRadio.setSelected(Settings.propAskForAutoDownload.getBoolean());
    mAskTimeRadio.setSelected(!Settings.propAskForAutoDownload.getBoolean());

    mSettingsPn.add(panel, cc.xy(2, 5));

    setAutoDownloadEnabled(mAutoDownloadCheck.isSelected());

    mDateCheck = new JCheckBox(mLocalizer.msg("checkDate", "Check date via NTP if data download fails"));
    mDateCheck.setSelected(Settings.propNTPTimeCheck.getBoolean());
    mSettingsPn.add(mDateCheck, cc.xy(2, 7));

    return mSettingsPn;
  }

  public void setAutoDownloadEnabled(boolean enabled) {
    mAskBeforeDownloadRadio.setEnabled(enabled);

    mHowOften.setEnabled(enabled);
    mAutoDownloadCombo.setEnabled(enabled);
    mAskTimeRadio.setEnabled(enabled);

    enabled = !(mAskBeforeDownloadRadio.isSelected() || !enabled);

    mAutoDownloadPeriodCB.setEnabled(enabled);
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {

    int inx = mAutoDownloadCombo.getSelectedIndex();

    if (!mAutoDownloadCheck.isSelected()) {
      Settings.propAutoDownloadType.setString("never");
    } else if (inx == 0) {
      Settings.propAutoDownloadType.setString("daily");
    } else if (inx == 1) {
      Settings.propAutoDownloadType.setString("every3days");
    } else if (inx == 2) {
      Settings.propAutoDownloadType.setString("weekly");
    }

    Settings.propAskForAutoDownload.setBoolean(mAskBeforeDownloadRadio.isSelected());

    PeriodItem periodItem = (PeriodItem) mAutoDownloadPeriodCB.getSelectedItem();
    Settings.propAutoDownloadPeriod.setInt(periodItem.getDays());
    
    Settings.propNTPTimeCheck.setBoolean(mDateCheck.isSelected());
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("apps", "system-software-update", 16);
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("title", "Refresh");
  }
}
