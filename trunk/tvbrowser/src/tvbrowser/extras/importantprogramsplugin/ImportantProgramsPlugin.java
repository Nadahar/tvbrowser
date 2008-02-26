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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.importantprogramsplugin;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.ConfigurationHandler;
import tvbrowser.ui.mainframe.MainFrame;
import tvdataservice.MarkedProgramsList;
import util.exc.ErrorHandler;
import util.ui.DefaultMarkingPrioritySelectionPanel;
import util.ui.Localizer;
import util.ui.MarkPriorityComboBoxRenderer;
import util.ui.ProgramList;

import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFilter;

/**
 * Shows all important programs in a time sorted list.
 * 
 * @author René Mach
 * @since 2.7
 */
public class ImportantProgramsPlugin {
  protected static final Localizer mLocalizer = Localizer.getLocalizerFor(ImportantProgramsPlugin.class);
  
  protected static final String DATAFILE_PREFIX = "importantprogramsplugin.ImportantProgramsPlugin";
  private ConfigurationHandler mConfigurationHandler;
  
  private JDialog mDialog;
  private JComboBox mBox;
  private ProgramFilter mFilter;
  private Properties mSettings;
  private DefaultListModel mModel;
  private ProgramList mList;
    
  private static ImportantProgramsPlugin mInstance;
  
  private ImportantProgramsPlugin() {
    mInstance = this;
    
    mConfigurationHandler = new ConfigurationHandler(DATAFILE_PREFIX);
    loadSettings();
  }
  
  /**
   * Gets the instance of this plugin.
   * 
   * @return The instance of this plugin.
   */
  public static ImportantProgramsPlugin getInstance() {
    if(mInstance == null) {
      new ImportantProgramsPlugin();
    }
    
    return mInstance;
  }
  
  private void loadSettings() {
    try {
      mSettings = mConfigurationHandler.loadSettings();
    } catch (IOException e) {
      ErrorHandler.handle("Could not load important programs settings.", e);
    }
  }
  
  /**
   * Stores the settings of this plugin.
   */
  public void store() {
    try {
      mConfigurationHandler.storeSettings(mSettings);
    } catch (IOException e) {
      ErrorHandler.handle("Could not store settings for important programs.", e);
    }
  }
  
  protected ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {try {
        if (mDialog == null) {
          mDialog = new JDialog(MainFrame.getInstance());
          mDialog.setTitle(mLocalizer.msg("name", "Important programs"));
          mDialog.getContentPane().setLayout(new BorderLayout());
          
          mModel = new DefaultListModel();
          mList = new ProgramList(mModel);
          
          mList.addMouseListeners(null);
                    
          mBox = new JComboBox(DefaultMarkingPrioritySelectionPanel.getMarkingColorNames(false));
          mBox.setRenderer(new MarkPriorityComboBoxRenderer());
          mBox.setSelectedIndex(Integer.parseInt(mSettings.getProperty("priority",String.valueOf(Program.MIN_MARK_PRIORITY))));

          final JComboBox mFilterBox = new JComboBox(Plugin.getPluginManager().getFilterManager().getAvailableFilters());
          
          if(mSettings.getProperty("filter") == null) {
            mFilter = Plugin.getPluginManager().getFilterManager().getAllFilter();
            mFilterBox.setSelectedItem(mFilter);
          }
          else {
            for(int i = 0; i < mFilterBox.getItemCount(); i++) {
              if(((ProgramFilter)mFilterBox.getItemAt(i)).getName().equals(mSettings.getProperty("filter"))) {
                mFilter = (ProgramFilter)mFilterBox.getItemAt(i);
                mFilterBox.setSelectedItem(mFilterBox.getItemAt(i));
                break;
              }
            }
          }
          
          mFilterBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
              mFilter = (ProgramFilter)mFilterBox.getSelectedItem();
              mSettings.setProperty("filter",mFilter.getName());
              mBox.getItemListeners()[0].itemStateChanged(null);
            }
          });
          
          mBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
              showImportantPrograms();
              mSettings.setProperty("priority", String.valueOf(mBox.getSelectedIndex()));
            }
          });

          CellConstraints cc = new CellConstraints();
          
          JPanel panel = new JPanel(new FormLayout("1dlu,default,3dlu,default:grow","pref,2dlu,pref,2dlu"));
          panel.add(new JLabel(mLocalizer.msg("priority","Minimum marking priority:")), cc.xy(2,1));
          panel.add(mBox, cc.xy(4,1));
          panel.add(new JLabel(mLocalizer.msg("filter","Filter:")), cc.xy(2,3));
          panel.add(mFilterBox, cc.xy(4,3));
          
          mDialog.getContentPane().add(panel, BorderLayout.NORTH);
          mDialog.getContentPane().add(new JScrollPane(mList),
              BorderLayout.CENTER);
          
          Settings.layoutWindow("extras.importantProgramsWindow", mDialog, new Dimension(500,500));
          
          mDialog.setVisible(true);
          
          mBox.getItemListeners()[0].itemStateChanged(null);
        } else {
          mDialog.setVisible(!mDialog.isVisible());
          
          if(mDialog.isVisible()) {
            showImportantPrograms();
          }
        }}catch(Exception ee){ee.printStackTrace();}
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("name","Important programs"));
    action.putValue(Action.SMALL_ICON, IconLoader.getInstance().getIconFromTheme("emblems","emblem-important",16));
    action.putValue(Plugin.BIG_ICON, IconLoader.getInstance().getIconFromTheme("emblems","emblem-important",22));
    
    return new ActionMenu(action);
  }
  
  private void showImportantPrograms() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {try {
        mDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        mModel.clear();
        
        Program[] programs = MarkedProgramsList.getInstance().getTimeSortedProgramsForTray(mFilter,mBox.getSelectedIndex(),0);
    
        int index = -1;
    
        for(Program p : programs) {
          mModel.addElement(p);
            
          if (!p.isExpired() && index == -1) {
            index = mModel.getSize() - 1;
          }
        }
        
        mList.ensureIndexIsVisible(mModel.size() - 1);
        mList.ensureIndexIsVisible(index);
        mDialog.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));}catch(Exception e) {e.printStackTrace();}
      }
    });
  }
}
