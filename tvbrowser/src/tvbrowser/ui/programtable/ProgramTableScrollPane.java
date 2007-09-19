/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package tvbrowser.ui.programtable;

import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;

import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.programtable.background.BackgroundPainter;
import devplugin.Channel;

/**
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ProgramTableScrollPane extends JScrollPane implements ProgramTableModelListener,
    MouseWheelListener, ChangeListener {

  private ProgramTable mProgramTable;

  private ChannelPanel mChannelPanel;

  private boolean initialScrollingDone = false;

  /**
   * Creates a new instance of ProgramTableScrollPane.
   */
  public ProgramTableScrollPane(ProgramTableModel model) {
    setFocusable(true);

    mProgramTable = new ProgramTable(model);
    setViewportView(mProgramTable);

    setWheelScrollingEnabled(false);
    addMouseWheelListener(this);

    getHorizontalScrollBar().setUnitIncrement(Settings.propColumnWidth.getInt());
    getVerticalScrollBar().setUnitIncrement(50);

    getHorizontalScrollBar().setFocusable(false);
    getVerticalScrollBar().setFocusable(false);

    mChannelPanel = new ChannelPanel(mProgramTable.getColumnWidth(), model.getShownChannels());
    setColumnHeaderView(mChannelPanel);

    setOpaque(false);
    // setBorder(mDefaultBorder);

    // NOTE: To avoid NullPointerExceptions the registration as listener must
    // happen after all member have been initialized.
    // (at the end of the constructor)
    model.addProgramTableModelListener(this);

    mProgramTable.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("backgroundpainter")) {
          BackgroundPainter painter = (BackgroundPainter) evt.getNewValue();
          handleBackgroundPainterChanged(painter);
        }
      }
    });
    handleBackgroundPainterChanged(mProgramTable.getBackgroundPainter());
    
    getViewport().addChangeListener(this);
    addComponentListener(new ComponentListener() {

      public void componentHidden(ComponentEvent e) {
      }

      public void componentMoved(ComponentEvent e) {
      }

      public void componentResized(ComponentEvent e) {
        updateScrollBars();
      }

      public void componentShown(ComponentEvent e) {
      }});
  }

  public ProgramTable getProgramTable() {
    return mProgramTable;
  }

  public void forceRepaintAll() {
    getProgramTable().forceRepaintAll();
    tableDataChanged(null);
    getProgramTable().tableDataChanged(null);
  }

  @Override
  public void repaint() {
    super.repaint();
    if (mProgramTable != null) {
      mProgramTable.repaint();
    }
    if (mChannelPanel != null) {
      mChannelPanel.repaint();
    }
  }

  public void updateChannelPanel() {
    mChannelPanel = new ChannelPanel(mProgramTable.getColumnWidth(), mProgramTable.getModel().getShownChannels());
    setColumnHeaderView(mChannelPanel);
    this.updateUI();
  }

  public void updateChannelLabelForChannel(Channel ch) {
    mChannelPanel.updateChannelLabelForChannel(ch);
  }

  public void setColumnWidth(int columnWidth) {
    mProgramTable.setColumnWidth(columnWidth);
    mChannelPanel.setColumnWidth(columnWidth);
    getHorizontalScrollBar().setUnitIncrement(columnWidth);
    updateScrollBars();
  }

  public void scrollToChannel(Channel channel) {
    Channel[] shownChannelArr = mProgramTable.getModel().getShownChannels();
    for (int col = 0; col < shownChannelArr.length; col++) {
      if (channel.equals(shownChannelArr[col])) {
        Point scrollPos = getViewport().getViewPosition();
        if (scrollPos != null) {
          int visibleColumns = (int) Math.floor(getViewport().getWidth() / mProgramTable.getColumnWidth());
          scrollPos.x = (col - visibleColumns / 2) * mProgramTable.getColumnWidth();
          if (scrollPos.x < 0) {
            scrollPos.x = 0;
          }
          int max = mProgramTable.getWidth() - getViewport().getWidth();
          if (scrollPos.x > max) {
            scrollPos.x = max;
          }
          getViewport().setViewPosition(scrollPos);
        }
      }
    }
  }

  public void scrollToTime(int minutesAfterMidnight) {
    Point scrollPos = getViewport().getViewPosition();

    scrollPos.y = mProgramTable.getTimeY(minutesAfterMidnight) - (getViewport().getHeight() / 4);

    if (scrollPos.y < 0) {
      scrollPos.y = 0;
    }

    int max = mProgramTable.getHeight() - getViewport().getHeight();
    if (max > 0 && scrollPos.y > max) {
      scrollPos.y = max;
    }

    getViewport().setViewPosition(scrollPos);
  }

  protected void handleBackgroundPainterChanged(BackgroundPainter painter) {
    setRowHeaderView(painter.getTableWest());
  }

  // implements ProgramTableModelListener

  public void tableDataChanged(Runnable callback) {
    mChannelPanel.setShownChannels(mProgramTable.getModel().getShownChannels());
  }

  public void tableCellUpdated(int col, int row) {
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
   */
  public void mouseWheelMoved(MouseWheelEvent e) {

    if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
      if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
        int amount = e.getUnitsToScroll() * getHorizontalScrollBar().getUnitIncrement();
        getHorizontalScrollBar().setValue(getHorizontalScrollBar().getValue() + amount);
      } else {
        int amount = e.getUnitsToScroll() * getVerticalScrollBar().getUnitIncrement();
        getVerticalScrollBar().setValue(getVerticalScrollBar().getValue() + amount);
      }
    }
  }

  /**
   * Go to the right program of the current program.
   * 
   */
  public void right() {
    mProgramTable.right();
  }

  /**
   * Go to the program on top of the current program.
   * 
   */
  public void up() {
    mProgramTable.up();
  }

  /**
   * Go to the program under the current program.
   * 
   */
  public void down() {
    mProgramTable.down();
  }

  /**
   * Go to the left program of the current program.
   * 
   */
  public void left() {
    mProgramTable.left();
  }

  /**
   * Opens the PopupMenu for the selected program.
   * 
   */
  public void showPopupMenu() {
    mProgramTable.showPopoupFromKeyboard();
  }

  /**
   * Starts the middle click Plugin.
   */
  public void handleMiddleClick() {
    mProgramTable.startMiddleClickPluginFromKeyboard();
  }

  /**
   * Starts the double click Plugin.
   */
  public void handleDoubleClick() {
    mProgramTable.startDoubleClickPluginFromKeyboard();
  }

  /**
   * Deselect the selected program.
   * 
   */
  public void deSelectItem() {
    mProgramTable.deSelectItem();
  }

  public void stateChanged(ChangeEvent e) {
    if (e.getSource()==viewport) {
      Point viewPos = viewport.getViewPosition();
      if (viewPos != null) {
        Channel[] shownChannels = MainFrame.getInstance().getProgramTableModel().getShownChannels();
        // no channels visible -> dont sync channel list selection
        if (shownChannels.length == 0) {
          return;
        }
        int columnIndex = (viewPos.x + viewport.getWidth()/2) / mProgramTable.getColumnWidth();
        if (columnIndex >= shownChannels.length) {
          columnIndex = shownChannels.length - 1;
        }
        MainFrame.getInstance().selectChannel(shownChannels[columnIndex]);
      }
    }
  }

  @Override
  public void doLayout() {
    super.doLayout();
    // scroll to current time when the main frame is switched visible
    if (!initialScrollingDone) {
      initialScrollingDone = true;
      Calendar cal = Calendar.getInstance();
      int hour = cal.get(Calendar.HOUR_OF_DAY);
      scrollToTime(hour * 60);
      updateScrollBars();
    }
  }

  private void updateScrollBars() {
    int columnWidth = mProgramTable.getColumnWidth();
    int fullColumns = (getViewport().getWidth() + 8) / columnWidth;
    if (fullColumns < 1) {
      fullColumns = 1;
    }
    getHorizontalScrollBar().setBlockIncrement(fullColumns * columnWidth);
  }
}

// class ProgramTableBorder
