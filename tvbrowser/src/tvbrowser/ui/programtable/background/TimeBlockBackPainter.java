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
package tvbrowser.ui.programtable.background;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.JComponent;

import tvbrowser.core.Settings;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import tvbrowser.ui.programtable.ProgramTableLayout;
import tvbrowser.ui.programtable.ProgramTableModel;
import util.ui.ImageUtilities;
import util.ui.ProgramPanel;
import devplugin.Date;
import devplugin.Program;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TimeBlockBackPainter extends AbstractBackPainter {
  
  private static final Font TABLE_WEST_FONT = new Font("Dialog", Font.PLAIN, 14);
  private static final Color TABLE_WEST_FONT_COLOR = Color.DARK_GRAY;

  private Image mBackgroundImage1, mBackgroundImage2;
  private Image mTableWestImage1, mTableWestImage2;
  private int mBlockSize;
  private TimeBlock[] mBlockArr;
  private JComponent mTableWest;
  
  
  public TimeBlockBackPainter() {
  }


  /**
   * Is called when the table's layout has changed.
   */
  public void layoutChanged(ProgramTableLayout layout, ProgramTableModel model) {
    mBackgroundImage1 = ImageUtilities.createImage(Settings.getTimeBlockBackground1());
    mBackgroundImage2 = ImageUtilities.createImage(Settings.getTimeBlockBackground2());

    mTableWestImage1 = ImageUtilities.createImage(Settings.getTimeBlockWestImage1());
    mTableWestImage2 = ImageUtilities.createImage(Settings.getTimeBlockWestImage2());
    
    boolean showTableWest = Settings.getTimeBlockShowWest();
    if (showTableWest) {
      if (mTableWest == null) {
        mTableWest = new TimeBlockTableWest();
      }
    } else {
      mTableWest = null;
    }
    
    mBlockSize = Settings.getTimeBlockSize();
    
    mBlockArr = createBlockArray(layout, model);
  }


  /**
   * Paints the background.
   * 
   * @param grp
   * @param columnWidth
   * @param tableHeight
   * @param clipBounds
   * @param layout The table's layout
   * @param model The table model
   */
  public void paintBackground(Graphics grp, int columnWidth, int tableHeight,
    int minCol, int maxCol, Rectangle clipBounds, ProgramTableLayout layout,
    ProgramTableModel model)
  {
    // We make a local copy of the block y array to get thread savety
    // (layoutChanged() may set mBlockYArr to null during paining)
    TimeBlock[] blockArr = createBlockArray(layout, model);
    boolean toggleFlag = true;
    for (int i = 0; i < blockArr.length; i++) {
      // Get the image of this time block
      Image backImg;
      if (toggleFlag) {
        backImg = mBackgroundImage1;
      } else {
        backImg = mBackgroundImage2;
      }

      // Get the y positions of this time block
      int minY = mBlockArr[i].mStartY;
      int maxY;
      if ((i + 1) < blockArr.length) {
        maxY = mBlockArr[i + 1].mStartY;
      } else {
        maxY = tableHeight;
      }
      
      // Paint the background of this time block
      int x = minCol * columnWidth;
      for (int col = minCol; col <= maxCol; col++) {
        fillImage(grp, x, minY, columnWidth, (maxY - minY), backImg, clipBounds);
        
        x += columnWidth;
      }
      
      // Toggle the background
      toggleFlag = ! toggleFlag;
    }
  }


  private TimeBlock[] createBlockArray(ProgramTableLayout layout,
    ProgramTableModel model)
  {
    ArrayList list = new ArrayList();
    int blockCount = 2 * 24 / mBlockSize;
    for (int i = 0; i < blockCount; i++) {
      list.add(new TimeBlock(i * mBlockSize * 60));
    }

    // Go through the model and find the block borders
    Date mainDate = ((DefaultProgramTableModel) model).getDate();
    for (int col = 0; col < model.getColumnCount(); col++) {
      int y = layout.getColumnStart(col);
      for (int row = 0; row < model.getRowCount(col); row++) {        
        ProgramPanel panel = model.getProgramPanel(col, row);
        if (panel != null) {
          Program prog = panel.getProgram();
          int startTime = prog.getStartTime();
          if (! mainDate.equals(prog.getDate())) {
            startTime += 24 * 60;
          }
          
          // Go to the block of this program
          int blockIndex = list.size() - 1;
          TimeBlock  block = (TimeBlock) list.get(blockIndex);
          while ((block.mStartTime > startTime) && (blockIndex > 0)) {
            blockIndex--;
            block = (TimeBlock) list.get(blockIndex);
          }
          
          // Check whether the y of the program is lower than the one of the block
          int blockY = block.mStartY;
          if ((blockY == -1) || (y < blockY)) {
            block.mStartY = y;
          }

          y += panel.getHeight();
        }
      }
    }
    
    // Remove the blocks that have no y
    for (int i = list.size() - 1; i >= 0; i--) {
      TimeBlock  block = (TimeBlock) list.get(i);
      if (block.mStartY == -1) {
        list.remove(i);
      }
    }
    
    // Create an array
    TimeBlock[] blockArr = new TimeBlock[list.size()];
    list.toArray(blockArr);
    
    return blockArr;
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
  
  
  class TimeBlock {
    
    public TimeBlock(int startTime) {
      mStartTime = startTime;
      mStartY = -1;
    }
    
    int mStartTime;
    int mStartY;
  } // class TimeBlock
  
  
  class TimeBlockTableWest extends JComponent {
    
    private FontMetrics mFontMetrics;
    
    public TimeBlockTableWest() {
      mFontMetrics = getFontMetrics(TABLE_WEST_FONT);
    }
    
    public Dimension getPreferredSize() {
      int width = mFontMetrics.stringWidth("23") + 4;
      int height = 1000000; // We don't know the size
      return new Dimension(width, height);
    }
    
    public void paintComponent(Graphics grp) {
      // We make a local copy of the block y array to get thread savety
      // (layoutChanged() may set mBlockYArr to null during paining)
      TimeBlock[] blockArr = mBlockArr;
      if ((blockArr != null) && (blockArr.length > 0)) {
        boolean toggleFlag = true;

        int width = getWidth();
        int height = getHeight();
        Rectangle clipBounds = grp.getClipBounds();

        grp.setFont(TABLE_WEST_FONT);
        grp.setColor(TABLE_WEST_FONT_COLOR);
        for (int i = 0; i < blockArr.length; i++) {
          // Get the image of this time block
          Image backImg;
          if (toggleFlag) {
            backImg = mTableWestImage1;
          } else {
            backImg = mTableWestImage2;
          }

          // Get the y positions of this time block
          int minY = mBlockArr[i].mStartY;
          int maxY;
          if ((i + 1) < blockArr.length) {
            maxY = mBlockArr[i + 1].mStartY;
          } else {
            maxY = height;
          }
      
          // Paint the block
          fillImage(grp, 0, minY, width, (maxY - minY), backImg, clipBounds);
          String msg = Integer.toString(mBlockArr[i].mStartTime / 60 % 24);
          int msgWidth = mFontMetrics.stringWidth(msg);
          int x = width - msgWidth - 2;
          grp.drawString(msg, x, minY + TABLE_WEST_FONT.getSize());
          
          toggleFlag = ! toggleFlag;
        }
      }
    }
    
  } // class TimeBlockTableWest

}
