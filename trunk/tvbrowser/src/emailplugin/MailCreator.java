/*
 * EMailPlugin by Bodo Tasche
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
package emailplugin;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;

import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.misc.OperatingSystem;
import util.paramhandler.ParamParser;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;

/**
 * This class creates the Mail and launches the Mail-Application.
 * If no Mail-Application is found, it asks the User to specify the App.
 * 
 * @author bodum
 */
public class MailCreator {
  /** Settings for this Plugin */
  private Properties mSettings;
  /** The Plugin */
  private EMailPlugin mPlugin;
  /** Localizer */
  private Localizer mLocalizer = Localizer.getLocalizerFor(MailCreator.class);

  /**
   * Create the MailCreator 
   * @param plugin Plugin to use
   * @param settings Settings for this MailCreator
   */
  public MailCreator(EMailPlugin plugin, Properties settings) {
    mPlugin = plugin;
    mSettings = settings;
  }

  /**
   * Create the Mail
   * 
   * @param parent Parent-Frame for Dialogs 
   * @param program Programs to show in the Mail
   */
  void createMail(Frame parent, Program[] program) {
    String param = mSettings.getProperty("paramToUse", EMailPlugin.DEFAULT_PARAMETER);
    StringBuffer result = new StringBuffer();
    ParamParser parser = new ParamParser();

    int i = 0;

    while (!parser.hasErrors() && (i < program.length)) {
      String prgResult = parser.analyse(param, (Program) program[i]);
      result.append(prgResult).append("\n\n");
      i++;
    }

    if (parser.hasErrors()) {
      JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(parent), parser.getErrorString(), Localizer.getLocalization(Localizer.I18N_ERROR),
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      String application;
      String execparam;

      if ((OperatingSystem.isMacOs() || OperatingSystem.isWindows())
          && mSettings.getProperty("defaultapp", "true").equals("true")) {

        if (OperatingSystem.isMacOs()) {
          application = "/usr/bin/open";
          execparam = "mailto:?body="
              + encodeString(result.toString());
        } else {
          application = "rundll32.exe";
          execparam = "url.dll,FileProtocolHandler mailto:?body="
            + encodeString(result.toString());
        }

      } else if (mSettings.getProperty("application", "").trim().equals("")) {
        if (OperatingSystem.isOther()) {
          if (!showKdeGnomeDialog(parent)) {
            return;
          }
          application = mSettings.getProperty("application", "");
          execparam = mSettings.getProperty("parameter", "").replaceAll(
              "\\{0\\}",
              "mailto:?body=" + encodeString(result.toString()));
        } else {
          showNotConfiguredCorrectly(parent);
          return;
        }
      } else {
        application = mSettings.getProperty("application", "");
        execparam = mSettings.getProperty("parameter", "").replaceAll(
            "\\{0\\}",
            "mailto:?body="
              + encodeString(result.toString()));
      }

      Runtime.getRuntime().exec(application + " " + execparam);

      if (mSettings.getProperty("showEmailOpened", "true").equals("true"))
        showEMailOpenedDialog(parent);

    } catch (Exception e) {
      e.printStackTrace();
      int ret = ErrorHandler.handle(mLocalizer.msg("ErrorWhileStarting", "Error while starting Mail-Application"), e,
          ErrorHandler.SHOW_YES_NO);

      if (ret == ErrorHandler.YES_PRESSED) {
        MainFrame.getInstance().showSettingsDialog(mPlugin);
      }
    }
  }

  /**
   * Encodes a String into an Url-Encoded String
   * 
   * @param string String to Encode
   * @return URL-Encoded String
   * @throws UnsupportedEncodingException
   */
  private String encodeString(String string) throws UnsupportedEncodingException {
    return URLEncoder.encode(string.trim(), mSettings.getProperty("encoding", "UTF-8")).replaceAll("\\+",
        "%20");
  }

  /**
   * Show the EMail-Open Dialog.
   * 
   * This Dialog says that the EMail should have been opend. It gives the
   * User a chance to specify another EMail Program if it went wrong.
   * 
   * @param parent Parent-Frame
   */
  private void showEMailOpenedDialog(Frame parent) {
    final JDialog dialog = new JDialog(parent, true);
    
    dialog.setTitle(mLocalizer.msg("EMailOpenedTitel", "Email was opened"));
    
    JPanel panel = (JPanel) dialog.getContentPane();
    panel.setLayout(new FormLayout("fill:200dlu:grow", "default, 3dlu, default, 3dlu, default"));
    panel.setBorder(Borders.DIALOG_BORDER);

    CellConstraints cc = new CellConstraints();

    panel.add(UiUtilities.createHelpTextArea(mLocalizer.msg("EMailOpened", "Email was opened. Configure it ?")), cc.xy(1,1));
    
    final JCheckBox dontShowAgain = new JCheckBox(mLocalizer.msg("DontShowAgain", "Don't show this Dialog again"));
    panel.add(dontShowAgain, cc.xy(1,3));
    
    JButton configure = new JButton(mLocalizer.msg("configure", "Configure"));
    configure.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        EMailPlugin.getPluginManager().showSettings(mPlugin);
        dialog.setVisible(false);
      }
    });
    
    JButton ok = new JButton(mLocalizer.msg("ok", "OK"));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (dontShowAgain.isSelected()) {
          mSettings.setProperty("showEmailOpened", "false");
        }
        dialog.setVisible(false);
      }
    });
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(configure);
    buttonPanel.add(ok);
    panel.add(buttonPanel, cc.xy(1, 5));

    UiUtilities.registerForClosing(new WindowClosingIf() {
      public void close() {
        dialog.setVisible(false);
      }
      public JRootPane getRootPane() {
        return dialog.getRootPane();
      }
    });
    
    dialog.getRootPane().setDefaultButton(ok);
    
    dialog.pack();
    UiUtilities.centerAndShow(dialog);
  }

  /**
   * Shows a Warning if the Plugin was not configured correctly.
   * 
   * @param parent Parent-Dialog
   */
  private void showNotConfiguredCorrectly(Frame parent) {
    int ret = JOptionPane.showConfirmDialog(parent, mLocalizer.msg("NotConfiguredCorrectly", "Not configured correctly"), Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
    
    if (ret == JOptionPane.YES_OPTION) {
      EMailPlugin.getPluginManager().showSettings(mPlugin);
    }
  }

  /**
   * Gives the User the oppertunity to specify wich Desktop he uses (KDE or Gnome)
   * 
   * @param parent Parent Dialog
   * @return true if KDE or Gnome has been selected, false if the User wanted to specify the App
   */
  private boolean showKdeGnomeDialog(Frame parent) {
    final JDialog dialog = new JDialog(parent, true);
    
    dialog.setTitle(mLocalizer.msg("chooseTitle", "Choose"));
    
    JPanel panel = (JPanel) dialog.getContentPane();
    panel.setLayout(new FormLayout("10dlu, fill:pref:grow", "default, 3dlu, default, 3dlu, default, 3dlu, default, 3dlu:grow, default"));
    panel.setBorder(Borders.DIALOG_BORDER);
   
    CellConstraints cc = new CellConstraints();

    panel.add(UiUtilities.createHelpTextArea(mLocalizer.msg("cantConfigure", "Can't configure on your system")), cc.xyw(1,1, 2));

    JRadioButton kdeButton = new JRadioButton(mLocalizer.msg("kde", "I am using KDE"));
    panel.add(kdeButton, cc.xy(2,3));

    JRadioButton gnomeButton = new JRadioButton(mLocalizer.msg("gnome", "I am using Gnome"));
    panel.add(gnomeButton, cc.xy(2,5));

    JRadioButton selfButton = new JRadioButton(mLocalizer.msg("self", "I want to configure by myself"));
    panel.add(selfButton, cc.xy(2,7));

    ButtonGroup group = new ButtonGroup();
    group.add(kdeButton);
    group.add(gnomeButton);
    group.add(selfButton);
    
    selfButton.setSelected(true);
    
    JButton ok = new JButton(Localizer.I18N_OK);
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });
    
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(ok);
    panel.add(buttonPanel, cc.xy(2, 9));

    UiUtilities.registerForClosing(new WindowClosingIf() {
      public void close() {
        dialog.setVisible(false);
      }
      public JRootPane getRootPane() {
        return dialog.getRootPane();
      }
    });
    
    dialog.getRootPane().setDefaultButton(ok);
    
    dialog.pack();
    UiUtilities.centerAndShow(dialog);
    
    if (kdeButton.isSelected()) {
      mSettings.setProperty("application", "kfmclient");
      mSettings.setProperty("parameter", "exec {0}");      
    } else if (gnomeButton.isSelected()) {
      mSettings.setProperty("application", "gnome-open");
      mSettings.setProperty("parameter", "{0}");      
    } else {
      EMailPlugin.getPluginManager().showSettings(mPlugin);
      return false;
    }
    
    return true;
  }

}