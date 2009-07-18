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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package emailplugin;

import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

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
import util.io.ExecutionHandler;
import util.misc.OperatingSystem;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
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
  private EMailSettings mSettings;
  
  private AbstractPluginProgramFormating mFormating;
  
  /** The Plugin */
  private EMailPlugin mPlugin;
  /** Localizer */
  private Localizer mLocalizer = Localizer.getLocalizerFor(MailCreator.class);

  /**
   * Create the MailCreator 
   * @param plugin Plugin to use
   * @param settings Settings for this MailCreator
   * @param formating The program formating to use.
   */
  public MailCreator(EMailPlugin plugin, EMailSettings settings, AbstractPluginProgramFormating formating) {
    mPlugin = plugin;
    mSettings = settings;
    mFormating = formating;
  }

  /**
   * Create the Mail
   * 
   * @param parent Parent-Frame for Dialogs 
   * @param program Programs to show in the Mail
   */
  void createMail(Frame parent, Program[] program) {
    final String param = mFormating.getContentValue();// mSettings.getProperty("paramToUse",
                                                      // EMailPlugin.DEFAULT_PARAMETER);
    final StringBuilder result = new StringBuilder();
    ParamParser parser = new ParamParser();

    int i = 0;

    while (!parser.hasErrors() && (i < program.length)) {
      String prgResult = parser.analyse(param, program[i]);
      result.append(prgResult).append("\n\n");
      i++;
    }

    if (parser.hasErrors()) {
      JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(parent), parser.getErrorString(), Localizer.getLocalization(Localizer.I18N_ERROR),
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    mail(parent, result.toString());
  }

  private void mail(Frame parent, String content) {
    // Java 6 desktop API
    boolean sent = false;
    if (Desktop.isDesktopSupported()) {
      Desktop desktop = Desktop.getDesktop();
      try {
        URI uriMailTo = new URI("mailto", "?body=" + content, null);
        desktop.mail(uriMailTo);
        sent = true;
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    if (sent) {
      return;
    }

    // fall back to non Java 6 code
    try {
      final String mailTo = "mailto:?body=" + encodeString(content);
      String application;
      String execparam;

      if ((OperatingSystem.isMacOs() || OperatingSystem.isWindows())
          && mSettings.getUseDefaultApplication()) {

        if (OperatingSystem.isMacOs()) {
          application = "/usr/bin/open";
          execparam = mailTo;
        } else {
          application = "rundll32.exe";
          execparam = "url.dll,FileProtocolHandler " + mailTo;
        }

      } else if (mSettings.getApplication().trim().equals("")) {
        if (OperatingSystem.isOther()) {
          if (!showKdeGnomeDialog(parent)) {
            return;
          }
          application = mSettings.getApplication();
          execparam = mSettings.getParameter().replaceAll(
              "\\{0\\}", mailTo);
        } else {
          showNotConfiguredCorrectly(parent);
          return;
        }
      } else {
        application = mSettings.getApplication();
        execparam = mSettings.getParameter().replaceAll(
            "\\{0\\}", mailTo);
      }

      new ExecutionHandler(execparam, application).execute();

      if (mSettings.getShowEmailOpened())
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
   * @throws UnsupportedEncodingException Problems during encoding
   */
  private String encodeString(String string) throws UnsupportedEncodingException {
    return URLEncoder.encode(string.trim(), mFormating.getEncodingValue()/*.getProperty("encoding", "UTF-8"))*/).replaceAll("\\+",
        "%20");
  }

  /**
   * Show the EMail-Open Dialog.
   * 
   * This Dialog says that the EMail should have been opened. It gives the User
   * a chance to specify another EMail Program if it went wrong.
   * 
   * @param parent
   *          Parent-Frame
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
    
    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (dontShowAgain.isSelected()) {
          mSettings.setShowEmailOpened(false);
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
   * Gives the User the opportunity to specify which Desktop he uses (KDE or
   * Gnome)
   * 
   * @param parent
   *          Parent Dialog
   * @return true if KDE or Gnome has been selected, false if the User wanted to
   *         specify the App
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
    
    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
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
      mSettings.setApplication("kfmclient");
      mSettings.setParameter("exec {0}");      
    } else if (gnomeButton.isSelected()) {
      mSettings.setApplication("gnome-open");
      mSettings.setParameter("{0}");      
    } else {
      EMailPlugin.getPluginManager().showSettings(mPlugin);
      return false;
    }
    
    return true;
  }

}