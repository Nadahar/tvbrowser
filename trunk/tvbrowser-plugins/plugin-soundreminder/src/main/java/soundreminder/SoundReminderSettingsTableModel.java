/*
 * SoundReminder - Plugin for TV-Browser
 * Copyright (C) 2009 René Mach
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
 *     $Date: 2009-03-01 09:56:39 +0100 (So, 01 Mrz 2009) $
 *   $Author: ds10 $
 * $Revision: 5521 $
 */
package soundreminder;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * The settings table for the value settings.
 * 
 * @author René Mach
 */
public class SoundReminderSettingsTableModel extends AbstractTableModel {
  private ArrayList<SoundSettingsEntry> mData = new ArrayList<SoundSettingsEntry>(0);
  private JTable mTable;
  
  /**
   * Creates the SoundReminderSettingsTableModel.
   * <p>
   * @param entries The current sound entries.
   */
  protected SoundReminderSettingsTableModel(final ArrayList<SoundEntry> entries) {
    for(SoundEntry entry : entries) {
      int index = mData.size();
      
      for(int i = mData.size() - 1; i >= 0; i--) {
        if(mData.get(i).compareTo(entry) > 0) {
          index = i;
        }
        else {
          break;
        }
      }
      
      mData.add(index,new SoundSettingsEntry(entry));
    }
  }
  
  public int getColumnCount() {
    return 3;
  }

  public int getRowCount() {
    return mData.size();
  }

  public boolean isCellEditable(final int row, final int column) {
    return column == 0;
  }
  
  protected boolean rowIsValid(final int row) {
    return mData.get(row).isValid();
  }
  
  public String getColumnName(final int column) {
    switch(column) {
      case 0: return SoundReminder.mLocalizer.msg("searchText","Search text");
      case 1: return SoundReminder.mLocalizer.msg("settings.caseSensitive","case-sensitive");
      case 2: return SoundReminder.mLocalizer.msg("settings.soundFile","Sound file");
    }
    
    return "";
  }
  
  /**
   * Adds a new row to this table.
   */
  public void addRow() {
    mData.add(new SoundSettingsEntry(new SoundEntry("DUMMY-ENTRY",true,"DUMMY-FILE")));
    fireTableRowsInserted(mData.size()-1,mData.size()-1);
  }
  
  /**
   * Deletes the row with the given row index.
   * @param row
   */
  public void deleteRow(final int row) {
    mData.remove(row);
    fireTableRowsDeleted(row,row);
  }
  
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    if(!mData.isEmpty() && rowIndex >= 0) {
      final SoundSettingsEntry entry = mData.get(rowIndex);
    
      switch(columnIndex) {
        case 0: return entry.mNewSearchText;
        case 1: return entry.mNewCaseSensitve;
        case 2: return entry.mNewPath;
      }
    }
    
    return null;
  }

  public void setValueAt(final Object aValue, final int rowIndex,
      final int columnIndex) {
    final SoundSettingsEntry entry = mData.get(rowIndex);
    
    switch(columnIndex) {
      case 0: entry.setSearchText((String)aValue);break;
      case 1: entry.setIsCaseSensitive((Boolean)aValue);break;
      case 2: entry.setPath((String)aValue);break;
    }
    
    fireTableDataChanged();
    
    if(mTable != null) {
      mTable.getSelectionModel().setSelectionInterval(rowIndex,rowIndex);
    }
  }
  
  protected void setTable(final JTable table) {
    mTable = table;
  }
  
  protected ArrayList<SoundEntry> getChangedList() {
    final ArrayList<SoundEntry> newList = new ArrayList<SoundEntry>(mData
        .size());
    
    for(SoundSettingsEntry entry : mData) {
      if(entry.isValid()) {
        newList.add(entry.doChanges());
      }
    }
    
    return newList;
  }
  
  protected static class SoundSettingsEntry implements Comparable<Object> {
    private SoundEntry mSoundEntry;
    
    private boolean mWasChanged;
    
    private boolean mNewCaseSensitve;
    private String mNewSearchText;
    private String mNewPath;
    
    protected SoundSettingsEntry(final SoundEntry soundEntry) {
      mSoundEntry = soundEntry;
      
      mWasChanged = false;
      
      mNewCaseSensitve = mSoundEntry.isCaseSensitive();
      mNewSearchText = mSoundEntry.getSearchText();
      mNewPath = mSoundEntry.getPath();
    }
    
    /**
     * Sets the new value for the case-sensitive flag.
     * <p>
     * @param value The new value for the case-sensitive flag.
     */
    protected void setIsCaseSensitive(final boolean value) {
      mWasChanged = !mNewSearchText.equals(mSoundEntry.getSearchText()) || mSoundEntry.isCaseSensitive() != value || !mSoundEntry.getPath().equals(mNewPath);
      mNewCaseSensitve = value;
    }
    
    /**
     * Sets the new search text.
     * <p>
     * @param text The new search text.
     */
    protected void setSearchText(final String text) {
      mWasChanged = !mSoundEntry.getSearchText().equals(text) || mSoundEntry.isCaseSensitive() != mNewCaseSensitve || !mSoundEntry.getPath().equals(mNewPath);
      mNewSearchText = text;      
    }
    
    /**
     * Sets the new path for sound file.
     * <p>
     * @param path The new path to the sound file.
     */
    protected void setPath(final String path) {
      mWasChanged = !mNewSearchText.equals(mSoundEntry.getSearchText()) || mSoundEntry.isCaseSensitive() != mNewCaseSensitve || !mSoundEntry.getPath().equals(path);
      mNewPath = path;
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
        && !test.equals("*") && mNewPath != null && new File(mNewPath).isFile();
    }
    
    /**
     * Performs the changes of this value.
     */
    protected SoundEntry doChanges() {
      if(mWasChanged) {
        mSoundEntry.setValues(mNewSearchText,mNewCaseSensitve,mNewPath);
      }
      
      return mSoundEntry;
    }
        
    public int compareTo(final Object o) {
      if(o instanceof String) {
        return mNewSearchText.compareToIgnoreCase((String)o);
      }
      else if(o instanceof SoundSettingsEntry) {
        return mNewSearchText.compareToIgnoreCase(((SoundSettingsEntry)o).mNewSearchText);
      }
      else if(o instanceof SoundEntry) {
        return mNewSearchText.compareToIgnoreCase(((SoundEntry)o).getSearchText());
      }
      
      return 0;
    }
  }
}
