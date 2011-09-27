/*
 * TV-Browser
 * Copyright (C) 2011 TV-Browser team (dev@tvbrowser.org)
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
package tvbrowser.ui.programtable.background;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.UIManager;

import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import tvbrowser.ui.programtable.ProgramTableLayout;
import tvbrowser.ui.programtable.ProgramTableModel;
import util.io.IOUtilities;
import util.ui.ProgramPanel;
import util.ui.TimeFormatter;
import devplugin.Date;
import devplugin.Program;

/**
 * @author René Mach
 */
public class UiTimeBlockBackPainter extends AbstractBackPainter {
  private static final Font TABLE_WEST_FONT = new Font("Dialog", Font.PLAIN, 14);
  private static final Color TABLE_WEST_FONT_COLOR = Color.DARK_GRAY;
  
  private int mBlockSize;
  private TimeBlock[] mBlockArr;
  private JComponent mTableWest;
  private TimeFormatter mFormatter;
  
  private Color mLineColor;
  
  private static final int DEFAULT_ALPHA = 35;
  private static final int EXPIRED_ALPHA = 20;

  public UiTimeBlockBackPainter() {
    if (Settings.propTwelveHourFormat.getBoolean()) {
      mFormatter = new TimeFormatter("hh a");
    } else {
      mFormatter = new TimeFormatter("HH");
    }

    if (Settings.propTimeBlockShowWest.getBoolean()) {
      mTableWest = new TimeBlockTableWest();
    } else {
      mTableWest = null;
    }
    
    Color c = UIManager.getColor("List.foreground");
    Color c1 = UIManager.getColor("List.background");
    
    int r = (c.getRed()   + c1.getRed()) >> 1;
    int g = (c.getGreen() + c1.getGreen()) >> 1;
    int b = (c.getBlue()  + c1.getBlue()) >> 1;
    
    mLineColor = new Color(r,g,b);
    
    double test2 = (0.2126 * c1.getRed()) + (0.7152 * c1.getGreen()) + (0.0722 * c1.getBlue());
    double test1 = (0.2126 * mLineColor.getRed()) + (0.7152 * mLineColor.getGreen()) + (0.0722 * mLineColor.getBlue());
    
    if(test2 - test1 > 90) {
      mLineColor = new Color(mLineColor.getRed()+30,mLineColor.getGreen()+30,mLineColor.getBlue()+30);
    }
    else if(test2 - test1 < -90) {
      mLineColor = mLineColor.darker();
    }
    
    mBlockSize = Settings.propTimeBlockSize.getInt();
  }

  /**
   * Is called when the table's layout has changed.
   */
  public void layoutChanged(ProgramTableLayout layout, ProgramTableModel model) {
    mBlockArr = createBlockArray(layout, model);
  }

  /**
   * Paints the background.
   * 
   * @param grp
   * @param columnWidth
   * @param tableHeight
   * @param clipBounds
   * @param layout
   *          The table's layout
   * @param model
   *          The table model
   */
  public void paintBackground(Graphics grp, int columnWidth, int tableHeight,
      int minCol, int maxCol, Rectangle clipBounds, ProgramTableLayout layout,
      ProgramTableModel model) {
    grp.setColor(UIManager.getColor("List.background"));
    grp.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
    // We make a local copy of the block y array to get thread safety
    // (layoutChanged() may set mBlockYArr to null during paining)
    TimeBlock[] blockArr = createBlockArray(layout, model);
    boolean toggleFlag = true;
    int minY;
    int maxY = -1;
    for (int i = 0; i < blockArr.length; i++) {
      // Get the y positions of this time block
      minY = blockArr[i].mStartY;
      
      if ((i + 1) < blockArr.length) {
        maxY = blockArr[i + 1].mStartY;
      } else {
        maxY = tableHeight;
      }

      // Paint the background of this time block
      int x = minCol * columnWidth;
      for (int col = minCol; col <= maxCol; col++) {
        int alpha = DEFAULT_ALPHA;
        
        int time = IOUtilities.getMinutesAfterMidnight();
        
        if(Date.getCurrentDate().compareTo(MainFrame.getInstance().getCurrentSelectedDate()) > 0) {
          time += 60*24;
        }
        
        if(blockArr[i].mStartTime + mBlockSize * 60 < time){
          alpha = EXPIRED_ALPHA;
        }

        if(toggleFlag) {
          Color c = UIManager.getColor("List.background");
          grp.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
        }
        else {
          Color c = UIManager.getColor("List.foreground");
          grp.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
        }
        
        grp.fillRect(x,minY,columnWidth,(maxY - minY));
        grp.setColor(mLineColor);
        grp.drawLine(x,minY,x,maxY);

        x += columnWidth;
      }

      // Toggle the background
      toggleFlag = !toggleFlag;
    }

    // Paint the rest if needed
    if (maxY < tableHeight) {
      minY = maxY + 1;
      maxY = tableHeight;

      // Paint the background of this time block
      int x = minCol * columnWidth;
      for (int col = minCol; col <= maxCol; col++) {
        if(toggleFlag) {
          Color c = UIManager.getColor("List.background");
          grp.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),DEFAULT_ALPHA));
        }
        else {
          Color c = UIManager.getColor("List.foreground");
          grp.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),DEFAULT_ALPHA));
        }
        
        grp.fillRect(x,minY,columnWidth,(maxY - minY));
        grp.setColor(mLineColor);
        grp.drawLine(x,minY,x,maxY);
        
        x += columnWidth;
      }
    }
  }

  private TimeBlock[] createBlockArray(ProgramTableLayout layout,
      ProgramTableModel model) {
    int blockCount = 2 * 24 / mBlockSize;
    TimeBlock[] blocks = new TimeBlock[blockCount];
    for (int i = 0; i < blockCount; i++) {
      blocks[i] = new TimeBlock(i * mBlockSize * 60);
    }

    // Go through the model and find the block borders
    Date mainDate = ((DefaultProgramTableModel) model).getDate();
    int columnCount = model.getColumnCount();
    for (int col = 0; col < columnCount; col++) {
      int y = layout.getColumnStart(col);
      int rowCount = model.getRowCount(col);
      for (int row = 0; row < rowCount; row++) {
        ProgramPanel panel = model.getProgramPanel(col, row);
        if (panel != null) {
          Program prog = panel.getProgram();
          int startTime = prog.getStartTime();
          if (!mainDate.equals(prog.getDate())) {
            startTime += 24 * 60;
          }

          // Go to the block of this program
          int blockIndex = startTime / (mBlockSize * 60);
          TimeBlock block = blocks[blockIndex];

          // Check whether the y of the program is lower than the one of the
          // block
          int blockY = block.mStartY;
          if ((blockY == -1) || (y < blockY)) {
            block.mStartY = y;
          }

          y += panel.getHeight();
        }
      }
    }

    // Remove the blocks that have no y
    ArrayList<TimeBlock> list = new ArrayList<TimeBlock>();
    for (int i = 0; i < blockCount; i++) {
      if (blocks[i].mStartY != -1) {
        list.add(blocks[i]);
      }
    }

    // Create an array
    return list.toArray(new TimeBlock[list.size()]);
  }

  /**
   * Gets the component that should be shown in the west of the table.
   * <p>
   * If nothing should be shown in the west, null is returned.
   * 
   * @return The table west.
   */
  public JComponent getTableWest() {
    return mTableWest;
  }

  private static class TimeBlock {

    public TimeBlock(int startTime) {
      mStartTime = startTime;
      mStartY = -1;
    }

    private int mStartTime;
    private int mStartY;
  } // class TimeBlock

  class TimeBlockTableWest extends JComponent {

    private FontMetrics mFontMetrics;

    public TimeBlockTableWest() {
      mFontMetrics = getFontMetrics(TABLE_WEST_FONT);
    }

    public Dimension getPreferredSize() {
      int width = mFontMetrics.stringWidth(mFormatter.formatTime(23, 0)) + 4;
      int height = 1000000; // We don't know the size
      return new Dimension(width, height);
    }

    public void paintComponent(Graphics grp) {
      // We make a local copy of the block y array to get thread savety
      // (layoutChanged() may set mBlockYArr to null during paining)
      TimeBlock[] blockArr = mBlockArr;
      if ((blockArr != null) && (blockArr.length > 0)) {
        grp.setColor(UIManager.getColor("List.background"));
        grp.fillRect(0,0,getWidth(),getHeight());
        
        boolean toggleFlag = false;

        int width = getWidth();
        int height = getHeight();
        Rectangle clipBounds = grp.getClipBounds();

        grp.setFont(TABLE_WEST_FONT);
        grp.setColor(TABLE_WEST_FONT_COLOR);
        for (int i = 0; i < blockArr.length; i++) {
          toggleFlag = !toggleFlag;
          // Get the y positions of this time block
          int minY = blockArr[i].mStartY;
          int maxY;
          if ((i + 1) < blockArr.length) {
            maxY = blockArr[i + 1].mStartY;
          } else {
            maxY = height;
          }
          if (!clipBounds.intersects(0, minY, width, maxY - minY)) {
            // this piece is not visible at all
            continue;
          }
          
          int alpha = DEFAULT_ALPHA;
          
          int time = IOUtilities.getMinutesAfterMidnight();
          
          if(Date.getCurrentDate().compareTo(MainFrame.getInstance().getCurrentSelectedDate()) > 0) {
            time += 60*24;
          }
          
          if(blockArr[i].mStartTime + mBlockSize * 60 < time){
            alpha = EXPIRED_ALPHA;
          }
          
          if(toggleFlag) {
            Color c = UIManager.getColor("List.background");
            grp.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
          }
          else {
            Color c = UIManager.getColor("List.foreground");
            grp.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
          }
          
          grp.fillRect(0,minY,width,(maxY - minY));
          
          // Paint the block
          String msg = mFormatter.formatTime(blockArr[i].mStartTime / 60 % 24,
              0);
          int msgWidth = mFontMetrics.stringWidth(msg);
          int x = width - msgWidth - 2;
          
          if(alpha == DEFAULT_ALPHA) {
            grp.setColor(UIManager.getColor("List.foreground"));
          }
          else {
            grp.setColor(Color.gray);
          }
          
          grp.drawString(msg, x, minY + TABLE_WEST_FONT.getSize());
        }
      }
    }

  } // class TimeBlockTableWest

}
