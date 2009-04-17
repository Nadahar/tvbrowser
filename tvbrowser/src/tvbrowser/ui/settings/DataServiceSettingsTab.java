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

package tvbrowser.ui.settings;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import tvbrowser.core.ChannelList;
import tvbrowser.core.PluginLoader;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;

public class DataServiceSettingsTab implements devplugin.SettingsTab {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(DataServiceSettingsTab.class);

  private DefaultTableModel mTableModel;

  private JTable mTable;

  private JButton mInfo;

  private JButton mRemove;

  private SettingsDialog mSettingsDialog;

  private JButton mConfigure;

  public DataServiceSettingsTab(SettingsDialog settingsDialog) {
    mSettingsDialog = settingsDialog;
  }

  public JPanel createSettingsPanel() {

    JPanel contentPanel = new JPanel(new FormLayout("default:grow, default",
        "default, 3dlu, fill:default:grow, 3dlu, default"));
    contentPanel.setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();
    contentPanel.add(UiUtilities.createHelpTextArea(mLocalizer.msg(
        "description", "description")), cc.xyw(1, 1, 2));

    mTableModel = new DefaultTableModel() {
      public boolean isCellEditable(int row, int column) {
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
    mTableModel.setColumnIdentifiers(new String[] {
        mLocalizer.msg("active", "Active"),
        mLocalizer.msg("dataService", "Daten-Service") });

    mTable = new JTable(mTableModel);
    mTable.getTableHeader().setReorderingAllowed(false);
    mTable.getTableHeader().setResizingAllowed(false);
    mTable.getColumnModel().getColumn(0).setCellRenderer(
        PluginTableCellRenderer.getInstance());
    mTable.getColumnModel().getColumn(1).setCellRenderer(
        PluginTableCellRenderer.getInstance());
    mTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mTable.setRowHeight(40);
    mTable.setShowVerticalLines(false);
    mTable.setShowHorizontalLines(false);
    // mTable.getModel().addTableModelListener(this);

    int columnWidth = UiUtilities.getStringWidth(mTable.getFont(), mTableModel
        .getColumnName(0)) + 16;
    mTable.getColumnModel().getColumn(0).setPreferredWidth(columnWidth);
    mTable.getColumnModel().getColumn(0).setMaxWidth(columnWidth);

    JScrollPane pane = new JScrollPane(mTable);
    pane.getViewport().setBackground(mTable.getBackground());

    contentPanel.add(pane, cc.xyw(1, 3, 2));

    ButtonBarBuilder builder = new ButtonBarBuilder();

    mInfo = new JButton(mLocalizer.msg("info", "Info"), IconLoader
        .getInstance().getIconFromTheme("status", "dialog-information", 16));
    mInfo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showInformation(getSelection());
      }
    });

    mConfigure = new JButton(mLocalizer.msg("configure", "Configure"), TVBrowserIcons.preferences(TVBrowserIcons.SIZE_SMALL));
    mConfigure.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        configureService(getSelection());
      }
    });
    
    builder.addGridded(mInfo);
    builder.addRelatedGap();
    builder.addGridded(mConfigure);
    builder.addRelatedGap();
    builder.addGlue();
    builder.addRelatedGap();

    mRemove = new JButton(Localizer.getLocalization(Localizer.I18N_DELETE),TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mRemove.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        removeService(getSelection());
      }
    });

    builder.addGridded(mRemove);

    contentPanel.add(builder.getPanel(), cc.xyw(1, 5, 2));

    populateTable();

    mTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent evt) {
        updateBtns();
      }
    });
    
    updateBtns();

   return contentPanel;
  }

  private void populateTable() {
    // clear existing entries
    while (mTableModel.getRowCount() > 0) {
      mTableModel.removeRow(0);
    }
    // add all services
    TvDataServiceProxy[] services = TvDataServiceProxyManager.getInstance().getDataServices();
    Arrays.sort(services, new TvDataServiceProxy.Comparator());
    for (int i = 0; i < services.length; i++) {
      mTableModel.addRow(new Object[]{true, services[i]});
    }
  }
  
  private void showInformation(TvDataServiceProxy service) {
    if (service == null) {
      return;
    }
    // show the dialog
    PluginInfoDialog dialog = new PluginInfoDialog(mSettingsDialog.getDialog(), null, service.getInfo());
    UiUtilities.centerAndShow(dialog);
  }

  private void configureService(TvDataServiceProxy service) {
    if (service == null) {
      return;
    }
    // show the configuration panel
    mSettingsDialog.showSettingsTab(service.getId());
  }
  
  /**
   * get the currently selected proxy (and make the selection visible)
   * @return selected proxy or <code>null</code> if none selected
   */
  private TvDataServiceProxy getSelection() {
    int rowIndex = mTable.getSelectedRow();
    if (rowIndex >= 0) {
      TvDataServiceProxy service = (TvDataServiceProxy) mTableModel.getValueAt(rowIndex, 1);
      mTable.scrollRectToVisible(mTable.getCellRect(rowIndex, 0, true));
      return service;
    }
    return null;
  }

  private void updateBtns() {
    int rowIndex = mTable.getSelectedRow();
    TvDataServiceProxy service = null;
    if (rowIndex >= 0) {
      service = (TvDataServiceProxy) mTable.getValueAt(rowIndex, 1);
    }
    mInfo.setEnabled(service != null);
    mConfigure.setEnabled(service != null);
    mRemove.setEnabled(service != null && PluginLoader.getInstance().isDataServiceDeletable(service));
  }

  private void removeService(TvDataServiceProxy service) {
    if (service == null) {
      return;
    }
    
    // count the removed channels
    int channelCount = 0;
    Channel[] subscribed = ChannelList.getSubscribedChannels();
    for (int i = 0; i < subscribed.length; i++) {
      if (subscribed[i].getDataServiceProxy().equals(service)) {
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
        JOptionPane.showMessageDialog(mSettingsDialog.getDialog(), mLocalizer.msg("successfully","Deletion was succesfully"));
      } else {
        JOptionPane.showMessageDialog(mSettingsDialog.getDialog(), mLocalizer.msg("failed","Deletion failed"));
      }
        
      populateTable();
//      mSettingsDialog.createPluginTreeItems();
      mTable.setRowSelectionInterval(0, 0);
    }
  }

  /**
   * Called by the host-application, if the user wants to save the settings.
   */
  public void saveSettings() {
    // empty
  }

  /**
   * Returns the name of the tab-sheet.
   */
  public Icon getIcon() {
    return TVBrowserIcons.webBrowser(TVBrowserIcons.SIZE_SMALL);
  }

  /**
   * Returns the title of the tab-sheet.
   */
  public String getTitle() {
    return mLocalizer.msg("title", "TV-Data Plugin");
  }

}