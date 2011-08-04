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
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.ui.settings.util.ColorLabel;
import util.ui.Localizer;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

/**
 * The exclusion table panel for settings of this plugin.
 * 
 * @author René Mach
 */
public class ExclusionTablePanel extends JPanel {
  private JTable mTable;
  private IDontWant2SeeSettingsTableModel mTableModel;
  
  protected ExclusionTablePanel(final IDontWant2SeeSettings settings) {
    Localizer mLocalizer = IDontWant2See.mLocalizer;
    mTableModel = new IDontWant2SeeSettingsTableModel(settings.getSearchList(),settings.getLastEnteredExclusionString());
    
    final IDontWant2SeeSettingsTableRenderer renderer = new IDontWant2SeeSettingsTableRenderer(settings.getLastUsedDate());        
    mTable = new JTable(mTableModel);
    mTableModel.setTable(mTable);
    mTable.setRowHeight(25);
    mTable.setPreferredScrollableViewportSize(new Dimension(200,150));
    mTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
    mTable.getColumnModel().getColumn(1).setCellRenderer(renderer);
    mTable.getColumnModel().getColumn(1).setMaxWidth(Locale.getDefault().getLanguage().equals("de") ? Sizes.dialogUnitXAsPixel(80,mTable) : Sizes.dialogUnitXAsPixel(55,mTable));
    mTable.getColumnModel().getColumn(1).setMinWidth(mTable.getColumnModel().getColumn(1).getMaxWidth());
    mTable.getTableHeader().setReorderingAllowed(false);
    mTable.getTableHeader().setResizingAllowed(false);
    
    final JScrollPane scrollPane = new JScrollPane(mTable);
    
    mTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(final MouseEvent e) {
        final int column = mTable.columnAtPoint(e.getPoint());
        
        if(column == 1) {
          final int row = mTable.rowAtPoint(e.getPoint());
          
          mTable.getModel().setValueAt(!((Boolean)mTable.getValueAt(row,column)),row,1);
          mTable.repaint();
        }
      }
    });
    
    mTable.addKeyListener(new KeyAdapter() {
      public void keyPressed(final KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_DELETE) {
          deleteSelectedRows();
          e.consume();
        }
        else if(mTable.getSelectedColumn() == 1 && 
            (e.getKeyCode() == KeyEvent.VK_F2 || e.getKeyCode() == KeyEvent.VK_SPACE)) {
          mTable.getModel().setValueAt(!((Boolean)mTable.getValueAt(mTable.getSelectedRow(),1)),
              mTable.getSelectedRow(),1);
          mTable.repaint();
        }
      }
    });
    
    addAncestorListener(new AncestorListener() {
      public void ancestorAdded(final AncestorEvent event) {
        for(int row = 0; row < mTableModel.getRowCount(); row++) {
          if(mTableModel.isLastChangedRow(row)) {
            final Rectangle rect = mTable.getCellRect(row, 0, true);
            rect.setBounds(0,scrollPane.getVisibleRect().height + rect.y - rect.height,0,0);
            
            mTable.scrollRectToVisible(rect);
            break;
          }
        }
      }

      public void ancestorMoved(final AncestorEvent event) {
      }

      public void ancestorRemoved(final AncestorEvent event) {
      }
    });
    
    final JButton add = new JButton(mLocalizer.msg("settings.add",
        "Add entry"),
        IDontWant2See.getInstance().createImageIcon("actions","document-new",16));
    add.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        mTableModel.addRow();
        mTable.scrollRectToVisible(mTable.getCellRect(mTableModel.getRowCount()-1,0,true));
      }
    });
    
    final JButton delete = new JButton(mLocalizer.msg("settings.delete",
        "Delete selected entries"),IDontWant2See.getInstance().createImageIcon("actions","edit-delete",16));
    delete.setEnabled(false);
    delete.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        deleteSelectedRows();
      }
    });
    
    mTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(final ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()) {
          delete.setEnabled(e.getFirstIndex() >= 0);
        }
      }
    });
    
    final FormLayout layout = new FormLayout("default,0dlu:grow,default",
        "fill:default:grow,1dlu,default,4dlu,default,5dlu,pref");
    final PanelBuilder pb = new PanelBuilder(layout, this);
    final CellConstraints cc = new CellConstraints();
    
    int y = 1;
    
    pb.add(scrollPane, cc.xyw(1,y++,3));
    
    final PanelBuilder pb2 = new PanelBuilder(
        new FormLayout("default,3dlu:grow,default,3dlu:grow,default,3dlu:grow,default",
            "default"));
    
    final ColorLabel blueLabel = new ColorLabel(
        IDontWant2SeeSettingsTableRenderer.LAST_CHANGED_COLOR);
    blueLabel.setText(mLocalizer.msg("changed","Last change"));
    pb2.add(blueLabel, cc.xy(1,1));
    
    final ColorLabel yellowLabel = new ColorLabel(
        IDontWant2SeeSettingsTableRenderer.LAST_USAGE_7_COLOR);
    yellowLabel.setText(mLocalizer.msg("unusedSince","Not used for {0} days",IDontWant2SeeSettingsTableRenderer.OUTDATED_7_DAY_COUNT));
    pb2.add(yellowLabel, cc.xy(3,1));
    
    final ColorLabel orangeLabel = new ColorLabel(
        IDontWant2SeeSettingsTableRenderer.LAST_USAGE_30_COLOR);
    orangeLabel.setText(mLocalizer.msg("unusedSince","Not used for {0} days",IDontWant2SeeSettingsTableRenderer.OUTDATED_30_DAY_COUNT));
    pb2.add(orangeLabel, cc.xy(5,1));
    
    final ColorLabel redLabel = new ColorLabel(
        IDontWant2SeeSettingsTableRenderer.NOT_VALID_COLOR);
    redLabel.setText(mLocalizer.msg("invalid","Invalid"));
    pb2.add(redLabel, cc.xy(7,1));
    
    pb.add(pb2.getPanel(), cc.xyw(1,++y,3));
    
    y++;
    pb.add(add, cc.xy(1,++y));
    pb.add(delete, cc.xy(3,y++));
    pb.add(UiUtilities.createHelpTextArea(mLocalizer.msg("settings.help",
    "To edit a value double click a cell. You can use wildcard * to search for any text.")), cc.xyw(1,++y,3));
  }
  
  private void deleteSelectedRows() {
    final int selectedIndex = mTable.getSelectedRow();
    final int[] selection = mTable.getSelectedRows();
    
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
  
  protected void saveSettings(IDontWant2SeeSettings settings) {
    if(mTable.isEditing()) {
      mTable.getCellEditor().stopCellEditing();
    }
    
    settings.setSearchList(mTableModel.getChangedList());
    
    if(mTableModel.getLastChangedValue() != null) {
      settings.setLastEnteredExclusionString(mTableModel.getLastChangedValue());
    }
    
    IDontWant2See.getInstance().updateFilter(true);
  }
}

