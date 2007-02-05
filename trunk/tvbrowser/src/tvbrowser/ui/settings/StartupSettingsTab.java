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

import java.io.File;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
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

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    FormLayout layout = new FormLayout(
        "5dlu, pref, 3dlu, pref, fill:3dlu:grow, 3dlu",
        "pref, 5dlu, pref, 3dlu, pref");
    mSettingsPn = new JPanel(layout);
    mSettingsPn.setBorder(Borders.DIALOG_BORDER);

    CellConstraints cc = new CellConstraints();

    mSettingsPn.add(DefaultComponentFactory.getInstance().createSeparator(
        mLocalizer.msg("title", "Startup")), cc.xyw(1, 1, 5));

    mMinimizeAfterStartUpChB = new JCheckBox(mLocalizer.msg(
        "minimizeAfterStartup", "Minimize main window after start up"),
        Settings.propMinimizeAfterStartup.getBoolean());
    mSettingsPn.add(mMinimizeAfterStartUpChB, cc.xy(2, 3));

    mShowSplashChB = new JCheckBox(mLocalizer.msg("showSplashScreen",
        "Show splash screen during start up"), Settings.propSplashShow
        .getBoolean());
    mSettingsPn.add(mShowSplashChB, cc.xy(2, 5));

    if (System.getProperty("os.name").toLowerCase().startsWith("windows") && !TVBrowser.isTransportable()) {
      layout.appendRow(new RowSpec("3dlu"));
      layout.appendRow(new RowSpec("pref"));
      
      try {
        RegistryKey shellFolders = new RegistryKey(RootKey.HKEY_CURRENT_USER, "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders");
        String path = shellFolders.getValue("Startup").getData().toString();
        
        if(path == null || path.length() < 1 || !(new File(path)).isDirectory())
          throw new Exception();
        
        mLinkFile = new File(path,"TV-Browser.url");        
        mLinkUrl = new UrlFile(mLinkFile);
          
        if(mLinkFile.exists())
          try {
            if (!mLinkUrl.getUrl().equals((new File("tvbrowser.exe")).getAbsoluteFile().toURL()))
              createLink(mLinkUrl);
          }catch(Exception linkException) {
            mLinkFile.delete();
          }

        mAutostartWithWindows = new JCheckBox(mLocalizer.msg("autostart","Start TV-Browser with Windows"),
            mLinkFile.isFile());
        
        mSettingsPn.add(mAutostartWithWindows, cc.xy(2, 7));
      } catch (Exception e) {}
    }

    return mSettingsPn;
  }

  private void createLink(UrlFile link) throws Exception {
    File tvb = new File("tvbrowser.exe");
    
    if(tvb.getAbsoluteFile().isFile()) {
      link.setUrl(tvb.toURL());
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
    return mLocalizer.msg("title", "Startup");
  }
}