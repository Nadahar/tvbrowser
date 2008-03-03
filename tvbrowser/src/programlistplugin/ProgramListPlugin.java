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
package programlistplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import util.program.ProgramUtilities;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFilter;
import devplugin.Version;

/**
 * Shows all important programs in a time sorted list.
 * 
 * @author Ren� Mach
 * @since 2.7
 */
public class ProgramListPlugin extends Plugin {
  protected static final Localizer mLocalizer = Localizer.getLocalizerFor(ProgramListPlugin.class);
  
  private static Version mVersion = new Version(2,70);
  
  private JDialog mDialog;
  private JComboBox mBox;
  private ProgramFilter mFilter;
  private Properties mSettings;
  private DefaultListModel mModel;
  private ProgramList mList;
  private ProgramPanelSettings mProgramPanelSettings;
  private JCheckBox mShowDescription;
  private JComboBox mFilterBox;
    
  private static ProgramListPlugin mInstance;
  
  /**
   * Creates an instance of this class.
   */
  public ProgramListPlugin() {
    mInstance = this;
  }
  
  public static Version getVersion() {
    return mVersion;
  }
  
  public PluginInfo getInfo() {
    return new PluginInfo(ProgramListPlugin.class,mLocalizer.msg("name","Program list"),mLocalizer.msg("description","Shows programs in a list."),"Ren� Mach");
  }
  
  /**
   * Gets the instance of this plugin.
   * 
   * @return The instance of this plugin.
   */
  public static ProgramListPlugin getInstance() {
    if(mInstance == null) {
      new ProgramListPlugin();
    }
    
    return mInstance;
  }
  
  public void loadSettings(Properties settings) {
    if(settings == null) {
      mSettings = new Properties();
    }
    else {
      mSettings = settings;
    }
  }
  
  public Properties storeSettings() {
    return mSettings;
  }
  
  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent e) {try {
        if (mDialog == null) {
          mDialog = new JDialog(getParentFrame());
          mDialog.setTitle(mLocalizer.msg("name", "Important programs"));
          mDialog.getContentPane().setLayout(new BorderLayout(0,10));
          ((JPanel)mDialog.getContentPane()).setBorder(Borders.DIALOG_BORDER);
          
          UiUtilities.registerForClosing(new WindowClosingIf() {
            public void close() {
              mDialog.setVisible(false);
              saveMe();
            }

            public JRootPane getRootPane() {
              return mDialog.getRootPane();
            }
          });
          
          mModel = new DefaultListModel();
          mProgramPanelSettings = new ProgramPanelSettings(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), mSettings.getProperty("showDescription","true").equals("false"),ProgramPanelSettings.X_AXIS);
          mList = new ProgramList(mModel, mProgramPanelSettings);
          
          mList.addMouseListeners(null);
          
          mBox = new JComboBox(Plugin.getPluginManager()
              .getSubscribedChannels());
          mBox.insertItemAt(mLocalizer.msg("allChannels", "All channels"), 0);
          mBox.setRenderer(new ChannelListCellRenderer());
          mBox.setSelectedIndex(Integer.parseInt(mSettings.getProperty("index",String.valueOf(0))));

          mFilterBox = new JComboBox();
          
          if(mSettings.getProperty("filter") == null) {
            mSettings.setProperty("filter", Plugin.getPluginManager().getFilterManager().getAllFilter().getName());
          }
          
          fillFilterBox();
          
          mFilterBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
              mFilter = (ProgramFilter)mFilterBox.getSelectedItem();
              mSettings.setProperty("filter",mFilter.getName());
              mBox.getItemListeners()[0].itemStateChanged(null);
            }
          });
          
          mBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
              fillProgramList();
              mSettings.setProperty("index", String.valueOf(mBox.getSelectedIndex()));
            }
          });

          CellConstraints cc = new CellConstraints();
          
          JPanel panel = new JPanel(new FormLayout("1dlu,default,3dlu,default:grow","pref,2dlu,pref,2dlu"));
          panel.add(new JLabel(Localizer.getLocalization(Localizer.I18N_CHANNELS) + ":"), cc.xy(2,1));
          panel.add(mBox, cc.xy(4,1));
          panel.add(new JLabel(mLocalizer.msg("filter","Filter:")), cc.xy(2,3));
          panel.add(mFilterBox, cc.xy(4,3));
          
          mShowDescription = new JCheckBox(mLocalizer.msg("showProgramDescription","Show program description"), mSettings.getProperty("showDescription","true").equals("true"));
          mShowDescription.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
              mProgramPanelSettings.setShowOnlyDateAndTitle(e.getStateChange() == ItemEvent.DESELECTED);
              mSettings.setProperty("showDescription", String.valueOf(e.getStateChange() == ItemEvent.SELECTED));
              mList.updateUI();
            }
          });
          
          JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
          close.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              mDialog.setVisible(false);
              saveMe();
            }
          });
          
          JPanel southPanel = new JPanel(new FormLayout("default,0dlu:grow,default","default"));
          southPanel.add(mShowDescription, cc.xy(1,1));
          southPanel.add(close, cc.xy(3,1));

          mDialog.getContentPane().add(panel, BorderLayout.NORTH);
          mDialog.getContentPane().add(new JScrollPane(mList),
              BorderLayout.CENTER);
          mDialog.getContentPane().add(southPanel, BorderLayout.SOUTH);
          
          layoutWindow("programListWindow", mDialog, new Dimension(500,500));          
          
          mDialog.setVisible(true);
          
          mBox.getItemListeners()[0].itemStateChanged(null);
        } else {
          if(!mDialog.isVisible()) {
            fillFilterBox();
          }
          
          mDialog.setVisible(!mDialog.isVisible());
          
          if(mDialog.isVisible()) {
            fillProgramList();
          }
          else {
            saveMe();
          }
        }}catch(Exception ee){ee.printStackTrace();}
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("name","Important programs"));
    action.putValue(Action.SMALL_ICON, createImageIcon("emblems","emblem-important",16));
    action.putValue(Plugin.BIG_ICON, createImageIcon("emblems","emblem-important",22));
    
    return new ActionMenu(action);
  }
  
  private void fillFilterBox() {
    ProgramFilter[] filters = Plugin.getPluginManager().getFilterManager().getAvailableFilters();    
    
    for(ProgramFilter filter : filters) {
      boolean found = false;
      
      for(int i = 0; i < mFilterBox.getItemCount(); i++) {
        if(filter.equals(mFilterBox.getItemAt(i))) {
          found = true;
          break;
        }
      }
      
      if(!found) {
        mFilterBox.addItem(filter);
        
        if(filter.getName().equals(mSettings.getProperty("filter"))) {
          mFilter = filter;
          mFilterBox.setSelectedItem(filter);
        }
      }
    }
    
    for(int i = mFilterBox.getItemCount()-1; i >= 0 ; i--) {
      boolean found = false;
      
      for(ProgramFilter filter : filters) {
        if(filter.equals(mFilterBox.getItemAt(i))) {
          found = true;
          break;
        }
      }
      
      if(!found) {
        mFilterBox.removeItemAt(i);
      }
    }
    
  }
  
  private void fillProgramList() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {try {
        mDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        mModel.clear();
        
        boolean showExpired = false;
        
        Vector<Program> programs = new Vector<Program>();
        Channel[] channels = mBox.getSelectedItem() instanceof String ? Plugin.getPluginManager().getSubscribedChannels() : new Channel[] {(Channel)mBox.getSelectedItem()};

        Date date = channels.length > 1 && mFilter.equals(Plugin.getPluginManager().getFilterManager().getAllFilter())? Plugin.getPluginManager().getCurrentDate() : Date.getCurrentDate();
        
        int startTime = Plugin.getPluginManager().getTvBrowserSettings()
            .getProgramTableStartOfDay();
        int endTime = Plugin.getPluginManager().getTvBrowserSettings()
            .getProgramTableEndOfDay();
        
        for (int d = 0; d < (channels.length > 1 && mFilter.equals(Plugin.getPluginManager().getFilterManager().getAllFilter()) ? 2 : 14); d++) {

          for (int i = 0; i < channels.length; i++) {
            Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(
                date, channels[i]);
            if (it != null) {
              while (it.hasNext()) {
                Program program = it.next();
                if ((showExpired || !program.isExpired()) && mFilter.accept(program)) {
                  if(mFilter.equals(Plugin.getPluginManager().getFilterManager().getAllFilter())) {
                    if ((d == 0 && program.getStartTime() >= startTime)
                        || (d == 1 && program.getStartTime() <= endTime)) {
                      programs.add(program);
                    }
                  }
                  else {
                    programs.add(program);
                  }
                }
              }
            }
          }
          date = date.addDays(1);
        }
        
        if(channels.length > 1) {
          Collections.sort(programs, ProgramUtilities.getProgramComparator());
        }
        
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
  
  private static class ChannelListCellRenderer extends DefaultListCellRenderer {
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      Component c = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
      
      if(c instanceof JLabel && value instanceof Channel) {
        ((JLabel)c).setIcon(UiUtilities.createChannelIcon(((Channel)value).getIcon()));
      }
      
      return c;
    }
  }
}
