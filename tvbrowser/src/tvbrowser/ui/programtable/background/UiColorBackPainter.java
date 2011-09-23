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


import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.UIManager;

import tvbrowser.ui.programtable.ProgramTableLayout;
import tvbrowser.ui.programtable.ProgramTableModel;
import util.ui.ProgramPanel;

/**
 * @author René Mach
 */
public class UiColorBackPainter extends AbstractBackPainter {

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
      GradientPaint paint = new GradientPaint(x-10 , 0, UIManager.getColor("List.background"),x +(float)ProgramPanel.WIDTH_LEFT+150, 0, UIManager.getColor("List.foreground"), false);
      ((Graphics2D)grp).setPaint(paint);
      ((Graphics2D)grp).fillRect(x, 0, ProgramPanel.WIDTH_LEFT-5, tableHeight);
      
      grp.setColor(UIManager.getColor("List.background"));
      grp.fillRect(x+ProgramPanel.WIDTH_LEFT-5, 0, columnWidth, tableHeight);
      
      x += columnWidth;
    }
  }

}
