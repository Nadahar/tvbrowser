/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package tvbrowser.ui.finder.calendar;

import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.table.AbstractTableModel;

import devplugin.Date;

public final class CalendarTableModel extends AbstractTableModel {

  private static final int COLUMNS = 7;
  private static final int ROWS = 5;
  private Date[][] mDate = new Date[ROWS][COLUMNS];
  private Date mCurrentDate = Date.getCurrentDate();

  public CalendarTableModel(final Date firstDate) {
    Date date = firstDate;
    while (date.getDayOfWeek() != Calendar.MONDAY) {
      date = date.addDays(-1);
    }
    for (int y = 0; y < ROWS; y++) {
      for (int x = 0; x < COLUMNS; x++) {
        mDate[y][x] = date;
        date = date.addDays(1);
      }
    }
  }

  @Override
  public int getColumnCount() {
    return COLUMNS;
  }

  @Override
  public int getRowCount() {
    return ROWS;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return mDate[rowIndex][columnIndex];
  }

  @Override
  public String getColumnName(int column) {
    String[] days = new SimpleDateFormat().getDateFormatSymbols().getWeekdays();
    int index = column + Calendar.MONDAY;
    if (index > Calendar.SATURDAY) {
      index -= 7;
    }
    return days[index].substring(0, 1);
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return Date.class;
  }

  public void setCurrentDate(final Date date) {
    mCurrentDate = date;
  }

  public Date getCurrentDate() {
    return mCurrentDate;
  }
  
  public Point getPositionOfDate(Date date) {
    for (int row = 0;row < ROWS;row++) {
      for (int column = 0;column < COLUMNS;column++) {
        if (mDate[row][column].equals(date)) {
          return new Point(column, row);
        }
      }
    }

    return null;
  }


}
