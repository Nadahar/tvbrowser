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

import java.awt.Dialog;
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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.ui.mainframe.MainFrame;
import util.settings.PluginPictureSettings;

import util.ui.Localizer;
import util.ui.ProgramTableCellRenderer;
import util.ui.SendToPluginDialog;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
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
  
  private ReminderTableModel mModel;
  private ReminderListItem[] mDeletedItems;
  private JButton mUndo, mDelete, mSend;
  private JComboBox mTitleSelection;

  public ReminderListDialog(Frame parent, ReminderList list) {
    super(parent, true);
    UiUtilities.registerForClosing(this);

    reminderList = list;
    setTitle(mLocalizer.msg("title", "Reminder"));
    createGui();
  }
  
  public ReminderListDialog(Dialog parent, ReminderList list) {
    super(parent, true);
    UiUtilities.registerForClosing(this);

    reminderList = list;
    setTitle(mLocalizer.msg("title", "Reminder"));
    createGui();
  }

  private void createGui() {
    JPanel panel = (JPanel) getContentPane();

    panel.setLayout(new FormLayout("default,5dlu,fill:default:grow", "default,5dlu,fill:default:grow, 3dlu, default"));

    panel.setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();

    
    
    mModel = new ReminderTableModel(reminderList, mTitleSelection = new JComboBox());

    mTable = new JTable();
    mTable.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER)
          mTable.getRootPane().dispatchEvent(e);
      }
    });

    mTable.addMouseListener(new MouseAdapter() {
      private Thread mLeftClickThread;
      
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

      public void mouseClicked(final MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 1) && e.getModifiersEx() == 0) {
          int column = mTable.columnAtPoint(e.getPoint());

          if (column == 1) {
            int row = mTable.rowAtPoint(e.getPoint());
            int height = mTable.getRowHeight(row);

            ((MinutesCellRenderer) mTable.getCellRenderer(row, column)).trackSingleClick(e.getPoint(), mTable, height,
                row, column);
          }
          
          mLeftClickThread = new Thread() {
            public void run() {
              try {
                sleep(Plugin.SINGLE_CLICK_WAITING_TIME);
                
                int column = mTable.columnAtPoint(e.getPoint());

                if (column == 1)
                  return;

                int row = mTable.rowAtPoint(e.getPoint());

                mTable.changeSelection(row, 0, false, false);
                Program p = (Program) mTable.getModel().getValueAt(row, 0);
                
                Plugin.getPluginManager().handleProgramSingleClick(p, ReminderPluginProxy.getInstance());      
              } catch (InterruptedException e) { // ignore
              }
            }
          };
          
          mLeftClickThread.setPriority(Thread.MIN_PRIORITY);
          mLeftClickThread.start();
        }
        else if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2) && e.getModifiersEx() == 0) {
          if(mLeftClickThread != null && mLeftClickThread.isAlive()) {
            mLeftClickThread.interrupt();
          }
          
          int column = mTable.columnAtPoint(e.getPoint());

          if (column == 1)
            return;

          int row = mTable.rowAtPoint(e.getPoint());

          mTable.changeSelection(row, 0, false, false);
          Program p = (Program) mTable.getModel().getValueAt(row, 0);

          PluginManagerImpl.getInstance().handleProgramDoubleClick(p, ReminderPluginProxy.getInstance());
        }
        else if (SwingUtilities.isMiddleMouseButton(e) && (e.getClickCount() == 1)) {
          int row = mTable.rowAtPoint(e.getPoint());
          mTable.changeSelection(row, 0, false, false);
          Program p = (Program) mTable.getModel().getValueAt(row, 0);

          PluginManagerImpl.getInstance().handleProgramMiddleClick(p, ReminderPluginProxy.getInstance());
        }
        mTable.repaint();
      }
    });

    installTableModel(mModel);

    panel.add(new JLabel(mLocalizer.msg("titleFilterText","Show only programs with the following title:")), cc.xy(1,1));
    panel.add(mTitleSelection, cc.xy(3,1));
    
    panel.add(new JScrollPane(mTable), cc.xyw(1, 3, 3));

    ButtonBarBuilder builder = ButtonBarBuilder.createLeftToRightBuilder();

    JButton config = new JButton(IconLoader.getInstance().getIconFromTheme("categories", "preferences-system", 16));

    config.setToolTipText(mLocalizer.msg("config", "Configure Reminder"));
    
    config.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showSettingsDialog(SettingsItem.REMINDER);
      }
    });

    builder.addFixed(config);
    builder.addRelatedGap();
    
    mSend = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "edit-copy", 16));
    mSend.setToolTipText(mLocalizer.msg("send", "Send to other Plugins"));
    mSend.setEnabled(mTable.getRowCount() > 0);

    mSend.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        showSendDialog();
      }
    });

    builder.addFixed(mSend);
    builder.addRelatedGap();

    mDelete = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "edit-delete", 16));
    mDelete.setToolTipText(mLocalizer.msg("delete", "Remove all/selected programs from reminder list"));
    mDelete.setEnabled(mTable.getRowCount() > 0);
    
    mDelete.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteItems();
      }
    });

    builder.addFixed(mDelete);
    builder.addRelatedGap();
    
    mUndo = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "edit-undo", 16));
    mUndo.setToolTipText(mLocalizer.msg("undo","Undo"));
    mUndo.setEnabled(false);
    
    mUndo.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        undo();
      }
    });
    
    builder.addFixed(mUndo);
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

    panel.add(builder.getPanel(), cc.xyw(1, 5, 3));

    getRootPane().setDefaultButton(ok);
    
    Settings.layoutWindow("extras.reminderListDlg", this, new Dimension(550,350)); 
  }
  
  private void installTableModel(ReminderTableModel model) {
    mTable.setModel(model);
    mTable.getColumnModel().getColumn(0).setCellRenderer(new ProgramTableCellRenderer(new PluginPictureSettings(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE)));
    mTable.getColumnModel().getColumn(1).setCellEditor(new MinutesCellEditor());
    mTable.getColumnModel().getColumn(1).setCellRenderer(new MinutesCellRenderer());
  }

  private void deleteItems() {
    int[] selected = mTable.getSelectedRows();

    if (selected.length < 1 && mTable.getRowCount() > 0) {
      mTable.getSelectionModel().setSelectionInterval(0, mTable.getRowCount() - 1);
      selected = mTable.getSelectedRows();
    }

    if (selected.length > 0) {
      Arrays.sort(selected);

      ArrayList<ReminderListItem> itemList = new ArrayList<ReminderListItem>();
      
      for (int i = 0; i < selected.length; i++) {
        Program prog = (Program) mTable.getValueAt(selected[i], 0);

        ReminderListItem item = reminderList.removeWithoutChecking(prog);
        
        if(item != null)
          itemList.add(item);
      }
      
      mDeletedItems = itemList.toArray(new ReminderListItem[itemList.size()]);

      final int row = selected[0] - 1;

      installTableModel(new ReminderTableModel(reminderList, mTitleSelection));
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          mTable.scrollRectToVisible(mTable.getCellRect(row, 0, true));
        };
      });
      
      ReminderPlugin.getInstance().updateRootNode(true);
    }
    
    mDelete.setEnabled(mTable.getRowCount() > 0);
    mSend.setEnabled(mTable.getRowCount() > 0);
    mUndo.setEnabled(mDeletedItems != null && mDeletedItems.length > 0);
  }
  
  private void undo() {
    for(ReminderListItem item : mDeletedItems) {
      reminderList.addWithoutChecking(item);
      item.getProgram().mark(ReminderPluginProxy.getInstance());
    }
    
    mDeletedItems = null;
    mUndo.setEnabled(false);
   
    installTableModel(new ReminderTableModel(reminderList, mTitleSelection));
    ReminderPlugin.getInstance().updateRootNode(true);
    
    mDelete.setEnabled(mTable.getRowCount() > 0);
    mSend.setEnabled(mTable.getRowCount() > 0);
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
      ArrayList<Program> programs = new ArrayList<Program>();

      for (int i = 0; i < rows.length; i++)
        programs.add((Program) mTable.getValueAt(rows[i], 0));

      programArr = programs.toArray(new Program[programs.size()]);
    }

    if (programArr.length > 0) {
      SendToPluginDialog send = new SendToPluginDialog(ReminderPluginProxy.getInstance(), this, programArr);
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