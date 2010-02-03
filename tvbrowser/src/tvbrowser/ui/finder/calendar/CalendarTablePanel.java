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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import devplugin.Date;

public class CalendarTablePanel extends AbstractCalendarPanel implements ListSelectionListener {
  private static final int COLUMNS = 7;

  private JTable mTable;
  private CalendarTableModel mTableModel;

  private int mLastColumn = -1;

  private int mLastRow = -1;

  private boolean mAllowEvents = true;

  public CalendarTablePanel() {
    setLayout(new BorderLayout());
    mTableModel = new CalendarTableModel(getFirstDate());
    mTable = new JTable(mTableModel);
    CalendarTableCellRenderer renderer = new CalendarTableCellRenderer();
    mTable.setDefaultRenderer(Date.class, renderer);
    mTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mTable.setRowSelectionAllowed(false);
    mTable.setColumnSelectionAllowed(false);
    mTable.setCellSelectionEnabled(true);
    mTable.setFillsViewportHeight(true);
    mTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    mTable.setShowGrid(true);
    for (int i=0; i < COLUMNS; i++) {
      TableColumn column = mTable.getColumnModel().getColumn(i);
      column.setResizable(false);
    }
    mTable.getTableHeader().setReorderingAllowed(false);
    mTable.getTableHeader().setFont(new Font(mTable.getTableHeader().getFont().getFontName(), Font.PLAIN, mTable.getTableHeader().getFont().getSize() - 2));
    JScrollPane pane = new JScrollPane(mTable);
    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    add(pane, BorderLayout.CENTER);
    addMouseListener(this);
    mTable.getSelectionModel().addListSelectionListener(this);
    mTable.getColumnModel().getSelectionModel().addListSelectionListener(this);
  }

  protected void rebuildControls() {
  }


  public void markDate(final Date date, final Runnable callback) {
    if (!isValidDate(date)) {
      askForDataUpdate(date);
      return;
    }

    if (date.equals(getSelectedDate())) {
      if (callback != null) {
        callback.run();
      }
      return;
    }

    setCurrentDate(date);
    mTableModel.setCurrentDate(date);

    Point position = mTableModel.getPositionOfDate(date);
    if (position != null) {
      mAllowEvents  = false;
      mTable.setColumnSelectionInterval(position.x, position.x);
      mAllowEvents = true;
      mTable.setRowSelectionInterval(position.y, position.y);
    }

    if (mDateChangedListener == null) {
      return;
    }
    repaint();

    Thread thread = new Thread("Finder") {
      public void run() {
        mDateChangedListener.dateChanged(date, CalendarTablePanel.this, callback);
      }
    };
    thread.start();
  }

  public void updateItems() {
    repaint();
  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    if (e.getValueIsAdjusting()) {
      return;
    }
    if (!mAllowEvents) {
      return;
    }
    int column = mTable.getSelectedColumn();
    int row = mTable.getSelectedRow();
    if (column >= 0 && row >= 0) {
      Date date = (Date) mTable.getValueAt(row, column);
      CalendarTableModel model = (CalendarTableModel)mTable.getModel();
      if (date != model.getCurrentDate()) {
        // filter out the duplicate events caused by listening to row and column selection changes
        if (column != mLastColumn || row != mLastRow) {
          markDate(date);
          mLastColumn = column;
          mLastRow = row;
        }
      }
    }
  }


}
