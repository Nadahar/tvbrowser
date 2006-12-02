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
import devplugin.Program;

/**
 * Does a program panel layout that is time synchronous.
 * <p>
 * It is guaranteed that a program that starts later than another one has
 * no smaller y value.
 *
 * @author Til Schneider, www.murfman.de
 */
public class TimeSynchronousLayout extends AbstractProgramTableLayout {
  
  /**
   * Creates a new instance of TimeSynchronousLayout.
   */
  public TimeSynchronousLayout() {
  }

  
  
  public void updateLayout(ProgramTableModel model) {
    // Init the column starts
    int[] columnStartArr = new int[model.getColumnCount()];

    // Holds the row index of the next currProgram to layout for each column
    int[] rowIdxArr = new int[model.getColumnCount()];

    // Holds the y-position of the end of the last currProgram
    int[] colYArr = new int[model.getColumnCount()];

    int minY = 0;
    int maxY = 0;
    
    Program minProgram;
    ProgramPanel minPanel;
    do {
      // Find out the program with the lowest start time
      minProgram = null;
      minPanel = null;
      int programCol = 0;
      long minStartTime = Long.MAX_VALUE;
      for (int col = 0; col < model.getColumnCount(); col++) {
        ProgramPanel panel = model.getProgramPanel(col, rowIdxArr[col]);
        if (panel != null) {
          Program program = panel.getProgram();
          if (program != null) {
            long startTime = program.getDate().getValue() * 10000
                + program.getHours() * 60 + program.getMinutes();
            
            if (startTime < minStartTime) {
              minStartTime = startTime;
              minProgram = program;
              minPanel = panel;
              programCol = col;
            }
          }
        }
      }

      // Layout the program
      if (minProgram != null) {
        
        int programRow = rowIdxArr[programCol];

        // Get the y position for the program
        int y = Math.max(minY, colYArr[programCol]);

        // Ensure that the start of the program is at the specified y
        if (programRow == 0) {
          // This is the first program of this column -> Set columnStartArr
          columnStartArr[programCol] = y;
        } else {
          // Adjust the last program of this column to reach the y position
          ProgramPanel lastPanel = model.getProgramPanel(programCol, programRow - 1);
          int height = lastPanel.getPreferredHeight();
          height += y - colYArr[programCol];
          lastPanel.setHeight(height);
        }

        // Get the height for the program
        int preferredHeight = minPanel.getPreferredHeight();

        // Set the height for the program if it is the last of the row
        if (programRow + 1 == model.getRowCount(programCol)) {
          // It is the last row
          minPanel.setHeight(preferredHeight);
        }

        // Prepare the next iteration
        minY = y;
        colYArr[programCol] = y + preferredHeight;
        maxY = Math.max(maxY,colYArr[programCol]);
        rowIdxArr[programCol]++;
      }
    } while (minProgram != null);
    
    for (int col = 0; col < model.getColumnCount(); col++) {
      int count = model.getRowCount(col);
      
      if(count > 0) {
        ProgramPanel panel = model.getProgramPanel(col, count-1);      
        panel.setHeight(maxY - colYArr[col] + panel.getHeight());
      }
    }
    
    // Set the column starts
    setColumnStarts(columnStartArr);
  }
  
}
