package tvbrowser.extras.reminderplugin;

import javax.swing.table.AbstractTableModel;

import util.ui.Localizer;

public class ReminderTableModel extends AbstractTableModel {

  private Localizer mLocalizer = Localizer.getLocalizerFor(ReminderTableModel.class);
  
  private ReminderListItem[] mProgramItems;

  public ReminderTableModel(ReminderList list) {
    mProgramItems = list.getReminderItems();
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
      return new Integer(mProgramItems[rowIndex].getMinutes());
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

  public Class getColumnClass(int c) {
    return getValueAt(0, c).getClass();
  }

}
