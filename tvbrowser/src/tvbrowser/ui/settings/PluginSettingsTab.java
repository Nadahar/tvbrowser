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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.core.PluginLoader;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.mainframe.MainFrame;
import util.exc.ErrorHandler;
import util.exc.TvBrowserException;
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

public class PluginSettingsTab implements devplugin.SettingsTab {
  /** Localizer */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(PluginSettingsTab.class);
  /** List of Plugins */
  private JList mList;
  /** Buttons of Panel */
  private JButton mStartStopBtn, mInfo, mRemove;
  /** ListModel with Plugins */
  private DefaultListModel mListModel;
  /** SettingsDialog */
  private SettingsDialog mSettingsDialog;

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
    
    contentPanel.add(new JLabel(mLocalizer.msg("installedPlugins","Installed Plugins")+":"), cc.xy(1,1));
    
    mListModel = new DefaultListModel();
    mList = new JList(mListModel);
    mList.setCellRenderer(new PluginListCellRenderer());
    mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mList.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent evt) {
        updateBtns();
      }
    });

    mList.addMouseListener(new MouseAdapter() {

       public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
          int index = mList.locationToIndex(e.getPoint());
          if (index >=0) {
            mList.setSelectedIndex(index);
            PluginProxy plugin = (PluginProxy)mList.getModel().getElementAt(index);
            JPopupMenu menu = createContextMenu(plugin);
            menu.show(mList, e.getX(),  e.getY());
          }
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
          int index = mList.locationToIndex(e.getPoint());
          if (index >=0) {
            mList.setSelectedIndex(index);
            PluginProxy plugin = (PluginProxy)mList.getModel().getElementAt(index);
            JPopupMenu menu = createContextMenu(plugin);
            menu.show(mList, e.getX(),  e.getY());
          }
        }
      }
       
      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
           int inx = mList.getSelectedIndex();
           if (inx >= 0) {
             PluginProxy item = (PluginProxy)mListModel.getElementAt(inx);
             showInfoDialog(item);
           }
        }
      }
    });

    populatePluginList();
    
    contentPanel.add(new JScrollPane(mList), cc.xyw(1,3,2));
    
    mStartStopBtn = new JButton(mLocalizer.msg("activate", ""), IconLoader.getInstance().getIconFromTheme("actions", "view-refresh", 16));
    mStartStopBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        PluginProxy plugin = (PluginProxy) mList.getSelectedValue();
        onStartStopBtnClicked(plugin);
      }
    });
    
    ButtonBarBuilder builder = new ButtonBarBuilder();

    mInfo = new JButton(mLocalizer.msg("info","Info"), IconLoader.getInstance().getIconFromTheme("status", "dialog-information", 16));
    mInfo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int inx = mList.getSelectedIndex();
        if (inx >= 0) {
          PluginProxy item = (PluginProxy)mListModel.getElementAt(inx);
          mList.ensureIndexIsVisible(inx);
          showInfoDialog(item);
        }
      }
    });
    
    builder.addGridded(mInfo);
    builder.addRelatedGap();
    builder.addGlue();
    builder.addFixed(mStartStopBtn);
    builder.addRelatedGap();
    
    mRemove = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "edit-delete", 16));
    mRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int inx = mList.getSelectedIndex();
        if (inx >= 0) {
          Object item = mListModel.getElementAt(inx);
          mList.ensureIndexIsVisible(inx);
          removePlugin((PluginProxy)item);
        }
      }
    });
    
    builder.addGridded(mRemove);
    
    contentPanel.add(builder.getPanel(), cc.xyw(1,5,2));
    
    updateBtns();

    return contentPanel;
  }


  private JPopupMenu createContextMenu(final PluginProxy plugin) {
    JPopupMenu menu = new JPopupMenu();
    JMenuItem infoMI = new JMenuItem(mLocalizer.msg("info","Info"), IconLoader.getInstance().getIconFromTheme("status", "dialog-information", 16));
    infoMI.setFont(infoMI.getFont().deriveFont(Font.BOLD));
    infoMI.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        showInfoDialog(plugin);
      }
    });
    menu.add(infoMI);

    JMenuItem enableMI;
    if (plugin.isActivated()) {
      enableMI = new JMenuItem(mLocalizer.msg("deactivate", ""),IconLoader.getInstance().getIconFromTheme("actions", "process-stop", 16));
    }
    else {
      enableMI = new JMenuItem(mLocalizer.msg("activate", ""), IconLoader.getInstance().getIconFromTheme("actions", "view-refresh", 16));

    }
    enableMI.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          onStartStopBtnClicked(plugin);
        }
      });
    menu.add(enableMI);

    JMenuItem deleteMI = new JMenuItem(mLocalizer.msg("remove","Remove"),  IconLoader.getInstance().getIconFromTheme("actions", "edit-delete", 16));
    deleteMI.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        removePlugin(plugin);
      }
    });
    deleteMI.setEnabled(PluginLoader.getInstance().isPluginDeletable(plugin));
    menu.add(deleteMI);

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
      
    int result = JOptionPane.showConfirmDialog(mSettingsDialog.getDialog(), text, mLocalizer.msg("delete", "Delete?"), JOptionPane.YES_NO_OPTION);
      
    if (result == JOptionPane.YES_OPTION) {
        
      boolean del = PluginLoader.getInstance().deletePlugin(plugin);
        
      if (del) {
        JOptionPane.showMessageDialog(mSettingsDialog.getDialog(), mLocalizer.msg("successfully","Deletion was sucesfully"));
      } else {
        JOptionPane.showMessageDialog(mSettingsDialog.getDialog(), mLocalizer.msg("failed","Deletion failed"));
      }
        
      populatePluginList();
      mSettingsDialog.createPluginTreeItems();
      mList.setSelectedIndex(0);
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

    mListModel.removeAllElements();
    
    Arrays.sort(pluginList, new Comparator<PluginProxy>() {
      public int compare(PluginProxy o1, PluginProxy o2) {
        return o1.getInfo().getName().compareTo(o2.getInfo().getName());
      }
    });

    for (int i = 0; i < pluginList.length; i++) {
      mListModel.addElement(pluginList[i]);
    }

  }

  /**
   * Updates the State of the Buttons
   *
   */
  private void updateBtns() {
    PluginProxy plugin = (PluginProxy) mList.getSelectedValue();

    if ((plugin != null) && plugin.isActivated()) {
      mStartStopBtn.setEnabled(true);
      mInfo.setEnabled(true);
      mRemove.setEnabled(PluginLoader.getInstance().isPluginDeletable(plugin));
      mStartStopBtn.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "process-stop", 16));
      mStartStopBtn.setText(mLocalizer.msg("deactivate", ""));
    } else {
      mStartStopBtn.setEnabled(plugin != null);
      mInfo.setEnabled(plugin != null);
      mRemove.setEnabled(PluginLoader.getInstance().isPluginDeletable(plugin));
      mStartStopBtn.setIcon(IconLoader.getInstance().getIconFromTheme("actions", "view-refresh", 16));
      mStartStopBtn.setText(mLocalizer.msg("activate", ""));
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

      mList.updateUI();
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

}
