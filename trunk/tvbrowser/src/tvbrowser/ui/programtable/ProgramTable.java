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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.plugin.PluginStateListener;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.programtable.background.BackgroundPainter;
import tvbrowser.ui.programtable.background.OneImageBackPainter;
import tvbrowser.ui.programtable.background.SingleColorBackPainter;
import tvbrowser.ui.programtable.background.TimeBlockBackPainter;
import tvbrowser.ui.programtable.background.TimeOfDayBackPainter;
import util.settings.ProgramPanelSettings;
import util.ui.ProgramPanel;
import util.ui.TransferProgram;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class ProgramTable extends JPanel
 implements ProgramTableModelListener,
    DragGestureListener, DragSourceListener, PluginStateListener, Scrollable {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ProgramTable.class);

  private int mColumnWidth;
  private int mHeight;

  private int mCurrentCol;
  private int mCurrentRow;
  private int mCurrentY;

  private ProgramTableLayout mLayout;
  private ProgramTableModel mModel;
  private BackgroundPainter mBackgroundPainter;

  private Point mDraggingPoint;

  /**
   * current mouse coordinates over program table
   */
  private Point mMouse;

  private JPopupMenu mPopupMenu;

  private Runnable mCallback;

  private Thread mClickThread;

  private Thread mLeftClickThread;
  private Thread mMiddleSingleClickThread;

  private Thread mAutoScrollThread;

  private boolean mPerformingSingleClick;
  private boolean mPerformingMiddleSingleClick;

  /**
   * index of the panel underneath the mouse
   */
  private Point mMouseMatrix = new Point(-1, -1);
  private long mLastDragTime;
  private int mLastDragDeltaX;
  private int mLastDragDeltaY;
  private Point mAutoScroll;

  private Point mDraggingPointOnScreen;

  /**
   * Creates a new instance of ProgramTable.
   * @param model program table model to use in the program table
   * @param keyListener The key listener for FAYT.
   */
  public ProgramTable(ProgramTableModel model,KeyListener keyListener) {
    setToolTipText("");
    setProgramTableLayout(null);
    addKeyListener(keyListener);
    
    mCurrentCol = -1;
    mCurrentRow = -1;
    mCurrentY = 0;

    mPerformingSingleClick = false;

    setColumnWidth(Settings.propColumnWidth.getInt());
    setModel(model);
    updateBackground();

    setBackground(Color.white);
    UIManager.put("programPanel.background",Color.white);

    setOpaque(true);

    // setFocusable(true);
    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent evt) {
        handleMouseDragged(evt);
      }

      public void mouseMoved(MouseEvent evt) {
        handleMouseMoved(evt);
      }
    });
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent evt) {
        handleMousePressed(evt);
        if (evt.isPopupTrigger()) {
          showPopup(evt);
        }
      }
      public void mouseReleased(MouseEvent evt) {
        // recognize auto scroll
        if (mDraggingPoint != null
            && Settings.propProgramTableMouseAutoScroll.getBoolean()
            && (System.currentTimeMillis() - mLastDragTime < 20)) {
          if (Math.abs(mLastDragDeltaX) >= 3 || Math.abs(mLastDragDeltaY) >= 3) {
            // stop last scroll, if it is still active
            stopAutoScroll();
            startAutoScroll(new Point(mLastDragDeltaX, mLastDragDeltaY), 2);
          }
        }

        // disable dragging
        mDraggingPoint = null;
        mDraggingPointOnScreen = null;

        if(mClickThread != null && mClickThread.isAlive()) {
          mClickThread.interrupt();
        }

        setCursor(Cursor.getDefaultCursor());
        if (evt.isPopupTrigger()) {
          showPopup(evt);
        }

        if (SwingUtilities.isMiddleMouseButton(evt)) {
          stopAutoScroll();
        }
      }
      public void mouseClicked(MouseEvent evt) {
        handleMouseClicked(evt);
      }
      public void mouseExited(MouseEvent evt) {
        handleMouseExited(evt);
      }
    });

    (new DragSource()).createDefaultDragGestureRecognizer(this,
        DnDConstants.ACTION_MOVE, this);
  }



  protected void setModel(ProgramTableModel model) {
    mModel = model;
    mModel.addProgramTableModelListener(this);

    updateLayout();
  }



  public ProgramTableModel getModel() {
    return mModel;
  }

  public ProgramTableLayout getProgramTableLayout() {
    return mLayout;
  }


  public void setProgramTableLayout(ProgramTableLayout layout) {
    if (layout == null) {
      // Use the default layout
      if (Settings.propTableLayout.getString().equals(Settings.LAYOUT_COMPACT)) {
        layout = new CompactLayout();
      } else if(Settings.propTableLayout.getString().equals(Settings.LAYOUT_REAL_COMPACT)) {
        layout = new RealCompactLayout();
      } else if(Settings.propTableLayout.getString().equals(Settings.LAYOUT_TIME_SYNCHRONOUS)) {
        layout = new TimeSynchronousLayout();
      } else if(Settings.propTableLayout.getString().equals(Settings.LAYOUT_TIME_BLOCK)) {
        layout = new TimeBlockLayout();
      } else if(Settings.propTableLayout.getString().equals(Settings.LAYOUT_COMPACT_TIME_BLOCK)) {
        layout = new CompactTimeBlockLayout();
      } else if(Settings.propTableLayout.getString().equals(Settings.LAYOUT_OPTIMIZED_COMPACT_TIME_BLOCK)) {
        layout = new OptimizedCompactTimeBlockLayout();
      } else {
        layout = new RealTimeSynchronousLayout();
      }
    }

    mLayout = layout;

    if (mModel != null) {
      updateLayout();
      revalidate();
    }
  }


  public void setColumnWidth(int columnWidth) {
    mColumnWidth = columnWidth;
  }



  public int getColumnWidth() {
    return mColumnWidth;
  }


  public void updateBackground() {
    BackgroundPainter oldPainter = mBackgroundPainter;
    
    String background = Settings.propTableBackgroundStyle.getString();
    
    if(oldPainter instanceof SingleColorBackPainter && !background.equals("singleColor")) {
      resetBackground();
    }
    
    if (background.equals("timeOfDay")) {
      mBackgroundPainter = new TimeOfDayBackPainter();
    } else if (background.equals("singleColor")) {
      mBackgroundPainter = new SingleColorBackPainter();
    } else if (background.equals("oneImage")) {
      mBackgroundPainter = new OneImageBackPainter();
    } else { // timeBlock
      mBackgroundPainter = new TimeBlockBackPainter();
    }
    mBackgroundPainter.layoutChanged(mLayout, mModel);

    firePropertyChange("backgroundpainter", oldPainter, mBackgroundPainter);

    repaint();
  }
  
  private void resetBackground() {
    Color temp = Settings.propProgramTableBackgroundSingleColor.getColor();
    
    Settings.propProgramTableBackgroundSingleColor.setColor(Color.white);
    repaint();
    Settings.propProgramTableBackgroundSingleColor.setColor(temp);
  }


  public BackgroundPainter getBackgroundPainter() {
    return mBackgroundPainter;
  }


  public void paintComponent(Graphics grp) {
    if (Settings.propEnableAntialiasing.getBoolean()) {
      final Graphics2D g2d = (Graphics2D) grp;
      if (null != g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      }
    }

    // Using the information of the clip bounds, we can speed up painting
    // significantly
    Rectangle clipBounds = grp.getClipBounds();

    // Paint the table cells
    int minCol = clipBounds.x / mColumnWidth;
    if (minCol < 0) {
      minCol = 0;
    }
    int maxCol = (clipBounds.x + clipBounds.width) / mColumnWidth;
    int columnCount = mModel.getColumnCount();
    if (maxCol >= columnCount) {
      maxCol = columnCount - 1;
    }

    // Paint the background
    super.paintComponent(grp);
    int tableHeight = Math.max(mHeight, clipBounds.y + clipBounds.height);
    mBackgroundPainter.paintBackground(grp, mColumnWidth, tableHeight,
        minCol, maxCol, clipBounds, mLayout, mModel);

    boolean mouseOver = false;

    int x = minCol * mColumnWidth;
    for (int col = minCol; col <= maxCol; col++) {
      int y = mLayout.getColumnStart(col);

      int rowCount = mModel.getRowCount(col);
      for (int row = 0; row < rowCount; row++) {
        // Get the program
        ProgramPanel panel = mModel.getProgramPanel(col, row);

        // Render the program
        if (panel != null) {
          int cellHeight = panel.getHeight();

          // Check whether the cell is within the clipping area
          if (((y + cellHeight) > clipBounds.y)
              && (y < (clipBounds.y + clipBounds.height))) {

            Rectangle rec = new Rectangle(x, y, mColumnWidth, cellHeight);
            if (Settings.propProgramTableMouseOver.getBoolean()) {
              if ((mMouse != null) && (rec.contains(mMouse))) {
                mouseOver = true;
              } else {
                mouseOver = false;
              }
            }

            // calculate clipping intersection between global clip border and current cell rectangle
            Shape oldClip = grp.getClip();
            rec = rec.intersection((Rectangle)oldClip);

            // Paint the cell
            if (rec.width > 0 || rec.height > 0) {
              grp.setClip(rec);
              grp.translate(x, y);

              panel.setSize(mColumnWidth, cellHeight);
              panel.paint(mouseOver,(row == mCurrentRow && col == mCurrentCol), grp);

              grp.translate(-x, -y);
              grp.setClip(oldClip);
            }
          }

          // Move to the next row in this column
          y += cellHeight;
        }
      }

      // paint the timeY
      // int timeY = getTimeYOfColumn(col, util.io.IOUtilities.getMinutesAfterMidnight());
      // grp.drawLine(x, timeY, x + mColumnWidth, timeY);

      // Move to the next column
      x += mColumnWidth;
    }

    grp.setClip(clipBounds);

    // Paint the copyright notices
    grp.setColor(Settings.propProgramPanelForegroundColor.getColor());
    Channel[] channelArr = mModel.getShownChannels();
    FontMetrics metric = grp.getFontMetrics();
    for (Channel channel : channelArr) {
      String msg = channel.getCopyrightNotice();
      // repeatedly reduce the font size while the copyright notice is wider than the column
      while (metric.stringWidth(msg) > mColumnWidth) {
        Font font = grp.getFont();
        grp.setFont(font.deriveFont((float)(font.getSize()-1)));
        metric = grp.getFontMetrics();
      }
    }
    for (int i = 0; i < channelArr.length; i++) {
      String msg = channelArr[i].getCopyrightNotice();
      grp.drawString(msg, i * mColumnWidth + 3, getHeight() - 5);
    }

    if (clipBounds.width - x > 0) {
      grp.setColor(Color.WHITE);
      grp.fillRect(x, 0, clipBounds.width - x, clipBounds.height);
    }

    /*
    // Paint the clipBounds
    System.out.println("Painting rect: " + clipBounds);
    grp.setColor(new Color((int)(Math.random() * 256), (int)(Math.random() * 256),
      (int)(Math.random() * 256)));
    grp.drawRect(clipBounds.x, clipBounds.y, clipBounds.width - 1, clipBounds.height - 1);
    /**/
    //scroll to somewhere?
    if(mCallback != null) {
      runCallback();
    }
  }


  public Dimension getPreferredSize() {
    return new Dimension(mModel.getColumnCount() * mColumnWidth, mHeight);
  }



  public Program getProgramAt(int x, int y) {
    int col = x / mColumnWidth;

    if ((col < 0) || (col >= mModel.getColumnCount())) {
      return null;
    }

    int currY = mLayout.getColumnStart(col);
    if (y < currY) {
      return null;
    }
    int rowCount = mModel.getRowCount(col);
    for (int row = 0; row < rowCount; row++) {
      ProgramPanel panel = mModel.getProgramPanel(col, row);
      currY += panel.getHeight();
      if (y < currY) {
        return panel.getProgram();
      }
    }

    return null;
  }


  public void forceRepaintAll() {
    int columnCount = mModel.getColumnCount();
    for (int col = 0; col < columnCount; col++) {
      int rowCount = mModel.getRowCount(col);
      for (int row = 0; row < rowCount; row++) {
        ProgramPanel panel = mModel.getProgramPanel(col, row);
        panel.setTextColor(Settings.propProgramPanelForegroundColor.getColor());
        panel.setProgramPanelSettings(new ProgramPanelSettings(Settings.propPictureType.getInt(), Settings.propPictureStartTime.getInt(), Settings.propPictureEndTime.getInt(), false, Settings.propIsPictureShowingDescription.getBoolean(), Settings.propPictureDuration.getInt(), Settings.propPicturePluginIds.getStringArray()));
        panel.forceRepaint();
      }
    }
  }


  public void updateLayout() {
    mLayout.updateLayout(mModel);

    // Set the height equal to the highest column
    mHeight = 0;
    int columnCount = mModel.getColumnCount();
    for (int col = 0; col < columnCount; col++) {
      int colHeight = mLayout.getColumnStart(col);
      int rowCount = mModel.getRowCount(col);
      for (int row = 0; row < rowCount; row++) {
        ProgramPanel panel = mModel.getProgramPanel(col, row);
        colHeight += panel.getHeight();
      }

      if (colHeight > mHeight) {
        mHeight = colHeight;
      }
    }

    // Add 20 for the copyright notice
    mHeight += 20;

    if (mBackgroundPainter != null) {
      mBackgroundPainter.layoutChanged(mLayout, mModel);
    }

    repaint();
  }



  public void scrollBy(int deltaX, int deltaY) {
    if (getParent() instanceof JViewport) {
      JViewport viewport = (JViewport) getParent();
      Point oldViewPos = viewport.getViewPosition();
      Point viewPos = new Point(oldViewPos.x, oldViewPos.y);
      if (deltaX!=0){
        viewPos.x += deltaX;
        int maxX = getWidth() - viewport.getWidth();

        viewPos.x = Math.min(viewPos.x, maxX);
        viewPos.x = Math.max(viewPos.x, 0);
      }
      if (deltaY !=0){
        viewPos.y += deltaY;
        int maxY = getHeight() - viewport.getHeight();

        viewPos.y = Math.min(viewPos.y, maxY);
        viewPos.y = Math.max(viewPos.y, 0);
      }
      if (viewPos.equals(oldViewPos)) {
        stopAutoScroll();
      } else {
        viewport.setViewPosition(viewPos);
      }
    }
  }

  public boolean stopAutoScroll() {
    if (mAutoScrollThread != null && mAutoScrollThread.isAlive()) {
      mAutoScrollThread.interrupt();
      mAutoScrollThread = null;
      return true;
    }
    return false;
  }



  /**
   * Creates a context menu containing all subscribed plugins that support
   * context menus.
   *
   * @param program
   *          The program to create the context menu for.
   * @return a plugin context menu.
   */
  private JPopupMenu createPluginContextMenu(Program program) {
    return PluginProxyManager.createPluginContextMenu(program);
  }

  private void showPopup(MouseEvent evt) {
    stopAutoScroll();
    mMouse = evt.getPoint();
    repaint();

    Program program = getProgramAt(evt.getX(), evt.getY());
    if (program != null) {
      deSelectItem();
      mPopupMenu = createPluginContextMenu(program);
      mPopupMenu.show(this, evt.getX(), evt.getY());
    }
  }

  private void handleMousePressed(MouseEvent evt) {
    requestFocus();

    if(mClickThread == null || !mClickThread.isAlive()) {
      mClickThread = new Thread("Single click") {
        public void run() {
          try {
            Thread.sleep(Plugin.SINGLE_CLICK_WAITING_TIME + 50);
            setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
          } catch (InterruptedException e) {}
        }
      };

      if(!evt.isShiftDown() && SwingUtilities.isLeftMouseButton(evt)) {
        mClickThread.start();
      }
    }

    mDraggingPoint = evt.getPoint();
    mDraggingPointOnScreen = new Point(evt.getXOnScreen(), evt.getYOnScreen());
  }



  private void handleMouseClicked(final MouseEvent evt) {
    // disable normal click handling if we only want to stop auto scrolling
    if (stopAutoScroll()) {
      return;
    }

    if(mClickThread != null && mClickThread.isAlive()) {
      mClickThread.interrupt();
    }

    mMouse = evt.getPoint();
    repaint();
    final Program program = getProgramAt(evt.getX(), evt.getY());

    if (SwingUtilities.isLeftMouseButton(evt) && (evt.getClickCount() == 1) && (evt.getModifiersEx() == 0 || evt.getModifiersEx() == InputEvent.CTRL_DOWN_MASK)) {
      mLeftClickThread = new Thread("Program table single click thread") {
      	int modifiers = evt.getModifiersEx();
        public void run() {
          try {
            mPerformingSingleClick = false;
            sleep(Plugin.SINGLE_CLICK_WAITING_TIME);
            mPerformingSingleClick = true;

            if(program != null) {
              deSelectItem();
              if (modifiers == 0) {
              	Plugin.getPluginManager().handleProgramSingleClick(program);
              }
              else if (modifiers == InputEvent.CTRL_DOWN_MASK) {
              	Plugin.getPluginManager().handleProgramSingleCtrlClick(program, null);
              }
            }

            if(mClickThread != null && mClickThread.isAlive()) {
              mClickThread.interrupt();
            }

            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            mPerformingSingleClick = false;
          } catch (InterruptedException e) {
            // IGNORE
          }
        }
      };

      mLeftClickThread.setPriority(Thread.MIN_PRIORITY);
      mLeftClickThread.start();
    }
    if (SwingUtilities.isLeftMouseButton(evt) && (evt.getClickCount() == 2)) {
      if(!mPerformingSingleClick && mLeftClickThread != null && mLeftClickThread.isAlive()) {
        mLeftClickThread.interrupt();
      }

      if (program != null && !mPerformingSingleClick) {
        deSelectItem();

        // This is a left double click
        // -> Execute the program using the user defined default plugin

        if(evt.getModifiersEx() == 0) {
          Plugin.getPluginManager().handleProgramDoubleClick(program);
        }
      }

      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
    else if (SwingUtilities.isLeftMouseButton(evt) && (evt.getClickCount() == 1) &&
        (evt.isShiftDown())) {
      if (program != null) {
        if(!isSelectedItemAt(evt.getX(),evt.getY())) {
          selectItemAt(evt.getX(),evt.getY());
        }
        else {
          deSelectItem();
        }
      }
    }
    else if (SwingUtilities.isMiddleMouseButton(evt) && (evt.getClickCount() == 1)) {
      mMiddleSingleClickThread = new Thread("Program table single middle click thread") {
        public void run() {
          try {
            mPerformingMiddleSingleClick = false;
            sleep(Plugin.SINGLE_CLICK_WAITING_TIME);
            mPerformingMiddleSingleClick = true;

            if(program != null) {
              deSelectItem();
              Plugin.getPluginManager().handleProgramMiddleClick(program);
            }

            if(mClickThread != null && mClickThread.isAlive()) {
              mClickThread.interrupt();
            }

            mPerformingMiddleSingleClick = false;
          } catch (InterruptedException e) {
            // IGNORE
          }
        }
      };

      mMiddleSingleClickThread.setPriority(Thread.MIN_PRIORITY);
      mMiddleSingleClickThread.start();
    }
    if (SwingUtilities.isMiddleMouseButton(evt) && (evt.getClickCount() == 2)) {
      if(!mPerformingMiddleSingleClick && mMiddleSingleClickThread != null && mMiddleSingleClickThread.isAlive()) {
        mMiddleSingleClickThread.interrupt();
      }

      if (program != null && !mPerformingMiddleSingleClick) {
        deSelectItem();
        // This is a middle double click
        // -> Execute the program using the user defined default plugin
        Plugin.getPluginManager().handleProgramMiddleDoubleClick(program);
      }

      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }



  private void handleMouseDragged(final MouseEvent evt) {
    if (mDraggingPoint != null && !evt.isShiftDown()) {
      if (SwingUtilities.isLeftMouseButton(evt)) {
        stopAutoScroll();
        mLastDragDeltaX = mDraggingPoint.x - evt.getX();
        mLastDragDeltaY = mDraggingPoint.y - evt.getY();
        scrollBy(mLastDragDeltaX, mLastDragDeltaY);
        mLastDragTime = System.currentTimeMillis();
      } else if (SwingUtilities.isMiddleMouseButton(evt)
          && mDraggingPointOnScreen != null) {
        Point scroll = new Point(evt.getXOnScreen() - mDraggingPointOnScreen.x,
            evt.getYOnScreen() - mDraggingPointOnScreen.y);
        startAutoScroll(scroll, 10);
      }
    }
  }


  private void handleMouseMoved(MouseEvent evt) {
    if (Settings.propProgramTableMouseOver.getBoolean()) {
      if ((mPopupMenu == null) || (!mPopupMenu.isVisible())) {
        mMouse = evt.getPoint();
        Point cellIndex = getMatrix(mMouse.x, mMouse.y);
        // restore previous panel under mouse
        repaintCell(mMouseMatrix);
        if (cellIndex.x >= 0 && cellIndex.y >= 0) {
          if (cellIndex.x != mMouseMatrix.x || cellIndex.y != mMouseMatrix.y) {
            // now update the current panel
            mMouseMatrix  = cellIndex;
            repaintCell(mMouseMatrix);
          }
        }
      }
    }
  }

  /**
   * repaint the program table cell with the given index
   *
   * @param cellIndex index of the program panel
   * @since 2.6
   */
  private void repaintCell(final Point cellIndex) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        if ((cellIndex.x >= 0 || cellIndex.y >= 0) && cellIndex.x < mModel.getColumnCount()) {
          Rectangle cellRect = getCellRect(cellIndex.x, cellIndex.y);
          if (cellRect != null) {
            repaint(cellRect);
          }
        }
      }
    });
  }


  /**
   * repaint the currently selected cell (keyboard selection)
   * @since 2.6
   */
  private void repaintCurrentCell() {
    repaintCell(new Point(mCurrentCol, mCurrentRow));
  }


  private void handleMouseExited(MouseEvent evt) {
    if (Settings.propProgramTableMouseOver.getBoolean()) {
      JViewport viewport = (JViewport) getParent();
      if (((mPopupMenu == null) || (!mPopupMenu.isVisible())) && !viewport.getViewRect().contains(evt.getPoint())) {
        repaintCell(mMouseMatrix);
        mMouse = null;
        mMouseMatrix = new Point(-1, -1);
      }
    }
  }


  /**
   * get the average Y coordinate of the center of the program panels of all
   * columns where the program is running at the given time
   *
   * @param minutesAfterMidnight
   * @return y offset
   */
  protected int getTimeY(final int minutesAfterMidnight) {
    // Get the total time y
    int totalTimeY = 0;
    int parts = 0;
    int columnCount = mModel.getColumnCount();
    int[] y = new int[columnCount];
    for (int col = 0; col < columnCount; col++) {
      y[col] = getTimeYOfColumn(col, minutesAfterMidnight);
      if (y[col] > 0) {
        totalTimeY += y[col];
        parts++;
      }
    }

    // Return the average time y
    if (parts == 0) {
      // avoid division by zero
      return 0;
    } else {
      return totalTimeY / parts;
    }
  }



  /**
   * get the Y coordinate of the center of the program panel in this column
   * where the program is running at the given time
   *
   * @param col
   * @param minutesAfterMidnight
   * @return
   */
  private int getTimeYOfColumn(int col, int minutesAfterMidnight) {
    int timeY = mLayout.getColumnStart(col);
    Date mainDate = mModel.getDate();

    int lastPanelHeight = 0;
    int rowCount = mModel.getRowCount(col);
    for (int row = 0; row < rowCount; row++) {
      ProgramPanel panel = mModel.getProgramPanel(col, row);
      Program program = panel.getProgram();
      int startTime = program.getStartTime();

      // Add 24 hours for every day different to the model's main date
      startTime += program.getDate().getNumberOfDaysSince(mainDate) * 24 * 60;

      // upper border of current program panel
      if (startTime == minutesAfterMidnight) {
        return timeY;
      }

      // somewhere inside current panel
      final int progLength = program.getLength();
      int panelHeight = panel.getHeight();
      if (progLength > 0 && startTime < minutesAfterMidnight
          && startTime + progLength > minutesAfterMidnight) {
        if (panelHeight > 800) {
          return 0;  // very large programs (due to filters) falsify calculation
        }
        return timeY + panelHeight * (minutesAfterMidnight - startTime)
            / progLength;
      }

      // It was between current and previous program
      if (startTime > minutesAfterMidnight) {
        if (row == 0) {
          return 0; // there is no panel for this time at all, do not take this column into account
        }
        if (lastPanelHeight > 800) {
          return 0; // last program was much to large to take this into account
        }
        return timeY;
      }

      timeY += panelHeight;
      lastPanelHeight = panelHeight;
    }

    return -1;
  }



  private Rectangle getCellRect(int cellCol, int cellRow) {
    int x = cellCol * mColumnWidth;
    int width = mColumnWidth;

    int y = mLayout.getColumnStart(cellCol);
    int rowCount = mModel.getRowCount(cellCol);
    for (int row = 0; row < rowCount; row++) {
      ProgramPanel panel = mModel.getProgramPanel(cellCol, row);
      int height = panel.getHeight();
      if (row == cellRow) {
        return new Rectangle(x, y, width, height);
      }
      y += height;
    }

    // Invalid cell
    return null;
  }


  // implements ProgramTableModelListener

  /**
   * runs the Runnable callback that scrolls to the wanted place
   * in the ProgramTable
   */
  public void runCallback() {
    SwingUtilities.invokeLater(new Runnable(){
      public void run() {
        if(mCallback != null) {
          mCallback.run();
        }
        mCallback = null;
      }
    });
  }


  public void tableDataChanged(Runnable callback) {
    mCallback = callback;
    updateLayout();
    revalidate();
    repaint();
  }



  public void tableCellUpdated(int col, int row) {
    Rectangle cellRect = getCellRect(col, row);
    if (cellRect != null) {
      repaint(cellRect);
    }
  }

  /**
   * Opens the PopupMenu for the selected program.
   *
   */
  public void showPopupFromKeyboard() {
    if(mCurrentCol == -1 || mCurrentRow == -1) {
      return;
    }

    Program program = mModel.getProgramPanel(mCurrentCol, mCurrentRow).getProgram();
    Rectangle rect = this.getCellRect(mCurrentCol,mCurrentRow);
    scrollRectToVisible(rect);

    mPopupMenu = createPluginContextMenu(program);
    mPopupMenu.show(this, rect.x + (rect.width / 3), rect.y + ((rect.height * 3) / 4));

  }

  /**
   * Starts the middle click Plugin.
   */
  public void startMiddleClickPluginFromKeyboard() {
    if(mCurrentCol == -1 || mCurrentRow == -1) {
      return;
    }

    Program program = mModel.getProgramPanel(mCurrentCol, mCurrentRow).getProgram();

    Plugin.getPluginManager().handleProgramMiddleClick(program);
  }

  /**
   * Starts the middle double click Plugin.
   */
  public void startMiddleDoubleClickPluginFromKeyboard() {
    if(mCurrentCol == -1 || mCurrentRow == -1) {
      return;
    }

    Program program = mModel.getProgramPanel(mCurrentCol, mCurrentRow).getProgram();

    Plugin.getPluginManager().handleProgramMiddleDoubleClick(program);
  }

  /**
   * Starts the left single click Plugin.
   */
  public void startLeftSingleClickPluginFromKeyboard() {
    if(mCurrentCol == -1 || mCurrentRow == -1) {
      return;
    }

    Program program = mModel.getProgramPanel(mCurrentCol, mCurrentRow).getProgram();

    Plugin.getPluginManager().handleProgramSingleClick(program);
  }

  /**
   * Starts the double click Plugin.
   */
  public void startDoubleClickPluginFromKeyboard() {
    if(mCurrentCol == -1 || mCurrentRow == -1) {
      return;
    }

    Program program = mModel.getProgramPanel(mCurrentCol, mCurrentRow).getProgram();

    Plugin.getPluginManager().handleProgramDoubleClick(program);
  }

  /**
   * Go to the right program of the current program.
   *
   */
  public void right() {
    repaintCurrentCell();
    int cols = mModel.getColumnCount();
    int previousCol = mCurrentCol;

    if(cols == 0) {
      return;
    }

    if(mCurrentCol != -1) {
      if(mCurrentCol < cols -1) {
        mCurrentCol++;
      } else {
        mCurrentCol = 0;
      }
    } else {
      mCurrentCol = 0;
    }

    boolean found = false, find = true;
    int colCount = 0;

    do {
      int rows = mModel.getRowCount(mCurrentCol);

      if(previousCol != -1 && rows > 0) {
        Rectangle rectPrev = getCellRect(previousCol,mCurrentRow);
        Rectangle rectCur = getCellRect(mCurrentCol,1);

        if(rectCur != null && rectPrev != null) {
          Point cellIndex = getMatrix(rectCur.x, mCurrentY);
          if(cellIndex.y != -1) {
            ProgramPanel panel = mModel.getProgramPanel(cellIndex.x, cellIndex.y);
            if(panel != null && !panel.getProgram().isExpired()) {
              find = false;
              found = true;
              mCurrentRow = cellIndex.y;
            }
          }
        }
      }

      if(find) {
        for(int i = 0; i < rows; i++) {
          ProgramPanel panel = mModel.getProgramPanel(mCurrentCol, i);
          if(panel.getProgram().isOnAir() || !panel.getProgram().isExpired()) {
            found = true;
            mCurrentRow = i;
            break;
          }
        }
      }

      if(!found) {
        colCount++;
        if(mCurrentCol < cols - 1) {
          mCurrentCol++;
        } else {
          mCurrentCol = 0;
        }
      }
      if(colCount >= cols) {
        deSelectItem();
        return;
      }
    }while(!found);

    repaintCurrentCell();
    scrollToSelection();
  }

  /**
   * Go to the program on top of the current program.
   *
   */
  public void up() {
    repaintCurrentCell();
    if(mCurrentCol == -1) {
      right();
    } else {
      int rows = mModel.getRowCount(mCurrentCol);
      ProgramPanel panel = mModel.getProgramPanel(mCurrentCol, mCurrentRow);

      if(panel.getProgram().isOnAir()) {
        mCurrentRow = rows - 1;
      } else {
        mCurrentRow--;
      }

      if(mCurrentRow < 0) {
        mCurrentRow = rows - 1;
      }

      repaintCurrentCell();
      mCurrentY = getCellRect(mCurrentCol, mCurrentRow).y;
      scrollToSelection();
    }
  }

  /**
   * Go to the program under the current program.
   *
   */
  public void down() {
    repaintCurrentCell();
    if(mCurrentCol == -1) {
      right();
    } else {
      int rows = mModel.getRowCount(mCurrentCol);
      if(mCurrentRow >= rows -1) {
        for(int i = 0; i < rows; i++) {
          ProgramPanel panel = mModel.getProgramPanel(mCurrentCol, i);
          if(panel.getProgram().isOnAir() || !panel.getProgram().isExpired()) {
            mCurrentRow = i;
            break;
          }
        }
      } else {
        mCurrentRow++;
      }

      repaintCurrentCell();
      mCurrentY = getCellRect(mCurrentCol, mCurrentRow).y;
      scrollToSelection();
    }
  }

  /**
   * Go to the left program of the current program.
   *
   */
  public void left() {
    repaintCurrentCell();
    if(mCurrentCol == -1) {
      right();
    } else {
      int previousCol = mCurrentCol;
      boolean found = false, find = true;

      do {
        if(mCurrentCol == 0) {
          mCurrentCol = mModel.getColumnCount() - 1;
        } else {
          mCurrentCol--;
        }

        int rows = mModel.getRowCount(mCurrentCol);

        if(previousCol != -1 && rows > 0) {
          Rectangle rectPrev = getCellRect(previousCol,mCurrentRow);
          Rectangle rectCur = getCellRect(mCurrentCol,1);

          if(rectCur != null && rectPrev != null) {
            Point cellIndex = getMatrix(rectCur.x, mCurrentY);
            if(cellIndex.y != -1) {
              ProgramPanel panel = mModel.getProgramPanel(cellIndex.x, cellIndex.y);
              if(panel != null && !panel.getProgram().isExpired()) {
                find = false;
                found = true;
                mCurrentRow = cellIndex.y;
              }
            }
          }
        }

        if(find) {
          for(int i = 0; i < rows; i++) {
            ProgramPanel panel = mModel.getProgramPanel(mCurrentCol, i);
            if(panel.getProgram().isOnAir() || !panel.getProgram().isExpired()) {
              found = true;
              mCurrentRow = i;
              break;
            }
          }
        }
      }while(!found);

      repaintCurrentCell();
      scrollToSelection();
    }
  }

  private void scrollToSelection() {
    int height = getVisibleRect().height;
    int width = getVisibleRect().width;
    Rectangle cell = getCellRect(mCurrentCol,mCurrentRow);

    if(cell.height > height) {
      cell.setSize(cell.width,height);
    }
    if(cell.width > width) {
      cell.setSize(width,cell.height);
    }

    this.scrollRectToVisible(cell);
  }

  /**
   * Deselect the selected program.
   *
   */
  public void deSelectItem() {
    repaintCurrentCell();
    mCurrentRow = -1;
    mCurrentCol = -1;
  }

  /**
   * Returns the cell indices for the given point with pixel coordinates
   *
   * @param pointX X position of the point.
   * @param pointY Y position of the point.
   * @return a point, where x is the column and y is the row number
   */
  private Point getMatrix(int pointX, int pointY) {
    int col = pointX / mColumnWidth;

    if ((col < 0) || (col >= mModel.getColumnCount())) {
      return new Point(-1, -1);
    }
    int currY = mLayout.getColumnStart(col);
    if (pointY < currY) {
      return new Point(-1, -1);
    }

    int rowCount = mModel.getRowCount(col);
    for (int row = 0; row < rowCount; row++) {
      ProgramPanel panel = mModel.getProgramPanel(col, row);
      currY += panel.getHeight();
      if (pointY < currY) {
        return new Point(col, row);
      }
    }

    return new Point(-1, -1);
  }

  /**
   * Selects the program at the point(x,y)
   * @param pointX X position of the point
   * @param pointY Y position of the point
   */
  public void selectItemAt(int pointX, int pointY) {
    // restore
    repaintCurrentCell();
    // select
    Point cellIndex = getMatrix(pointX,pointY);
    mCurrentCol = cellIndex.x;
    mCurrentRow = cellIndex.y;
    repaintCurrentCell();
  }

  /**
   *
   * @param x X position of the point
   * @param y Y position of the point
   * @return Is the point at a selected program?
   */
  private boolean isSelectedItemAt(int x, int y) {
    Point cellIndex = getMatrix(x,y);
    return (mCurrentRow == cellIndex.y && mCurrentCol == cellIndex.x);
  }

  public void dragGestureRecognized(DragGestureEvent evt) {
    if(!evt.getTriggerEvent().isShiftDown()) {
      return;
    }
    mMouse = evt.getDragOrigin();

    Program program = getProgramAt(mMouse.x, mMouse.y);
    if (program != null) {
      if(!isSelectedItemAt(mMouse.x,mMouse.y)) {
        selectItemAt(mMouse.x,mMouse.y);
      }
      evt.startDrag(null,new TransferProgram(program), this);
    }
  }

  public void dragEnter(DragSourceDragEvent dsde) {}
  public void dragOver(DragSourceDragEvent dsde) {}
  public void dropActionChanged(DragSourceDragEvent dsde) {}
  public void dragExit(DragSourceEvent dse) {}

  public void dragDropEnd(DragSourceDropEvent dsde) {
    deSelectItem();
  }



  /**
   * Select (highlight) a program in the program table. This will deselect any other programs.
   * @param program the program to select
   * @since 2.6
   */
  public void selectProgram(Program program) {
    int columnCount = mModel.getColumnCount();
    for (int col = 0; col < columnCount; col++) {
      int rowCount = mModel.getRowCount(col);
      for (int row = 0; row < rowCount; row++) {
        ProgramPanel panel = mModel.getProgramPanel(col, row);
        if (panel.getProgram().equals(program)) {
          mCurrentCol = col;
          mCurrentRow = row;
          repaintCurrentCell();
          return;
        }
      }
    }
  }

  // Fix for [TVB-50]

  @Override
  public void addNotify() {
    super.addNotify();
    PluginProxyManager.getInstance().addPluginStateListener(this);
  }

  @Override
  public void removeNotify() {
    super.removeNotify();
    PluginProxyManager.getInstance().removePluginStateListener(this);
  }

  public void pluginActivated(PluginProxy plugin) {
    if (plugin.getProgramTableIcons(Plugin.getPluginManager().getExampleProgram()) != null) {
      updatePrograms();
    }
  }

  public void pluginDeactivated(PluginProxy plugin) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        updatePrograms();
      }
    });
  }

  private void updatePrograms() {
    ProgramTableModel model = getModel();
    int cols = model.getColumnCount();
    for (int c = 0; c < cols; c++) {
      int rows = model.getRowCount(c);
      for (int r = 0; r < rows; r++) {
        ProgramPanel programPanel = model.getProgramPanel(c, r);
        programPanel.programHasChanged();
      }
    }
    repaint();
  }

  public void pluginLoaded(PluginProxy plugin) {
    // noop
  }

  public void pluginUnloaded(PluginProxy plugin) {
    // noop
  }

  public Dimension getPreferredScrollableViewportSize() {
    // not implemented
    return getPreferredSize();
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect,
      int orientation, int direction) {
    if (orientation == SwingConstants.VERTICAL) {
      // scroll full page when page up/down is used
      return visibleRect.height;
    } else {
      // force block scrolling to always align the columns as before
      int fullColumns = (int) ((visibleRect.getWidth() + 8) / mColumnWidth);
      if (fullColumns < 1) {
        fullColumns = 1;
      }
      return fullColumns * mColumnWidth;
    }
  }

  public boolean getScrollableTracksViewportHeight() {
    // not implemented
    return false;
  }

  public boolean getScrollableTracksViewportWidth() {
    // not implemented
    return false;
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation,
      int direction) {
    // scroll by full column width, when cursor left/right is used
    if (orientation == SwingConstants.HORIZONTAL) {
      return mColumnWidth;
    }
    // scroll 50 pixels when cursor up/down is used
    return 50;
  }

  @Override
  public String getToolTipText(MouseEvent event) {
    Point mousePoint = event.getPoint();
    Point panelIndex = getMatrix(mousePoint.x, mousePoint.y);
    if (panelIndex.x != -1) {
      ProgramPanel panel = mModel.getProgramPanel(panelIndex.x, panelIndex.y);

      // calculate relative mouse coordinates
      int currY = mLayout.getColumnStart(panelIndex.x);
      for (int row = 0; row < panelIndex.y; row++) {
        currY += mModel.getProgramPanel(panelIndex.x, row).getHeight();
      }
      final int panelX = mousePoint.x - panelIndex.x * mColumnWidth;
      final int panelY = mousePoint.y - currY;
      StringBuilder buffer = new StringBuilder();
      String tooltip = panel.getToolTipText(panelX, panelY);
      if (tooltip != null && tooltip.length() > 0) {
        buffer.append(tooltip);
      }

      // if program is partially not visible then show the title as tooltip
      final JViewport viewport = MainFrame.getInstance()
          .getProgramTableScrollPane().getViewport();
      Point viewPos = viewport.getViewPosition();
      Dimension viewSize = viewport.getSize();
      final Program program = panel.getProgram();
      if ((currY < viewPos.y)
          || (panelIndex.x * mColumnWidth + panel.getTitleX() < viewPos.x)
          || ((panelIndex.x + 1) * mColumnWidth - 1 > viewPos.x
              + viewSize.width)) {
        if (buffer.indexOf(program.getTitle()) < 0) {
          appendTooltip(buffer, program.getTitle());
        }
      }

      // show end time if start time of next
      // shown program is not end of current program
      ProgramPanel nextPanel = mModel.getProgramPanel(panelIndex.x,
          panelIndex.y + 1);

      boolean showTime = (nextPanel == null && program.getLength() > 0);
      if (nextPanel != null) {
        int length = program.getLength();
        int nextStartTime = nextPanel.getProgram().getStartTime();
        if (nextStartTime < program.getStartTime()) {
          nextStartTime += 24 * 60;
        }
        if ((length > 0)
            && (program.getStartTime() + length + 1 < nextStartTime)) {
          showTime = true;
        }
      }
      if (showTime) {
        appendTooltip(buffer, mLocalizer.msg("until", "until {0}", program
          .getEndTimeString()));
      }
      if (buffer.length() > 0) {
        return buffer.toString();
      }
    }
    return null;
  }

  private void appendTooltip(final StringBuilder buffer, final String text) {
    if (buffer.length() > 0) {
      buffer.append(" - ");
    }
    buffer.append(text);
  }

  private void startAutoScroll(final Point scroll, int scaling) {
    // decide which direction to scroll
    if (Math.abs(scroll.x) > Math.abs(scroll.y)) {
      scroll.y = 0;
    } else {
      scroll.x = 0;
    }
    // scale the delta
    if (Math.abs(scroll.x) >= scaling) {
      scroll.x = scroll.x / scaling;
    }
    if (Math.abs(scroll.y) >= scaling) {
      scroll.y = scroll.y / scaling;
    }
    mAutoScroll = scroll;
    // now start, if we are not running already
    if (mAutoScrollThread == null) {
      mAutoScrollThread = new Thread("Autoscrolling") {
        @Override
        public void run() {
          while (mAutoScrollThread != null) {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                scrollBy(mAutoScroll.x, mAutoScroll.y);
              }
            });
            try {
              sleep(30); // speed of scrolling
            } catch (InterruptedException e) {
              mAutoScrollThread = null;
            }
          }
          mAutoScrollThread = null;
        }
      };
      mAutoScrollThread.start();
    }
  }
  
  public boolean isSelected() {
    return mCurrentRow != -1 && mCurrentCol != -1;
  }
}