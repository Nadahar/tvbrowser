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

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import devplugin.Date;

/**
 * The settings table for the value settings.
 * 
 * @author René Mach
 */
public class IDontWant2SeeSettingsTableModel extends AbstractTableModel {
  private ArrayList<IDontWant2SeeSettingsTableEntry> mData = new ArrayList<IDontWant2SeeSettingsTableEntry>();
  private String mLastChangedValue;
  private JTable mTable;
  
  protected IDontWant2SeeSettingsTableModel(ArrayList<IDontWant2SeeListEntry> entries,
      String lastEnteredExclusionString) {
    mLastChangedValue = lastEnteredExclusionString;
    
    for(IDontWant2SeeListEntry entry : entries) {
      int index = mData.size();
      
      for(int i = mData.size() - 1; i >= 0; i--) {
        if(mData.get(i).compareTo(entry) > 0) {
          index = i;
        }
        else {
          break;
        }
      }
      
      mData.add(index,new IDontWant2SeeSettingsTableEntry(entry));
    }
  }
  
  public int getColumnCount() {
    return 2;
  }

  public int getRowCount() {
    return mData.size();
  }
  
  public boolean isCellEditable(int row, int column) {
    return column == 0;
  }
  
  /**
   * Adds a new row to this table.
   */
  public void addRow() {
    mData.add(new IDontWant2SeeSettingsTableEntry(new IDontWant2SeeListEntry("DUMMY-ENTRY",true)));
    fireTableRowsInserted(mData.size()-1,mData.size()-1);
  }
  
  protected boolean rowIsValid(int row) {
    return mData.get(row).isValid();
  }
  
  protected boolean isRowOutdated(int row, Date compareValue, int outdatedDayCount) {
    return mData.get(row).isOutdated(compareValue,outdatedDayCount);
  }
  
  protected boolean isLastChangedRow(int row) {
    return mData.get(row).isLastChanged(mLastChangedValue);
  }
  
  public String getColumnName(int column) {
    if(column == 0) {
      return IDontWant2See.mLocalizer.msg("searchText","Search text");
    }
    
    return IDontWant2See.mLocalizer.msg("settings.caseSensitive","case-sensitive");
  }
  
  /**
   * Deletes the row with the given row index.
   * @param row
   */
  public void deleteRow(int row) {
    mData.remove(row);
    fireTableRowsDeleted(row,row);
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    IDontWant2SeeSettingsTableEntry entry = mData.get(rowIndex);
    
    if(columnIndex == 0) {
      return entry.mNewSearchText;
    }
    else {
      return entry.mNewCaseSensitve;
    }
  }
  
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) { 
    IDontWant2SeeSettingsTableEntry entry = mData.get(rowIndex);
    
    if(columnIndex == 0) {
      mLastChangedValue = (String)aValue;
      entry.setSearchText((String)aValue);
    }
    else {
      entry.setIsCaseSensitive((Boolean)aValue);
    }
    
    fireTableDataChanged();
    
    if(mTable != null) {
      mTable.getSelectionModel().setSelectionInterval(rowIndex,rowIndex);
    }
  }
  
  protected void setTable(JTable table) {
    mTable = table;
  }
  
  protected ArrayList<IDontWant2SeeListEntry> getChangedList() {
    ArrayList<IDontWant2SeeListEntry> newList = new ArrayList<IDontWant2SeeListEntry>();
    
    for(IDontWant2SeeSettingsTableEntry entry : mData) {
      if(entry.isValid()) {
        newList.add(entry.doChanges());
      }
    }
    
    return newList;
  }
  
  protected String getLastChangedValue() {
    return mLastChangedValue;
  }
  
  /**
   * The table entry class.
   */
  protected static class IDontWant2SeeSettingsTableEntry implements Comparable<Object> {
    private IDontWant2SeeListEntry mListEntry;
    
    private boolean mWasChanged;
    
    private boolean mNewCaseSensitve;
    private String mNewSearchText;
    
    protected IDontWant2SeeSettingsTableEntry(IDontWant2SeeListEntry entry) {
      mListEntry = entry;
      mWasChanged = false;
      mNewCaseSensitve = entry.isCaseSensitive();
      mNewSearchText = entry.getSearchText();
    }
    
    /**
     * Sets the new value for the case-sensitive flag.
     * <p>
     * @param value The new value for the case-sensitive flag.
     */
    protected void setIsCaseSensitive(boolean value) {
      mWasChanged = true;
      mNewCaseSensitve = value;
    }
    
    /**
     * Sets the new search text.
     * <p>
     * @param text The new search text.
     */
    protected void setSearchText(String text) {
      mWasChanged = true;
      mNewSearchText = text;
    }
    
    /**
     * Gets if this table entry has a valid search text
     * <p>
     * @return <code>True</code> if the search text is valid.
     */
    protected boolean isValid() {
      String test = "";
      
      if(mNewSearchText != null) {
        test = mNewSearchText.replaceAll("\\*+","\\*").trim();
      }
      
      return test.length() > 0 && !test.equals("DUMMY-ENTRY") 
        && !test.equals("*");
    }
    
    /**
     * Performs the changes of this value.
     */
    protected IDontWant2SeeListEntry doChanges() {
      if(mWasChanged) {
        mListEntry.setValues(mNewSearchText,mNewCaseSensitve);
      }
      
      return mListEntry;
    }
    
    protected boolean isOutdated(Date compareValue, int outdatedDayCount) {
      return mListEntry.isOutdated(compareValue,outdatedDayCount);
    }
    
    protected boolean isLastChanged(String lastChangedText) {
      return mNewSearchText.equals(lastChangedText);
    }
    
    public int compareTo(Object o) {
      if(o instanceof String) {
        return mNewSearchText.compareToIgnoreCase((String)o);
      }
      else if(o instanceof IDontWant2SeeSettingsTableEntry) {
        return mNewSearchText.compareToIgnoreCase(((IDontWant2SeeSettingsTableEntry)o).mNewSearchText);
      }
      else if(o instanceof IDontWant2SeeListEntry) {
        return mNewSearchText.compareToIgnoreCase(((IDontWant2SeeListEntry)o).getSearchText());
      }
      
      return 0;
    }
  }
}
