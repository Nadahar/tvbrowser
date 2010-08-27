/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourcceforge.net)
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

package tvbrowser.ui.settings;

import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import tvbrowser.core.ChannelList;
import tvbrowser.core.PluginAndDataServiceComparator;
import tvbrowser.core.PluginLoader;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.ui.mainframe.MainFrame;
import util.browserlauncher.Launch;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.InfoIf;
import devplugin.PluginInfo;

/**
 * This Tab shows the Plugin-Manager.
 * 
 * @author Martin Oberhauser
 */

public class PluginSettingsTab implements devplugin.SettingsTab, TableModelListener {
  /** Localizer */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(PluginSettingsTab.class);
  /** Logger */
  private static final Logger mLog = Logger.getLogger(PluginSettingsTab.class.getName());
  /** List of Plugins */
  private JTable mTable;
  /** Buttons of Panel */
  private JButton mInfo, mRemove;
  /** ListModel with Plugins */
  private DefaultTableModel mTableModel;
  /** SettingsDialog */
  private SettingsDialog mSettingsDialog;
  /** The auto update check box */
  private JCheckBox mAutoUpdates;
  private JButton mConfigure;

  /**
   * Creates an instance of this class.
   * 
   * @param settingsDialog The TV-Browser settings dialog.
   */
  public PluginSettingsTab(SettingsDialog settingsDialog) {
    mSettingsDialog = settingsDialog;
  }

  public JPanel createSettingsPanel() {

    JPanel contentPanel = new JPanel(new FormLayout("default:grow, default", "default, 3dlu, fill:default:grow, 3dlu, default"));
    contentPanel.setBorder(Borders.DLU4_BORDER);
    
    CellConstraints cc = new CellConstraints();
    
    JButton update = new JButton(mLocalizer.msg("updateInstallPlugin", "Update/Install Plugins"), IconLoader.getInstance().getIconFromTheme("actions", "web-search", 16));
    
    update.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showUpdatePluginsDlg(false);
      }
    });

    contentPanel.add(update, cc.xy(2,1));
    
    mAutoUpdates = new JCheckBox(mLocalizer.msg("autoUpdates","Find plugin updates automatically"), Settings.propAutoUpdatePlugins.getBoolean());
    mAutoUpdates.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        Settings.propAutoUpdatePlugins.setBoolean(e.getStateChange() == ItemEvent.SELECTED);
      }
    });

    contentPanel.add(mAutoUpdates, cc.xy(1,1));
    
    mTableModel = new DefaultTableModel() {
      public boolean isCellEditable(int row, int column) {
        if (column == 0) {
          if (row >= InternalPluginProxyList.getInstance().getAvailableProxys().length) {
            Object value = getValueAt(row,1);
            
            if(value instanceof PluginProxy) {
              return !Settings.propBlockedPluginArray.isBlocked(((PluginProxy)value));
            }
          }
        }
        return false;
      }

      @Override
      public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
          return Boolean.class;
        }
        return super.getColumnClass(columnIndex);
      }
      
    };
    mTableModel.setColumnCount(2);
    mTableModel.setColumnIdentifiers(new String[] {mLocalizer.msg("active","Active"),mLocalizer.msg("plugin","Plugin")});

    mTable = new JTable(mTableModel);
    mTable.getTableHeader().setReorderingAllowed(false);
    mTable.getTableHeader().setResizingAllowed(false);
    mTable.getColumnModel().getColumn(0).setCellRenderer(PluginTableCellRenderer.getInstance());
    mTable.getColumnModel().getColumn(1).setCellRenderer(PluginTableCellRenderer.getInstance());
    mTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mTable.setRowHeight(40);
    mTable.setShowVerticalLines(false);
    mTable.setShowHorizontalLines(false);
    mTable.getModel().addTableModelListener(this);

    int columnWidth = UiUtilities.getStringWidth(mTable.getFont(),mTableModel.getColumnName(0)) + 16;
    mTable.getColumnModel().getColumn(0).setPreferredWidth(columnWidth);
    mTable.getColumnModel().getColumn(0).setMaxWidth(columnWidth);

    mTable.addMouseListener(new MouseAdapter() {

       public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          int rowIndex = mTable.rowAtPoint(e.getPoint());
          if (rowIndex >=0) {
            mTable.setRowSelectionInterval(rowIndex, rowIndex);
            Object plugin = mTable.getModel().getValueAt(rowIndex, 1);
            JPopupMenu menu;
            
            if(plugin instanceof InfoIf) {
              menu = createContextMenu((InfoIf)plugin);
            }
            else {
              menu = createContextMenu((InternalPluginProxyIf)plugin);
            }
            
            menu.show(mTable, e.getX(),  e.getY());
          }
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          int rowIndex = mTable.rowAtPoint(e.getPoint());
          if (rowIndex >=0) {
            mTable.setRowSelectionInterval(rowIndex, rowIndex);
            Object plugin = mTable.getModel().getValueAt(rowIndex, 1);
            JPopupMenu menu;
            
            if(plugin instanceof InfoIf) {
              menu = createContextMenu((InfoIf)plugin);
            }
            else {
              menu = createContextMenu((InternalPluginProxyIf)plugin);
            }
            
            menu.show(mTable, e.getX(),  e.getY());
          }
        }
      }
       
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
          int rowIndex = mTable.getSelectedRow();
           if (rowIndex >= 0) {
             Object proxy = mTableModel.getValueAt(rowIndex, 1);
             if (proxy instanceof PluginProxy) {
               showInfoDialog((PluginProxy) proxy);
             }
             else if (proxy instanceof InternalPluginProxyIf) {
               showInfoDialog((InternalPluginProxyIf)proxy);
             }
             else if (proxy instanceof TvDataServiceProxy) {
               showInformation((TvDataServiceProxy)proxy);
             }
           }
        }
      }
    });

    populatePluginList();
    
    JScrollPane pane = new JScrollPane(mTable);
    pane.getViewport().setBackground(mTable.getBackground());
    
    contentPanel.add(pane, cc.xyw(1,3,2));
    
    ButtonBarBuilder2 builder = new ButtonBarBuilder2();

    mInfo = new JButton(mLocalizer.msg("info","Info"), IconLoader.getInstance().getIconFromTheme("status", "dialog-information", 16));
    mInfo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object selection = getSelection();
        
        if(selection instanceof PluginProxy) {
          showInfoDialog((PluginProxy)selection);
        }
        else if (selection instanceof InternalPluginProxyIf) {
          showInfoDialog((InternalPluginProxyIf)selection);
        }
        else if (selection instanceof TvDataServiceProxy) {
          showInformation((TvDataServiceProxy)selection);
        }
      }
    });
    
    mConfigure = new JButton(mLocalizer.msg("configure", "Configure"), TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));
    mConfigure.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        Object selection = getSelection();
        
        if(selection instanceof PluginProxy) {
          configurePlugin((PluginProxy)selection);
        }
        else if (selection instanceof InternalPluginProxyIf) {
          mSettingsDialog.showSettingsTab(((InternalPluginProxyIf) selection)
              .getSettingsId());
        }
        else if (selection instanceof TvDataServiceProxy) {
          configureService((TvDataServiceProxy)selection);
        }
      }
    });
    
    builder.addButton(mInfo);
    builder.addRelatedGap();
    builder.addButton(mConfigure);
    builder.addRelatedGap();
    builder.addGlue();
    builder.addRelatedGap();
    
    mRemove = new JButton(Localizer.getLocalization(Localizer.I18N_DELETE),TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if(getSelection() instanceof PluginProxy) {
          removePlugin((PluginProxy)getSelection());
        }
        else {
          removeService((TvDataServiceProxy)getSelection());
        }
      }
    });
    
    builder.addButton(mRemove);
    
    contentPanel.add(builder.getPanel(), cc.xyw(1,5,2));
    
    mTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent evt) {
        updateBtns();
      }
    });
    
    updateBtns();

    return contentPanel;
  }

  private Object getSelection() {
    int rowIndex = mTable.getSelectedRow();
    if (rowIndex >= 0) {
      Object proxy = mTableModel.getValueAt(rowIndex, 1);
      mTable.scrollRectToVisible(mTable.getCellRect(rowIndex, 0, true));
      return proxy;
    }
    return null;
  }
  
  private JPopupMenu createContextMenu(final InternalPluginProxyIf plugin) {
    JPopupMenu menu = new JPopupMenu();
    
    //configure
    JMenuItem configureMI;
    configureMI = new JMenuItem(mLocalizer.msg("configure", ""),TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));
    configureMI.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          mSettingsDialog.showSettingsTab(plugin.getSettingsId());
        }
      });
    menu.add(configureMI);
    
    //help
    JMenuItem helpMI = new JMenuItem(mLocalizer.msg("pluginHelp","Online help"), IconLoader.getInstance().getIconFromTheme("apps", "help-browser", 16));
    helpMI.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        String url = PluginInfo.getHelpUrl(plugin.getId());
        Launch.openURL(url);
      }
    });
    menu.add(helpMI);

    return menu;
  }

  private JPopupMenu createContextMenu(final InfoIf plugin) {
    JPopupMenu menu = new JPopupMenu();
    
    //info
    JMenuItem infoMI = new JMenuItem(mLocalizer.msg("info","Info"), IconLoader.getInstance().getIconFromTheme("status", "dialog-information", 16));
    infoMI.setFont(infoMI.getFont().deriveFont(Font.BOLD));
    infoMI.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        if(plugin instanceof PluginProxy) {
          showInfoDialog((PluginProxy)plugin);
        }
        else if(plugin instanceof TvDataServiceProxy) {
          showInformation((TvDataServiceProxy)plugin);
        }
      }
    });
    menu.add(infoMI);

    //configure
    JMenuItem configureMI;
   	configureMI = new JMenuItem(mLocalizer.msg("configure", ""),TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));
   	configureMI.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          if(plugin instanceof PluginProxy) {
            configurePlugin((PluginProxy)plugin);
          }
          else if(plugin instanceof TvDataServiceProxy) {
            configureService((TvDataServiceProxy)plugin);
          }
        }
      });
    menu.add(configureMI);

    if(plugin instanceof PluginProxy) {
      //activate
      JMenuItem enableMI;
      if (((PluginProxy)plugin).isActivated()) {
        enableMI = new JMenuItem(mLocalizer.msg("deactivate", ""),IconLoader.getInstance().getIconFromTheme("actions", "process-stop", 16));
      }
      else {
        enableMI = new JMenuItem(mLocalizer.msg("activate", ""), TVBrowserIcons.refresh(TVBrowserIcons.SIZE_SMALL));
        enableMI.setEnabled(!Settings.propBlockedPluginArray.isBlocked((PluginProxy)plugin));
      }
      enableMI.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e) {
            int row = mTable.getSelectedRow();
            if (row >= 0) {
              mTableModel.setValueAt(!(Boolean)mTableModel.getValueAt(row, 0), row, 0);
            }
          }
        });
      menu.add(enableMI);

      //delete
      JMenuItem deleteMI = new JMenuItem(mLocalizer.msg("remove","Remove"),  TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
      deleteMI.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          if(plugin instanceof PluginProxy) {
            removePlugin((PluginProxy)plugin);
          }
          else {
            removeService((TvDataServiceProxy)plugin);
          }
        }
      });
      deleteMI.setEnabled(PluginLoader.getInstance().isPluginDeletable((PluginProxy)plugin));
      menu.add(deleteMI);
    }
    
    //help
      JMenuItem helpMI = new JMenuItem(mLocalizer.msg("pluginHelp","Online help"), IconLoader.getInstance().getIconFromTheme("apps", "help-browser", 16));
      helpMI.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          String url = PluginInfo.getHelpUrl(plugin.getId());
          Launch.openURL(url);
        }
      });
      menu.add(helpMI);
    
    menu.addSeparator();

    JMenuItem refreshMI = new JMenuItem(mLocalizer.msg("updateInstallPlugin", "Update/Install Plugins"), IconLoader.getInstance().getIconFromTheme("actions", "web-search", 16));
    refreshMI.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showUpdatePluginsDlg(false);
      }
    });
    menu.add(refreshMI);

    return menu;
  }

  /**
   * Remove a selected Plugin
   */
  private void removePlugin(PluginProxy plugin) {
    if (plugin == null) {
      return;
    }
    String text = mLocalizer.msg("deletePlugin","Really delete the Plugin \"{0}\" ?",plugin.toString());
      
    int result = JOptionPane.showConfirmDialog(mSettingsDialog.getDialog(), text, Localizer.getLocalization(Localizer.I18N_DELETE)+"?", JOptionPane.YES_NO_OPTION);
    if (result == JOptionPane.YES_OPTION) {
        
      if (PluginLoader.getInstance().deletePlugin(plugin)) {
        JOptionPane.showMessageDialog(mSettingsDialog.getDialog(), mLocalizer.msg("successfully","Deletion was succesfully"));
      } else {
        JOptionPane.showMessageDialog(mSettingsDialog.getDialog(), mLocalizer.msg("failed","Deletion failed"));
      }
        
      populatePluginList();
      mSettingsDialog.createPluginTreeItems();
      mTable.setRowSelectionInterval(0, 0);
    }
  }
  
  private void removeService(TvDataServiceProxy service) {
    if (service == null) {
      return;
    }
    
    // count the removed channels
    int channelCount = 0;
    Channel[] subscribed = ChannelList.getSubscribedChannels();
    for (Channel element : subscribed) {
      if (element.getDataServiceProxy().equals(service)) {
        channelCount++;
      }
    }
    
    // show message depending on whether channels will be removed
    String text = mLocalizer.msg("deleteService","Really delete the data service \"{0}\"?",service.getInfo().getName());
    if (channelCount > 0) {
      text = mLocalizer.msg("deleteServiceCount","Really delete the data service \"{0}\"?\nThis will remove {1} of your subscribed channels.",service.getInfo().getName(), channelCount);
    }
    int result = JOptionPane.showConfirmDialog(mSettingsDialog.getDialog(), text, Localizer.getLocalization(Localizer.I18N_DELETE)+"?", JOptionPane.YES_NO_OPTION);
      
    if (result == JOptionPane.YES_OPTION) {
      if (PluginLoader.getInstance().deleteDataService(service)) {
        JOptionPane.showMessageDialog(mSettingsDialog.getDialog(), mLocalizer.msg("dataservice.successfully","Deletion was succesfully"));
      } else {
        JOptionPane.showMessageDialog(mSettingsDialog.getDialog(), mLocalizer.msg("failed","Deletion failed"));
      }
        
      populatePluginList();
//      mSettingsDialog.createPluginTreeItems();
      mTable.setRowSelectionInterval(0, 0);
    }
  }

  /**
   * Show the info dialog
   *
   */
  private void showInfoDialog(PluginProxy plugin) {
    if (plugin == null) {
      return;
    }
    ActionMenu actionMenu = plugin.getButtonAction();
    Action action = null;
    if (actionMenu !=null) {
      action = actionMenu.getAction();
    }
    Icon ico = null;
    if (action != null) {
      ico = (Icon) action.getValue(Action.SMALL_ICON);
    }
    
    if (ico == null) {
      // The plugin has no button icon -> Try the mark icon
      ico = plugin.getMarkIcon();
    }
    
    if (ico == null) {
      ico = new ImageIcon("imgs/Jar16.gif");
    }
    
    PluginInfoDialog dialog = new PluginInfoDialog(mSettingsDialog.getDialog(), ico, plugin.getInfo());
    UiUtilities.centerAndShow(dialog);
  }
  
  /**
   * Show the Info-Dialog for internal plugins
   *
   */
  private void showInfoDialog(InternalPluginProxyIf plugin) {
    JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(MainFrame
        .getInstance()), mLocalizer.msg("internalPlugin",
        "This is an internal plugin which cannot be disabled."), mLocalizer
        .msg("internalPluginTitle", "Internal plugin"),
        JOptionPane.INFORMATION_MESSAGE);
  }
  
  private void showInformation(TvDataServiceProxy service) {
    if (service == null) {
      return;
    }
    // show the dialog
    PluginInfoDialog dialog = new PluginInfoDialog(mSettingsDialog.getDialog(), null, service.getInfo());
    UiUtilities.centerAndShow(dialog);
  }

  /**
   * configure the selected plugin (by switching to respective node in dialog)
   */
  private void configurePlugin(PluginProxy plugin) {
    if (plugin == null) {
      return;
    }
    
    mSettingsDialog.showSettingsTab(plugin.getId());
  }
  
  private void configureService(TvDataServiceProxy service) {
    if (service == null) {
      return;
    }
    // show the configuration panel
    mSettingsDialog.showSettingsTab(service.getId());
  }
  
  /**
   * Populate the Plugin-List
   */
  private void populatePluginList() {   
    while (mTableModel.getRowCount() > 0) {
      mTableModel.removeRow(0);
    }
    
    /* Add base plugins */
    
    InternalPluginProxyIf[] internalPluginProxies = InternalPluginProxyList.getInstance().getAvailableProxys();
    Arrays.sort(internalPluginProxies, new InternalPluginProxyIf.Comparator());
    
    for (InternalPluginProxyIf internalPluginProxy : internalPluginProxies) {
      mTableModel.addRow(new Object[]{true, internalPluginProxy});
    }
    
    /* Add plugins and data services */
    PluginProxy[] pluginList = PluginProxyManager.getInstance().getAllPlugins();
    TvDataServiceProxy[] services = TvDataServiceProxyManager.getInstance().getDataServices();
    
    InfoIf[] infoArr = new InfoIf[pluginList.length + services.length];
    
    System.arraycopy(pluginList,0,infoArr,0,pluginList.length);
    System.arraycopy(services,0,infoArr,pluginList.length, services.length);

    Arrays.sort(infoArr, new PluginAndDataServiceComparator());
    
    for (InfoIf info : infoArr) {
      mTableModel.addRow(new Object[]{true, info});
    }
    
  /*  for (TvDataServiceProxy service : services) {
      mTableModel.addRow(new Object[]{true, service});
    }
    
    Arrays.sort(pluginList, new PluginProxy.Comparator());

    for (PluginProxy element : pluginList) {
      mTableModel.addRow(new Object[]{Boolean.valueOf(element.isActivated()),element});
    }*/

  }

  /**
   * Updates the State of the Buttons
   *
   */
  private void updateBtns() {
    int rowIndex = mTable.getSelectedRow();
    Object plugin = null;
    if (rowIndex >= 0) {
      plugin = mTable.getValueAt(rowIndex, 1);
    }

    mInfo.setEnabled(plugin != null && (plugin instanceof PluginProxy || plugin instanceof TvDataServiceProxy));
    mRemove.setEnabled(plugin != null && ((plugin instanceof PluginProxy && PluginLoader.getInstance().isPluginDeletable((PluginProxy)plugin)) ||
        (plugin instanceof TvDataServiceProxy && PluginLoader.getInstance().isDataServiceDeletable((TvDataServiceProxy)plugin))));
    mConfigure.setEnabled(plugin != null);
  }

  /**
   * Start/Stop was clicked
   * 
   * @param plugin Plugin that is started/stopped
   */
  private void onStartStopBtnClicked(PluginProxy plugin) {
    if (plugin != null) {
      try {
        if (plugin.isActivated()) {
          PluginProxyManager.getInstance().deactivatePlugin(plugin);
        } else {
          PluginProxyManager.getInstance().activatePlugin(plugin, true);
          try {
            PluginProxyManager.getInstance().fireTvBrowserStartFinished(plugin);
          }catch(Throwable t) {
            /* Catch all possible not catched errors that occur in the plugin mehtod*/
            mLog.log(Level.WARNING, "A not catched error occured in 'fireTvBrowserStartFinishedThread' of Plugin '" + plugin +"'.", t);
          }
        }
      } catch (TvBrowserException exc) {
        ErrorHandler.handle(exc);
      }

      mTable.repaint();
      updateBtns();
      mSettingsDialog.invalidateTree();
      mSettingsDialog.createPluginTreeItems();
      MainFrame.getInstance().getToolbar().updatePluginButtons();
    }

    // Update the settings
    String[] deactivatedPlugins = PluginProxyManager.getInstance().getDeactivatedPluginIds();
    Settings.propDeactivatedPlugins.setStringArray(deactivatedPlugins);
  }

  public void saveSettings() {
    
  }

  public Icon getIcon() {
    return TVBrowserIcons.plugin(TVBrowserIcons.SIZE_SMALL);
  }

  public String getTitle() {
    return mLocalizer.msg("plugins", "Plugins");
  }

  public void tableChanged(TableModelEvent e) {
    int row = e.getFirstRow();
    int column = e.getColumn();
    if (column == 0) {
      Cursor oldCursor = mTable.getCursor();
      mTable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      Object plugin = mTableModel.getValueAt(row, 1);
      if (plugin instanceof PluginProxy) {
        onStartStopBtnClicked((PluginProxy) plugin);
      }
      mTable.setCursor(oldCursor);
    }
  }

}
