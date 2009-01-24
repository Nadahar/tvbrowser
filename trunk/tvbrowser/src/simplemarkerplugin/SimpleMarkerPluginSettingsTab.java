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

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import util.io.IOUtilities;
import util.ui.ExtensionFileFilter;
import util.ui.Localizer;
import util.ui.MarkPriorityComboBoxRenderer;
import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Program;
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
  private DefaultTableModel mModel;
  private ArrayList<MarkListItem> mToDeleteItems;
  private JEditorPane mHelpLabel;
  private JCheckBox mShowDeletedPrograms;

  public JPanel createSettingsPanel() {
    mToDeleteItems = new ArrayList<MarkListItem>();
    JPanel panel = new JPanel(new FormLayout("5dlu,default:grow,5dlu",
        "default,5dlu,fill:default:grow,3dlu,pref,10dlu,pref,5dlu"));
    CellConstraints cc = new CellConstraints();
    
    mShowDeletedPrograms = new JCheckBox(SimpleMarkerPlugin.mLocalizer.msg(
        "settings.informAboutDeletedPrograms",
        "Inform about program that were deleted during a data update"),
        SimpleMarkerPlugin.getInstance().getSettings().showDeletedPrograms());    
    
    panel.add(mShowDeletedPrograms, cc.xy(2,1));
    
    String[] column = {
        SimpleMarkerPlugin.mLocalizer.msg("settings.list",
            "Additional Mark List"), "Icon" , SimpleMarkerPlugin.mLocalizer.msg("settings.markPriority",
            "Mark priority")};

    MarkList[] lists = SimpleMarkerPlugin.getInstance().getMarkLists();

    Object[][] tableData = new Object[lists.length][3];

    for (int i = 0; i < lists.length; i++) {
      tableData[i][0] = new MarkListItem(lists[i]);
      tableData[i][1] = lists[i].getMarkIcon();
      tableData[i][2] = lists[i].getMarkPriority();
    }

    mModel = new MarkListTableModel(tableData, column);

    mListTable = new JTable(mModel);
    mListTable.getColumnModel().getColumn(0).setCellRenderer(
        new TableRenderer());
    mListTable.getColumnModel().getColumn(1).setCellRenderer(
        new TableRenderer());
    mListTable.getColumnModel().getColumn(1).setMaxWidth(Sizes.dialogUnitXAsPixel(20,mListTable));
    mListTable.getColumnModel().getColumn(2).setCellRenderer(
        new TableRenderer());
    mListTable.getColumnModel().getColumn(2).setMaxWidth(Sizes.dialogUnitXAsPixel(70,mListTable));
    mListTable.getColumnModel().getColumn(2).setMinWidth(Sizes.dialogUnitXAsPixel(70,mListTable));
    
    mListTable.setRowHeight(25);
    
    mListTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting())
          mDelete.setEnabled(mListTable.getSelectionModel().getMinSelectionIndex() > 0);
      }
    });
    
    mListTable.addMouseListener(this);    
    mListTable.addKeyListener(this);
    mListTable.getColumnModel().getColumn(0).setCellEditor(
        new MarkListItemCellEditor(MarkListItemCellEditor.TEXT_EDITOR));
    mListTable.getColumnModel().getColumn(2).setCellEditor(
        new MarkListItemCellEditor(MarkListItemCellEditor.COMBO_BOX_EDITOR));

    JScrollPane pane = new JScrollPane(mListTable);

    panel.add(pane, cc.xy(2, 3));

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

    panel.add(south, cc.xy(2, 5));
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(SimpleMarkerPlugin.mLocalizer.msg("settings.prioHelp","The mark priority is used for selecting the marking color. The marking colors of the priorities can be change in the <a href=\"#link\">program panel settings</a>. If a program is marked by more than one plugin/list the color with the highest priority given by the marking plugins/lists is used."), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SimpleMarkerPlugin.getPluginManager().showSettings(SettingsItem.PROGRAMPANELMARKING);
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
    
    if (mListTable.isEditing())
      mListTable.getCellEditor().stopCellEditing();

    for (MarkListItem item : mToDeleteItems)
      item.doChanges();

    for (int i = 0; i < mListTable.getRowCount(); i++)
      ((MarkListItem) mListTable.getValueAt(i, 0)).doChanges();
        
    SimpleMarkerPlugin.getInstance().updateTree();
    
    if(mToDeleteItems.isEmpty()) {
      SimpleMarkerPlugin.getInstance().save();
    }
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
    if (mListTable.isEditing())
      mListTable.getCellEditor().cancelCellEditing();

    if (e.getSource() == mAdd) {
      int n = mListTable.getRowCount() + 1;
      
      String name = SimpleMarkerPlugin.mLocalizer.msg("settings.listName","List {0}", n);
      
      for(int i = 0; i < mListTable.getRowCount(); i++) {
        if (name.equals(mListTable.getValueAt(i, 0).toString())) {
          name = SimpleMarkerPlugin.mLocalizer.msg("settings.listName","List {0}", ++n);
          i = -1;
        }
      }
      
      MarkListItem item = new MarkListItem(new MarkList(name), true);
      
      Object[] row = { item,
          SimpleMarkerPlugin.getInstance().getIconForFileName(null), Program.MIN_MARK_PRIORITY };
      mModel.addRow(row);
      mListTable.setRowSelectionInterval(mListTable.getRowCount()-1,mListTable.getRowCount()-1);
      checkForIcon(item);
    }
    if (e.getActionCommand().equals(SimpleMarkerPlugin.mLocalizer.msg("settings.delete",
    "Delete selected list"))) {
      deleteSelectedRows();
    }
  }
  
  private void deleteSelectedRows() {
    int selectedIndex = mListTable.getSelectedRow();
    int[] rows = mListTable.getSelectedRows();
    for (int i = rows.length - 1; i >= 0; i--) {
      MarkListItem item = ((MarkListItem) mListTable.getValueAt(rows[i], 0));
      item.setIsToDelete();
      mToDeleteItems.add(item);

      mModel.removeRow(rows[i]);
    }
    if ((selectedIndex > 0) && (selectedIndex<mListTable.getRowCount())) {
      mListTable.setRowSelectionInterval(selectedIndex,selectedIndex);
    }

    mDelete.setEnabled(mListTable.getSelectedRowCount() > 0);
  }

  public void keyPressed(KeyEvent e) {
    mListTable.getRootPane().dispatchEvent(e);

    if (mListTable.getSelectionModel().getMinSelectionIndex() > 0)
      mDelete.setEnabled(true);
    
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
    
    JMenuItem item = new JMenuItem(SimpleMarkerPlugin.mLocalizer.msg("settings.delete",
    "Delete selected list"));
    item.setIcon(SimpleMarkerPlugin.getPluginManager().getIconFromTheme(
        SimpleMarkerPlugin.getInstance(), "actions", "edit-delete", 16));
    item.addActionListener(this);
    
    if(mListTable.getSelectionModel().getMinSelectionIndex() > 0)
      popupMenu.add(item);
    
    item = new JMenuItem(SimpleMarkerPlugin.mLocalizer.msg("settings.changeIcon",
    "Change list icon"));
    item.setIcon(SimpleMarkerPlugin.getPluginManager().getIconFromTheme(
        SimpleMarkerPlugin.getInstance(), "actions", "document-edit", 16));
    item.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        chooseIcon(row);
      }
    });
    
    popupMenu.add(item);
    
    popupMenu.show(mListTable, p.x, p.y);
  }

  private static class TableRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 1L;

    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
      Component c = super.getTableCellRendererComponent(table, value,
          isSelected, hasFocus, row, column);

      JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));

      Color color = c.getBackground();
      
      if(value instanceof Icon) {      
        JLabel b = new JLabel((Icon) value);
        b.setOpaque(false);

        p.add(b);
      }
      else if(value instanceof Integer) {
        JLabel b = new JLabel();
                
        switch((Integer)value) {
          case Program.MIN_MARK_PRIORITY: b.setText(SimpleMarkerPlugin.mLocalizer.msg("settings.min","Minimum"));break;
          case Program.LOWER_MEDIUM_MARK_PRIORITY: b.setText(SimpleMarkerPlugin.mLocalizer.msg("settings.lowerMedium","Lower medium"));break;
          case Program.MEDIUM_MARK_PRIORITY: b.setText(SimpleMarkerPlugin.mLocalizer.msg("settings.medium","Medium"));break;
          case Program.HIGHER_MEDIUM_MARK_PRIORITY: b.setText(SimpleMarkerPlugin.mLocalizer.msg("settings.higherMedium","Higher Medium"));break;
          case Program.MAX_MARK_PRIORITY: b.setText(SimpleMarkerPlugin.mLocalizer.msg("settings.max","Maximum"));break;
        
          default: b.setText(SimpleMarkerPlugin.mLocalizer.msg("settings.noPriority","None"));break;
        }
        Color testColor = SimpleMarkerPlugin.getPluginManager().getTvBrowserSettings().getColorForMarkingPriority((Integer)value);
        
        if(color != null && !isSelected) {
          color = testColor;
        }
        
        b.setOpaque(false);
        b.setForeground(c.getForeground());
        p.add(b);
      }
      else {
        JLabel b = new JLabel();
        b.setOpaque(false);
        b.setForeground(c.getForeground());
        
        int count = ((MarkListItem)value).getProgramCountOfList();
        
        if(count > 0)
          b.setText(value.toString() + " [" + count + "]");
        else
          b.setText(value.toString());
        
        p.setLayout(new FlowLayout(FlowLayout.LEFT));
        p.add(b);
      }
      
      p.setBackground(color);
      
      return p;
    }
  }

  private static class MarkListTableModel extends DefaultTableModel {

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

  private static class MarkListItem {
    private MarkList mList;

    private boolean mDelete = false;
    private boolean mIsNewList = false;
    private String mName = null;
    private String mIconPath = null;
    private int mMarkPriority = -2;

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
     * Sets the mark priority for this list item.
     * 
     * @param markPriority The mark priority.
     */
    public void setMarkPriority(int markPriority) {
      mMarkPriority = markPriority;
    }
    
    /**
     * Gets the number of programs contained by the mark list of this item.
     * 
     * @return The number of programs contained by the mark list of this item.
     */
    public int getProgramCountOfList() {
      return mList.size();
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
        if (mMarkPriority != -2) {
          mList.setMarkPriority(mMarkPriority);
          SimpleMarkerPlugin.getInstance().revalidate(
              mList.toArray(new Program[mList.size()]));
        }
      }
    }
    
    /**
     * @return The mark priority.
     */
    public int getMarkPriority() {
      if(mMarkPriority != -1)
        return mMarkPriority;
      else
        return mList.getMarkPriority();
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
    /** The text editor type for this editor*/
    public final static int TEXT_EDITOR = 0;
    /** The combo box editor type for thid editor*/
    public final static int COMBO_BOX_EDITOR = 1;
    
    private static final long serialVersionUID = 1L;
    private MarkListItem mItem;
    private JTextField mTextField;
    private JComboBox mComboBox;
    
    private final String[] prioValues = {
        SimpleMarkerPlugin.mLocalizer.msg("settings.noPriority","None"),
        SimpleMarkerPlugin.mLocalizer.msg("settings.min","Minimum"),
        SimpleMarkerPlugin.mLocalizer.msg("settings.lowerMedium","Lower Medium"),
        SimpleMarkerPlugin.mLocalizer.msg("settings.medium","Medium"),
        SimpleMarkerPlugin.mLocalizer.msg("settings.higherMedium","Higher Medium"),
        SimpleMarkerPlugin.mLocalizer.msg("settings.max","Maximum")};

    /** Constructor 
     * @param type The type of this editor. 
     */
    public MarkListItemCellEditor(int type) {
      if(type == TEXT_EDITOR) {
        mTextField = new JTextField();
        mComboBox = null;
      }
      else {
        mComboBox = new JComboBox(prioValues);
        mComboBox.setRenderer(new MarkPriorityComboBoxRenderer());/*DefaultListCellRenderer() {
          public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
            
            if(!isSelected) {
              JPanel colorPanel = new JPanel(new FormLayout("default:grow","fill:default:grow"));
              ((JLabel)c).setOpaque(true);
              
              int colorIndex = index-1;
              Color color = list.getBackground();
              
              if(index == -1) {
                colorIndex = list.getSelectedIndex()-1;
              }
              
              color = SimpleMarkerPlugin.getPluginManager().getTvBrowserSettings().getColorForMarkingPriority(colorIndex);
              
              if(color != null) {
                c.setBackground(color);
              }
              
              colorPanel.setOpaque(false);        
              colorPanel.add(c, new CellConstraints().xy(1,1));
              
              c = colorPanel;
            }
            
            return c;
          }
        });*/
        mTextField = null;
      }
    }

    public boolean isCellEditable(EventObject evt) {
      if (evt instanceof MouseEvent) {
        return ((MouseEvent) evt).getClickCount() >= 2;
      }
      return true;
    }

    public Object getCellEditorValue() {
      if(mTextField != null) {
        final String name = mTextField.getText();

        boolean found = false;

        for (int i = 0; i < mListTable.getRowCount(); i++)
          if (name.equals(mListTable.getValueAt(i, 0).toString())) {
            found = true;
            break;
          }

        if (name.length() > 0 && !found) {
          mItem.setName(name);
          checkForIcon(mItem);
        }
        
        return mItem;
      }
      else {
        mItem.setMarkPriority(mComboBox.getSelectedIndex()-1);
        
        return mComboBox.getSelectedIndex() - 1;
      }
    }

    public Component getTableCellEditorComponent(JTable table, Object value,
        boolean isSelected, int row, int column) {      
      if(mTextField != null) {
        mItem = (MarkListItem) value;
        mTextField.setText(value.toString());
        return mTextField;
      }
      else {
        mItem = (MarkListItem)table.getValueAt(table.getSelectedRow(),0);
        mComboBox.setSelectedIndex((Integer)value+1);
        return mComboBox;
      }
    }
  }
  
  private void checkForIcon(final MarkListItem item) {
    File dir = new File(SimpleMarkerPlugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome(),"simplemarkericons");
    
    File[] icons = dir.listFiles(new FileFilter() {
      public boolean accept(File pathname) {
        String file = pathname.getName();
        
        if(pathname.isFile() && file.substring(0,file.lastIndexOf(".")).equalsIgnoreCase(item.toString()))
          return true;
        
        return false;
      }
    });
    
    if(icons != null && icons.length > 0) {
      Icon icon = SimpleMarkerPlugin.getInstance().getIconForFileName(
          icons[0].toString());

      if (icon.getIconWidth() == 16 && icon.getIconHeight() == 16) {
        mListTable.setValueAt(icon, mListTable.getSelectedRow(), 1);
        item.setMarkIconFileName(icons[0].getName());
      }
    }
  }
  
  private void chooseIcon(int row) {    
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

    if (chooser.showDialog(w, Localizer.getLocalization(Localizer.I18N_SELECT)) == JFileChooser.APPROVE_OPTION) {
      if (chooser.getSelectedFile() != null) {
        File dir = new File(SimpleMarkerPlugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome(),"simplemarkericons");
        
        if(!dir.isDirectory())
          dir.mkdir();
  
        String ext =  chooser.getSelectedFile().getName();
        ext = ext.substring(ext.lastIndexOf("."));
        
        Icon icon = SimpleMarkerPlugin.getInstance().getIconForFileName(
            chooser.getSelectedFile().getAbsolutePath());
  
        if (icon.getIconWidth() != 16 || icon.getIconHeight() != 16) {
          JOptionPane.showMessageDialog(w, SimpleMarkerPlugin.mLocalizer.msg(
              "iconSize", "The icon has to be 16x16 in size."));
          return;
        }
        
        if(!new File(dir, mListTable.getValueAt(row, 0).toString() + ext).equals(chooser.getSelectedFile())) {
          try {
            IOUtilities.copy(chooser.getSelectedFile(),new File(dir,mListTable.getValueAt(row, 0).toString() + ext));
          } catch (IOException e1) {
            e1.printStackTrace();
          }
        }
  
        icon = SimpleMarkerPlugin.getInstance().getIconForFileName(
               dir + "/" + mListTable.getValueAt(row, 0).toString() + ext);
  
        mListTable.setValueAt(icon, row, 1);
  
        ((MarkListItem) mListTable.getValueAt(row, 0))
            .setMarkIconFileName(mListTable.getValueAt(row, 0).toString() + ext);
      }
    }
  }
}
