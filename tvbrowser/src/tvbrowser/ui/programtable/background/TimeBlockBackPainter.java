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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;

import javax.swing.JComponent;

import tvbrowser.core.Settings;
import tvbrowser.ui.programtable.DefaultProgramTableModel;
import tvbrowser.ui.programtable.ProgramTableLayout;
import tvbrowser.ui.programtable.ProgramTableModel;
import util.ui.ImageUtilities;
import util.ui.ProgramPanel;
import util.ui.TimeFormatter;
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
  private TimeFormatter mFormatter;

  public TimeBlockBackPainter() {
    if (Settings.propTwelveHourFormat.getBoolean()) {
      mFormatter = new TimeFormatter("hh a");
    } else {
      mFormatter = new TimeFormatter("HH");
    }
    mBackgroundImage1 = ImageUtilities
        .createImageAsynchronous(Settings.propTimeBlockBackground1.getString());
    mBackgroundImage2 = ImageUtilities
        .createImageAsynchronous(Settings.propTimeBlockBackground2.getString());

    mTableWestImage1 = ImageUtilities
        .createImageAsynchronous(Settings.propTimeBlockWestImage1.getString());
    mTableWestImage2 = ImageUtilities
        .createImageAsynchronous(Settings.propTimeBlockWestImage2.getString());

    if (Settings.propTimeBlockShowWest.getBoolean()) {
      if (mTableWest == null) {
        mTableWest = new TimeBlockTableWest();
      }
    } else {
      mTableWest = null;
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
    // We make a local copy of the block y array to get thread safety
    // (layoutChanged() may set mBlockYArr to null during paining)
    TimeBlock[] blockArr = createBlockArray(layout, model);
    boolean toggleFlag = true;
    int minY;
    int maxY = -1;
    for (int i = 0; i < blockArr.length; i++) {
      // Get the image of this time block
      Image backImg;
      if (toggleFlag) {
        backImg = mBackgroundImage1;
      } else {
        backImg = mBackgroundImage2;
      }

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
        fillImage(grp, x, minY, columnWidth, (maxY - minY), backImg, clipBounds);

        x += columnWidth;
      }

      // Toggle the background
      toggleFlag = !toggleFlag;
    }

    // Paint the rest if needed
    if (maxY < tableHeight) {
      minY = maxY + 1;
      maxY = tableHeight;

      // Get the image of this time block
      Image backImg;
      if (toggleFlag) {
        backImg = mBackgroundImage1;
      } else {
        backImg = mBackgroundImage2;
      }

      // Paint the background of this time block
      int x = minCol * columnWidth;
      for (int col = minCol; col <= maxCol; col++) {
        fillImage(grp, x, minY, columnWidth, (maxY - minY), backImg, clipBounds);

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

          // Get the image of this time block
          Image backImg;
          if (toggleFlag) {
            backImg = mTableWestImage1;
          } else {
            backImg = mTableWestImage2;
          }

          // Paint the block
          fillImage(grp, 0, minY, width, (maxY - minY), backImg, clipBounds);
          String msg = mFormatter.formatTime(blockArr[i].mStartTime / 60 % 24,
              0);
          int msgWidth = mFontMetrics.stringWidth(msg);
          int x = width - msgWidth - 2;
          grp.drawString(msg, x, minY + TABLE_WEST_FONT.getSize());
        }
      }
    }

  } // class TimeBlockTableWest

}
