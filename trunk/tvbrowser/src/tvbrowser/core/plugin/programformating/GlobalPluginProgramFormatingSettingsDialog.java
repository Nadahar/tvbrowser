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
 *     $Date: 2007-01-20 23:10:59 +0100 (Sa, 20 Jan 2007) $
 *   $Author: ds10 $
 * $Revision: 3037 $
 */
package tvbrowser.core.plugin.programformating;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import util.paramhandler.ParamHelpDialog;
import util.paramhandler.ParamLibrary;
import util.paramhandler.ParamParser;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Plugin;

/**
 * A settings dialog for the program configuration.
 * 
 * @author René Mach
 * @since 2.5.1
 */
public final class GlobalPluginProgramFormatingSettingsDialog extends JDialog
    implements WindowClosingIf, ActionListener {
  private static Localizer mLocalizer = Localizer.getLocalizerFor(GlobalPluginProgramFormatingSettingsDialog.class);
  
  private GlobalPluginProgramFormating mConfig, mDefaultConfig;
  private JButton mSetName, mPreview, mSetBack, mHelp, mOk, mCancel;
  private JLabel mName;
  private JTextField mTitle;
  private JTextArea mContentArea;
  private JComboBox mEncoding;
  
  /**
   * Creates an instance of this settings dialog.
   * 
   * @param parent The parent window.
   * @param config The program configuration to edit.
   * @param defaultConfig The default program configurations.
   * @param showTitleSetting If the settings dialog should contain the title setting.
   * @param showEncodingSetting If the settings dialog should contain the encoding setting.
   */
  public static void createInstance(Window parent, GlobalPluginProgramFormating config, GlobalPluginProgramFormating defaultConfig, boolean showTitleSetting, boolean showEncodingSetting) {
    new GlobalPluginProgramFormatingSettingsDialog(parent, config,
        defaultConfig, showTitleSetting, showEncodingSetting);
  }
  
  private GlobalPluginProgramFormatingSettingsDialog(Window parent,
      GlobalPluginProgramFormating config,
      GlobalPluginProgramFormating defaultConfig, boolean showTitleSetting,
      boolean showEncodingSetting) {
    super(parent);
    setModal(true);
    createGui(parent, config, defaultConfig, showTitleSetting, showEncodingSetting);
  }

  private void createGui(Window w, GlobalPluginProgramFormating config, GlobalPluginProgramFormating defaultConfig, boolean showTitleSetting, boolean showEncodingSetting) {
    mConfig = config;
    mDefaultConfig = defaultConfig;
    
    setTitle(mLocalizer.msg("settingsFor","Settings for ") + config.getName());
    UiUtilities.registerForClosing(this);
    
    CellConstraints cc = new CellConstraints();
    FormLayout baseLayout = new FormLayout("pref,5dlu,pref:grow","pref,5dlu,pref,fill:default:grow,5dlu,pref");
    PanelBuilder pb = new PanelBuilder(baseLayout,(JPanel)getContentPane());
    pb.setDefaultDialogBorder();
    
    mName = new JLabel(config.getName());
    mSetName = new JButton(mLocalizer.msg("changeName","Change name"));
    mSetName.addActionListener(this);
    
    JPanel panel = new JPanel(new FormLayout("pref:grow,5dlu,pref","pref"));
    panel.add(mName, cc.xy(1,1));
    panel.add(mSetName, cc.xy(3,1));
        
    mTitle = new JTextField(config.getTitleValue());
    mContentArea = new JTextArea(config.getContentValue());
    
    Vector<String> encodings = new Vector<String>();
    Map<String, Charset> availcs = Charset.availableCharsets();
    Set<String> keys = availcs.keySet();
    for (String string : keys) {
       encodings.add(string);
    }
    
    mEncoding = new JComboBox(encodings);
    mEncoding.setSelectedItem(config.getEncodingValue());
    mEncoding.addActionListener(this);
    
    mPreview = new JButton(mLocalizer.msg("preview","Preview"));
    mPreview.addActionListener(this);
    
    mSetBack = new JButton(Localizer.getLocalization(Localizer.I18N_DEFAULT));
    mSetBack.addActionListener(this);
    
    mHelp = new JButton(Localizer.getLocalization(Localizer.I18N_HELP));
    mHelp.addActionListener(this);
    
    mOk = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    mOk.addActionListener(this);
    
    mCancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    mCancel.addActionListener(this);
    
    FormLayout layout = new FormLayout("pref,3dlu,pref,3dlu,pref,0dlu:grow,pref,3dlu,pref","pref");
    layout.setColumnGroups(new int[][] {{1,3,5,7,9}});
    
    JPanel buttonPanel = new JPanel(layout);
    
    buttonPanel.add(mPreview, cc.xy(1,1));
    buttonPanel.add(mSetBack, cc.xy(3,1));
    buttonPanel.add(mHelp, cc.xy(5,1));
    buttonPanel.add(mOk, cc.xy(7,1));
    buttonPanel.add(mCancel, cc.xy(9,1));
    
    int y = 1;
    
    pb.addLabel(mLocalizer.msg("name","Name") + ":", cc.xy(1,y));
    pb.add(panel, cc.xy(3,y++));
    
    if(showTitleSetting) {
      baseLayout.insertRow(y++, RowSpec.decode("2dlu"));
      baseLayout.insertRow(y, RowSpec.decode("pref"));
      
      pb.addLabel(mLocalizer.msg("title","Titel") + ":", cc.xy(1,y));
      pb.add(mTitle, cc.xy(3,y++));
    }
    
    pb.addLabel(mLocalizer.msg("content","Content") + ":", cc.xyw(1,++y,3));
    pb.add(new JScrollPane(mContentArea), cc.xyw(1,++y,3));
    
    y++;
    
    if(showEncodingSetting) {
      baseLayout.insertRow(y++, RowSpec.decode("5dlu"));
      baseLayout.insertRow(y, RowSpec.decode("pref"));
      
      pb.addLabel(mLocalizer.msg("encoding","Encoding") + ":", cc.xy(1,y));
      pb.add(mEncoding, cc.xy(3,y++));
    }
    
    pb.add(buttonPanel, cc.xyw(1,++y,3));
    
    setSize(Sizes.dialogUnitXAsPixel(400,this),Sizes.dialogUnitYAsPixel(300,this));
    setLocationRelativeTo(w);
    setVisible(true);
  }

  public void close() {
    setVisible(false);
    dispose();
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getSource() == mCancel) {
      close();
    } else if(e.getSource() == mPreview) {
      showPreview();
    } else if(e.getSource() == mSetBack) {
      defaultPressed();
    } else if(e.getSource() == mHelp) {
      ParamHelpDialog dialog = new ParamHelpDialog(this, new ParamLibrary());
      dialog.setVisible(true);
    }
    else if(e.getSource() == mSetName) {
      String value = JOptionPane.showInputDialog(this,mLocalizer.msg("changeName","Change name") + ":",mName.getText());
      
      if(value != null) {
        mName.setText(value);
      }
    }
    else if(e.getSource() == mOk) {
      mConfig.setName(mName.getText());
      mConfig.setTitleValue(mTitle.getText());
      mConfig.setContentValue(mContentArea.getText());
      mConfig.setEncodingValue(mEncoding.getSelectedItem().toString());
      
      close();
    }
  }
  
  /**
   * Show a Preview of the HTML that will be generated
   */
  protected void showPreview() {
    ParamParser parser = new ParamParser();
    String content = parser.analyse(mContentArea.getText(), Plugin.getPluginManager().getExampleProgram());
    if (parser.showErrors()) {
      return;
    }
    content = content.trim();
    
    final JDialog dialog = new JDialog(this, mLocalizer.msg("preview", "Preview"), true);
    JPanel contentPanel = (JPanel) dialog.getContentPane();
    
    UiUtilities.registerForClosing(new WindowClosingIf() {
      public void close() {
        dialog.setVisible(false);
      }
      public JRootPane getRootPane() {
        return dialog.getRootPane();
      }
    });
    
    contentPanel.setLayout(new FormLayout("fill:default:grow, pref", "fill:default:grow, 3dlu, pref"));
    contentPanel.setBorder(Borders.DLU4_BORDER);
    
    JEditorPane example = new JEditorPane("text", content);
    example.setEditable(false);
    example.setCaretPosition(0);
    
    CellConstraints cc = new CellConstraints();
    
    contentPanel.add(new JScrollPane(example), cc.xyw(1, 1, 2));
    
    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });
    dialog.getRootPane().setDefaultButton(ok);
    
    contentPanel.add(ok, cc.xy(2, 3));
    
    dialog.setSize(500, 400);
    UiUtilities.centerAndShow(dialog);
  }
  
  /**
   * Default was pressed.
   * The Settings will be set to default-values after a confirm dialog
   */
  protected void defaultPressed() {
    int ret = JOptionPane.showConfirmDialog(this,
        mLocalizer.msg("reset", "Reset to default Settings?"),
        Localizer.getLocalization(Localizer.I18N_DEFAULT)+"?", JOptionPane.YES_NO_OPTION);
    if (ret == JOptionPane.YES_OPTION) {
      mTitle.setText(mDefaultConfig.getTitleValue());
      mContentArea.setText(mDefaultConfig.getContentValue());
      mEncoding.setSelectedItem(mDefaultConfig.getEncodingValue());
    }
  }
}
