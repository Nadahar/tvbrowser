package onlinereminder;

import javax.swing.table.AbstractTableModel;

public class ReminderTableModel extends AbstractTableModel {

  private Configuration mConf;

  private ReminderProgramItem[] mProgramItems;

  public ReminderTableModel(Configuration conf) {
    mConf = conf;

    mProgramItems = new ReminderProgramItem[conf.getProgramItems().length];

    for (int i = 0; i < conf.getProgramItems().length; i++) {
      mProgramItems[i] = (ReminderProgramItem) conf.getProgramItems()[i];
    }
  }

  public String getColumnName(int column) {
    switch (column) {
    case 0:
      return "Program";
    case 1:
      return "Remind after";
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
