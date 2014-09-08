/*
 * Filter Info Icon plugin for TV-Browser
 * Copyright (C) 2014 René Mach (rene@tvbrowser.org)
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
 */
package filterinfoicon;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

import util.ui.Localizer;
import util.ui.PluginChooserDlg;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.ProgramFilter;
import devplugin.ProgramReceiveTarget;

/**
 * A panel to add, edit and delete filter info icons.
 * <p>
 * @author René Mach
 */
public class FilterInfoIconManagePanel extends JPanel {
  private FilterInfoIconTableModel mTableModel;
  private JTable mTable;
  private JDialog mParentDialog;
  
  public FilterInfoIconManagePanel(HashSet<FilterEntry> entries, JDialog parent, JButton ok) {
    String[] header = new String[] {
        FilterInfoIcon.LOCALIZER.msg("table.filter", "Program filter"),
        FilterInfoIcon.LOCALIZER.msg("table.icon", "Icon"),
        FilterInfoIcon.LOCALIZER.msg("table.send", "Send to other plugins")
    };
    
    mTableModel = new FilterInfoIconTableModel(header,entries);
    mParentDialog = parent;
    
    mTable = new JTable(mTableModel) {
      @Override
      public TableCellEditor getCellEditor(int row, int column) {
        if(column != 0) {
          return super.getCellEditor(row, column);
        }
        else {
          JComboBox comboBox = new JComboBox();
          
          String filterName = (String)getValueAt(row, column);
          
          ProgramFilter[] filters = Plugin.getPluginManager().getFilterManager().getAvailableFilters();
          
          for(ProgramFilter filter : filters) {
            comboBox.addItem(filter);
            
            if(filter.getName().equals(filterName)) {
              comboBox.setSelectedItem(filter);
            }
          }
          
          DefaultCellEditor edit = new DefaultCellEditor(comboBox);
          edit.setClickCountToStart(2);
          
          return edit;
        }
      }
    };
    mTable.addMouseListener(new MouseAdapter() {      
      @Override
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
          edit(mTable.rowAtPoint(e.getPoint()), mTable.columnAtPoint(e.getPoint()));
          
          e.consume();
        }
      }
    });
    
    mTable.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_F2 || e.getKeyCode() == KeyEvent.VK_SPACE) {
          if(mTable.getSelectedRow() >= 0 && (mTable.getSelectedColumn() == 1 || mTable.getSelectedColumn() == 2)) {
            edit(mTable.getSelectedRow(), mTable.getSelectedColumn());
            e.consume();
          }
        }
      }
    });
    
    FilterInfoIconTableCellRenderer cellRenderer = new FilterInfoIconTableCellRenderer();
    mTable.getColumnModel().getColumn(0).setCellRenderer(cellRenderer);
    mTable.getColumnModel().getColumn(1).setCellRenderer(cellRenderer);
    mTable.getColumnModel().getColumn(1).setMaxWidth(mTable.getColumnModel().getColumn(1).getPreferredWidth());
    mTable.getColumnModel().getColumn(2).setCellRenderer(cellRenderer);
    
    final JButton delete = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    delete.setToolTipText(Localizer.getLocalization(Localizer.I18N_DELETE));
    delete.setEnabled(false);
    delete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int[] rows = mTable.getSelectedRows();
        
        for(int i = rows.length-1; i >= 0; i--) {
          mTableModel.removeRow(rows[i]);
        }
      }
    });
    
    mTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()) {
          delete.setEnabled(mTable.getSelectedRow() >= 0 && mTable.getSelectedRow() < mTable.getRowCount());
        }
      }
    });

    JButton add = new JButton(TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
    add.setToolTipText(Localizer.getLocalization(Localizer.I18N_ADD));
    add.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mTableModel.addEntry(new FilterEntry());
      }
    });

    setLayout(new FormLayout("default,3dlu,default,default:grow,default","fill:default:grow,3dlu,default"));
    
    add(new JScrollPane(mTable), CC.xyw(1, 1, 5));
    add(add, CC.xy(1, 3));
    add(delete, CC.xy(3, 3));
    
    if(ok != null) {
      add(ok, CC.xy(5, 3));
    }
  }
  
  private void edit(int row, int column) {
    if(row >= 0 && row <= mTableModel.getRowCount()) {
      if(column == 1) {
        final FilterEntry entry = mTableModel.getEntry(row);
        
        JFileChooser mFileChooser = new JFileChooser(FilterInfoIcon.LOCALIZER.msg("selectIcon", "Select icon"));
        mFileChooser.setFileFilter(new FileFilter() {
          @Override
          public String getDescription() {
            return "";
          }
          
          @Override
          public boolean accept(File f) {
            return f != null && (f.isDirectory() || f.getName().toLowerCase().endsWith(".jpg") || f.getName().toLowerCase().endsWith(".png"));
          }
        });
        
        if(entry.getIconFilePath() != null) {
          mFileChooser.setSelectedFile(entry.getIconFilePath());
          mFileChooser.setCurrentDirectory(entry.getIconFilePath().getParentFile());
        }
        else {
          String test = FilterInfoIcon.getLastIconPath();
          
          if(test != null) {
            mFileChooser.setCurrentDirectory(new File(test));
          }
          else {
            mFileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
          }
        }
        
        if(mFileChooser.showDialog(mTable, Localizer.getLocalization(Localizer.I18N_SELECT)) == JFileChooser.APPROVE_OPTION) {
          entry.setIconFilePath(mFileChooser.getSelectedFile().getAbsolutePath());
          FilterInfoIcon.setLastIconPath(mFileChooser.getSelectedFile().getParent());
          mTableModel.fireTableCellUpdated(row, column);
          
          new Thread() {
            @ Override
            public void run() {
              entry.findForReceiveTargets();
            };
          }.start();
        }
      }
      else if(column == 2) {
        final FilterEntry entry = mTableModel.getEntry(row);
        
        PluginChooserDlg choose = new PluginChooserDlg(UiUtilities.getLastModalChildOf(mParentDialog), entry.getReceiveTargets(), null, null);
        choose.setLocationRelativeTo(mTable);
        choose.setVisible(true);
        
        ProgramReceiveTarget[] targets = choose.getReceiveTargets();
        
        if(targets.length == 0) {
          entry.setProgramReceiveTargets(null);
        }
        else {
          entry.setProgramReceiveTargets(targets);
        }
        
        mTableModel.fireTableCellUpdated(row, column);
      }
    }
  }
  
  private class FilterInfoIconTableModel extends DefaultTableModel {
    private HashSet<FilterEntry> mEntrySet;
    private ArrayList<FilterEntry> mEntryList;
    
    FilterInfoIconTableModel(String[] header, HashSet<FilterEntry> entries) {
      super(header, entries.size());
      
      mEntrySet = entries;
      mEntryList = new ArrayList<FilterEntry>(entries.size());
      
      for(Iterator<FilterEntry> it = entries.iterator();it.hasNext();) {
        mEntryList.add(it.next());
      }
      
      Collections.sort(mEntryList);
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
      return column == 0;
    }
    
    @Override
    public int getColumnCount() {
      return super.getColumnCount();
    }
    
    @Override
    public Object getValueAt(int row, int column) {
      FilterEntry entry = mEntryList.get(row);
      
      switch(column) {
        case 0: return entry.toString();
        case 1: return entry.getIconFilePath();
        case 2: return entry.getReceiveTargets();
      }
      
      return null;
    }
    
    @Override
    public void setValueAt(Object aValue, int row, int column) {
      FilterEntry entry = mEntryList.get(row);
      
      switch (column) {
        case 0: entry.updateFilter((ProgramFilter)aValue);break;
        case 1: entry.setIconFilePath(((File)aValue).getAbsolutePath());break;
        case 2: entry.setProgramReceiveTargets((ProgramReceiveTarget[])aValue);break;
      }
      
      fireTableCellUpdated(row, column);
    }
    
    public FilterEntry getEntry(int row) {
      return mEntryList.get(row);
    }
    
    public void addEntry(FilterEntry entry) {
      mEntryList.add(entry);
      mEntrySet.add(entry);
      
      addRow(new Object[] {entry.toString(),entry.getIconFilePath(),entry.getReceiveTargets()});
    }
    
    @Override
    public void removeRow(int row) {
      super.removeRow(row);
      
      final FilterEntry entry = mEntryList.remove(row);
      
      if(entry != null) {
        mEntrySet.remove(entry);
        entry.setProgramReceiveTargets(null);
        
        new Thread() {
          @Override
          public void run() {
            entry.findForReceiveTargets();
          };
        }.start();
      }
    }
  }
  
  private class FilterInfoIconTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
      Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
          row, column);
      
      if(column == 0) {
        ((JLabel)c).setIcon(null);
        ((JLabel)c).setHorizontalAlignment(JLabel.LEFT);
        FilterEntry entry = ((FilterInfoIconTableModel)table.getModel()).getEntry(row);
        
        if(!entry.isValidFilter()) {
          ((JLabel)c).setText("<html><div style=\"color:orange;\">"+ FilterInfoIcon.LOCALIZER.msg("table.invalid", "INVALID")+" <strike>"+entry.toString()+"</strike></div></html>");
        }
      }
      else if(column == 1) {
        ((JLabel)c).setText("");
        ((JLabel)c).setHorizontalAlignment(JLabel.CENTER);
        
        if(value != null) {          
          FilterEntry entry = ((FilterInfoIconTableModel)table.getModel()).getEntry(row);
          ((JLabel)c).setIcon(entry.getIcon());
        }
        else {
          ((JLabel)c).setIcon(FilterInfoIcon.getDefaultIcon());
        }
      }
      else if(column == 2) {
        ((JLabel)c).setHorizontalAlignment(JLabel.LEFT);
        ((JLabel)c).setIcon(null);
        
        if(value != null) {
          ProgramReceiveTarget[] targets = (ProgramReceiveTarget[])value;
          
          if(targets.length == 0) {
            ((JLabel)c).setText(FilterInfoIcon.LOCALIZER.msg("noTarget", "Don't send"));
          }
          else if(targets.length == 1) {
            ((JLabel)c).setText(targets[0].getTargetName());
          }
          else {
            ((JLabel)c).setText(targets.length+" "+FilterInfoIcon.LOCALIZER.msg("targets", "targets"));
          }
        }
        else {
          ((JLabel)c).setText(FilterInfoIcon.LOCALIZER.msg("noTarget", "Don't send"));
        }
      }
      
      return c;
    }
  }
}
