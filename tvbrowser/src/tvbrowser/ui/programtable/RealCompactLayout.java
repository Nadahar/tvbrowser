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
 *     $Date: 2004-01-03 14:41:10 +0100 (Sa, 03 Jan 2004) $
 *   $Author: til132 $
 * $Revision: 319 $
 */

package tvbrowser.ui.programtable;

import util.ui.ProgramPanel;

/**
 * Each program panel is shown in preferred size and
 * each column starts at the topmost point of the program table.
 * <p>
 * This layout has no additional space added anywhere.
 *
 * @author René Mach
 */
public class RealCompactLayout extends AbstractProgramTableLayout {
  
  public void updateLayout(ProgramTableModel model) {
    // Init the column starts
    int columnCount = model.getColumnCount();
    int[] columnStartArr = new int[columnCount];
    
    // The total height of each column
    int[] columnHeightArr = new int[columnCount];
    
    for (int col = 0; col < columnCount; col++) {
      int rowCount = model.getRowCount(col);
      for (int row = 0; row < rowCount; row++) {
        ProgramPanel panel = model.getProgramPanel(col, row);
        columnHeightArr[col] += panel.getPreferredHeight();
        panel.setHeight(panel.getPreferredHeight());
      }
    }
    
    // Set the column starts
    setColumnStarts(columnStartArr);
  }
  
}
