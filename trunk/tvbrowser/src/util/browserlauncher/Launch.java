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
package util.browserlauncher;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.io.ExecutionHandler;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsItem;

/**
 * This Class opens an Url in a Web-Browser.
 * 
 * If the Web-Browser was not found or a problem occurred, the User is asked to
 * enter his Web-Browser in the Configuration.
 */
public class Launch {
  /** The localizer used by this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(Launch.class);

  /** Mac OS JVM */
  public static final int OS_MAC = 0;
  /** Windows JVM */
  public static final int OS_WINDOWS = 1;
  /** Other OS JVM */
  public static final int OS_OTHER = 2;
  /** Linux OS JVM*/
  public static final int OS_LINUX = 3;
  
  /**
   * Opens an URL in a web-browser
   * @param url Url to open
   */
  public static void openURL(String url) {
    String browserExecutable = Settings.propUserDefinedWebbrowser.getString();
    try {
      if (browserExecutable != null) {
        // Test if the JVM is a Mac-VM and the Application is an .app-File.
        // These Files must be launched differently
        if ((getOs() == OS_MAC) && (browserExecutable.trim().toLowerCase().endsWith(".app"))) {
          new ExecutionHandler(new String[] { "open", "-a", browserExecutable, url }).execute();
        } else {
          new ExecutionHandler(new String[] { browserExecutable, url }).execute();
        }
      } else {
        boolean opened = false;
        // Java 6 specific code of how to run the browser
        if (Desktop.isDesktopSupported()) {
          try {
            Desktop desktop = Desktop.getDesktop();
            desktop.browse(new URI(url));
            opened = true;
          } catch (Exception e) {
            BrowserLauncher.openURL(url);
          }
        }
        // use alternative code for systems where desktop is not supported
        if (!opened) {
          BrowserLauncher.openURL(url);
        }
      }
      
      if (Settings.propShowBrowserOpenDialog.getBoolean()){
        final JDialog dialog = new JDialog(MainFrame.getInstance(), true);
        dialog.setTitle(mLocalizer.msg("okTitle", "okTitle"));
        
        UiUtilities.registerForClosing(new WindowClosingIf() {
          public void close() {
            dialog.setVisible(false);
            Settings.propShowBrowserOpenDialog.setBoolean(true);
          }
          public JRootPane getRootPane() {
            return dialog.getRootPane();
          }
        });
        
        JPanel content = (JPanel) dialog.getContentPane();
        content.setBorder(Borders.DIALOG_BORDER);
        
        FormLayout layout = new FormLayout("fill:235dlu:grow", "default, 3dlu, default, 3dlu, default");
        dialog.getContentPane().setLayout(layout);
        
        CellConstraints cc = new CellConstraints();
        
        content.add(UiUtilities.createHelpTextArea(mLocalizer.msg("okMessage", "OK Message")), cc.xy(1, 1));
        
        final JCheckBox showBrowserDialog = new JCheckBox(mLocalizer.msg("okCheckbox", "OK Checkbox")); 
        content.add(showBrowserDialog, cc.xy(1, 3));
        
        JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
        ok.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            dialog.setVisible(false);
            if (showBrowserDialog.isSelected()) {
              Settings.propShowBrowserOpenDialog.setBoolean(false);
            } else {
              Settings.propShowBrowserOpenDialog.setBoolean(true);
            }
          };
        });
        
        JButton configure = new JButton(mLocalizer.msg("okConfigure", "Configure"));
        configure.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            dialog.setVisible(false);
            MainFrame.getInstance().showSettingsDialog(SettingsItem.WEBBROWSER);
          }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(configure);
        buttonPanel.add(ok);
        content.add(buttonPanel, cc.xy(1, 5));
        
        dialog.pack();
        UiUtilities.centerAndShow(dialog);
      }
      
    } catch (IOException e) {
      e.printStackTrace();
      int ret = ErrorHandler.handle(mLocalizer.msg("error", "An error occured"), e, ErrorHandler.SHOW_YES_NO);
      
      if (ret == ErrorHandler.YES_PRESSED) {
        MainFrame.getInstance().showSettingsDialog(SettingsItem.WEBBROWSER);
      }
    }
  }
  
  /**
   * Returns the OS of the VM
   * @return VM OS_MAC, OS_WINDOWS or OS_OTHER
   */
  public static int getOs() {
    if (BrowserLauncher.getJvm() < 0) {
      return OS_OTHER;
    } else if (BrowserLauncher.getJvm() < 6) {
      return OS_MAC;
    } else if (BrowserLauncher.getJvm() <= 7) {
      return OS_WINDOWS;
    } else if (BrowserLauncher.getJvm() == 8) {
      return OS_LINUX;
    }
    
    return OS_OTHER;
  }
  
  /**
   * 
   * @return If the OS is Windows NT branch.
   */
  public static boolean isOsWindowsNtBranch() {
    return BrowserLauncher.getJvm() == 6;
  }
}