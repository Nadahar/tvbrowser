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

import util.ui.ProgramPanel;

/**
 * This layout has (nearly) the same height for each column.
 * The height of all columns is defined by the height of the channel column with the largest
 * sum of panel heights. In all other columns space is added after each program to "stretch"
 * the column to the same height.
 * 
 * algorithm:
 * <li> find the column with the largest height (using preferred size of panels)</li>
 * <li> make all columns equally high, do this by adding the same amount of empty space
 * at the end of each panel in a column
 * 
 * @author Til Schneider, www.murfman.de
 */
public class CompactLayout extends AbstractProgramTableLayout {
  
  /**
   * Creates a new instance of CompactLayout.
   */
  public CompactLayout() {
  }

  
  
  public void updateLayout(ProgramTableModel model) {
    // Init the column starts
    int columnCount = model.getColumnCount();
    int[] columnStartArr = new int[columnCount];
    
    // The total height of each column
    int[] columnHeightArr = new int[columnCount];
    
    int maxColHeight = 0;
    for (int col = 0; col < columnCount; col++) {
      int rowCount = model.getRowCount(col);
      for (int row = 0; row < rowCount; row++) {
        ProgramPanel panel = model.getProgramPanel(col, row);
        columnHeightArr[col] += panel.getPreferredHeight();
      }
      
      maxColHeight = Math.max(columnHeightArr[col], maxColHeight);
    }
    
    // Adjust all columns so they have the same height
    for (int col = 0; col < columnCount; col++) {
      int rowCount = model.getRowCount(col);
      int difference = maxColHeight - columnHeightArr[col];
      for (int row = 0; row < rowCount; row++) {
        int remainingRows = rowCount - row;
        int yPlus = difference / remainingRows;

        ProgramPanel panel = model.getProgramPanel(col, row);
        int height = panel.getPreferredHeight();
        
        height += yPlus;
        difference -= yPlus;
        
        panel.setHeight(height);
      }
    }

    // Set the column starts
    setColumnStarts(columnStartArr);
  }
  
}
