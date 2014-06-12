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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import tvbrowser.extras.favoritesplugin.core.Favorite;
import tvbrowser.extras.favoritesplugin.dlgs.ExclusionPanel;
import tvbrowser.extras.favoritesplugin.dlgs.FavoriteTreeModel;
import tvbrowser.extras.favoritesplugin.dlgs.ManageFavoritesPanel;
import tvbrowser.extras.reminderplugin.ReminderPluginProxy;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.DefaultMarkingPrioritySelectionPanel;
import util.ui.FilterableProgramListPanel;
import util.ui.PluginChooserDlg;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

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
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(FavoritesSettingTab.class);

  private ProgramReceiveTarget[] mClientPluginTargets, mCurrentClientPluginTargets;
  private JLabel mPluginLabel;
  private JCheckBox mExpertMode, mShowRepetitions, mAutoSelectRemider, mProvideTab, mShowDateSeparators;
  private JRadioButton mScrollTimeNext, mScrollTimeDay;
  private JRadioButton mFilterStartAll, mFilterStartDefault, mFilterStartCurrent, mFilterStartLast;
  private JCheckBox mFilterReactOnChange;

  private DefaultMarkingPrioritySelectionPanel mMarkingsPanel;
  private ExclusionPanel mExclusionPanel;

  /**
   * Creates the settings panel for this tab.
   */
  public JPanel createSettingsPanel() {
    PanelBuilder builder = new PanelBuilder(new FormLayout(
        "5dlu,min(150dlu;pref):grow,5dlu,pref,5dlu",
        "pref,5dlu,pref,10dlu,pref,5dlu,pref,10dlu,pref,5dlu," +
        "pref,10dlu,pref,5dlu,pref,10dlu,pref,5dlu,pref,10dlu," +
        "pref,5dlu,default,default,default,10dlu,default,5dlu,default,10dlu,default,5dlu,default"));
    builder.border(Borders.DIALOG);

    mPluginLabel = new JLabel();
    JButton choose = new JButton(mLocalizer.msg("selectPlugins","Choose Plugins"));
    mExpertMode = new JCheckBox(mLocalizer.msg("expertMode","Always show advanced favorite edit dialog"),FavoritesPlugin.getInstance().isUsingExpertMode());
    mShowRepetitions = new JCheckBox(mLocalizer.msg("showRepetitions","Show repetitions in context menu of a favorite program"),FavoritesPlugin.getInstance().isShowingRepetitions());
    mAutoSelectRemider = new JCheckBox(mLocalizer.msg("autoSelectReminder","Automatically remind of new favorite programs"),FavoritesPlugin.getInstance().isAutoSelectingReminder());
    mShowDateSeparators = new JCheckBox(mLocalizer.msg("showDateSeparator","Show date separator in found programs list"),FavoritesPlugin.getInstance().showDateSeparators());
    mProvideTab = new JCheckBox(mLocalizer.msg("provideTab","Provide tab in TV-Browser main window"),FavoritesPlugin.getInstance().provideTab());

    ProgramReceiveTarget[] targetsArr
    = FavoritesPlugin.getInstance().getClientPluginTargetIds();

    ArrayList<ProgramReceiveTarget> clientPlugins = new ArrayList<ProgramReceiveTarget>();

    for (ProgramReceiveTarget target : targetsArr) {
      ProgramReceiveIf plugin = target.getReceifeIfForIdOfTarget();
      if(plugin != null) {
        clientPlugins.add(target);
      }
    }

    mCurrentClientPluginTargets = mClientPluginTargets = clientPlugins.toArray(new ProgramReceiveTarget[clientPlugins.size()]);

    handlePluginSelection();

    choose.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Window parent = UiUtilities
            .getLastModalChildOf(MainFrame.getInstance());
        PluginChooserDlg chooser = null;
        chooser = new PluginChooserDlg(parent, mClientPluginTargets, null,
            ReminderPluginProxy.getInstance());
        
        chooser.setVisible(true);

        if(chooser.getReceiveTargets() != null) {
          mClientPluginTargets = chooser.getReceiveTargets();
        }

        handlePluginSelection();
      }
    });

    builder.addSeparator(mLocalizer.msg("passTo", "Pass favorite programs to"), CC.xyw(1,1,5));
    builder.add(mPluginLabel, CC.xy(2,3));
    builder.add(choose, CC.xy(4,3));
    builder.addSeparator(mLocalizer.msg("expertSettings","Expert mode"), CC.xyw(1,5,5));
    builder.add(mExpertMode, CC.xyw(2,7,3));
    builder.addSeparator(mLocalizer.msg("repetitionSettings","Repetitions"), CC.xyw(1,9,4));
    builder.add(mShowRepetitions, CC.xyw(2,11,3));
    builder.addSeparator(mLocalizer.msg("reminderSettings","Automatic reminder"), CC.xyw(1,13,4));
    builder.add(mAutoSelectRemider, CC.xyw(2,15,3));

    builder.addSeparator(mLocalizer.msg("exclusions","Global exclusion criterions"), CC.xyw(1,17,4));
    builder.add(mExclusionPanel = new ExclusionPanel(FavoritesPlugin.getInstance().getGlobalExclusions(), UiUtilities.getLastModalChildOf(MainFrame.getInstance()), null), CC.xyw(2,19,3));

    builder.addSeparator(mLocalizer.msg("miscSettings","Miscellaneous"), CC.xyw(1,21,4));
    builder.add(mShowDateSeparators, CC.xyw(2,23,3));
    builder.add(mProvideTab, CC.xyw(2,24,3));
    
    JPanel timeButtonSettings = new JPanel(new FormLayout("10dlu,default:grow","5dlu,default,5dlu,default,1dlu,default"));
    
    final JLabel timeButtonBehaviour = new JLabel(mLocalizer.msg("timeButtonBehaviour", "Time buttons behaviour:"));
    
    mScrollTimeNext = new JRadioButton(mLocalizer.msg("timeButtonScrollNext", "Scroll to next occurence of time from shown programs onward"), FavoritesPlugin.getInstance().timeButtonsScrollToNextTimeInTab());
    mScrollTimeDay = new JRadioButton(mLocalizer.msg("timeButtonScrollDay", "Scroll to occurence of time on shown day in list"), !mScrollTimeNext.isSelected());
    
    ButtonGroup bg = new ButtonGroup();
    
    bg.add(mScrollTimeDay);
    bg.add(mScrollTimeNext);
    
    timeButtonBehaviour.setEnabled(mProvideTab.isSelected());
    mScrollTimeDay.setEnabled(mProvideTab.isSelected());
    mScrollTimeNext.setEnabled(mProvideTab.isSelected());
    
    mProvideTab.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        timeButtonBehaviour.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        mScrollTimeDay.setEnabled(timeButtonBehaviour.isEnabled());
        mScrollTimeNext.setEnabled(timeButtonBehaviour.isEnabled());
      }
    });
    
    timeButtonSettings.add(timeButtonBehaviour, CC.xy(2, 2));
    timeButtonSettings.add(mScrollTimeNext, CC.xy(2, 4));
    timeButtonSettings.add(mScrollTimeDay, CC.xy(2, 6));
    
    builder.add(timeButtonSettings, CC.xyw(2, 25, 3));
    
    int filterStartType = FavoritesPlugin.getInstance().getFilterStartType();
    
    JPanel filterSettingsPanel = new JPanel(new FormLayout("10dlu,default:grow","default,2dlu,default,1dlu,default,1dlu,default,1dlu,default,3dlu,default"));
    
    mFilterStartAll = new JRadioButton(mLocalizer.msg("filterStartAll", "Show all filter"), filterStartType == FilterableProgramListPanel.FILTER_START_ALL_TYPE);
    mFilterStartDefault = new JRadioButton(mLocalizer.msg("filterStartDefault", "Default filter"), filterStartType == FilterableProgramListPanel.FILTER_START_DEFAULT_TYPE);
    mFilterStartCurrent = new JRadioButton(mLocalizer.msg("filterStartCurrent", "Current TV-Browser filter"), filterStartType == FilterableProgramListPanel.FILTER_START_CURRENT_TYPE);
    mFilterStartLast = new JRadioButton(mLocalizer.msg("filterStartLast", "Last used filter"), filterStartType == ManageFavoritesPanel.FILTER_START_LAST_TYPE);
    
    ButtonGroup filterStartGroup = new ButtonGroup();
    
    filterStartGroup.add(mFilterStartAll);
    filterStartGroup.add(mFilterStartDefault);
    filterStartGroup.add(mFilterStartCurrent);
    filterStartGroup.add(mFilterStartLast);
    
    mFilterReactOnChange = new JCheckBox(mLocalizer.msg("filterReactOnChange", "React on changes of selected filter of TV-Browser"), FavoritesPlugin.getInstance().reactOnFilterChange());
    
    JLabel filterStartLabel = new JLabel(mLocalizer.msg("filterStart", "Start with:"));
    
    filterSettingsPanel.add(filterStartLabel, CC.xyw(1, 1, 2));
    filterSettingsPanel.add(mFilterStartAll, CC.xy(2, 3));
    filterSettingsPanel.add(mFilterStartDefault, CC.xy(2, 5));
    filterSettingsPanel.add(mFilterStartCurrent, CC.xy(2, 7));
    filterSettingsPanel.add(mFilterStartLast, CC.xy(2, 9));
    filterSettingsPanel.add(mFilterReactOnChange, CC.xyw(1, 11, 2));
    
    builder.addSeparator(mLocalizer.msg("filter", "Program Filter"), CC.xyw(1,27,4));
    builder.add(filterSettingsPanel, CC.xyw(2,29,3));
    
    builder.addSeparator(DefaultMarkingPrioritySelectionPanel.getTitle(), CC.xyw(1,31,4));
    builder.add(mMarkingsPanel = DefaultMarkingPrioritySelectionPanel.createPanel(FavoritesPlugin.getInstance().getMarkPriority(),false,false), CC.xyw(2,33,3));
    
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
        mPluginLabel.setText(mPluginLabel.getText() + " (" + (mClientPlugins.length - 3) + " " + mLocalizer.ellipsisMsg("otherPlugins","others") + ")");
      }
    }
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    if(!Arrays.equals(mCurrentClientPluginTargets, mClientPluginTargets)) {
      FavoritesPlugin.getInstance().setClientPluginTargets(mClientPluginTargets);

      Favorite[] favoriteArr = FavoriteTreeModel.getInstance().getFavoriteArr();

      for(Favorite favorite : favoriteArr) {
        favorite.handleNewGlobalReceiveTargets(mCurrentClientPluginTargets);
      }
    }
    FavoritesPlugin.getInstance().setIsUsingExpertMode(mExpertMode.isSelected());
    FavoritesPlugin.getInstance().setShowRepetitions(mShowRepetitions.isSelected());
    FavoritesPlugin.getInstance().setAutoSelectingReminder(mAutoSelectRemider.isSelected());
    FavoritesPlugin.getInstance().setMarkPriority(mMarkingsPanel.getSelectedPriority());
    FavoritesPlugin.getInstance().setShowDateSeparators(mShowDateSeparators.isSelected());
    FavoritesPlugin.getInstance().setProvideTab(mProvideTab.isSelected());
    FavoritesPlugin.getInstance().setTimeButtonsScrollToNextTimeInTab(mScrollTimeNext.isSelected());
    FavoritesPlugin.getInstance().setReactOnFilterChange(mFilterReactOnChange.isSelected());
    
    if(mFilterStartAll.isSelected()) {
      FavoritesPlugin.getInstance().setFilterStartType(FilterableProgramListPanel.FILTER_START_ALL_TYPE);
    }
    else if(mFilterStartDefault.isSelected()) {
      FavoritesPlugin.getInstance().setFilterStartType(FilterableProgramListPanel.FILTER_START_DEFAULT_TYPE);
    }
    else if(mFilterStartCurrent.isSelected()) {
      FavoritesPlugin.getInstance().setFilterStartType(FilterableProgramListPanel.FILTER_START_CURRENT_TYPE);
    }
    else if(mFilterStartLast.isSelected()) {
      FavoritesPlugin.getInstance().setFilterStartType(ManageFavoritesPanel.FILTER_START_LAST_TYPE);
    }

    if(mExclusionPanel.wasChanged()) {
      FavoritesPlugin.getInstance().setGlobalExclusions(mExclusionPanel.getExclusions(),mExclusionPanel.wasAdded() && !mExclusionPanel.wasEditedOrDeleted());
    }

    FavoritesPlugin.getInstance().saveFavorites();
  }

  /**
   * Returns the icon of the tab-sheet.
   */
  public Icon getIcon() {
    return FavoritesPlugin.getFavoritesIcon(16);
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("name", "Favorite programs");
  }

}
