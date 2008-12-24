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
      case 0: return "Typ";
      case 1: return "Gewichtung";
    }

    return null;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0: return mInput.get(rowIndex).getName();
      case 1: return mInput.get(rowIndex).getWeight();
    }
    return null;
  }
}
