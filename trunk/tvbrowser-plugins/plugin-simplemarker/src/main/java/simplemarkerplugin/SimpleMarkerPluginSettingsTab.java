/*
 * SimpleMarkerPlugin by René Mach
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * SVN information:
 *     $Date: 2011-03-26 21:21:11 +0100 (Sa, 26 Mrz 2011) $
 *   $Author: bananeweizen $
 * $Revision: 6974 $
 */
package simplemarkerplugin;

import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import simplemarkerplugin.table.MarkListPriorityCellEditor;
import simplemarkerplugin.table.MarkListProgramImportanceCellEditor;
import simplemarkerplugin.table.MarkListSendToPluginCellEditor;
import simplemarkerplugin.table.MarkListTableModel;
import simplemarkerplugin.table.MarkerIDRenderer;
import simplemarkerplugin.table.MarkerIconRenderer;
import simplemarkerplugin.table.MarkerPriorityRenderer;
import simplemarkerplugin.table.MarkerProgramImportanceRenderer;
import simplemarkerplugin.table.MarkerSendToPluginRenderer;
import util.io.IOUtilities;
import util.ui.ExtensionFileFilter;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.SettingsItem;
import devplugin.SettingsTab;

/**
 * SimpleMarkerPlugin 1.4 Plugin for TV-Browser since version 2.3 to only mark
 * programs and add them to the Plugin tree.
 *
 * (Formerly known as Just_Mark ;-))
 *
 * The SettingsTab for the SimpleMarkerPlugin.
 *
 * @author René Mach
 *
 */
public class SimpleMarkerPluginSettingsTab implements SettingsTab,
    MouseListener, ActionListener, KeyListener {

  private JTable mListTable;
  private JButton mAdd, mDelete;
  private MarkListTableModel mModel;
  private JEditorPane mHelpLabel;
  private JCheckBox mShowDeletedPrograms;
  private ArrayList<MarkList> mMarkLists;

  public JPanel createSettingsPanel() {
    JPanel panel = new JPanel(new FormLayout("5dlu,default:grow,5dlu",
        "default,5dlu,fill:default:grow,3dlu,pref,10dlu,pref,5dlu"));
    CellConstraints cc = new CellConstraints();

    mShowDeletedPrograms = new JCheckBox(SimpleMarkerPlugin.getLocalizer().msg(
        "settings.informAboutDeletedPrograms",
        "Inform about program that were deleted during a data update"),
        SimpleMarkerPlugin.getInstance().getSettings().showDeletedPrograms());

    panel.add(mShowDeletedPrograms, cc.xy(2,1));

    mMarkLists = new ArrayList<MarkList>();
    MarkList[] lists = SimpleMarkerPlugin.getInstance().getMarkLists();
    for (MarkList m:lists) {
      mMarkLists.add((MarkList)m.clone());
    }

    mModel = new MarkListTableModel(mMarkLists);

    mListTable = new JTable(mModel);
    mListTable.getTableHeader().setReorderingAllowed(false);
    mListTable.getTableHeader().setResizingAllowed(false);
    mListTable.getColumnModel().getColumn(0).setCellRenderer(new MarkerIDRenderer());
    mListTable.getColumnModel().getColumn(1).setCellRenderer(new MarkerIconRenderer());

    int columnWidth = UiUtilities.getStringWidth(mListTable.getFont(),mModel.getColumnName(1)) + 10;
    mListTable.getColumnModel().getColumn(1).setMaxWidth(columnWidth);
    mListTable.getColumnModel().getColumn(1).setMinWidth(columnWidth);

    mListTable.getColumnModel().getColumn(2).setCellRenderer(new MarkerPriorityRenderer());
    columnWidth = UiUtilities.getStringWidth(mListTable.getFont(),mModel.getColumnName(2)) + 10;
    mListTable.getColumnModel().getColumn(2).setMaxWidth(columnWidth);
    mListTable.getColumnModel().getColumn(2).setMinWidth(columnWidth);

    mListTable.getColumnModel().getColumn(3).setCellRenderer(new MarkerProgramImportanceRenderer());
    columnWidth = UiUtilities.getStringWidth(mListTable.getFont(),mModel.getColumnName(3)) + 10;
    mListTable.getColumnModel().getColumn(3).setMaxWidth(columnWidth);
    mListTable.getColumnModel().getColumn(3).setMinWidth(columnWidth);

    mListTable.getColumnModel().getColumn(4).setCellRenderer(new MarkerSendToPluginRenderer());
    columnWidth = UiUtilities.getStringWidth(mListTable.getFont(),mModel.getColumnName(4)) + 10;
    mListTable.getColumnModel().getColumn(4).setMaxWidth(columnWidth);
    mListTable.getColumnModel().getColumn(4).setMinWidth(columnWidth);

    mListTable.setRowHeight(25);

    mListTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()) {
          mDelete.setEnabled(mListTable.getSelectionModel().getMinSelectionIndex() > 0);
        }
      }
    });

    mListTable.addMouseListener(this);
    mListTable.addKeyListener(this);
    mListTable.getColumnModel().getColumn(2).setCellEditor(new MarkListPriorityCellEditor());
    mListTable.getColumnModel().getColumn(3).setCellEditor(new MarkListProgramImportanceCellEditor());
    mListTable.getColumnModel().getColumn(4).setCellEditor(new MarkListSendToPluginCellEditor());

    JScrollPane pane = new JScrollPane(mListTable);

    panel.add(pane, cc.xy(2, 3));

    JPanel south = new JPanel();
    south.setLayout(new BoxLayout(south, BoxLayout.X_AXIS));
    south.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

    mAdd = new JButton(SimpleMarkerPlugin.getLocalizer().msg("settings.add",
        "Add new list"));
    mAdd.setIcon(TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
    mAdd.addActionListener(this);

    mDelete = new JButton(SimpleMarkerPlugin.getLocalizer().msg("settings.delete",
        "Delete selected list"));
    mDelete.setIcon(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mDelete.setEnabled(false);
    mDelete.addActionListener(this);

    south.add(mAdd);
    south.add(Box.createHorizontalGlue());
    south.add(mDelete);

    panel.add(south, cc.xy(2, 5));

    mHelpLabel = UiUtilities.createHtmlHelpTextArea(SimpleMarkerPlugin.getLocalizer().msg("settings.prioHelp","The mark priority is used for selecting the marking color. The marking colors of the priorities can be change in the <a href=\"#link\">program panel settings</a>. If a program is marked by more than one plugin/list the color with the highest priority given by the marking plugins/lists is used."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          if(e.getDescription() != null) {
            if(e.getDescription().equals("#link1")) {
              Plugin.getPluginManager().showSettings(SettingsItem.PROGRAMPANELMARKING);
            }
            else if(e.getDescription().equals("#link2")) {
              Plugin.getPluginManager().showSettings(SettingsItem.PROGRAMPANELLOOK);
            }
          }
        }
      }
    });

    panel.add(mHelpLabel, cc.xy(2,7));


    JPanel p = new JPanel(new FormLayout("default:grow","5dlu,fill:default:grow"));
    p.add(panel, cc.xy(1,2));

    return p;
  }

  public void saveSettings() {
    SimpleMarkerPlugin.getInstance().getSettings().setShowDeletedPrograms(
        mShowDeletedPrograms.isSelected());

    if (mListTable.isEditing()) {
      mListTable.getCellEditor().stopCellEditing();
    }

    SimpleMarkerPlugin.getInstance().setMarkLists(mMarkLists.toArray(new MarkList[mMarkLists.size()]));
    SimpleMarkerPlugin.getInstance().save();
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return null;
  }

  public void mouseClicked(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)
        && mListTable.columnAtPoint(e.getPoint()) == 1
        && e.getClickCount() >= 2) {
      chooseIcon(mListTable.rowAtPoint(e.getPoint()));
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (mListTable.isEditing()) {
      mListTable.getCellEditor().cancelCellEditing();
    }

    if (e.getSource() == mAdd) {
      int n = mListTable.getRowCount() + 1;

      String name = SimpleMarkerPlugin.getLocalizer().msg("settings.listName","List {0}", n);

      for(int i = 0; i < mListTable.getRowCount(); i++) {
        if (name.equals(mListTable.getValueAt(i, 0).toString())) {
          name = SimpleMarkerPlugin.getLocalizer().msg("settings.listName","List {0}", ++n);
          i = -1;
        }
      }

      MarkList list = new MarkList(name);
      mModel.addRow(list);
      mListTable.setRowSelectionInterval(mListTable.getRowCount()-1,mListTable.getRowCount()-1);
    }
    if (e.getActionCommand().equals(SimpleMarkerPlugin.getLocalizer().msg("settings.delete",
    "Delete selected list"))) {
      deleteSelectedRows();
    }
  }

  private void deleteSelectedRows() {
    int selectedIndex = mListTable.getSelectedRow();
    int[] rows = mListTable.getSelectedRows();
    for (int i = rows.length - 1; i >= 0; i--) {
      mModel.removeRow(rows[i]);
    }
    if ((selectedIndex > 0) && (selectedIndex<mListTable.getRowCount())) {
      mListTable.setRowSelectionInterval(selectedIndex,selectedIndex);
    }

    mDelete.setEnabled(mListTable.getSelectedRowCount() > 0);
  }

  public void keyPressed(KeyEvent e) {
    mListTable.getRootPane().dispatchEvent(e);

    if (mListTable.getSelectionModel().getMinSelectionIndex() > 0) {
      mDelete.setEnabled(true);
    }

    if(e.getKeyCode() == KeyEvent.VK_DELETE) {
      deleteSelectedRows();
      e.consume();
    }
    if(e.getKeyCode() == KeyEvent.VK_F2 && mListTable.getSelectedColumn() == 1) {
      chooseIcon(mListTable.getSelectedRow());
    }
  }

  public void keyReleased(KeyEvent e) {}

  public void keyTyped(KeyEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {
    if(e.isPopupTrigger() && mListTable.rowAtPoint(e.getPoint()) > -1) {
      int row = mListTable.rowAtPoint(e.getPoint());
      mListTable.setRowSelectionInterval(row,row);
      showPopupMenu(e.getPoint(), row);
    }
  }

  public void mouseReleased(MouseEvent e) {
    if(e.isPopupTrigger() && mListTable.rowAtPoint(e.getPoint()) > -1) {
      int row = mListTable.rowAtPoint(e.getPoint());
      mListTable.setRowSelectionInterval(row,row);
      showPopupMenu(e.getPoint(), row);
    }
  }

  private void showPopupMenu(Point p, final int row) {
    JPopupMenu popupMenu = new JPopupMenu();

    JMenuItem item = new JMenuItem(SimpleMarkerPlugin.getLocalizer().msg("settings.delete",
    "Delete selected list"));
    item.setIcon(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    item.addActionListener(this);

    if(mListTable.getSelectionModel().getMinSelectionIndex() > 0) {
      popupMenu.add(item);
    }

    item = new JMenuItem(SimpleMarkerPlugin.getLocalizer().msg("settings.changeIcon",
    "Change list icon"));
    item.setIcon(TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        chooseIcon(row);
      }
    });

    popupMenu.add(item);

    popupMenu.show(mListTable, p.x, p.y);
  }

  private void chooseIcon(int row) {
    MarkList markList = (MarkList) mListTable.getValueAt(row, 0);

    String iconPath = markList.getMarkIconPath();

    JFileChooser chooser = new JFileChooser(iconPath == null ? new File("")
        : (new File(iconPath)).getParentFile());
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

    String msg = SimpleMarkerPlugin.getLocalizer().msg("iconFiles",
        "Icon Files ({0})", "*.png,*.jpg, *.gif");
    String[] extArr = { ".png", ".jpg", ".gif" };

    chooser.setFileFilter(new ExtensionFileFilter(extArr, msg));
    chooser.setDialogTitle(SimpleMarkerPlugin.getLocalizer().msg("chooseIcon",
        "Choose icon for '{0}'", markList.getName()));

    Window w = UiUtilities.getLastModalChildOf(SimpleMarkerPlugin
        .getInstance().getSuperFrame());

    if (chooser.showDialog(w, Localizer.getLocalization(Localizer.I18N_SELECT)) == JFileChooser.APPROVE_OPTION) {
      if (chooser.getSelectedFile() != null) {
        File dir = new File(Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome(),"simplemarkericons");

        if(!dir.isDirectory()) {
          dir.mkdir();
        }

        String ext =  chooser.getSelectedFile().getName();
        ext = ext.substring(ext.lastIndexOf('.'));

        Icon icon = SimpleMarkerPlugin.getInstance().getIconForFileName(
            chooser.getSelectedFile().getAbsolutePath());

        if (icon.getIconWidth() != 16 || icon.getIconHeight() != 16) {
          JOptionPane.showMessageDialog(w, SimpleMarkerPlugin.getLocalizer().msg(
              "iconSize", "The icon has to be 16x16 in size."));
          return;
        }

        if(!new File(dir, mListTable.getValueAt(row, 0).toString() + ext).equals(chooser.getSelectedFile())) {
          try {
            IOUtilities.copy(chooser.getSelectedFile(),new File(dir,markList.getName() + ext));
          } catch (IOException e1) {
            e1.printStackTrace();
          }
        }

        SimpleMarkerPlugin.getInstance().getIconForFileName(dir + "/" + markList.getName() + ext);
        mListTable.setValueAt(markList.getName() + ext, row, 1);
      }
    }
  }
}
