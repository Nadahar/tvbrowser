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
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import javax.swing.JComponent;

import tvbrowser.ui.programtable.ProgramTableLayout;
import tvbrowser.ui.programtable.ProgramTableModel;

/**
 *
 *
 * @author Til Schneider, www.murfman.de
 */
public abstract class AbstractBackPainter implements BackgroundPainter {

  /**
   * Is called when the table's layout has changed.
   */
  public void layoutChanged(ProgramTableLayout layout, ProgramTableModel model) {
  }


  protected void fillImage(Graphics grp, int x, int y, int width, int height,
    Image img, Rectangle clipBounds)
  {
    if (img == null) {
      grp.setColor(Color.WHITE);
      grp.fillRect(x, y, width, height);
    } else {
      // Check whether we have to paint anything
      if (! clipBounds.intersects(x, y, width, height)) {
        // Nothing to do
        return;
      }

      int imgWidth = img.getWidth(null);
      int imgHeight = img.getHeight(null);
      if ((imgWidth < 1) || (imgHeight < 1)) {
        // Illegal image size
        return;
      }

      int minY = Math.max(y, clipBounds.y - ((clipBounds.y - y) % imgHeight));
      int maxY = Math.min(y + height, clipBounds.y + clipBounds.height);
      for (int py = minY; py < maxY; py += imgHeight) {
        grp.drawImage(img, x, py, null);
      }
    }
  }

  /**
   * Gets the component that should be shown in the west of the table.
   * <p>
   * If nothing should be shown in the west, null is returned.
   *
   * @return The table west.
   */
  public JComponent getTableWest() {
    return null;
  }

}
