/*
 * TV-Browser
 * Copyright (C) 04-2003 TV-Browser team (dev@tvbrowser.org)
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
package programlistplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import compat.FilterCompat;
import compat.PersonaCompat;
import compat.PluginCompat;
import compat.UiCompat;
import compat.VersionCompat;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginCenterPanel;
import devplugin.PluginCenterPanelWrapper;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.Version;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

/**
 * Shows all important programs in a time sorted list.
 *
 * @author René Mach
 * @since 2.7
 */
public class ProgramListPlugin extends Plugin {
  static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramListPlugin.class);

  private static Version mVersion = new Version(3, 30, 2, true);
  
  private static final int MAX_DIALOG_LIST_SIZE = 5000;
  static final int MAX_PANEL_LIST_SIZE = 2500;

  private JDialog mDialog;
  private ProgramListSettings mSettings;
  
  private ProgramFilter mReceiveFilter;

  private static ProgramListPlugin mInstance;
  
  private ProgramListPanel mDialogPanel;
  private ProgramListPanel mCenterPanelEntry;
  private ProgramListCenterPanel mCenterPanel;
  
  private PluginCenterPanelWrapper mWrapper;
  private JPanel mCenterPanelWrapper;

  private boolean mTvBrowserWasStarted;
  /**
   * Creates an instance of this class.
   */
  public ProgramListPlugin() {
    mInstance = this;
    mTvBrowserWasStarted = false;
  }
  
  public void onActivation() {
    SwingUtilities.invokeLater(new Runnable() {
      
      @Override
      public void run() {
        mCenterPanelWrapper = UiCompat.createPersonaBackgroundPanel();
        mCenterPanel = new ProgramListCenterPanel();
        
        mWrapper = new PluginCenterPanelWrapper() {
          @Override
          public PluginCenterPanel[] getCenterPanels() {
            return new PluginCenterPanel[] {mCenterPanel};
          }
          
          public void filterSelected(ProgramFilter filter) {
            if(mCenterPanelEntry != null && getSettings().getBooleanValue(ProgramListSettings.KEY_TAB_REACT_ON_FILTER_CHANGE)) {
              mCenterPanelEntry.updateFilter(filter);
            }
          }
          
          public void timeEvent() {
            if(mCenterPanelEntry != null) {
              mCenterPanelEntry.timeEvent();
            }
          }
          
          @Override
          public void scrolledToDate(Date date) {
            if(mCenterPanelEntry != null && getSettings().getBooleanValue(ProgramListSettings.KEY_TAB_REACT_ON_DATE)) {
              mCenterPanelEntry.dateSelected(date);
            }
          }
          
          @Override
          public void scrolledToNow() {
            if(mCenterPanelEntry != null && getSettings().getBooleanValue(ProgramListSettings.KEY_TAB_REACT_ON_TIME)) {
              mCenterPanelEntry.nowSelected();
            }
          }
          
          @Override
          public void scrolledToTime(int time) {
            if(mCenterPanelEntry != null && getSettings().getBooleanValue(ProgramListSettings.KEY_TAB_REACT_ON_TIME)) {
              mCenterPanelEntry.scrollToTime(time);
            }
          }
          
          @Override
          public void scrolledToChannel(Channel channel) {
            if(mCenterPanelEntry != null && getSettings().getBooleanValue(ProgramListSettings.KEY_TAB_REACT_ON_CHANNEL)) {
              mCenterPanelEntry.selectChannel(channel);
            }
          }
        };
        
        new Thread("wait for TV-Browser start finished") {
          public void run() {
            while(!mTvBrowserWasStarted) {
              try {
                sleep(100);
              } catch (InterruptedException e) {
                // ignore
              }
            }
            
            try {
              sleep(1000);
            } catch (InterruptedException e) {
              // ignore
            }
            
            addPanel();
          }
        }.start();
      }
    });

  }
  
  
  public void addPanel() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        if(getSettings().getBooleanValue(ProgramListSettings.KEY_PROVIDE_TAB)) {
          mCenterPanelEntry = new ProgramListPanel(null, false, MAX_PANEL_LIST_SIZE);
          PersonaCompat.getInstance().registerPersonaListener(mCenterPanelEntry);
          FilterCompat.getInstance().registerFilterChangeListener(mCenterPanelEntry);
          
          SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
              mCenterPanelWrapper.add(mCenterPanelEntry, BorderLayout.CENTER);
              
              if(PersonaCompat.getInstance().getHeaderImage() != null) {
                mCenterPanelEntry.updatePersona();
              }
            }
          });
        } else {
          if(mCenterPanelEntry != null) {
            PersonaCompat.getInstance().removePersonaListener(mCenterPanelEntry);
            FilterCompat.getInstance().unregisterFilterChangeListener(mCenterPanelEntry);
          }
          
          mCenterPanelEntry = null;
        }
      }
    });
  }
  
  public void handleTvDataUpdateFinished() {
    if(mCenterPanelEntry != null) {
      mCenterPanelEntry.fillDateBox();
      mCenterPanelEntry.fillProgramList();
    }
  }
  
  public void handleTvBrowserStartFinished() {
    mTvBrowserWasStarted = true;
  }

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    return new PluginInfo(ProgramListPlugin.class, mLocalizer.msg("name", "Program list"), mLocalizer.msg(
        "description", "Shows programs in a list."), "René Mach");
  }

  /**
   * Gets the instance of this plugin.
   *
   * @return The instance of this plugin.
   */
  public static ProgramListPlugin getInstance() {
    if (mInstance == null) {
      new ProgramListPlugin();
    }

    return mInstance;
  }

  public void loadSettings(final Properties properties) {
    mSettings = new ProgramListSettings(properties);
  }

  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        mReceiveFilter = null;
        showProgramList();
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("name", "Important programs"));
    action.putValue(Action.SMALL_ICON, createImageIcon("emblems", "emblem-important", 16));
    action.putValue(Plugin.BIG_ICON, createImageIcon("emblems", "emblem-important", 22));

    return new ActionMenu(action);
  }



  public void onDeactivation() {
    if (mDialog != null) {
      mDialog.setVisible(false);
    }
  }


  @Override
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return new ProgramReceiveTarget[] { new ProgramReceiveTarget(this, mLocalizer.msg("programTarget", "Program list"),
        "program list") };
  }

  @Override
  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
    final List<Program> programs = Arrays.asList(programArr);
    mReceiveFilter = new ProgramFilter() {
      @Override
      public boolean accept(Program prog) {
        return programs.contains(prog);
      }

      @Override
      public String getName() {
        return mLocalizer.msg("filterName", "Received programs");
      }

      @Override
      public String toString() {
        return mLocalizer.msg("filterName", "Received programs");
      }
    };
    showProgramList();
    return true;
  }

  private void showProgramList() {
    showProgramList(null);
  }

  private void showProgramList(final Channel selectedChannel) {
    try {
      if (mDialog == null) {
        mDialog = new JDialog(getParentFrame());
        mDialog.setTitle(mLocalizer.msg("name", "Important programs"));
        mDialog.getContentPane().setLayout(new BorderLayout(0,0));
        
        UiUtilities.registerForClosing(new WindowClosingIf() {

          public void close() {
            closeDialog();
          }

          public JRootPane getRootPane() {
            return mDialog.getRootPane();
          }
        });

        mDialogPanel = new ProgramListPanel(selectedChannel,true,MAX_DIALOG_LIST_SIZE);
        
        mDialog.getContentPane().add(mDialogPanel, BorderLayout.CENTER);

        layoutWindow("programListWindow", mDialog, new Dimension(500, 500));

        mDialog.setVisible(true); 
      } else {
        if (!mDialog.isVisible()) {
          mDialogPanel.fillFilterBox();
        }

        mDialog.setVisible(!mDialog.isVisible());

        if (mDialog.isVisible()) {
          mDialogPanel.fillProgramList();
        } else {
          saveMe();
        }
      }
    } catch (Exception ee) {
      ee.printStackTrace();
    }
  }

  void closeDialog() {
    mDialog.setVisible(false);
    saveMe();
  }

  @Override
  public ActionMenu getContextMenuActions(final Channel channel) {
    return new ActionMenu(new AbstractAction(mLocalizer.msg("showChannel", "Show programs of channel in program list")) {
      
      @Override
      public void actionPerformed(ActionEvent e) {
        showProgramList(channel);
      }
    });
  }
  
  public String getPluginCategory() {
    return PluginCompat.CATEGORY_OTHER;
  }
  
  ProgramFilter getReceiveFilter() {
    return mReceiveFilter;
  }

  ProgramListSettings getSettings() {
    return mSettings;
  }
  
  Frame getSuperFrame() {
    return getParentFrame();
  }
  
  JDialog getDialog() {
    return mDialog;
  }
  
  void updateDescriptionSelection(boolean value) {
    if(mCenterPanelEntry != null) {
      mCenterPanelEntry.updateDescriptionSelection(value);
    }
  }
  
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private JCheckBox mProvideTab;
      private JCheckBox mShowDateSeparator;
      
      private JRadioButton mTabTimeScrollNext;
      private JRadioButton mTabTimeScrollDay;
      
      private JCheckBox mReactOnFilterChange;
      private JCheckBox mReactOnTime;
      private JCheckBox mReactOnDate;
      private JCheckBox mReactOnChannel;
      
      @Override
      public JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new FormLayout("5dlu,10dlu,10dlu,min:grow",
            "5dlu,default,default,5dlu,default,5dlu,default,default,default,default,5dlu,default,5dlu,default,default"));
        
        mShowDateSeparator = new JCheckBox(mLocalizer.msg("showDateSeparator", "Show date separator in list"), getSettings().getBooleanValue(ProgramListSettings.KEY_SHOW_DATE_SEPARATOR));
        mProvideTab = new JCheckBox(mLocalizer.msg("provideTab", "Provide tab in TV-Browser main window"), getSettings().getBooleanValue(ProgramListSettings.KEY_PROVIDE_TAB));
        
        final JLabel tabReactOnLabel = new JLabel(mLocalizer.msg("reactOn", "React on selection of the following actions in main window:"));
        
        mReactOnTime = new JCheckBox(mLocalizer.msg("reactOnTime", "Time"), getSettings().getBooleanValue(ProgramListSettings.KEY_TAB_REACT_ON_TIME));
        mReactOnDate = new JCheckBox(mLocalizer.msg("reactOnDate", "Date"), getSettings().getBooleanValue(ProgramListSettings.KEY_TAB_REACT_ON_DATE));
        mReactOnChannel = new JCheckBox(mLocalizer.msg("reactOnChannel", "Channel"), getSettings().getBooleanValue(ProgramListSettings.KEY_TAB_REACT_ON_CHANNEL));
        mReactOnFilterChange = new JCheckBox(mLocalizer.msg("reactOnFilterChange", "Filter (also changes)"), getSettings().getBooleanValue(ProgramListSettings.KEY_TAB_REACT_ON_FILTER_CHANGE));
        
        final JLabel tabTimeScrollLabel = new JLabel(mLocalizer.msg("timeButtonBehaviour", "Time buttons behaviour:"));
        mTabTimeScrollNext = new JRadioButton(mLocalizer.msg("timeButtonScrollNext", "Scroll to next occurence of time from shown programs onward"), !getSettings().getBooleanValue(ProgramListSettings.KEY_TAB_TIME_SCROLL_AROUND));
        mTabTimeScrollDay = new JRadioButton(mLocalizer.msg("timeButtonScrollDay", "Scroll to occurence of time on shown day in list"), getSettings().getBooleanValue(ProgramListSettings.KEY_TAB_TIME_SCROLL_AROUND));
        
        ButtonGroup bg = new ButtonGroup();
        
        bg.add(mTabTimeScrollNext);
        bg.add(mTabTimeScrollDay);
        
        panel.add(mShowDateSeparator, CC.xyw(2, 2, 3));
        
        if(VersionCompat.isCenterPanelSupported()) {
          panel.add(mProvideTab, CC.xyw(2, 3, 3));
          
          panel.add(tabReactOnLabel, CC.xyw(3, 5, 2));
          panel.add(mReactOnTime, CC.xy(4, 7));
          panel.add(mReactOnDate, CC.xy(4, 8));
          panel.add(mReactOnChannel, CC.xy(4, 9));
          panel.add(mReactOnFilterChange, CC.xy(4, 10));
          
          panel.add(tabTimeScrollLabel, CC.xyw(3, 12, 2));
          panel.add(mTabTimeScrollNext, CC.xy(4, 14));
          panel.add(mTabTimeScrollDay, CC.xy(4, 15));
        }
        
        mReactOnFilterChange.setEnabled(mProvideTab.isSelected());
        tabTimeScrollLabel.setEnabled(mProvideTab.isSelected() && mReactOnTime.isSelected());
        mTabTimeScrollNext.setEnabled(tabTimeScrollLabel.isEnabled());
        mTabTimeScrollDay.setEnabled(tabTimeScrollLabel.isEnabled());
        tabReactOnLabel.setEnabled(mProvideTab.isSelected());
        mReactOnTime.setEnabled(mProvideTab.isSelected());
        mReactOnDate.setEnabled(mProvideTab.isSelected());
        mReactOnChannel.setEnabled(mProvideTab.isSelected());
        
        mReactOnTime.addItemListener(new ItemListener() {
          @Override
          public void itemStateChanged(ItemEvent e) {
            tabTimeScrollLabel.setEnabled(e.getStateChange() == ItemEvent.SELECTED && mProvideTab.isSelected());
            mTabTimeScrollNext.setEnabled(tabTimeScrollLabel.isEnabled());
            mTabTimeScrollDay.setEnabled(tabTimeScrollLabel.isEnabled());
          }
        });
        
        mProvideTab.addItemListener(new ItemListener() {
          @Override
          public void itemStateChanged(ItemEvent e) {
            tabTimeScrollLabel.setEnabled(e.getStateChange() == ItemEvent.SELECTED && mReactOnTime.isSelected());
            mTabTimeScrollNext.setEnabled(tabTimeScrollLabel.isEnabled());
            mTabTimeScrollDay.setEnabled(tabTimeScrollLabel.isEnabled());
            mReactOnFilterChange.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            tabReactOnLabel.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            mReactOnTime.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            mReactOnDate.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            mReactOnChannel.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
          }
        });
        
        return panel;
      }
      
      @Override
      public void saveSettings() {
        getSettings().setBooleanValue(ProgramListSettings.KEY_PROVIDE_TAB, mProvideTab.isSelected());
        
        getSettings().setBooleanValue(ProgramListSettings.KEY_TAB_REACT_ON_TIME, mReactOnTime.isSelected());
        getSettings().setBooleanValue(ProgramListSettings.KEY_TAB_REACT_ON_DATE, mReactOnDate.isSelected());
        getSettings().setBooleanValue(ProgramListSettings.KEY_TAB_REACT_ON_CHANNEL, mReactOnChannel.isSelected());
        getSettings().setBooleanValue(ProgramListSettings.KEY_TAB_REACT_ON_FILTER_CHANGE, mReactOnFilterChange.isSelected());
        
        getSettings().setBooleanValue(ProgramListSettings.KEY_SHOW_DATE_SEPARATOR, mShowDateSeparator.isSelected());
        getSettings().setBooleanValue(ProgramListSettings.KEY_TAB_TIME_SCROLL_AROUND, mTabTimeScrollDay.isSelected());
        
        addPanel();
      }
      
      @Override
      public String getTitle() {
        return getInfo().getName();
      }
      
      @Override
      public Icon getIcon() {
        return null;
      }      
    };
  }
  
  public PluginCenterPanelWrapper getPluginCenterPanelWrapper() {
    return getSettings().getBooleanValue(ProgramListSettings.KEY_PROVIDE_TAB) ? mWrapper : null;
  }
  
  private class ProgramListCenterPanel extends PluginCenterPanel {
    @Override
    public String getName() {
      return getInfo().getName();
    }

    @Override
    public JPanel getPanel() {
      return mCenterPanelWrapper;
    }
  }
}
