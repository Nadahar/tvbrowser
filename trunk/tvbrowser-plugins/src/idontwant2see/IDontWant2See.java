/*
 * IDontWant2See - Plugin for TV-Browser
 * Copyright (C) 2008 René Mach
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
package idontwant2see;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginsFilterComponent;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * A very simple filter plugin to easily get
 * rid of stupid programs in the program table.
 * 
 * @author René Mach
 */
public class IDontWant2See extends Plugin {
  protected static final Localizer mLocalizer = Localizer.getLocalizerFor(IDontWant2See.class); 
  
  private ArrayList<IDontWant2SeeListEntry> mSearchList;
  private PluginsProgramFilter mFilter;
  private static IDontWant2See mInstance;
  private boolean mSimpleMenu;
  
  public static Version getVersion() {
    return new Version(0,5,3,false);
  }
  
  /**
   * Creates an instance of this plugin.
   */
  public IDontWant2See() {
    mInstance = this;
    mSearchList = new ArrayList<IDontWant2SeeListEntry>();
    mSimpleMenu = true;
    
    mFilter = new PluginsProgramFilter(this) {
      public String getSubName() {
        return "";
      }

      public boolean accept(Program prog) {
        return acceptInternal(prog);
      }
    };
  }
  
  protected static IDontWant2See getInstance() {
    return mInstance;
  }
  
  protected boolean acceptInternal(Program prog) {
    for(IDontWant2SeeListEntry entry : mSearchList) {
      if(entry.matches(prog)) {
        return false;
      }
    }
    
    return true;
  }
  
  public PluginInfo getInfo() {
    return new PluginInfo(IDontWant2See.class,
        mLocalizer.msg("name","I don't want to see!"),
        mLocalizer.msg("desc","Removes all programs with an entered search text in the title from the program table."),
        "René Mach","GPL");
  }
  
  private int getSearchTextIndexForProgram(Program p) {
    if(p != null) {
      for(int i = 0; i < mSearchList.size(); i++) {
        if(mSearchList.get(i).matches(p)) {
          return i;
        }
      }
    }
    
    return -1;
  }
  
  public ActionMenu getContextMenuActions(final Program p) {
    final int index = getSearchTextIndexForProgram(p);
    
    if(p == null || p.equals(getPluginManager().getExampleProgram()) || index == -1) {
      AbstractAction action1 = new AbstractAction(mLocalizer.msg("menu.userEntered","User entered value")) {
        public void actionPerformed(ActionEvent e) {
          JCheckBox caseSensitive = new JCheckBox(mLocalizer.msg("caseSensitive","case-sensitive"));
          JTextField input = new JTextField(p.getTitle());
                    
          if(JOptionPane.showConfirmDialog(UiUtilities.getLastModalChildOf(getParentFrame()),
              new Object[] {mLocalizer.msg("exclusionText","What should be excluded? (You can use the wildcard *)"),input,caseSensitive},
              mLocalizer.msg("exclusionTitle","Exclusion value entering"),
              JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String test = "";
            
            if(input.getText() != null) {
              test = input.getText().replaceAll("\\*+","\\*").trim();
              
              if(test.length() >= 0 && !test.equals("*")) {
                mSearchList.add(new IDontWant2SeeListEntry(input.getText(),caseSensitive.isSelected()));
                updateFilter();
              }
            }
            
            if(test.trim().length() <= 1) {
              JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(getParentFrame()),mLocalizer.msg("notValid","The entered text is not valid."),
                  Localizer.getLocalization(Localizer.I18N_ERROR),JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      };
      
      AbstractAction action2 = new AbstractAction(mLocalizer.msg("menu.completeCaseSensitive","Complete title case-sensitive")) {
        public void actionPerformed(ActionEvent e) {
          mSearchList.add(new IDontWant2SeeListEntry(p.getTitle(),true));
          updateFilter();
        }
      };
      
      if(mSimpleMenu) {
        action2.putValue(Action.NAME,mLocalizer.msg("name","I don't want to see!"));
        action2.putValue(Action.SMALL_ICON,createImageIcon("actions","edit-delete",16));
        
        return new ActionMenu(action2);
      }
      else {
        ContextMenuAction baseAction = new ContextMenuAction(mLocalizer.msg("name","I don't want to see!"),
            createImageIcon("actions","edit-delete",16));
        
        return new ActionMenu(baseAction, new Action[] {action2,action1});
      }
    }
    else if(index != -1) {
      ContextMenuAction baseAction = new ContextMenuAction(mLocalizer.msg("menu.reshow","I want to see!"),
          createImageIcon("actions","edit-paste",16));
      baseAction.setActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          mSearchList.remove(index);
          updateFilter();
        }
      });
      
      return new ActionMenu(baseAction);
    }
    
    return null;
  }
  
  private void updateFilter() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        getPluginManager().getFilterManager().setCurrentFilter(mFilter);
      }
    });
  }
  
  public PluginsProgramFilter[] getAvailableFilter() {
    return new PluginsProgramFilter[] {mFilter};
  }
  
  public void readData(ObjectInputStream in) throws IOException {
    int version = in.readInt(); //read Version
    
    int n = in.readInt();
    
    if(version <= 2) {
      for(int i = 0; i < n; i++) {
        StringBuilder value = new StringBuilder("*");
        value.append(in.readUTF()).append("*");
        
        mSearchList.add(new IDontWant2SeeListEntry(value.toString(),false));
      }
      
      if(version == 2) {
        n = in.readInt();
        
        for(int i = 0; i < n; i++) {
          mSearchList.add(new IDontWant2SeeListEntry(in.readUTF(),true));
        }
        
        mSimpleMenu = false;
      }
    }
    else {
      for(int i = 0; i < n; i++) {
        mSearchList.add(new IDontWant2SeeListEntry(in, version));
      }
      
      mSimpleMenu = in.readBoolean();
    }
  }
  
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(3); //version
    out.writeInt(mSearchList.size());
    
    for(IDontWant2SeeListEntry entry : mSearchList) {
      entry.writeData(out);
    }
    
    out.writeBoolean(mSimpleMenu);
  }
  
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private IDontWant2SeeSettingsTableModel mTableModel;
      private JCheckBox mSimpleMenuBox;
      private JTable mTable;
      
      public JPanel createSettingsPanel() {
        CellConstraints cc = new CellConstraints();
        JPanel pb = new JPanel(new FormLayout("default,0dlu:grow,default",
            "default,5dlu,pref,2dlu,fill:default:grow,5dlu,pref"));        
        
        mSimpleMenuBox = new JCheckBox(mLocalizer.msg("settings.oneMenuOnly",
            "Show only one menu entry to remove complete titles"),mSimpleMenu);
        mTableModel = new IDontWant2SeeSettingsTableModel(mSearchList);
        
        final IDontWant2SeeSettingsTableRenderer renderer = new IDontWant2SeeSettingsTableRenderer();        
        mTable = new JTable(mTableModel);
        mTable.setRowHeight(25);
        mTable.setPreferredScrollableViewportSize(new Dimension(200,200));
        mTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
        mTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
        mTable.getColumnModel().getColumn(1).setMaxWidth(Locale.getDefault().getLanguage().equals("de") ? Sizes.dialogUnitXAsPixel(80,mTable) : Sizes.dialogUnitXAsPixel(55,mTable));
        mTable.getColumnModel().getColumn(1).setMinWidth(mTable.getColumnModel().getColumn(1).getMaxWidth());
        mTable.getTableHeader().setReorderingAllowed(false);
        mTable.getTableHeader().setResizingAllowed(false);
        
        mTable.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            int column = mTable.columnAtPoint(e.getPoint());
            
            if(column == 1) {
              int row = mTable.rowAtPoint(e.getPoint());
              
              mTable.getModel().setValueAt(!((Boolean)mTable.getValueAt(row,column)),row,1);
              mTable.repaint();
            }
          }
        });
        
        mTable.addKeyListener(new KeyAdapter() {
          public void keyReleased(KeyEvent e) {
            if(mTable.getSelectedColumn() == 1 && 
                (e.getKeyCode() == KeyEvent.VK_F2 || e.getKeyCode() == KeyEvent.VK_SPACE)) {
              mTable.getModel().setValueAt(!((Boolean)mTable.getValueAt(mTable.getSelectedRow(),1)),
                  mTable.getSelectedRow(),1);
              mTable.repaint();
            }
          }
        });
        
        JButton add = new JButton(mLocalizer.msg("settings.add","Add entry"),
            createImageIcon("actions","document-new",16));
        add.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            mTableModel.addRow();
            mTable.scrollRectToVisible(mTable.getCellRect(mTableModel.getRowCount()-1,0,true));
          }
        });
        
        final JButton delete = new JButton(mLocalizer.msg("settings.delete",
            "Delete selected entries"),createImageIcon("actions","edit-delete",16));
        delete.setEnabled(false);
        delete.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            int selectedIndex = mTable.getSelectedRow();
            int[] selection = mTable.getSelectedRows();
            
            for(int i = selection.length-1; i >= 0; i--) {
              mTableModel.deleteRow(selection[i]);
            }
            
            if ((selectedIndex > 0) && (selectedIndex<mTable.getRowCount())) {
              mTable.setRowSelectionInterval(selectedIndex,selectedIndex);
            }
            else if(mTable.getRowCount() > 0) {
              if(mTable.getRowCount() - selectedIndex > 0) {
                mTable.setRowSelectionInterval(0,0);
              }
              else {
                mTable.setRowSelectionInterval(mTable.getRowCount()-1,mTable.getRowCount()-1);
              }
            }
          }
        });
        
        mTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent e) {
            if(!e.getValueIsAdjusting()) {
              delete.setEnabled(e.getFirstIndex() >= 0);
            }
          }
        });
                
        pb.add(mSimpleMenuBox, cc.xyw(1,1,3));
        pb.add(UiUtilities.createHelpTextArea(mLocalizer.msg("settings.help",
            "To edit a value double click a cell. You can use wildcard * to search for any text.")), cc.xyw(1,3,3));
        pb.add(new JScrollPane(mTable), cc.xyw(1,5,3));
        pb.add(add, cc.xy(1,7));
        pb.add(delete, cc.xy(3,7));
        
        JPanel p = new JPanel(new FormLayout("5dlu,0dlu:grow","5dlu,fill:default:grow"));
        p.add(pb, cc.xy(2,2));
        
        return p;
      }

      public Icon getIcon() {
        return null;
      }

      public String getTitle() {
        return null;
      }

      public void saveSettings() {
        if(mTable.isEditing()) {
          mTable.getCellEditor().stopCellEditing();
        }
        
        mSimpleMenu = mSimpleMenuBox.isSelected();
        
        mSearchList = mTableModel.getChangedList();
        
        updateFilter();
      }      
    };
  }
  
  @SuppressWarnings("unchecked")
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
    return (Class<? extends PluginsFilterComponent>[]) new Class[] {IDontWant2SeeFilterComponent.class};
  }
}
