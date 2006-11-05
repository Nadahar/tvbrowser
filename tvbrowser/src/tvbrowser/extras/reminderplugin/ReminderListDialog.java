/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
package tvbrowser.extras.reminderplugin;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import util.ui.ProgramTableCellRenderer;
import util.ui.SendToPluginDialog;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;
import devplugin.SettingsItem;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class ReminderListDialog extends JDialog implements WindowClosingIf {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ReminderListDialog.class);

  private ReminderList reminderList;

  private JTable mTable;

  public ReminderListDialog(Frame parent, ReminderList list) {
    super(parent, true);
    UiUtilities.registerForClosing(this);

    reminderList = list;
    setTitle(mLocalizer.msg("title", "Reminder"));
    createGui();
  }

  private void createGui() {
    JPanel panel = (JPanel) getContentPane();

    panel.setLayout(new FormLayout("fill:default:grow", "fill:default:grow, 3dlu, default"));

    panel.setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();

    ReminderTableModel model = new ReminderTableModel(reminderList);

    mTable = new JTable();
    mTable.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER)
          mTable.getRootPane().dispatchEvent(e);
      }
    });

    mTable.addMouseListener(new MouseAdapter() {

      public void mousePressed(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
          showPopup(evt);
        }
      }

      public void mouseReleased(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
          showPopup(evt);
        }
      }

      public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 1)) {
          int column = mTable.columnAtPoint(e.getPoint());

          if (column == 1) {
            int row = mTable.rowAtPoint(e.getPoint());
            int height = mTable.getRowHeight(row);

            ((MinutesCellRenderer) mTable.getCellRenderer(row, column)).trackSingleClick(e.getPoint(), mTable, height,
                row, column);
          }
        }
        if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
          int column = mTable.columnAtPoint(e.getPoint());

          if (column == 1)
            return;

          int row = mTable.rowAtPoint(e.getPoint());

          mTable.changeSelection(row, 0, false, false);
          Program p = (Program) mTable.getModel().getValueAt(row, 0);

          PluginManagerImpl.getInstance().handleProgramDoubleClick(p, ReminderPluginProxy.getInstance());
        }
        if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1)) {
          int row = mTable.rowAtPoint(e.getPoint());
          mTable.changeSelection(row, 0, false, false);
          Program p = (Program) mTable.getModel().getValueAt(row, 0);

          PluginManagerImpl.getInstance().handleProgramMiddleClick(p, ReminderPluginProxy.getInstance());
        }
        mTable.updateUI();
      }
    });

    installTableModel(model);

    panel.add(new JScrollPane(mTable), cc.xy(1, 1));

    ButtonBarBuilder builder = ButtonBarBuilder.createLeftToRightBuilder();

    JButton config = new JButton(IconLoader.getInstance().getIconFromTheme("categories", "preferences-desktop", 16));

    config.setToolTipText(mLocalizer.msg("config", "Configure Reminder"));
    
    config.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showSettingsDialog(SettingsItem.REMINDER);
      }
    });

    builder.addFixed(config);
    builder.addRelatedGap();
    
    JButton send = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "edit-copy", 16));
    send.setToolTipText(mLocalizer.msg("send", "Send to other Plugins"));

    send.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showSendDialog();
      }
    });

    builder.addFixed(send);
    builder.addRelatedGap();

    JButton delete = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "edit-delete", 16));
    delete.setToolTipText(mLocalizer.msg("delete", "Remove selected programs from reminder list"));

    delete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteItems();
      }
    });

    builder.addFixed(delete);

    builder.addRelatedGap();

    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (mTable.isEditing()) {
          mTable.getCellEditor().stopCellEditing();
        }
        dispose();
      }
    });

    builder.addGlue();
    builder.addFixed(ok);

    panel.add(builder.getPanel(), cc.xy(1, 3));
    pack();

    getRootPane().setDefaultButton(ok);

    int width = Integer.parseInt(ReminderPlugin.getInstance().getSettings().getProperty("dlgWidth","550"));
    int height = Integer.parseInt(ReminderPlugin.getInstance().getSettings().getProperty("dlgHeight","350"));
    
    setSize(new Dimension(width,height));
  }

  private void installTableModel(ReminderTableModel model) {
    mTable.setModel(model);
    mTable.getColumnModel().getColumn(0).setCellRenderer(new ProgramTableCellRenderer(ReminderPlugin.getInstance().getProgramPanelSettings(false)));
    mTable.getColumnModel().getColumn(1).setCellEditor(new MinutesCellEditor());
    mTable.getColumnModel().getColumn(1).setCellRenderer(new MinutesCellRenderer());
  }

  private void deleteItems() {
    int[] selected = mTable.getSelectedRows();

    if (selected.length < 1 && mTable.getRowCount() > 0) {
      int i = JOptionPane.showConfirmDialog(this, mLocalizer.msg("deleteQuestion", "Should all Reminders be deleted?"),
          mLocalizer.msg("delTitle", "Delete Reminder"), JOptionPane.YES_NO_CANCEL_OPTION);

      if (i == 0) {
        mTable.getSelectionModel().setSelectionInterval(0, mTable.getRowCount() - 1);
        selected = mTable.getSelectedRows();
      }
    }

    if (selected.length > 0) {
      Arrays.sort(selected);

      for (int i = 0; i < selected.length; i++) {
        Program prog = (Program) mTable.getValueAt(selected[i], 0);

        reminderList.removeWithoutChecking(prog);
      }

      final int row = selected[0] - 1;

      installTableModel(new ReminderTableModel(reminderList));
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          mTable.scrollRectToVisible(mTable.getCellRect(row, 0, true));
        };
      });
      
      ReminderPlugin.getInstance().updateRootNode();
    }

  }

  private void showSendDialog() {
    int[] rows = mTable.getSelectedRows();
    Program[] programArr;

    if (rows == null || rows.length == 0) {
      ReminderListItem[] items = reminderList.getReminderItems();
      programArr = new Program[items.length];

      for (int i = 0; i < items.length; i++)
        programArr[i] = items[i].getProgram();
    } else {
      ArrayList programs = new ArrayList();

      for (int i = 0; i < rows.length; i++)
        programs.add((Program) mTable.getValueAt(rows[i], 0));

      programArr = new Program[programs.size()];
      programs.toArray(programArr);
    }

    if (programArr.length > 0) {
      SendToPluginDialog send = new SendToPluginDialog(null, this, programArr);
      send.setVisible(true);
    }
  }

  /**
   * Shows the Popup
   * 
   * @param e Mouse-Event
   */
  private void showPopup(MouseEvent e) {
    int row = mTable.rowAtPoint(e.getPoint());

    mTable.changeSelection(row, 0, false, false);

    Program p = (Program) mTable.getModel().getValueAt(row, 0);

    JPopupMenu menu = PluginManagerImpl.getInstance().createPluginContextMenu(p, ReminderPluginProxy.getInstance());
    menu.show(mTable, e.getX() - 15, e.getY() - 15);
  }

  public void close() {
    dispose();
  }

}