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
import java.util.Comparator;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
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

import tvbrowser.core.PluginLoader;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.ui.mainframe.MainFrame;
import util.browserlauncher.Launch;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.ActionMenu;

/**
 * This Tab shows the Plugin-Manager.
 * 
 * @author Martin Oberhauser
 */

public class PluginSettingsTab implements devplugin.SettingsTab, TableModelListener {
  /** Localizer */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(PluginSettingsTab.class);
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
        MainFrame.getInstance().showUpdatePluginsDlg();
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
          return true;
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
    mTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent evt) {
        updateBtns();
      }
    });

    mTable.addMouseListener(new MouseAdapter() {

       public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          int rowIndex = mTable.rowAtPoint(e.getPoint());
          if (rowIndex >=0) {
            mTable.setRowSelectionInterval(rowIndex, rowIndex);
            Object plugin = mTable.getModel().getValueAt(rowIndex, 1);
            JPopupMenu menu;
            
            if(plugin instanceof PluginProxy) {
              menu = createContextMenu((PluginProxy)plugin);
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
            
            if(plugin instanceof PluginProxy) {
              menu = createContextMenu((PluginProxy)plugin);
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
             PluginProxy item = (PluginProxy)mTableModel.getValueAt(rowIndex, 1);
             showInfoDialog(item);
           }
        }
      }
    });

    populatePluginList();
    
    contentPanel.add(new JScrollPane(mTable), cc.xyw(1,3,2));
    
    ButtonBarBuilder builder = new ButtonBarBuilder();

    mInfo = new JButton(mLocalizer.msg("info","Info"), IconLoader.getInstance().getIconFromTheme("status", "dialog-information", 16));
    mInfo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rowIndex = mTable.getSelectedRow();
        if (rowIndex >= 0) {
          PluginProxy item = (PluginProxy)mTableModel.getValueAt(rowIndex, 1);
          mTable.scrollRectToVisible(mTable.getCellRect(rowIndex, 0, true));
          showInfoDialog(item);
        }
      }
    });
    
    builder.addGridded(mInfo);
    builder.addRelatedGap();
    builder.addGlue();
    builder.addRelatedGap();
    
    mRemove = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "edit-delete", 16));
    mRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int rowIndex = mTable.getSelectedRow();
        if (rowIndex >= 0) {
          Object item = mTableModel.getValueAt(rowIndex, 1);
          mTable.scrollRectToVisible(mTable.getCellRect(rowIndex, 0, true));
          removePlugin((PluginProxy)item);
        }
      }
    });
    
    builder.addGridded(mRemove);
    
    contentPanel.add(builder.getPanel(), cc.xyw(1,5,2));
    
    updateBtns();

    return contentPanel;
  }
  
  private JPopupMenu createContextMenu(final InternalPluginProxyIf plugin) {
    JPopupMenu menu = new JPopupMenu();
    
    //configure
    JMenuItem configureMI;
    configureMI = new JMenuItem(mLocalizer.msg("configure", ""),IconLoader.getInstance().getIconFromTheme("category", "preferences-desktop", 16));
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
        String url = "http://www.tvbrowser.org/showHelpFor.php?id=" + plugin.getId() + "&lang=" + System.getProperty("user.language");
        
        Launch.openURL(url);
      }
    });
    menu.add(helpMI);

    return menu;
  }

  private JPopupMenu createContextMenu(final PluginProxy plugin) {
    JPopupMenu menu = new JPopupMenu();
    
    //info
    JMenuItem infoMI = new JMenuItem(mLocalizer.msg("info","Info"), IconLoader.getInstance().getIconFromTheme("status", "dialog-information", 16));
    infoMI.setFont(infoMI.getFont().deriveFont(Font.BOLD));
    infoMI.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        showInfoDialog(plugin);
      }
    });
    menu.add(infoMI);

    //configure
    JMenuItem configureMI;
   	configureMI = new JMenuItem(mLocalizer.msg("configure", ""),IconLoader.getInstance().getIconFromTheme("category", "preferences-desktop", 16));
   	configureMI.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          mSettingsDialog.showSettingsTab(plugin.getId());
        }
      });
    menu.add(configureMI);

    //activate
    JMenuItem enableMI;
    if (plugin.isActivated()) {
      enableMI = new JMenuItem(mLocalizer.msg("deactivate", ""),IconLoader.getInstance().getIconFromTheme("actions", "process-stop", 16));
    }
    else {
      enableMI = new JMenuItem(mLocalizer.msg("activate", ""), IconLoader.getInstance().getIconFromTheme("actions", "view-refresh", 16));

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
    JMenuItem deleteMI = new JMenuItem(mLocalizer.msg("remove","Remove"),  IconLoader.getInstance().getIconFromTheme("actions", "edit-delete", 16));
    deleteMI.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        removePlugin(plugin);
      }
    });
    deleteMI.setEnabled(PluginLoader.getInstance().isPluginDeletable(plugin));
    menu.add(deleteMI);
    
    //help
      JMenuItem helpMI = new JMenuItem(mLocalizer.msg("pluginHelp","Online help"), IconLoader.getInstance().getIconFromTheme("apps", "help-browser", 16));
      helpMI.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          String url = plugin.getInfo().getHelpUrl();
          
          if(url == null) {
            url = "http://www.tvbrowser.org/showHelpFor.php?id=" + plugin.getId() + "&lang=" + System.getProperty("user.language");
          }
          
          Launch.openURL(url);
        }
      });
      menu.add(helpMI);
    
    menu.addSeparator();

    JMenuItem refreshMI = new JMenuItem(mLocalizer.msg("updateInstallPlugin", "Update/Install Plugins"), IconLoader.getInstance().getIconFromTheme("actions", "web-search", 16));
    refreshMI.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showUpdatePluginsDlg();
      }
    });
    menu.add(refreshMI);

    return menu;
  }

  /**
   * Remove a selected Plugin
   */
  private void removePlugin(PluginProxy plugin) {
    String text = mLocalizer.msg("deletePlugin","Really delete the Plugin \"{0}\" ?",plugin.toString());
      
    int result = JOptionPane.showConfirmDialog(mSettingsDialog.getDialog(), text, Localizer.getLocalization(Localizer.I18N_DELETE)+"?", JOptionPane.YES_NO_OPTION);
      
    if (result == JOptionPane.YES_OPTION) {
        
      boolean del = PluginLoader.getInstance().deletePlugin(plugin);
        
      if (del) {
        JOptionPane.showMessageDialog(mSettingsDialog.getDialog(), mLocalizer.msg("successfully","Deletion was sucesfully"));
      } else {
        JOptionPane.showMessageDialog(mSettingsDialog.getDialog(), mLocalizer.msg("failed","Deletion failed"));
      }
        
      populatePluginList();
      mSettingsDialog.createPluginTreeItems();
      mTable.setRowSelectionInterval(0, 0);
    }
  }

  /**
   * Show the Info-Dialog
   *
   */
  private void showInfoDialog(PluginProxy plugin) {
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
   * Populate the Plugin-List
   */
  private void populatePluginList() {
    PluginProxy[] pluginList = PluginProxyManager.getInstance().getAllPlugins();

    while (mTableModel.getRowCount() > 0) {
      mTableModel.removeRow(0);
    }
    
    /* Add base plugins */
    InternalPluginProxyIf[] internalPluginProxies = InternalPluginProxyList.getInstance().getAvailableProxys();
    Arrays.sort(internalPluginProxies, new Comparator<InternalPluginProxyIf>() {
      public int compare(InternalPluginProxyIf o1, InternalPluginProxyIf o2) {
        return o1.getName().compareToIgnoreCase(o2.getName());
      }
    });
    
    for (InternalPluginProxyIf internalPluginProxy : internalPluginProxies) {
      mTableModel.addRow(new Object[]{null, internalPluginProxy});
    }
    
    Arrays.sort(pluginList, new Comparator<PluginProxy>() {
      public int compare(PluginProxy o1, PluginProxy o2) {
        return o1.getInfo().getName().compareToIgnoreCase(o2.getInfo().getName());
      }
    });

    for (int i = 0; i < pluginList.length; i++) {
      mTableModel.addRow(new Object[]{new Boolean(pluginList[i].isActivated()),pluginList[i]});
    }

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

    if ((plugin != null) && ((plugin instanceof PluginProxy && ((PluginProxy)plugin).isActivated()) || plugin instanceof InternalPluginProxyIf)) {
      mInfo.setEnabled(plugin instanceof PluginProxy);
      mRemove.setEnabled(plugin instanceof PluginProxy && PluginLoader.getInstance().isPluginDeletable((PluginProxy)plugin));
    } else {
      mInfo.setEnabled(plugin != null);
      mRemove.setEnabled(plugin != null && PluginLoader.getInstance().isPluginDeletable((PluginProxy)plugin));
    }
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
          PluginProxyManager.getInstance().activatePlugin(plugin);
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
    return null;
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
