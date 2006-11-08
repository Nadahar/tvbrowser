/*
 * SimpleMarkerPlugin by René Mach
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package simplemarkerplugin;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import util.ui.ExtensionFileFilter;
import util.ui.Localizer;
import util.ui.PictureSettingsPanel;
import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Program;
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
  private DefaultTableModel mModel;
  private ArrayList<MarkListItem> mToDeleteItems;
  private PictureSettingsPanel mPictureSettings;
  private int mSelectedIndex = 0;

  public JPanel createSettingsPanel() {
    mToDeleteItems = new ArrayList<MarkListItem>();
    JPanel panel = new JPanel(new FormLayout("5dlu,default:grow,5dlu",
        "5dlu,fill:default:grow,3dlu,pref,5dlu"));
    CellConstraints cc = new CellConstraints();

    mPictureSettings = new PictureSettingsPanel(SimpleMarkerPlugin.getInstance().getProgramPanelSettings(), true, true);
    
    String[] column = {
        SimpleMarkerPlugin.mLocalizer.msg("settings.list",
            "Additional Mark Lists"), "Icon" };

    MarkList[] lists = SimpleMarkerPlugin.getInstance().getMarkLists();

    Object[][] tableData = new Object[lists.length - 1][2];

    for (int i = 1; i < lists.length; i++) {
      tableData[i - 1][0] = new MarkListItem(lists[i]);
      tableData[i - 1][1] = lists[i].getMarkIcon();
    }

    mModel = new MarkListTableModel(tableData, column);

    mListTable = new JTable(mModel);
    mListTable.getColumnModel().getColumn(1).setCellRenderer(
        new TableRenderer());
    mListTable.getColumnModel().getColumn(1).setMaxWidth(40);

    mListTable.setRowHeight(25);

    mListTable.addMouseListener(this);
    mListTable.addKeyListener(this);
    mListTable.getColumnModel().getColumn(0).setCellEditor(
        new MarkListItemCellEditor());

    JScrollPane pane = new JScrollPane(mListTable);

    panel.add(pane, cc.xy(2, 2));

    JPanel south = new JPanel();
    south.setLayout(new BoxLayout(south, BoxLayout.X_AXIS));
    south.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

    mAdd = new JButton(SimpleMarkerPlugin.mLocalizer.msg("settings.add",
        "Add new list"));
    mAdd.setIcon(SimpleMarkerPlugin.getPluginManager().getIconFromTheme(
        SimpleMarkerPlugin.getInstance(), "actions", "document-new", 16));
    mAdd.addActionListener(this);

    mDelete = new JButton(SimpleMarkerPlugin.mLocalizer.msg("settings.delete",
        "Delete selected list"));
    mDelete.setIcon(SimpleMarkerPlugin.getPluginManager().getIconFromTheme(
        SimpleMarkerPlugin.getInstance(), "actions", "edit-delete", 16));
    mDelete.setEnabled(false);
    mDelete.addActionListener(this);

    south.add(mAdd);
    south.add(Box.createHorizontalGlue());
    south.add(mDelete);

    panel.add(south, cc.xy(2, 4));
    
    final JTabbedPane tabbedPane = new JTabbedPane();
    
    tabbedPane.addTab(SimpleMarkerPlugin.mLocalizer.msg("markList","Mark lists"),panel);
    tabbedPane.addTab(Localizer.getLocalization(Localizer.I18N_PICTURES), mPictureSettings);
    tabbedPane.setSelectedIndex(mSelectedIndex);
    
    tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mSelectedIndex = tabbedPane.getSelectedIndex();
      }
    });
    
    JPanel p = new JPanel(new FormLayout("default:grow","5dlu,fill:default:grow"));
    p.add(tabbedPane, cc.xy(1,2));
    
    return p;
  }

  public void saveSettings() {
    SimpleMarkerPlugin.getInstance().getSettings().setProperty("pictureType", String.valueOf(mPictureSettings.getPictureShowingType()));
    SimpleMarkerPlugin.getInstance().getSettings().setProperty("pictureTimeRangeStart", String.valueOf(mPictureSettings.getPictureTimeRangeStart()));
    SimpleMarkerPlugin.getInstance().getSettings().setProperty("pictureTimeRangeEnd", String.valueOf(mPictureSettings.getPictureTimeRangeEnd()));
    SimpleMarkerPlugin.getInstance().getSettings().setProperty("pictureShowsDescription", String.valueOf(mPictureSettings.getPictureIsShowingDescription()));
    SimpleMarkerPlugin.getInstance().getSettings().setProperty("pictureDuration", String.valueOf(mPictureSettings.getPictureDurationTime()));
    
    if (mListTable.isEditing())
      mListTable.getCellEditor().stopCellEditing();

    for (MarkListItem item : mToDeleteItems)
      item.doChanges();

    for (int i = 0; i < mListTable.getRowCount(); i++)
      ((MarkListItem) mListTable.getValueAt(i, 0)).doChanges();
    
    SimpleMarkerPlugin.getInstance().updateTree();
  }

  public Icon getIcon() {
    return null;
  }

  public String getTitle() {
    return null;
  }

  public void mouseClicked(MouseEvent e) {
    if (mListTable.getSelectedRow() != -1)
      mDelete.setEnabled(true);

    if (SwingUtilities.isLeftMouseButton(e)
        && mListTable.columnAtPoint(e.getPoint()) == 1
        && e.getClickCount() >= 2) {
      int row = mListTable.rowAtPoint(e.getPoint());
      String iconPath = ((MarkListItem) mListTable.getValueAt(row, 0)).getMarkIconFileName();

      JFileChooser chooser = new JFileChooser(iconPath == null ? new File("")
          : (new File(iconPath)).getParentFile());
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

      String msg = SimpleMarkerPlugin.mLocalizer.msg("iconFiles",
          "Icon Files {0}", "*.png,*.jpg, *.gif");
      String[] extArr = { ".png", ".jpg", ".gif" };

      chooser.setFileFilter(new ExtensionFileFilter(extArr, msg));
      chooser.setDialogTitle(SimpleMarkerPlugin.mLocalizer.msg("chooseIcon",
          "Choose icon for {0}", mListTable.getValueAt(row, 0).toString()));

      Window w = UiUtilities.getLastModalChildOf(SimpleMarkerPlugin
          .getInstance().getSuperFrame());

      chooser.showDialog(w, Localizer.getLocalization(Localizer.I18N_SELECT));

      if (chooser.getSelectedFile() != null) {
        Icon icon = SimpleMarkerPlugin.getInstance().getIconForFileName(
            chooser.getSelectedFile().toString());

        if (icon.getIconWidth() != 16 || icon.getIconHeight() != 16) {
          JOptionPane.showMessageDialog(w, SimpleMarkerPlugin.mLocalizer.msg(
              "iconSize", "The icon has to be 16x16 in size."));
          return;
        }

        mListTable.setValueAt(icon, row, 1);

        ((MarkListItem) mListTable.getValueAt(row, 0))
            .setMarkIconFileName(chooser.getSelectedFile().toString());
      }

    }
  }

  public void actionPerformed(ActionEvent e) {
    if (mListTable.isEditing())
      mListTable.getCellEditor().cancelCellEditing();

    if (e.getActionCommand().equals(
        SimpleMarkerPlugin.mLocalizer.msg("settings.add", "Add new list"))) {

      String name = "List " + (mListTable.getRowCount() + 1);

      boolean testName = false;

      int n = -1;

      do {
        testName = false;
        n++;

        for (int i = 0; i < mListTable.getRowCount(); i++)
          if (name.equals(mListTable.getValueAt(i, 0).toString())) {
            testName = true;
            name = "List " + n;
            break;
          }
      } while (testName);

      Object[] row = { new MarkListItem(new MarkList(name), true),
          SimpleMarkerPlugin.getInstance().getIconForFileName(null) };
      mModel.addRow(row);
    }
    if (e.getActionCommand().equals(
        SimpleMarkerPlugin.mLocalizer.msg("settings.delete",
            "Delete selected list(s)"))) {

      int[] rows = mListTable.getSelectedRows();
      for (int i = rows.length - 1; i >= 0; i--) {
        MarkListItem item = ((MarkListItem) mListTable.getValueAt(rows[i], 0));
        item.setIsToDelete();
        mToDeleteItems.add(item);

        mModel.removeRow(rows[i]);
      }

      mDelete.setEnabled(false);
    }
  }

  public void keyPressed(KeyEvent e) {
    mListTable.getRootPane().dispatchEvent(e);

    if (mListTable.getSelectedRow() != -1)
      mDelete.setEnabled(true);
  }

  public void keyReleased(KeyEvent e) {}

  public void keyTyped(KeyEvent e) {}

  public void mouseEntered(MouseEvent e) {}

  public void mouseExited(MouseEvent e) {}

  public void mousePressed(MouseEvent e) {}

  public void mouseReleased(MouseEvent e) {}

  private class TableRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;

    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
      Component c = super.getTableCellRendererComponent(table, value,
          isSelected, hasFocus, row, column);

      JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
      JLabel b = new JLabel((Icon) value);
      b.setOpaque(false);

      p.add(b);
      p.setBackground(c.getBackground());

      return p;
    }
  }

  private class MarkListTableModel extends DefaultTableModel {

    private static final long serialVersionUID = 1L;

    /**
     * @param rowData
     * @param columns
     */
    public MarkListTableModel(Object[][] rowData, Object[] columns) {
      super(rowData, columns);
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
      if (columnIndex == 1) {
        return false;
      }

      return true;
    }
  }

  private class MarkListItem {
    private MarkList mList;

    private boolean mDelete = false;
    private boolean mIsNewList = false;
    private String mName = null;
    private String mIconPath = null;

    /**
     * @param list
     *          The list.
     */
    public MarkListItem(MarkList list) {
      mList = list;
    }

    /**
     * @param list
     *          The list.
     * @param newList
     *          True for this contains a new list.
     */
    public MarkListItem(MarkList list, boolean newList) {
      mList = list;
      mIsNewList = newList;
    }

    /**
     * Sets this items list to be deleted.
     */
    public void setIsToDelete() {
      mDelete = true;
    }

    /**
     * Sets the new name of this items list.
     * 
     * @param name
     *          The new name for the list.
     */
    public void setName(String name) {
      mName = name;
    }

    /**
     * Sets the icon path for the icon to use for this items list.
     * 
     * @param iconPath
     *          The path to the icon.
     */
    public void setMarkIconFileName(String iconPath) {
      mIconPath = iconPath;
    }

    /**
     * Starts the processing of the changes.
     */
    public void doChanges() {
      if (mIsNewList && !mDelete)
        SimpleMarkerPlugin.getInstance().addList(mList);
      if (mDelete && !mIsNewList)
        SimpleMarkerPlugin.getInstance().removeList(mList);
      else if (!mDelete) {
        if (mName != null)
          mList.setName(mName);
        if (mIconPath != null) {
          mList.setMarkIconFileName(mIconPath);
          SimpleMarkerPlugin.getInstance().revalidate(
              mList.toArray(new Program[mList.size()]));
        }
      }
    }
    
    /**
     * @return The icon file name.
     */
    public String getMarkIconFileName() {
      if(mIconPath != null)
        return mIconPath;
      else
        return mList.getMarkIconPath();
    }

    public String toString() {
      if (mName == null)
        return mList.getName();
      else
        return mName;
    }
  }

  private class MarkListItemCellEditor extends AbstractCellEditor implements
      TableCellEditor {

    private static final long serialVersionUID = 1L;
    private MarkListItem mItem;
    private JTextField mTextField;

    /** Constructor */
    public MarkListItemCellEditor() {
      mTextField = new JTextField();
    }

    public boolean isCellEditable(EventObject evt) {
      if (evt instanceof MouseEvent) {
        return ((MouseEvent) evt).getClickCount() >= 2;
      }
      return true;
    }

    public Object getCellEditorValue() {
      String name = mTextField.getText();

      boolean found = false;

      for (int i = 0; i < mListTable.getRowCount(); i++)
        if (name.equals(mListTable.getValueAt(i, 0).toString())) {
          found = true;
          break;
        }

      if (name.length() > 0 && !found)
        mItem.setName(name);

      return mItem;
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int row, int column) {
      mItem = (MarkListItem) value;
      mTextField.setText(value.toString());
      return mTextField;
    }
  }
}
