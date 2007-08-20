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
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.ui.programtable.background.BackgroundPainter;
import tvbrowser.ui.programtable.background.OneImageBackPainter;
import tvbrowser.ui.programtable.background.TimeBlockBackPainter;
import tvbrowser.ui.programtable.background.TimeOfDayBackPainter;
import tvbrowser.ui.programtable.background.WhiteBackPainter;
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
implements ProgramTableModelListener, DragGestureListener, DragSourceListener {

  private int mColumnWidth;
  private int mHeight;
  
  private int mCurrentCol;
  private int mCurrentRow;
  private int mCurrentY;
  
  private ProgramTableLayout mLayout;
  private ProgramTableModel mModel;
  private BackgroundPainter mBackgroundPainter;

  private Point mDraggingPoint;

  private Point mMouse;

  private JPopupMenu mPopupMenu;

  private Runnable mCallback;
  
  private Thread mClickThread;
  /**
   * Creates a new instance of ProgramTable.
   */
  public ProgramTable(ProgramTableModel model) {
    setProgramTableLayout(null);
    
    mCurrentCol = -1;
    mCurrentRow = -1;
    mCurrentY = 0;

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
        mDraggingPoint = null;
        
        if(mClickThread != null && mClickThread.isAlive()) {
          mClickThread.interrupt();
        }
        
        setCursor(Cursor.getDefaultCursor());
        if (evt.isPopupTrigger()) {
          showPopup(evt);
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
      if (Settings.propTableLayout.getString().equals("compact")) {
        layout = new CompactLayout();
      } else if(Settings.propTableLayout.getString().equals("realCompact")) {
        layout = new RealCompactLayout();
      } else if(Settings.propTableLayout.getString().equals("timeSynchronous")) {
        layout = new TimeSynchronousLayout();
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
    if (background.equals("timeOfDay")) {
      mBackgroundPainter = new TimeOfDayBackPainter();
    } else if (background.equals("white")) {
      mBackgroundPainter = new WhiteBackPainter();
    } else if (background.equals("oneImage")) {
      mBackgroundPainter = new OneImageBackPainter();
    } else { // timeBlock
      mBackgroundPainter = new TimeBlockBackPainter();
    }
    mBackgroundPainter.layoutChanged(mLayout, mModel);

    firePropertyChange("backgroundpainter", oldPainter, mBackgroundPainter);

    repaint();
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

            if (Settings.propMouseOver.getBoolean()) {
              Rectangle rec = new Rectangle(x, y, mColumnWidth, cellHeight);
              if ((mMouse != null) && (rec.contains(mMouse))) {
                mouseOver = true;
              } else {
                mouseOver = false;
              }
            }

            //          Paint the cell
            grp.translate(x, y);

            panel.setSize(mColumnWidth, cellHeight);
            panel.paint(mouseOver,(row == mCurrentRow && col == mCurrentCol), grp);

            // grp.drawRect(0, 0, mColumnWidth, cellHeight);
            grp.translate(-x, -y);
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


    // Paint the copyright notices
    Channel[] channelArr = mModel.getShownChannels();
    FontMetrics metric = grp.getFontMetrics();
    for (Channel channel : channelArr) {
      String msg = channel.getCopyrightNotice();
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
      Point viewPos = viewport.getViewPosition();

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
      viewport.setViewPosition(viewPos);
    }
  }


  /**
   * Creates a context menu containg all subscribed plugins that support context
   * menues.
   *
   * @param program The program to create the context menu for.
   * @return a plugin context menu.
   */
  private JPopupMenu createPluginContextMenu(Program program) {
    return PluginProxyManager.createPluginContextMenu(program);
  }

  private void showPopup(MouseEvent evt) {
    mMouse = evt.getPoint();
    updateUI();

    Program program = getProgramAt(evt.getX(), evt.getY());
    if (program != null) {
      deSelectItem();
      mPopupMenu = createPluginContextMenu(program);
      mPopupMenu.show(this, evt.getX() - 15, evt.getY() - 15);
    }
  }

  private void handleMousePressed(MouseEvent evt) {
    requestFocus();
    
    if(mClickThread != null && mClickThread.isAlive()) {
      mClickThread.interrupt();
    }
    
    mClickThread = new Thread() {
      public void run() {
        try {
          Thread.sleep(200);
          setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        } catch (InterruptedException e) {}
      }
    };
    
    if(!evt.isShiftDown() && SwingUtilities.isLeftMouseButton(evt)) {
      mClickThread.start();
    }
    
    mDraggingPoint = evt.getPoint();
  }



  private void handleMouseClicked(MouseEvent evt) {
    mMouse = evt.getPoint();
    updateUI();
    Program program = getProgramAt(evt.getX(), evt.getY());
    
    if (SwingUtilities.isLeftMouseButton(evt) && (evt.getClickCount() == 2)) {
      if(mClickThread != null && mClickThread.isAlive()) {
        mClickThread.interrupt();
      }
      
      if (program != null) {
        deSelectItem();
        
        // This is a left double click
        // -> Execute the program using the user defined default plugin
        Plugin.getPluginManager().handleProgramDoubleClick(program);
      }
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
      if (program != null) {
        deSelectItem();
        
        // This is a middle click
        // -> Execute the program using the user defined middle click plugin
        Plugin.getPluginManager().handleProgramMiddleClick(program);
      }
    }
  }



  private void handleMouseDragged(MouseEvent evt) {
    if (mDraggingPoint != null && !evt.isShiftDown()) {
      int deltaX = mDraggingPoint.x - evt.getX();
      int deltaY = mDraggingPoint.y - evt.getY();
      scrollBy(deltaX, deltaY);
    }
  }


  private void handleMouseMoved(MouseEvent evt) {
    if (Settings.propMouseOver.getBoolean()) {
      if ((mPopupMenu == null) || (!mPopupMenu.isVisible())) {
        mMouse = evt.getPoint();
        updateUI();
      }
    }
  }

  private void handleMouseExited(MouseEvent evt) {
    if (Settings.propMouseOver.getBoolean()) {
      JViewport viewport = (JViewport) getParent();
      if (((mPopupMenu == null) || (!mPopupMenu.isVisible())) && !viewport.getViewRect().contains(evt.getPoint())) {
        mMouse = null;
        updateUI();
      }
    }
  }

  public int getTimeY(int minutesAfterMidnight) {
    // Get the total time y
    int totalTimeY = 0;
    int parts = 0;
    int columnCount = mModel.getColumnCount();
	for (int col = 0; col < columnCount; col++) {
      int timeY = getTimeYOfColumn(col, minutesAfterMidnight);
      if (timeY != -1) {
        totalTimeY += timeY;
        parts++;
      }
    }

    // Return the avarage time y
    if (parts == 0) {
      // avoid division by zero
      return 0;
    } else {
      return totalTimeY / parts;
    }
  }



  private int getTimeYOfColumn(int col, int minutesAfterMidnight) {
    int timeY = mLayout.getColumnStart(col);
    Date mainDate = mModel.getDate();

    // Walk to the program that starts before the specified time
    int lastCellHeight = 0;
    int lastDuration = 0;
    int rowCount = mModel.getRowCount(col);
	for (int row = 0; row < rowCount; row++) {
      ProgramPanel panel = mModel.getProgramPanel(col, row);
      Program program = panel.getProgram();
      int startTime = program.getStartTime();
      
      // Add 24 hours for every day different to the model's main date
      startTime += program.getDate().getNumberOfDaysSince(mainDate) * 24 * 60;

      if (startTime > minutesAfterMidnight || row == rowCount-1) {
        // It was the last program
        if(lastCellHeight != 0 && lastDuration > 0 && lastDuration < 360) {
          timeY += lastCellHeight / 2; // Hit the center of the program
        } else {
          timeY = -1;
        }
        return timeY;
      } else {
        timeY += lastCellHeight;
      }

      // Remember the cell height
      lastCellHeight = panel.getHeight();
      lastDuration = program.getLength();
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
  public void showPopoupFromKeyboard() {
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
          int[] matrix = getMatrix(rectCur.x, mCurrentY);
          if(matrix[0] != -1) {
            ProgramPanel panel = mModel.getProgramPanel(matrix[1], matrix[0]);
            if(panel != null && !panel.getProgram().isExpired()) {
              find = false;
              found = true;
              mCurrentRow = matrix[0];              
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
        mCurrentCol = -1;
        mCurrentRow = -1;
        updateUI();
        return;
      }
    }while(!found);
    
    scrollToSelection();
  }
  
  /**
   * Go to the program on top of the current program.
   *
   */
  public void up() {
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
      
      mCurrentY = getCellRect(mCurrentCol, mCurrentRow).y;
      scrollToSelection();
    }    
  }
  
  /**
   * Go to the program under the current program.
   *
   */
  public void down() {
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
      
      mCurrentY = getCellRect(mCurrentCol, mCurrentRow).y;
      scrollToSelection();
    }
  }  
  
  /**
   * Go to the left program of the current program.
   *
   */
  public void left() {
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
            int[] matrix = getMatrix(rectCur.x, mCurrentY);
            if(matrix[0] != -1) {
              ProgramPanel panel = mModel.getProgramPanel(matrix[1], matrix[0]);
              if(panel != null && !panel.getProgram().isExpired()) {
                find = false;
                found = true;
                mCurrentRow = matrix[0];              
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
    updateUI();
  }
  
  /**
   * Deselect the selected program.
   *
   */
  public void deSelectItem() {
    mCurrentRow = -1;
    mCurrentCol = -1;
    updateUI();
  }
  
  /**
   * Returns an array of the indices [0] = row
   * 
   * @param x X position of the point.
   * @param y Y position of the point.
   * @return An array of the indices at the point that was given.
   */
  private int[] getMatrix(int x, int y) {
    int col = x / mColumnWidth;
    int[] matrix = new int[2];

	if ((col < 0) || (col >= mModel.getColumnCount())) {
      matrix[0] = -1;
      matrix[1] = -1;      
      return matrix;
    }

    int currY = mLayout.getColumnStart(col);
    if (y < currY) {
      matrix[0] = -1;
      matrix[1] = -1;      
      return matrix;
    }  

    int rowCount = mModel.getRowCount(col);
	for (int row = 0; row < rowCount; row++) {
      ProgramPanel panel = mModel.getProgramPanel(col, row);
      currY += panel.getHeight();
      if (y < currY) {
        matrix[0] = row;
        matrix[1] = col;
        break;
      }
    }
    
    return matrix;
  }
  
  /**
   * Selects the program at the point(x,y)
   * @param x X position of the point
   * @param y Y position of the point
   */
  public void selectItemAt(int x, int y) {
    int[] matrix = getMatrix(x,y);
    mCurrentRow = matrix[0];
    mCurrentCol = matrix[1];
    updateUI();
  }
  
  /**
   * 
   * @param x X position of the point
   * @param y Y position of the point
   * @return Is the point at a selected program?
   */
  private boolean isSelectedItemAt(int x, int y) {
    int[] matrix = getMatrix(x,y);
    return (mCurrentRow == matrix[0] && mCurrentCol == matrix[1]);
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
}