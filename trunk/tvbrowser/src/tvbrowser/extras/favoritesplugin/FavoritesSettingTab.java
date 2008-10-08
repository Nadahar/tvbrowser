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

import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.dlgs.ExclusionPanel;
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteTreeModel;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.DefaultMarkingPrioritySelectionPanel;
import util.ui.PluginChooserDlg;
import util.ui.UiUtilities;
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
  
  private ProgramReceiveTarget[] mClientPluginTargets, mCurrentCientPluginTargets;
  private JLabel mPluginLabel;
  private JCheckBox mExpertMode, mShowRepetitions, mAutoSelectRemider;
  
  private DefaultMarkingPrioritySelectionPanel mMarkingsPanel;
  private ExclusionPanel mExclusionPanel;
  
  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    CellConstraints cc = new CellConstraints();
    PanelBuilder builder = new PanelBuilder(new FormLayout(
        "5dlu,min(150dlu;pref):grow,5dlu,pref,5dlu",
        "pref,5dlu,pref,10dlu,pref,5dlu,pref,10dlu,pref,5dlu," +
        "pref,10dlu,pref,5dlu,pref,10dlu,pref,5dlu,pref,10dlu," +
        "pref,5dlu,pref"));
    builder.setDefaultDialogBorder();
    
    mPluginLabel = new JLabel();
    JButton choose = new JButton(mLocalizer.msg("selectPlugins","Choose Plugins"));    
    mExpertMode = new JCheckBox(mLocalizer.msg("expertMode","Always show advanced favorite edit dialog"),FavoritesPlugin.getInstance().isUsingExpertMode());
    mShowRepetitions = new JCheckBox(mLocalizer.msg("showRepetitions","Show repetitions in context menu of a favorite program"),FavoritesPlugin.getInstance().isShowingRepetitions());
    mAutoSelectRemider = new JCheckBox(mLocalizer.msg("autoSelectReminder","Automatically remind of new favorite programs"),FavoritesPlugin.getInstance().isAutoSelectingRemider());
    
    ProgramReceiveTarget[] targetsArr
    = FavoritesPlugin.getInstance().getClientPluginTargetIds();    
    
    ArrayList<ProgramReceiveTarget> clientPlugins = new ArrayList<ProgramReceiveTarget>();
    
    for (ProgramReceiveTarget target : targetsArr) {
      ProgramReceiveIf plugin = target.getReceifeIfForIdOfTarget();
      if(plugin != null) {
        clientPlugins.add(target);
      }
    }
    
    mCurrentCientPluginTargets = mClientPluginTargets = clientPlugins.toArray(new ProgramReceiveTarget[clientPlugins.size()]);
    
    handlePluginSelection();
    
    choose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Window w = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
        PluginChooserDlg chooser = null;
        if(w instanceof JDialog) {
          chooser = new PluginChooserDlg((JDialog)w,mClientPluginTargets, null, ReminderPluginProxy.getInstance());
        } else {
          chooser = new PluginChooserDlg((JFrame)w,mClientPluginTargets, null, ReminderPluginProxy.getInstance());
        }
        
        chooser.setLocationRelativeTo(w);
        chooser.setVisible(true);
        
        if(chooser.getReceiveTargets() != null) {
          mClientPluginTargets = chooser.getReceiveTargets();
        }
        
        handlePluginSelection();
      }
    });
    
    builder.addSeparator(mLocalizer.msg("passTo", "Pass favorite programs to"), cc.xyw(1,1,5));
    builder.add(mPluginLabel, cc.xy(2,3));
    builder.add(choose, cc.xy(4,3));
    builder.addSeparator(mLocalizer.msg("expertSettings","Expert mode"), cc.xyw(1,5,5));
    builder.add(mExpertMode, cc.xyw(2,7,3));
    builder.addSeparator(mLocalizer.msg("repetitionSettings","Repetitions"), cc.xyw(1,9,4));
    builder.add(mShowRepetitions, cc.xyw(2,11,3));
    builder.addSeparator(mLocalizer.msg("reminderSettings","Automatic reminder"), cc.xyw(1,13,4));
    builder.add(mAutoSelectRemider, cc.xyw(2,15,3));

    builder.addSeparator(mLocalizer.msg("exclusions","Global exclusion criterions"), cc.xyw(1,17,4));
    builder.add(mExclusionPanel = new ExclusionPanel(FavoritesPlugin.getInstance().getGlobalExclusions(), UiUtilities.getLastModalChildOf(MainFrame.getInstance()), null), cc.xyw(2,19,3));

    builder.addSeparator(DefaultMarkingPrioritySelectionPanel.getTitle(), cc.xyw(1,21,4));
    builder.add(mMarkingsPanel = DefaultMarkingPrioritySelectionPanel.createPanel(FavoritesPlugin.getInstance().getMarkPriority(),false,false), cc.xyw(2,23,3));
    
    return builder.getPanel();
  }

  private void handlePluginSelection() {
    ArrayList<ProgramReceiveIf> plugins = new ArrayList<ProgramReceiveIf>();
    
    if(mClientPluginTargets != null) {
      for (ProgramReceiveTarget element : mClientPluginTargets) {
        if(!plugins.contains(element.getReceifeIfForIdOfTarget())) {
          plugins.add(element.getReceifeIfForIdOfTarget());
        }
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
    
      if(mClientPlugins.length > 4) {
        mPluginLabel.setText(mPluginLabel.getText() + " (" + (mClientPlugins.length - 3) + " " + mLocalizer.msg("otherPlugins","others...") + ")");
      }
    }
  }
  
  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    if(mCurrentCientPluginTargets != mClientPluginTargets) {
      FavoritesPlugin.getInstance().setClientPluginTargets(mClientPluginTargets);
      
      Favorite[] favoriteArr = FavoriteTreeModel.getInstance().getFavoriteArr();
      
      for(Favorite favorite : favoriteArr) {
        favorite.handleNewGlobalReceiveTargets(mCurrentCientPluginTargets);
      }
    }
    FavoritesPlugin.getInstance().setIsUsingExpertMode(mExpertMode.isSelected());    
    FavoritesPlugin.getInstance().setShowRepetitions(mShowRepetitions.isSelected());
    FavoritesPlugin.getInstance().setAutoSelectingReminder(mAutoSelectRemider.isSelected());
    FavoritesPlugin.getInstance().setMarkPriority(mMarkingsPanel.getSelectedPriority());
    
    if(mExclusionPanel.wasChanged()) {
      FavoritesPlugin.getInstance().setGlobalExclusions(mExclusionPanel.getExclusions());
    }
    
    FavoritesPlugin.getInstance().saveFavorites();
  }
  
  /**
   * Returns the icon of the tab-sheet.
   */
  public Icon getIcon() {
    return FavoritesPlugin.getInstance().getFavoritesIcon(16);
  }
  
  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("name", "Favorite programs");
  }
  
}
