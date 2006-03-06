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
 * Does a program panel layout that is real time synchronous.
 * 
 * @author Ren� Mach
 * 
 */
public class RealTimeSynchronousLayout extends AbstractProgramTableLayout {

  public void updateLayout(ProgramTableModel model) {
    // Init the column starts
    int[] columnStartArr = new int[model.getColumnCount()];

    // the value to scale the length with
    float scaleValue = 1;
    
    // the minimum length of a program
    int minLength = 10000;
    
    for (int col = 0; col < model.getColumnCount(); col++) {
      for (int row = 0; row < model.getRowCount(col); row++) {
        ProgramPanel panel = model.getProgramPanel(col, row);
        
        if(panel.getProgram().getLength() > 0) {
          Program p = panel.getProgram();
          float scale = ((float)(panel.getMinimumHeight())) / p.getLength();
          
          if(scale > scaleValue && minLength >= p.getLength()) {
            scaleValue = scale;
            minLength = p.getLength();
          }
        }
      }
    }
    
    for (int col = 0; col < model.getColumnCount(); col++) {
      for (int row = 0; row < model.getRowCount(col); row++) {
        ProgramPanel panel = model.getProgramPanel(col, row);
        Program program = panel.getProgram();
        
        if (row == 0)
          columnStartArr[col] = (int)(program.getStartTime() * scaleValue);
        
        if(row != model.getRowCount(col) - 1) {
          Program next = model.getProgramPanel(col, row + 1).getProgram();
          int startTime = program.getStartTime();
          int endTime = next.getStartTime();
          if (endTime < startTime) {
            // The program ends the next day
            endTime += 24 * 60;
          }
          
          int length = endTime - startTime;
          
          panel.setHeight((int)(length * scaleValue));
        }
        else
          panel.setHeight(panel.getPreferredHeight());
      }
    }

    // Set the column starts
    setColumnStarts(columnStartArr);
  }

}
