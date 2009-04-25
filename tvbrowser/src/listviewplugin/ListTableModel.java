/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package listviewplugin;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import util.ui.Localizer;
import devplugin.Channel;
import devplugin.Program;

/**
 * This Class contains the TableModel for the Table
 */
public class ListTableModel extends AbstractTableModel {

  /** Translator */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ListTableModel.class);

  /** Data in this Model */
  private Vector<ListTableModelData> mData = new Vector<ListTableModelData>();

  /**
   * Returns the Column-Name
   */
  public String getColumnName(final int column) {
    switch (column) {
    case 0:
      return Localizer.getLocalization(Localizer.I18N_CHANNEL);
    case 1:
      return Localizer.getLocalization(Localizer.I18N_PROGRAM);
    case 2:
      return mLocalizer.msg("NextProgram", "Next Program");
    default:
      break;
    }
    return super.getColumnName(column);
  }

  /**
   * Adds one Row to the Table
   * 
   * @param channel
   *          Channel
   * @param first
   *          First Program
   * @param second
   *          Second Program
   */
  public void addRow(final Channel channel, final Program first,
      final Program second) {
    final ListTableModelData data = new ListTableModelData();
    data.mChannel = channel;
    data.mFirst = first;
    data.mSecond = checkSecondProgram(first, second);

    mData.add(data);
    fireTableRowsInserted(mData.indexOf(data), mData.indexOf(data));
  }

  /**
   * Updates a Channel-Row in the Table. If the Row wasn't found, it will be
   * added to the Model
   * 
   * @param channel
   *          Channel in Row
   * @param first
   *          First Program
   * @param second
   *          Second Program
   */
  public void updateRow(final Channel channel, final Program first,
      final Program second) {
    final ListTableModelData data = new ListTableModelData();
    data.mChannel = channel;
    data.mFirst = first;
    data.mSecond = checkSecondProgram(first, second);

    final int pos = findRowWithChannel(channel);
    if (pos > -1) {
      final ListTableModelData olddata = mData.get(pos);
      if (!olddata.equals(data)) {
        mData.remove(pos);
        mData.add(pos, data);
        fireTableRowsUpdated(pos, pos);
      }
    } else {
      mData.add(data);
      fireTableRowsInserted(mData.indexOf(data), mData.indexOf(data));
    }
  }

  private Program checkSecondProgram(final Program first, final Program second) {
    // if the second program is more than 12 hours after the first program,
    // don't display it
    if (first != null && second != null) {
      final int diff = second.getDate().compareTo(first.getDate()) * 24
          + second.getStartTime() / 60 - first.getStartTime() / 60;
      if (diff > 12) {
        return null;
      }
    }
    return second;
  }

  public void removeAllRows() {
    final int size = mData.size();
    mData.removeAllElements();
    fireTableRowsDeleted(0, size - 1);
  }

  /**
   * Trys to find a Row with a specific Channel and returns it's Index
   * 
   * @param channel
   *          Find Row with this Channel
   * @return Row with Channel, -1 if not found
   */
  private int findRowWithChannel(final Channel channel) {
    final int size = mData.size();
    for (int i = 0; i < size; i++) {
      final ListTableModelData data = mData.get(i);
      if (data.mChannel.equals(channel)) {
        return i;
      }
    }
    return -1;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount() {
    return 3;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount() {
    return mData.size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt(final int rowIndex, final int columnIndex) {

    if (rowIndex > mData.size()) {
      return null;
    }

    final ListTableModelData data = mData.get(rowIndex);

    switch (columnIndex) {
    case 0:
      return data.mChannel;
    case 1:
      return data.mFirst;
    case 2:
      return data.mSecond;

    default:
      break;
    }

    return null;
  }

  /**
   * Gets the first Program in the Row
   * 
   * @param row
   *          Row
   * @return First Program in Row
   */
  public Program getProgram(final int row) {
    final ListTableModelData data = getListTableModelData(row);

    if (data != null) {
      return data.mFirst;
    }
    return null;
  }

  /**
   * Gets the second Program in the Row
   * 
   * @param row
   *          Row
   * @return Second Program in the Row
   */
  public Program getNextProgram(final int row) {
    final ListTableModelData data = getListTableModelData(row);

    if (data != null) {
      return data.mSecond;
    }
    return null;
  }

  /**
   * Returns the ListTableModelData for a specific Row
   * 
   * @param row
   *          Get Data from this Row
   * @return ListTableModelData in row
   */
  private ListTableModelData getListTableModelData(final int row) {
    if ((row < 0) || (row >= mData.size())) {
      return null;
    }

    return mData.get(row);
  }

  /**
   * The Data in a rows
   */
  private static class ListTableModelData {
    private Channel mChannel;
    private Program mFirst, mSecond;

    public boolean equals(final Object obj) {
      if (obj instanceof ListTableModelData) {
        final ListTableModelData data = (ListTableModelData) obj;

        if (mFirst == null && data.mFirst != null) {
          return false;
        }

        if (mSecond == null && data.mSecond != null) {
          return false;
        }

        if (mFirst != null && !mFirst.equals(data.mFirst)) {
          return false;
        }

        if (mSecond != null && !mSecond.equals(data.mSecond)) {
          return false;
        }
      }

      return super.equals(obj);
    }
  }

}