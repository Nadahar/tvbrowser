/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This currProgram is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This currProgram is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this currProgram; if not, write to the Free Software
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

import java.awt.Component;
import java.awt.Dimension;

import devplugin.Program;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public class CompactLayout extends AbstractProgramTableLayout {
  
  /**
   * Creates a new instance of CompactLayout.
   */
  public CompactLayout() {
  }

  
  
  public void updateLayout(ProgramTableModel model, ProgramTableCellRenderer renderer) {
    // Init the cell heights
    int[][] cellHeightArr = createRawCellHeights(model);

    // Init the column starts
    int[] columnStartArr = new int[model.getColumnCount()];
    
    // The total height of each column
    int[] columnHeightArr = new int[model.getColumnCount()];
    
    int maxColHeight = 0;
    for (int col = 0; col < cellHeightArr.length; col++) {
      for (int row = 0; row < cellHeightArr[col].length; row++) {
        Program program = model.getProgram(col, row);
        Component rendererComp
          = renderer.getCellRenderer(col, row, -1, -1, program);
        Dimension preferredSize = rendererComp.getPreferredSize();
        cellHeightArr[col][row] = preferredSize.height;
        columnHeightArr[col] += preferredSize.height;
      }
      
      maxColHeight = Math.max(columnHeightArr[col], maxColHeight);
    }
    
    // Adjust all columns so they have the same height
    for (int col = 0; col < cellHeightArr.length; col++) {
      int difference = maxColHeight - columnHeightArr[col];
      for (int row = 0; row < cellHeightArr[col].length; row++) {
        int remainingRows = cellHeightArr[col].length - row;
        int yPlus = difference / remainingRows;
        cellHeightArr[col][row] += yPlus;
        difference -= yPlus;
      }
    }

    // Set the column starts
    setColumnStarts(columnStartArr);
    
    // Set the heights
    setCellHeights(cellHeightArr);
  }
  
}
