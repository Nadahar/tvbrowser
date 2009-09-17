package recommendationplugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import recommendationplugin.weighting.FacadeWeighting;
import util.ui.Localizer;

public class RecommendationTableModel extends AbstractTableModel {

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(RecommendationTableModel.class);
  
  final List<RecommendationWeighting> mWeightings;

  public RecommendationTableModel(final List<RecommendationWeighting> weightings) {
    mWeightings = new ArrayList<RecommendationWeighting>(weightings.size());
    for (RecommendationWeighting recommendationWeighting : weightings) {
      mWeightings.add(new FacadeWeighting(recommendationWeighting));
    }
    Collections.sort(mWeightings, new Comparator<RecommendationWeighting>() {

      public int compare(RecommendationWeighting o1, RecommendationWeighting o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
  }

  public int getRowCount() {
    return mWeightings.size();
  }

  public int getColumnCount() {
    return 2;
  }

  @Override
  public String getColumnName(int column) {
    switch (column) {
      case 0:
        return mLocalizer.msg("type", "Type");
      case 1:
        return mLocalizer.msg("weighting", "Weighting");
    }

    return null;
  }

  public Object getValueAt(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0:
        return mWeightings.get(rowIndex).getName();
      case 1:
        return mWeightings.get(rowIndex).getWeighting();
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
        mWeightings.get(row).setWeighting(weight);
      }
    }
  }

  public Class getColumnClass(int c) {
    return getValueAt(0, c).getClass();
  }
  
  List<RecommendationWeighting> getWeightings() {
    return mWeightings;
  }
}
