
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import tvbrowser.core.Settings;
import tvbrowser.core.PluginManager;
import tvbrowser.ui.SkinPanel;
import util.ui.ProgramPanel;

import devplugin.Channel;
import devplugin.Plugin;
import devplugin.Program;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class ProgramTable extends JPanel
  implements ProgramTableModelListener
{
  
  private int mColumnWidth;
  private int mHeight;
  
  private ProgramTableLayout mLayout;
  private ProgramTableModel mModel;
  
  private Point mDraggingPoint;
  
  private Image mBackgroundImage;
  private int mBackgroundMode;
  
  /**
   * Creates a new instance of ProgramTable.
   */
  public ProgramTable(ProgramTableModel model) {
    setProgramTableLayout(null);

    setColumnWidth(Settings.getColumnWidth());
    setModel(model);
    setCursor(new Cursor(Cursor.HAND_CURSOR));
    updateBackground();

    setBackground(Color.white);
    setOpaque(true);

    // setFocusable(true);
    addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent evt) {
        handleMouseDragged(evt);
      }
    });
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent evt) {
        handleMousePressed(evt);
      }
      public void mouseClicked(MouseEvent evt) {
        handleMouseClicked(evt);
      }
    });
    addKeyListener(new KeyAdapter() {
      public void heyPressed(KeyEvent evt) {
        handleKeyPressed(evt);
      }
    });
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
      if (Settings.getTableLayout() == Settings.TABLE_LAYOUT_COMPACT) {
        layout = new CompactLayout();
      } else {
        layout = new TimeSynchronousLayout();
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
    
    mBackgroundMode=Settings.getTableBGMode();
    java.io.File f=new java.io.File(Settings.getTableSkin());
    if (f.exists() && f.isFile()) {
      mBackgroundImage=new ImageIcon(Settings.getTableSkin()).getImage();
    }
    repaint();
  }
  


  public void paintComponent(Graphics grp) {
    super.paintComponent(grp);
    
    // Using the information of the clip bounds, we can speed up painting
    // significantly
    Rectangle clipBounds = grp.getClipBounds();
    
    if (mBackgroundMode==SkinPanel.WALLPAPER && mBackgroundImage!=null) {
      int xMin=clipBounds.x/mBackgroundImage.getWidth(this);
      int yMin=clipBounds.y/mBackgroundImage.getHeight(this);
      if (xMin<0) xMin=0;
      if (yMin<0) yMin=0;
      
      int xMax=(clipBounds.x+clipBounds.width) / mBackgroundImage.getWidth(this);
      int yMax=(clipBounds.y+clipBounds.height) / mBackgroundImage.getHeight(this);
            
      if (xMax>=getWidth()) xMax=getWidth();
      if (yMax>=getHeight()) yMax=getHeight();
      
      for (int i=xMin; i<=xMax; i++) {        
        for (int j=yMin; j<=yMax; j++) {          
          grp.drawImage(mBackgroundImage,i*mBackgroundImage.getWidth(this),j*mBackgroundImage.getHeight(this),this);
        }
      }
    }
    
    int minCol = clipBounds.x / mColumnWidth;
    if (minCol < 0) minCol = 0;
    int maxCol = (clipBounds.x + clipBounds.width) / mColumnWidth;
    if (maxCol >= mModel.getColumnCount()) {
      maxCol = mModel.getColumnCount() - 1;
    }
    
    int x = minCol * mColumnWidth;
    for (int col = minCol; col <= maxCol; col++) {
      // paint background (columns)
      if (mBackgroundMode==SkinPanel.COLUMNS && mBackgroundImage!=null) {
        int imgH=mBackgroundImage.getHeight(this);
        for (int py=0;py<clipBounds.height;py+=imgH) {
          grp.drawImage(mBackgroundImage,col*mColumnWidth,py+clipBounds.y,this);
        }  
      }
      
      int y = mLayout.getColumnStart(col);
      
      for (int row = 0; row < mModel.getRowCount(col); row++) {
        // Get the program
        ProgramPanel panel = mModel.getProgramPanel(col, row);
        
        // Render the program
        if (panel != null) {
          // Check whether the cell is within the clipping area
          int cellHeight = panel.getHeight();
          if (((y + cellHeight) > clipBounds.y)
            && (y < (clipBounds.y + clipBounds.height)))
          {
            // Paint the cell
            grp.translate(x, y);
            panel.setSize(mColumnWidth, cellHeight);
            panel.paint(grp);
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
    for (int i = 0; i < channelArr.length; i++) {
      String msg = channelArr[i].getCopyrightNotice();
      grp.drawString(msg, i * mColumnWidth + 3, getHeight() - 5);
    }
    
    /*
    // Paint the clipBounds
    System.out.println("Painting rect: " + clipBounds);
    grp.setColor(new Color((int)(Math.random() * 256), (int)(Math.random() * 256),
      (int)(Math.random() * 256)));
    grp.drawRect(clipBounds.x, clipBounds.y, clipBounds.width - 1, clipBounds.height - 1);
    /**/
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
    for (int row = 0; row < mModel.getRowCount(col); row++) {
      ProgramPanel panel = mModel.getProgramPanel(col, row);
      currY += panel.getHeight();
      if (y < currY) {
        return panel.getProgram();
      }
    }
    
    return null;
  }


  public void fontChanged() {
    updateLayout();
  }


  public void updateLayout() {
    mLayout.updateLayout(mModel);
    
    // Set the height equal to the highest column
    mHeight = 0;
    for (int col = 0; col < mModel.getColumnCount(); col++) {
      int colHeight = mLayout.getColumnStart(col);
      for (int row = 0; row < mModel.getRowCount(col); row++) {
        ProgramPanel panel = mModel.getProgramPanel(col, row);
        colHeight += panel.getHeight();
      }
      
      if (colHeight > mHeight) {
        mHeight = colHeight;
      }
    }

    // Add 20 for the copyright notice
    mHeight += 20;

    repaint();
  }



  public void scrollBy(int deltaX, int deltaY) {
    if (getParent() instanceof JViewport) {
      JViewport viewport = (JViewport) getParent();
      Point viewPos = viewport.getViewPosition();
      
      viewPos.x += deltaX;
      viewPos.y += deltaY;

      int maxX = getWidth() - viewport.getWidth();
      int maxY = getHeight() - viewport.getHeight();
      
      viewPos.x = Math.min(viewPos.x, maxX);
      viewPos.x = Math.max(viewPos.x, 0);
      viewPos.y = Math.min(viewPos.y, maxY);
      viewPos.y = Math.max(viewPos.y, 0);
      
      viewport.setViewPosition(viewPos);
    }
  }



  /**
   * Creates a context menu containg all subscribed plugins that support context
   * menues.
   *
   * @return a plugin context menu.
   */
  private JPopupMenu createPluginContextMenu(final Program program) {  
  	return PluginManager.createPluginContextMenu(program, null); 
  }

  
  
  private void handleMousePressed(MouseEvent evt) {
    requestFocus();

    mDraggingPoint = evt.getPoint();
  }

  
  
  private void handleMouseClicked(MouseEvent evt) {
    Program program = getProgramAt(evt.getX(), evt.getY());
    if (SwingUtilities.isRightMouseButton(evt)) {
      if (program != null) {
        JPopupMenu menu = createPluginContextMenu(program);
        menu.show(this, evt.getX() - 15, evt.getY() - 15);
      }
    }
    else if (SwingUtilities.isLeftMouseButton(evt) && (evt.getClickCount() == 2)) {
      if (program != null) {
        // This is a left double click
        // -> Execute the program using the user defined default plugin
        Plugin plugin = PluginManager.getInstance().getContextMenuDefaultPlugin();
        if (plugin!=null) {
          plugin.execute(program);  
        }
      }
    }
  }
  
  
  
  private void handleMouseDragged(MouseEvent evt) {
    if (mDraggingPoint != null) {
      int deltaX = mDraggingPoint.x - evt.getX();
      int deltaY = mDraggingPoint.y - evt.getY();
      scrollBy(deltaX, deltaY);
    }
  }

  
  
  public void handleKeyPressed(KeyEvent evt) {
    int scrollDistance = evt.isShiftDown() ? 50 : 10;
    
    System.out.println("evt.getKeyCode(): " + evt.getKeyCode() + " up: "
      + ((evt.getKeyCode() & KeyEvent.VK_UP) == KeyEvent.VK_UP));
    
    if (evt.getKeyCode() == KeyEvent.VK_UP) {
      scrollBy(0, -scrollDistance);
    }
    else if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
      // scrollBy(0, scrollDistance);
    }
    else if (evt.getKeyCode() == KeyEvent.VK_LEFT) {
      scrollBy(-scrollDistance, 0);
    }
    else if (evt.getKeyCode() == KeyEvent.VK_RIGHT) {
      scrollBy(scrollDistance, 0);
    }
  }

  
  
  public int getTimeY(int minutesAfterMidnight) {
    // Get the total time y
    int totalTimeY = 0;
    int parts = 0;
    for (int col = 0; col < mModel.getColumnCount(); col++) {
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
    
    // Walk to the program that starts before the specified time
    int lastCellHeight = 0;
    for (int row = 0; row < mModel.getRowCount(col); row++) {
      ProgramPanel panel = mModel.getProgramPanel(col, row);
      Program program = panel.getProgram();
      int startTime = program.getHours() * 60 + program.getMinutes();
      if (startTime > minutesAfterMidnight) {
        // It was the last program
        timeY += lastCellHeight / 2; // Hit the center of the program
        return timeY;
      } else {
        timeY += lastCellHeight;
      }
      
      // Remember the cell height
      lastCellHeight = panel.getHeight();
    }
    
    return -1;
  }
  
  
  
  private Rectangle getCellRect(int cellCol, int cellRow) {
    int x = cellCol * mColumnWidth;
    int width = mColumnWidth;

    int y = mLayout.getColumnStart(cellCol);
    for (int row = 0; row < mModel.getRowCount(cellCol); row++) {
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


  public void tableDataChanged() {
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

}
