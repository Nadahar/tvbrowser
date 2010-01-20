package tvbrowser.extras.reminderplugin;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;

import util.ui.Localizer;

/**
 * The model for the reminder table.
 */
public class ReminderTableModel extends AbstractTableModel {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ReminderTableModel.class);
  private static String mLastSelectedTitle = mLocalizer.msg("all","All");
  
  private ReminderList mList;
  private ReminderListItem[] mProgramItems;
  private JComboBox mTitleFilterBox;

  private boolean mHandleBoxSelection;
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param list The list with the reminders.
   * @param titleFilterBox The title filter selection combo box.
   */
  public ReminderTableModel(ReminderList list, JComboBox titleFilterBox) {
    mList = list;
    mTitleFilterBox = titleFilterBox;
    mHandleBoxSelection = true;
    
    ItemListener[] itemListeners = titleFilterBox.getItemListeners();
    
    for(ItemListener itemListener : itemListeners) {
      titleFilterBox.removeItemListener(itemListener);
    }
    
    insertAvailableTitles();
    
    mTitleFilterBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        if(e.getStateChange() == ItemEvent.SELECTED && mHandleBoxSelection) {
          mLastSelectedTitle = e.getItem().toString();
          updateTableEntries();
        }
      }
    });
    
    mProgramItems = getItemsForTitleSelection();
  }
  
  /**
   * Updates the entries of the reminder list table.
   * @since 2.7.2
   */
  public synchronized void updateTableEntries() {
    mProgramItems = getItemsForTitleSelection();
    fireTableDataChanged();
  }
  
  private void insertAvailableTitles() {
    mHandleBoxSelection = false;
    mTitleFilterBox.removeAllItems();
    ReminderListItem[] allItems = mList.getReminderItems();
    
    mTitleFilterBox.addItem(mLocalizer.msg("all","All"));
    
    for(ReminderListItem item : allItems) {
      boolean found = false;
      int index = 0;
      
      for(int i = 0; i < mTitleFilterBox.getItemCount(); i++) {
        if (mTitleFilterBox.getItemAt(i).toString().compareToIgnoreCase(item.getProgram().getTitle()) < 0) {
          index = i;
        }
        
        if(mTitleFilterBox.getItemAt(i).equals(item.getProgram().getTitle())) {
          found = true;
          break;
        }
      }
      
      if(!found) {
        mTitleFilterBox.insertItemAt(item.getProgram().getTitle(),index+1);
      }
    }
    
    mHandleBoxSelection = true;
    
    mTitleFilterBox.setSelectedItem(mLastSelectedTitle);
  }
  
  private ReminderListItem[] getItemsForTitleSelection() {
    if(mTitleFilterBox.getSelectedIndex() != 0) {
      ArrayList<ReminderListItem> filteredList = new ArrayList<ReminderListItem>();
      ReminderListItem[] allItems = mList.getReminderItems();
      
      for(ReminderListItem item : allItems) {
        if(mTitleFilterBox.getSelectedItem().equals(item.getProgram().getTitle())) {
          filteredList.add(item);
        }
      }
      
      return filteredList.toArray(new ReminderListItem[filteredList.size()]);
    }
    
    return mList.getReminderItems();
  }

  public String getColumnName(int column) {
    switch (column) {
    case 0:
      return Localizer.getLocalization(Localizer.I18N_PROGRAMS);
    case 1:
      return mLocalizer.msg("timeMenu","Reminder time");
    default:
      return "";
    }
  }

  public int getRowCount() {
    return mProgramItems.length;
  }

  public int getColumnCount() {
    return 2;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    if (columnIndex == 0) {
      return mProgramItems[rowIndex].getProgram();
    } else if (columnIndex == 1) {
      return mProgramItems[rowIndex];
    }
    
    return "";
  }

  public boolean isCellEditable(int rowIndex, int columnIndex) {

    if (columnIndex == 1) {
      return true;
    }

    return false;
  }

  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    if (columnIndex == 1) {
      mProgramItems[rowIndex].setMinutes(((Integer) aValue).intValue());
    }
  }

  public Class<?> getColumnClass(int c) {
    return getValueAt(0, c).getClass();
  }

}
