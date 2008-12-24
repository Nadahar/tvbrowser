package recommendationplugin;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class RecommendationTableModel extends AbstractTableModel {

  final List<RecommendationInputIf> mInput;

  public RecommendationTableModel(final List<RecommendationInputIf> input) {
    mInput = input;
  }

  public int getRowCount() {
    return mInput.size();
  }

  public int getColumnCount() {
    return 2;
  }

  @Override
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return "Typ";
      case 1:
        return "Gewichtung";
    }

    return null;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return mInput.get(rowIndex).getName();
      case 1:
        return mInput.get(rowIndex).getWeight();
    }
    return null;
  }

  @Override
  public boolean isCellEditable(int row, int column) {
    if (column == 1) {
      return true;
    }
    return false;
  }

  @Override
  public void setValueAt(Object value, int row, int column) {
    if (column == 1) {
      int weight = -1;

      if (value instanceof Integer) {
        weight = (Integer) value;
      } else if (value instanceof String) {
        try {
          weight = Integer.parseInt((String) value);
        } catch (NumberFormatException e) {
          e.printStackTrace();
        }
      }

      if (weight != -1) {
        mInput.get(row).setWeight(weight);
      }
    }
  }

  public Class getColumnClass(int c) {
    return getValueAt(0, c).getClass();
  }
}
