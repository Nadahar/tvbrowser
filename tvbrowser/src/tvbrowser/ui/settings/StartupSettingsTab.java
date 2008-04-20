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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

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

  private JCheckBox mShowSplashChB, mMinimizeAfterStartUpChB, mStartFullscreen,
      mAutostartWithWindows;
  
  private File mLinkFileFile;
  private LinkFile mLinkFile;
  
  /* Refresh settings */
  private static final String[] AUTO_DOWNLOAD_MSG_ARR = new String[] {
    mLocalizer.msg("autoDownload.daily", "Once a day"),
    mLocalizer.msg("autoDownload.every3days", "Every three days"), mLocalizer.msg("autoDownload.weekly", "Weekly")
  };

  private JRadioButton mNoAutoDownload;
  private JRadioButton mStartDownload;
  private JRadioButton mAutoDownload;
  
  private JComboBox mAutoDownloadCombo;

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
        "pref, 5dlu, pref, 1dlu, pref, 1dlu, pref, 10dlu, pref, 10dlu, pref, 5dlu, pref");
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
    
    mStartFullscreen = new JCheckBox(mLocalizer.msg(
        "startFullscreen","Start in fullscreen mode"),
        Settings.propIsUsingFullscreen.getBoolean());
    mSettingsPn.add(mStartFullscreen, cc.xy(2,++y));
    
    mMinimizeAfterStartUpChB.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          mStartFullscreen.setSelected(false);
        }
      }
    });

    mStartFullscreen.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED) {
          mMinimizeAfterStartUpChB.setSelected(false);
        }
      }
    });
    
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
        
        mLinkFileFile = new File(path,"TV-Browser.url");
        
        try {
          mLinkFile = new LinkFile(mLinkFileFile);
        
          if(mLinkFileFile.isFile()) {
            try {
              if (!mLinkFile.hasTarget((new File("tvbrowser.exe")).getAbsoluteFile())) {
                createLink(mLinkFile);
              }
            }catch(Exception linkException) {
              mLinkFileFile.delete();
            }
          }
        }catch(FileNotFoundException fe) {}

        mAutostartWithWindows = new JCheckBox(mLocalizer.msg("autostart","Start TV-Browser with Windows"),
            mLinkFileFile.isFile());
        
        mSettingsPn.add(mAutostartWithWindows, cc.xy(2, y));
      } catch (Throwable e) {e.printStackTrace();}
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

  private void createLink(LinkFile link) throws Exception {
    File tvb = new File("tvbrowser.exe");
    
    if(tvb.getAbsoluteFile().isFile()) {
      mLinkFile = new LinkFile(mLinkFileFile, tvb, new File(tvb.getAbsoluteFile().getParent() + "\\imgs\\desktop.ico"),0);
    }
  }
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    Settings.propMinimizeAfterStartup.setBoolean(mMinimizeAfterStartUpChB
        .isSelected());
    Settings.propSplashShow.setBoolean(mShowSplashChB.isSelected());
    Settings.propIsUsingFullscreen.setBoolean(mStartFullscreen.isSelected());
    
    if(mAutostartWithWindows != null) {        
        if (mAutostartWithWindows.isSelected()) {
          if(!mLinkFileFile.isFile()) {
            try {
              createLink(mLinkFile);
            } catch (Exception createLink) {}

            if (!mLinkFileFile.isFile()) {
              mAutostartWithWindows.setSelected(false);
              JOptionPane.showMessageDialog(
                  UiUtilities.getLastModalChildOf(MainFrame.getInstance()),
                  mLocalizer.msg("creationError","Couldn't create autostart shortcut.\nMaybe your have not the right to write in the autostart directory."),
                  Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.ERROR_MESSAGE);
            }
          }
        } else if (mLinkFileFile.isFile() && !mLinkFileFile.delete()) {
            mAutostartWithWindows.setSelected(true);
            JOptionPane.showMessageDialog(
                UiUtilities.getLastModalChildOf(MainFrame.getInstance()),
                mLocalizer.msg("deletionError","Couldn't delete autostart shortcut.\nMaybe your have not the right to write in the autostart directory."),
                mLocalizer.msg("error","Error"), JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /* Refresh settings*/
    int inx = mAutoDownloadCombo.getSelectedIndex();

    if (mNoAutoDownload.isSelected()) {
      Settings.propAutoDownloadType.setString("never");
    } else if (inx == 0) {
      Settings.propAutoDownloadType.setString("daily");
    } else if (inx == 1) {
      Settings.propAutoDownloadType.setString("every3days");
    } else if (inx == 2) {
      Settings.propAutoDownloadType.setString("weekly");
    }

    Settings.propAutoDataDownloadEnabled.setBoolean(mAutoDownload.isSelected());
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
    "pref, 5dlu, pref, pref, pref, 3dlu, pref, 5dlu, pref"));
    
    CellConstraints cc = new CellConstraints();
    
    refreshSettings.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("titleRefresh", "Startup")), cc.xyw(
        1, 1, 5));
    
    mNoAutoDownload = new JRadioButton(mLocalizer.msg("never","Never update TV listings automatically"));
    mStartDownload = new JRadioButton(mLocalizer.msg("onStartUp", "Update TV listings only on start up of TV-Browser"));
    mAutoDownload = new JRadioButton(mLocalizer.msg("autoUpdate","Update TV listings automatically"));
    
    ButtonGroup bg = new ButtonGroup();
    
    bg.add(mNoAutoDownload);
    bg.add(mStartDownload);
    bg.add(mAutoDownload);
    
    refreshSettings.add(mNoAutoDownload, cc.xy(2, 3));
    refreshSettings.add(mStartDownload, cc.xy(2, 4));
    refreshSettings.add(mAutoDownload, cc.xy(2, 5));
    
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
    
    mStartDownload.setSelected(!dlType.equals("never") && !Settings.propAutoDataDownloadEnabled.getBoolean());
    mAutoDownload.setSelected(Settings.propAutoDataDownloadEnabled.getBoolean());
    mNoAutoDownload.setSelected(!mStartDownload.isSelected() && !mAutoDownload.isSelected());
    
    mHowOften = new JLabel(mLocalizer.msg("autoDownload.howOften", "How often?"));
    panel.add(mHowOften, cc.xy(2, 1));
    panel.add(mAutoDownloadCombo, cc.xy(4, 1));
    
    mAskBeforeDownloadRadio = new JRadioButton(mLocalizer.msg("autoDownload.ask", "Ask before downloading"));
    mAutoDownloadPeriodCB = new JComboBox(PeriodItem.PERIOD_ARR);
    
    int autoDLPeriod = Settings.propAutoDownloadPeriod.getInt();
    PeriodItem pi = new PeriodItem(autoDLPeriod);
    mAutoDownloadPeriodCB.setSelectedItem(pi);
    
    mNoAutoDownload.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setAutoDownloadEnabled(mStartDownload.isSelected() || mAutoDownload.isSelected());
      }
    });
    
    mStartDownload.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setAutoDownloadEnabled(mStartDownload.isSelected() || mAutoDownload.isSelected());
      }
    });

    mAutoDownload.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setAutoDownloadEnabled(mStartDownload.isSelected() || mAutoDownload.isSelected());
      }
    });
    
    mAskBeforeDownloadRadio.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setAutoDownloadEnabled(mStartDownload.isSelected() || mAutoDownload.isSelected());
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
    
    refreshSettings.add(panel, cc.xy(2, 7));
    
    setAutoDownloadEnabled(mStartDownload.isSelected() || mAutoDownload.isSelected());
    
    mDateCheck = new JCheckBox(mLocalizer.msg("checkDate", "Check date via NTP if data download fails"));
    mDateCheck.setSelected(Settings.propNTPTimeCheck.getBoolean());

    refreshSettings.add(mDateCheck, cc.xy(2, 9));
    
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
  
  /**
   * Used to create autostart link for Windows.
   * 
   * @author René Mach
   */
  private static class LinkFile {
    private String mTarget;
    
    private LinkFile(File linkFile, File target, File icon, int iconIndex) throws IOException {
      RandomAccessFile write = new RandomAccessFile(linkFile, "rw");
      
      write.getChannel().truncate(0);
      
      write.writeBytes("[InternetShortcut]\r\n");
      write.writeBytes("URL=" + target.getAbsoluteFile().toURI().toURL() + "\r\n");
      write.writeBytes("WorkingDirectory=" + target.getParent());
      
      if(icon != null && icon.isFile()) {
        write.writeBytes("\r\nIconFile=" + icon.getAbsolutePath() + "\r\n");
        write.writeBytes("IconIndex=" + iconIndex);
      }
      
      write.close();
    }
    
    /**
     * @param linkFile The file the link is stored in.
     * @throws IOException Thrown if something went wrong.
     */
    public LinkFile(File linkFile) throws IOException {
      RandomAccessFile read = new RandomAccessFile(linkFile,"r");
      
      String line = null;
      
      while((line = read.readLine()) != null) {
        if(line.startsWith("URL")) {
          mTarget = line.substring(line.indexOf(":/")+2);
        }
      }
      
      read.close();
    }
    
    /**
     * If the link target equals the given file.
     * 
     * @param file The file to check the target for.
     * @return <code>True</code> if the target matches the link of the file.
     */
    public boolean hasTarget(File file) {
      return new File(mTarget).equals(file);
    }
  }
}