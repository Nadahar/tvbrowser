/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package tvbrowser.extras.favoritesplugin;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.PluginChooserDlg;
import util.ui.UiUtilities;
import devplugin.Plugin;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;

/**
 * The settings tab for the favorites plugin.
 *
 * @author Til Schneider, www.murfman.de
 */
public class FavoritesSettingTab implements SettingsTab {

  /** The localizer for this class. */  
  public static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(FavoritesSettingTab.class);
  
  private ProgramReceiveTarget[] mClientPluginTargets;
  private JLabel mPluginLabel;
  private JCheckBox mExpertMode;
  
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout(
        "5dlu,min(150dlu;pref):grow,5dlu,pref,5dlu",
        "pref,5dlu,pref,10dlu,pref,5dlu,pref"));
    builder.setDefaultDialogBorder();
    
    mPluginLabel = new JLabel();
    JButton choose = new JButton(mLocalizer.msg("selectPlugins","Choose Plugins"));    
    mExpertMode = new JCheckBox(mLocalizer.msg("expertMode","Always use expert mode"),FavoritesPlugin.getInstance().isUsingExpertMode());    
    
    ProgramReceiveTarget[] clientPluginIdArr
    = FavoritesPlugin.getInstance().getClientPluginTargetIds();    
    
    ArrayList<ProgramReceiveTarget> clientPlugins = new ArrayList<ProgramReceiveTarget>();
    
    for(int i = 0; i < clientPluginIdArr.length; i++) {
      ProgramReceiveIf plugin = clientPluginIdArr[i].getReceifeIfForIdOfTarget();
      if(plugin != null)
        clientPlugins.add(clientPluginIdArr[i]);
    }
    
    mClientPluginTargets = clientPlugins.toArray(new ProgramReceiveTarget[clientPlugins.size()]);
    
    handlePluginSelection();
    
    choose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
        PluginChooserDlg chooser = null;
        if(w instanceof JDialog)
          chooser = new PluginChooserDlg((JDialog)w,mClientPluginTargets, null, ReminderPluginProxy.getInstance());
        else
          chooser = new PluginChooserDlg((JFrame)w,mClientPluginTargets, null, ReminderPluginProxy.getInstance());
        
        chooser.setLocationRelativeTo(w);
        chooser.setVisible(true);
        
        mClientPluginTargets = chooser.getReceiveTargets();
        
        handlePluginSelection();
      }
    });
    
    builder.addSeparator(mLocalizer.msg("passTo", "Pass favorite programs to"), cc.xyw(1,1,5));
    builder.add(mPluginLabel, cc.xy(2,3));
    builder.add(choose, cc.xy(4,3));
    builder.addSeparator(mLocalizer.msg("expertSettings","Expert mode"), cc.xyw(1,5,5));
    builder.add(mExpertMode, cc.xyw(2,7,3));  
    
    return builder.getPanel();
  }

  private void handlePluginSelection() {
    ArrayList<ProgramReceiveIf> plugins = new ArrayList<ProgramReceiveIf>();
    
    for(int i = 0; i < mClientPluginTargets.length; i++) {
      if(!plugins.contains(mClientPluginTargets[i].getReceifeIfForIdOfTarget()))
        plugins.add(mClientPluginTargets[i].getReceifeIfForIdOfTarget());
    }
    
    ProgramReceiveIf[] mClientPlugins = plugins.toArray(new ProgramReceiveIf[plugins.size()]);
    
    if(mClientPlugins.length > 0) {
      mPluginLabel.setText(mClientPlugins[0].toString());
      mPluginLabel.setEnabled(true);
    }
    else {
      mPluginLabel.setText(mLocalizer.msg("noPlugins","No Plugins choosen"));
      mPluginLabel.setEnabled(false);
    }
    
    for (int i = 1; i < (mClientPlugins.length > 4 ? 3 : mClientPlugins.length); i++) {
      mPluginLabel.setText(mPluginLabel.getText() + ", " + mClientPlugins[i]);
    }
    
    if(mClientPlugins.length > 4)
      mPluginLabel.setText(mPluginLabel.getText() + " (" + (mClientPlugins.length - 3) + " " + mLocalizer.msg("otherPlugins","others...") + ")");
  }
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {    
    FavoritesPlugin.getInstance().setClientPluginTargets(mClientPluginTargets);
    FavoritesPlugin.getInstance().setIsUsingExpertMode(mExpertMode.isSelected());    
  }
  
  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return FavoritesPlugin.getInstance().getIconFromTheme("apps", "bookmark", 16);
  }
  
  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("basicSettings", "Basic settings");
  }
  
}
