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

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import tvbrowser.ui.programtable.ProgramTableLayout;
import tvbrowser.ui.programtable.ProgramTableModel;
import util.ui.ProgramPanel;
import devplugin.Program;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public abstract class AbstractCellBasedBackPainter extends AbstractBackPainter {

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
    int x = minCol * columnWidth;
    for (int col = minCol; col <= maxCol; col++) {
      int y = layout.getColumnStart(col);
      
      // Hintergrund vor den Sendungen
      fillImage(grp, x, 0, columnWidth, y, getOuterBackgroundImage(), clipBounds);
      
      int rowCount = model.getRowCount(col);
	  for (int row = 0; row < rowCount; row++) {
        // Get the program
        ProgramPanel panel = model.getProgramPanel(col, row);
        
        // Render the program
        if (panel != null) {
          int cellHeight = panel.getHeight();

          Image backImg = getBackgroundImageFor(panel.getProgram());
          fillImage(grp, x, y, columnWidth, cellHeight, backImg, clipBounds);
          
          // Move to the next row in this column
          y += cellHeight;
        }
      }

      // Hintergrund nach den Sendungen
      fillImage(grp, x, y, columnWidth, tableHeight - y, getOuterBackgroundImage(),
                clipBounds);
      
      // paint the timeY
      // int timeY = getTimeYOfColumn(col, util.io.IOUtilities.getMinutesAfterMidnight());
      // grp.drawLine(x, timeY, x + mColumnWidth, timeY);

      // Move to the next column
      x += columnWidth;
    }
    
    super.paintBackground(grp, columnWidth, tableHeight, minCol, maxCol, clipBounds, layout, model);
  }
  
  
  /**
   * Gets the background image for the outer areas, where no programs are.
   * 
   * @return The background image for the outer areas
   */
  protected abstract Image getOuterBackgroundImage();
  
  
  /**
   * Gets the background image for the given program.
   * 
   * @param prog The program.
   * @return The background image for the given program.
   */
  protected abstract Image getBackgroundImageFor(Program prog);

}
