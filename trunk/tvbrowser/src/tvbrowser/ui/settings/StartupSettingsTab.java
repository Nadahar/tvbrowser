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
import java.io.File;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.mainframe.PeriodItem;
import util.io.UrlFile;
import util.ui.Localizer;
import util.ui.UiUtilities;

import ca.beq.util.win32.registry.RegistryKey;
import ca.beq.util.win32.registry.RootKey;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class StartupSettingsTab implements devplugin.SettingsTab {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(StartupSettingsTab.class);

  private JPanel mSettingsPn;

  private JCheckBox mShowSplashChB, mMinimizeAfterStartUpChB,
      mAutostartWithWindows;
  
  private UrlFile mLinkUrl;
  private File mLinkFile;
  
  /* Refresh settings */
  private static final String[] AUTO_DOWNLOAD_MSG_ARR = new String[] {
    mLocalizer.msg("autoDownload.daily", "Once a day"),
    mLocalizer.msg("autoDownload.every3days", "Every three days"), mLocalizer.msg("autoDownload.weekly", "Weekly")
  };
  
  private JComboBox mAutoDownloadCombo;

  private JCheckBox mAutoDownloadCheck;

  private JComboBox mAutoDownloadPeriodCB;

  private JRadioButton mAskBeforeDownloadRadio;

  private JRadioButton mAskTimeRadio;

  private JLabel mHowOften;
  
  private JCheckBox mDateCheck;
  
  /* Close settings */
  private JCheckBox mOnlyMinimizeWhenWindowClosingChB;

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    FormLayout layout = new FormLayout(
        "5dlu, pref, 3dlu, pref, fill:3dlu:grow, 3dlu",
        "pref, 5dlu, pref, 1dlu, pref, 10dlu, pref, 10dlu, pref, 5dlu, pref");
    mSettingsPn = new JPanel(layout);
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);

    CellConstraints cc = new CellConstraints();

    int y = 1;
    
    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("title", "Startup")), cc.xyw(1, y++, 5));

    mMinimizeAfterStartUpChB = new JCheckBox(mLocalizer.msg(
        "minimizeAfterStartup", "Minimize main window after start up"),
        Settings.propMinimizeAfterStartup.getBoolean());
    mSettingsPn.add(mMinimizeAfterStartUpChB, cc.xy(2, ++y));

    y++;
    
    mShowSplashChB = new JCheckBox(mLocalizer.msg("showSplashScreen",
        "Show splash screen during start up"), Settings.propSplashShow
        .getBoolean());
    mSettingsPn.add(mShowSplashChB, cc.xy(2, ++y));

    if (System.getProperty("os.name").toLowerCase().startsWith("windows") && !TVBrowser.isTransportable()) {
      layout.insertRow(++y,new RowSpec("1dlu"));
      layout.insertRow(++y,new RowSpec("pref"));
      
      try {
        RegistryKey shellFolders = new RegistryKey(RootKey.HKEY_CURRENT_USER, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders");
        String path = shellFolders.getValue("Startup").getData().toString();
        
        if(path == null || path.length() < 1 || !(new File(path)).isDirectory())
          throw new Exception();
        
        mLinkFile = new File(path,"TV-Browser.url");        
        mLinkUrl = new UrlFile(mLinkFile);
          
        if(mLinkFile.exists())
          try {
            if (!mLinkUrl.getUrl().equals((new File("tvbrowser.exe")).getAbsoluteFile().toURI().toURL()))
              createLink(mLinkUrl);
          }catch(Exception linkException) {
            mLinkFile.delete();
          }

        mAutostartWithWindows = new JCheckBox(mLocalizer.msg("autostart","Start TV-Browser with Windows"),
            mLinkFile.isFile());
        
        
        mSettingsPn.add(mAutostartWithWindows, cc.xy(2, y));
      } catch (Throwable e) {}
    }

    y++;
    
    mSettingsPn.add(createRefreshPanel(), cc.xyw(1,++y,5));
    
    y++;
    
    String msg = mLocalizer.msg("onlyMinimizeWhenWindowClosing",
    "When closing the main window only minimize TV-Browser, don't quit.");
    
    mOnlyMinimizeWhenWindowClosingChB = new JCheckBox(msg, Settings.propOnlyMinimizeWhenWindowClosing.getBoolean());     

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("closing","Closing")), cc.xyw(1,++y,5));
    
    y++;
    
    mSettingsPn.add(mOnlyMinimizeWhenWindowClosingChB, cc.xyw(2,++y,4));
    
    return mSettingsPn;
  }

  private void createLink(UrlFile link) throws Exception {
    File tvb = new File("tvbrowser.exe");
    
    if(tvb.getAbsoluteFile().isFile()) {
      link.setUrl(tvb.toURI().toURL());
      link.setIconFile(tvb.getAbsoluteFile().getParent() + "\\imgs\\desktop.ico");
      link.setIconIndex(0);
      link.setWorkingDirectory(tvb.getAbsoluteFile().getParent());
      link.setShowCommand(UrlFile.SHOWCOMMAND_MAXIMIZED);
      link.save();
    }
  }
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Settings.propMinimizeAfterStartup.setBoolean(mMinimizeAfterStartUpChB
        .isSelected());
    Settings.propSplashShow.setBoolean(mShowSplashChB.isSelected());
    
    if(mAutostartWithWindows != null) {        
        if (mAutostartWithWindows.isSelected()) {
          if(!mLinkFile.isFile()) {
            try {
              createLink(mLinkUrl);
            } catch (Exception createLink) {}

            if (!mLinkFile.isFile()) {
              mAutostartWithWindows.setSelected(false);
              JOptionPane.showMessageDialog(
                  UiUtilities.getLastModalChildOf(MainFrame.getInstance()),
                  mLocalizer.msg("creationError","Couldn't create autostart shortcut.\nMaybe your have not the right to write in the autostart directory."),
                  Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.ERROR_MESSAGE);
            }
          }
        } else if (mLinkFile.isFile() && !mLinkFile.delete()) {
            mAutostartWithWindows.setSelected(true);
            JOptionPane.showMessageDialog(
                UiUtilities.getLastModalChildOf(MainFrame.getInstance()),
                mLocalizer.msg("deletionError","Couldn't delete autostart shortcut.\nMaybe your have not the right to write in the autostart directory."),
                mLocalizer.msg("error","Error"), JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /* Refresh settings*/
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
    
    /* Close settings */
    if (mOnlyMinimizeWhenWindowClosingChB != null) {
      boolean checked = mOnlyMinimizeWhenWindowClosingChB.isSelected();
      Settings.propOnlyMinimizeWhenWindowClosing.setBoolean(checked);
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
    return mLocalizer.msg("general", "General");
  }
  
  private JPanel createRefreshPanel() {
    JPanel refreshSettings = new JPanel(new FormLayout("5dlu, pref, 3dlu, pref, fill:3dlu:grow, 3dlu",
    "pref, 5dlu, pref, 3dlu, pref, 5dlu, pref"));
    
    CellConstraints cc = new CellConstraints();
    
    refreshSettings.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("titleRefresh", "Startup")), cc.xyw(
        1, 1, 5));
    
    mAutoDownloadCheck = new JCheckBox(mLocalizer.msg("onStartUp", "On startup"));
    
    refreshSettings.add(mAutoDownloadCheck, cc.xy(2, 3));
    
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
    
    refreshSettings.add(panel, cc.xy(2, 5));
    
    setAutoDownloadEnabled(mAutoDownloadCheck.isSelected());
    
    mDateCheck = new JCheckBox(mLocalizer.msg("checkDate", "Check date via NTP if data download fails"));
    mDateCheck.setSelected(Settings.propNTPTimeCheck.getBoolean());

    refreshSettings.add(mDateCheck, cc.xy(2, 7));
    
    return refreshSettings;
  }
  
  private void setAutoDownloadEnabled(boolean enabled) {
    mAskBeforeDownloadRadio.setEnabled(enabled);

    mHowOften.setEnabled(enabled);
    mAutoDownloadCombo.setEnabled(enabled);
    mAskTimeRadio.setEnabled(enabled);

    enabled = !(mAskBeforeDownloadRadio.isSelected() || !enabled);

    mAutoDownloadPeriodCB.setEnabled(enabled);
  }
}