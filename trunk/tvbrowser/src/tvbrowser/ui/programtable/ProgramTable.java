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
  private ProgramTableCellRenderer mRenderer;
  
  private Point mDraggingPoint;
  
  private Image mBackgroundImage;
  private int mBackgroundMode;
  
  /**
   * Creates a new instance of ProgramTable.
   */
  public ProgramTable(ProgramTableModel model) {
    mRenderer = new DefaultProgramTableCellRenderer();
    
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
    //!!!!setColDiff(mColumnWidth);
    mRenderer = new DefaultProgramTableCellRenderer();
    
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
        Program program = mModel.getProgram(col, row);
        
        // Render the program
        int cellHeight = mLayout.getCellHeight(col, row);
        if ((program != null) && ((y + cellHeight) > clipBounds.y)
          && (y < (clipBounds.y + clipBounds.height)))
        {
          grp.translate(x, y);
          Component renderer = mRenderer.getCellRenderer(col, row, mColumnWidth, 
            cellHeight, program);
          renderer.setSize(mColumnWidth, cellHeight);
          renderer.paint(grp);
          // grp.drawRect(0, 0, mColumnWidth, cellHeight);
          grp.translate(-x, -y);
        }
        
        // Move to the next row in this column
        y += cellHeight;
      }
      
      // paint the timeY
      // int timeY = getTimeYOfColumn(col, util.io.IOUtilities.getMinutesAfterMidnight());
      // grp.drawLine(x, timeY, x + mColumnWidth, timeY);

      // Move to the next column
      x += mColumnWidth;
    }
    
    for (int i=0;i<mModel.getColumnCount();i++) {
      Program p=mModel.getProgram(i,0);
      if (p!=null) {
        grp.drawString(p.getChannel().getCopyrightNotice(),i*mColumnWidth+3,getHeight()-5);
      }
    }
    

    /*
    // Paint the clipBounds
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
      currY += mLayout.getCellHeight(col, row);
      if (y < currY) {
        return mModel.getProgram(col, row);
      }
    }
    
    return null;
  }

  public void fontChanged() {
    mRenderer = new DefaultProgramTableCellRenderer();
    repaint();
  }

  public void updateLayout() {
    mLayout.updateLayout(mModel, mRenderer);
    
    // Set the height equal to the highest column
    mHeight = 0; //0;
    for (int col = 0; col < mModel.getColumnCount(); col++) {
      int colHeight = mLayout.getColumnStart(col)+20;
      for (int row = 0; row < mModel.getRowCount(col); row++) {
        colHeight += mLayout.getCellHeight(col, row);
      }
      
      if (colHeight > mHeight) {
        mHeight = colHeight;
      }
    }
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
  	return tvbrowser.core.DataService.getInstance().createPluginContextMenu(program, null); 
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
        // -> Execute the program using the first plugin that is in the context menu
        devplugin.Plugin[] instPluginArr = PluginManager.getInstalledPlugins();
        for (int i = 0; i < instPluginArr.length; i++) {
          if (instPluginArr[i].getContextMenuItemText() != null) {
            instPluginArr[i].execute(program);
            break;
          }
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
      Program program = mModel.getProgram(col, row);
      int startTime = program.getHours() * 60 + program.getMinutes();
      if (startTime > minutesAfterMidnight) {
        // It was the last program
        timeY += lastCellHeight / 2; // Hit the center of the program
        return timeY;
      } else {
        timeY += lastCellHeight;
      }
      
      lastCellHeight = mLayout.getCellHeight(col, row);
    }
    
    return -1;
  }
  
  
  
  private Rectangle getCellRect(int col, int row) {
    int x = col * mColumnWidth;
    int width = mColumnWidth;

    int y = mLayout.getColumnStart(col);
    for (int r = 0; r < mModel.getRowCount(col); r++) {
      int height = mLayout.getCellHeight(col, r);
      if (r == row) {
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
