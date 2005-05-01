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

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import util.paramhandler.ParamCheckDialog;
import util.paramhandler.ParamHelpDialog;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

/**
 * This Class implements the Settings-Tab
 * 
 * @author bodum
 */
public class EMailSettingsTab implements SettingsTab {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(EMailSettingsTab.class);

  /** Settings */
  private Properties mSettings;

  /** Application-Path */
  private JTextField mApplication;

  /** Parameters */
  private JTextField mParameter;

  /** Text-Area for the Parameters in the EMail-Body*/
  private JTextArea mParamText;  

  /** Encoding of Mailto: */
  private JComboBox mEncoding;
  
  /**
   * Creates the SettingsTab
   * 
   * @param settings Settings to use
   */
  public EMailSettingsTab(Properties settings) {
    mSettings = settings;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#createSettingsPanel()
   */
  public JPanel createSettingsPanel() {
    final JPanel configPanel = new JPanel();

    configPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("configuration", "Configuration")));

    FormLayout layout = new FormLayout("3dlu, pref, 3dlu, pref:grow, 3dlu, pref, 3dlu",
        "3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref");
    configPanel.setLayout(layout);

    CellConstraints cc = new CellConstraints();

    configPanel.add(new JLabel(mLocalizer.msg("Application", "Application") + ":"), cc.xy(2, 2));

    mApplication = new JTextField(mSettings.getProperty("application"));

    configPanel.add(mApplication, cc.xy(4, 2));

    JButton appFinder = new JButton("...");
    appFinder.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        findApplictation(configPanel);
      }

    });

    configPanel.add(appFinder, cc.xy(6, 2));

    configPanel.add(new JLabel(mLocalizer.msg("Parameter", "Parameter") + ":"), cc.xy(2, 4));

    mParameter = new JTextField(mSettings.getProperty("parameter", "{0}"));

    configPanel.add(mParameter, cc.xyw(4, 4, 3));

    JTextArea ta = UiUtilities.createHelpTextArea(mLocalizer.msg("Desc","Desc"));
    configPanel.add(ta, cc.xyw(2,6,6));

    configPanel.add(new JLabel(mLocalizer.msg("encoding", "Encoding") + ":"), cc.xy(2,8));

    Vector encodings = new Vector();
    Map availcs = Charset.availableCharsets();
    Set keys = availcs.keySet();
    for (Iterator iter = keys.iterator();iter.hasNext();) {
       encodings.add(iter.next());
    }
    
    mEncoding = new JComboBox(encodings);

    mEncoding.setSelectedItem(mSettings.getProperty("encoding", "UTF-8"));
    
    configPanel.add(mEncoding, cc.xyw(4, 8, 3));
    
    JPanel panel = new JPanel(new BorderLayout());
    
    panel.add(configPanel, BorderLayout.NORTH);
    
    panel.add(createParameterPanel(), BorderLayout.CENTER);
    
    return panel;
  }

  /**
   * Creates the SettingsPanel
   * @return Settings-Panel
   */
  public JPanel createParameterPanel() {
    final JPanel panel = new JPanel(
        new FormLayout("fill:pref:grow, 3dlu, default, 3dlu, default", 
                 "fill:pref:grow, 3dlu, default"));
    
    panel.setBorder(BorderFactory.createTitledBorder(
        mLocalizer.msg("createText", "Text to create for each Program")));
    
    CellConstraints cc = new CellConstraints();
    
    mParamText = new JTextArea();
    
    mParamText.setText(mSettings.getProperty("paramToUse", EMailPlugin.DEFAULT_PARAMETER));
    
    panel.add(new JScrollPane(mParamText), cc.xyw(1,1,5));
    
    JButton check = new JButton(mLocalizer.msg("check","Check"));
    
    check.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        Window bestparent = UiUtilities.getBestDialogParent(panel);
        
        ParamCheckDialog dialog;
        if (bestparent instanceof JDialog) {
          dialog = new ParamCheckDialog((JDialog)bestparent, mParamText.getText());
        } else {
          dialog = new ParamCheckDialog((JFrame)bestparent, mParamText.getText());
        }
        dialog.show();
      }
      
    });
    
    panel.add(check, cc.xy(3,3));
    
    JButton help = new JButton(mLocalizer.msg("help","Help"));
    
    help.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent arg0) {
        Window bestparent = UiUtilities.getBestDialogParent(panel);
        
        ParamHelpDialog dialog;
        if (bestparent instanceof JDialog) {
          dialog = new ParamHelpDialog((JDialog)bestparent);
        } else {
          dialog = new ParamHelpDialog((JFrame)bestparent);
        }
        dialog.show();
      }
      
    });
    
    panel.add(help, cc.xy(5,3));
    
    return panel;
  }  
  
  /**
   * Opens a FileChooser and let the User open a File
   */
  private void findApplictation(JPanel panel) {
    JFileChooser chooser = new JFileChooser(new File(mSettings.getProperty("application", "")));
    int returnVal = chooser.showOpenDialog(UiUtilities.getBestDialogParent(panel));
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      mApplication.setText(chooser.getSelectedFile().getAbsolutePath());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#saveSettings()
   */
  public void saveSettings() {
    mSettings.put("application", mApplication.getText());
    mSettings.put("parameter", mParameter.getText());
    mSettings.put("encoding", mEncoding.getSelectedItem());
    mSettings.put("paramToUse", mParamText.getText());
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#getIcon()
   */
  public Icon getIcon() {
    return new ImageIcon(ImageUtilities.createImageFromJar("emailplugin/email.gif", EMailSettingsTab.class));
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.SettingsTab#getTitle()
   */
  public String getTitle() {
    return mLocalizer.msg("name", "Send EMail");
  }
}