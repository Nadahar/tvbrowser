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
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
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

  private static final double SCROLL_OFFSET_FROM_TOP = 0.15;

  private ProgramTable mProgramTable;

  private ChannelPanel mChannelPanel;

  private boolean initialScrollingDone = false;

  private int mScrolledTime = -1;
  
  private KeyListener mKeyListener;

  /**
   * Creates a new instance of ProgramTableScrollPane.
   * 
   */
  public ProgramTableScrollPane(ProgramTableModel model,KeyListener keyListener) {
    setFocusable(true);

    mKeyListener = keyListener;
    mProgramTable = new ProgramTable(model,keyListener);
    setViewportView(mProgramTable);
    
    getViewport().addKeyListener(keyListener);
    addKeyListener(keyListener);

    
    setWheelScrollingEnabled(false);
    addMouseWheelListener(this);

    getHorizontalScrollBar().setUnitIncrement(Settings.propColumnWidth.getInt());
    getVerticalScrollBar().setUnitIncrement(50);

    getHorizontalScrollBar().setFocusable(false);
    getVerticalScrollBar().setFocusable(false);

    mChannelPanel = new ChannelPanel(mProgramTable.getColumnWidth(), model.getShownChannels(), keyListener);
    
    JPanel dummy = new JPanel();
    dummy.setOpaque(false);
    setRowHeaderView(dummy);
    getRowHeader().setOpaque(false);
    
    setColumnHeaderView(mChannelPanel);
    getColumnHeader().setOpaque(false);
    getViewport().setOpaque(false);
    setOpaque(false);

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

    // whenever the vertical scroll bar is moved, reset the current time
    getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

      @Override
      public void adjustmentValueChanged(AdjustmentEvent e) {
        resetScrolledTime();
      }
    });
    
    getVerticalScrollBar().addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        requestFocus();
      }
    });
    getHorizontalScrollBar().addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        requestFocus();
      }
    });
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

  /**
   * completely refresh the channel labels at the top of the program table
   */
  public void updateChannelPanel() {
    mChannelPanel = new ChannelPanel(mProgramTable.getColumnWidth(), mProgramTable.getModel().getShownChannels(),mKeyListener);
    setColumnHeaderView(mChannelPanel);
    this.repaint();
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
          int visibleColumns = getViewport().getWidth() / mProgramTable.getColumnWidth();
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

    scrollPos.y = mProgramTable.getTimeY(minutesAfterMidnight) - (int)(Math.round(getViewport().getHeight() * SCROLL_OFFSET_FROM_TOP));

    if (scrollPos.y < 0) {
      scrollPos.y = 0;
    }

    int max = Math.max(0, mProgramTable.getHeight() - getViewport().getHeight());
    if (scrollPos.y > max) {
      scrollPos.y = max;
    }

    getViewport().setViewPosition(scrollPos);

    mScrolledTime = minutesAfterMidnight;
  }

  protected void handleBackgroundPainterChanged(BackgroundPainter painter) {
    setRowHeaderView(painter.getTableWest());
  }

  // implements ProgramTableModelListener

  public void tableDataChanged(Runnable callback) {
    mChannelPanel.setShownChannels(mProgramTable.getModel().getShownChannels(),mKeyListener);
    if (Settings.propTableBackgroundStyle.getString().equals("timeBlock") && Settings.propTimeBlockShowWest.getBoolean()) {
      getRowHeader().getView().repaint();
    }
  }

  public void tableCellUpdated(int col, int row) {
  }

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
    mProgramTable.showPopupFromKeyboard();
  }

  /**
   * Starts the middle click Plugin.
   */
  public void handleMiddleClick() {
    mProgramTable.startMiddleClickPluginFromKeyboard();
  }

  /**
   * Starts the middle double click Plugin.
   */
  public void handleMiddleDoubleClick() {
    mProgramTable.startMiddleDoubleClickPluginFromKeyboard();
  }

  /**
   * Starts the double click Plugin.
   */
  public void handleLeftSingleClick() {
    mProgramTable.startLeftSingleClickPluginFromKeyboard();
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
    getHorizontalScrollBar().setBlockIncrement(getFullColumns() * columnWidth);
  }

  private int getFullColumns() {
    int columnWidth = mProgramTable.getColumnWidth();
    int fullColumns = (getViewport().getWidth() + 8) / columnWidth;
    if (fullColumns < 1) {
      return 1;
    }
    return fullColumns;
  }

  /**
   * get the currently scrolled time of the program table
   * @return time in minutes after midnight, or -1 if it is unknown
   */
  public int getScrolledTime() {
    return mScrolledTime;
  }

  public void resetScrolledTime() {
    mScrolledTime = -1;
  }

  public void scrollPageRight() {
    scrollPageHorizontal(+1);
  }

  public void scrollPageLeft() {
    scrollPageHorizontal(-1);
  }

  private void scrollPageHorizontal(int direction) {
    JScrollBar scrollBar = getHorizontalScrollBar();
    int pos = scrollBar.getValue() + direction * getFullColumns() * mProgramTable.getColumnWidth();
    int max = scrollBar.getMaximum() - scrollBar.getVisibleAmount();
    if (pos > max) {
      pos = max;
    }
    if (pos < 0) {
      pos = 0;
    }
    scrollBar.setValue(pos);
  }
  
  /**
   * Updates Persona after change.
   */
  public void updatePersona() {
    mChannelPanel.updatePersona();
  }
}