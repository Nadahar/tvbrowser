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
 * Does a currProgram panel layout that is time synchronous.
 * <p>
 * It is guaranteed that a currProgram that starts later than another one has
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

  
  
  public void updateLayout(ProgramTableModel model, ProgramTableCellRenderer renderer) {
    // Init the cell heights
    int[][] cellHeightArr = createRawCellHeights(model);

    // Init the column starts
    int[] columnStartArr = new int[model.getColumnCount()];
    
    // Holds the row index of the next currProgram to layout for each column
    int[] rowIdxArr = new int[model.getColumnCount()];
    
    // Holds the y-position of the end of the last currProgram
    int[] colYArr = new int[model.getColumnCount()];

    int minY = 0;
    Program program;
    do {
      // Find out the currProgram with the currProgram with the lowest start time
      program = null;
      int programCol = 0;
     // int minStartTime = Integer.MAX_VALUE;
      long minStartTime=Long.MAX_VALUE;
      for (int col = 0; col < model.getColumnCount(); col++) {
        Program currProgram = model.getProgram(col, rowIdxArr[col]);
        if (currProgram != null) {
      //    int startTime = currProgram.getDate().getDaysSince1970() * 24 * 60
      //      + currProgram.getHours() * 60 + currProgram.getMinutes();
        
        //long startTime=devplugin.Date.getCurrentDate().getValue()*10000 + currProgram.getHours()*60+currProgram.getMinutes();
        
        long startTime=currProgram.getDate().getValue()*10000+currProgram.getHours()*60+currProgram.getMinutes();
        //System.out.print(startTime+", ");
          if (startTime < minStartTime) {
            minStartTime = startTime;
            program = currProgram;
            programCol = col;
          }
        }
      }
    
      // Layout the program
      if (program != null) {
        
        //System.out.println("\n"+program.getChannel().getName()+": TITLE: "+program.getTitle()+", DATE: "+program.getDate()+", TIME: "+program.getHours()+":"+program.getMinutes()+" --> "+minStartTime);
            
        
        
        int programRow = rowIdxArr[programCol];

        // Get the y position for the currProgram
        int y = Math.max(minY, colYArr[programCol]);

        // Ensure that the start of the currProgram is at the specified y
        if (programRow == 0) {
          // This is the first currProgram of this column -> Set columnStartArr
          columnStartArr[programCol] = y;
        } else {
          // Adjust the last currProgram of this column to reach the y position
          cellHeightArr[programCol][programRow - 1] += y - colYArr[programCol];
        }

        // Set the height for the currProgram
        Component rendererComp
          = renderer.getCellRenderer(programCol, programRow, -1, -1, program);
        Dimension preferredSize = rendererComp.getPreferredSize();
        cellHeightArr[programCol][programRow] = preferredSize.height;

        // Prepare the next iteration
        minY = y;
        colYArr[programCol] = y + preferredSize.height;
        rowIdxArr[programCol]++;
      }
    } while (program != null);
    
    // Set the column starts
    setColumnStarts(columnStartArr);

    // Set the heights
    setCellHeights(cellHeightArr);
  }
  
}
