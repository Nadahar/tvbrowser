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

package util.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class GridFlowLayout implements LayoutManager {

    public static final int CENTER = 0, LEFT = 1, RIGHT = 2, TOP = LEFT, BOTTOM = RIGHT;

    private int mHGap, mVGap;
    private int mVAlign, mHAlign;


    public GridFlowLayout(int hgap, int vgap, int valign, int halign) {
      mHGap = hgap;
      mVGap = vgap;
      mVAlign = valign;
      mHAlign = halign;
    }

    public GridFlowLayout(int hgap, int vgap) {
      this(hgap, vgap, CENTER, CENTER);
    }

    public GridFlowLayout() {
      this(0,0);
    }

    private Dimension getMaximumComponentSize(Container container) {
      int width = 0;
      int height = 0;
      int cnt = container.getComponentCount();

      for (int i = 0; i<cnt; i++) {
        Component c = container.getComponent(i);
        Dimension dim = c.getPreferredSize();
        if (dim.getWidth() > width) {
          width = (int) dim.getWidth();
        }
        if (dim.getHeight() > height) {
          height = (int) dim.getHeight();
        }
      }
      return new Dimension(width, height);

    }

    public void removeLayoutComponent(Component arg0) {
    }

    public void layoutContainer(Container container) {
      synchronized (container.getTreeLock()) {
        Dimension compDimension = getMaximumComponentSize(container);
        int cnt = container.getComponentCount();

        int width = container.getWidth();
        if (width == 0) {
          width = 1;
        }
        int height = container.getHeight();

        int totalLength = (int)(compDimension.getWidth()+mHGap) * cnt;

        int rows = totalLength / width +1;
        int compsPerRow = (int) (width / (compDimension.getWidth()+mHGap));
				if (compsPerRow == 0) {
          compsPerRow = 1;
				}

        int rowLength = (int) (compsPerRow * compDimension.getWidth()+ (mHGap*compsPerRow-1));
        int rowHeight = (int) (rows * compDimension.getHeight()+ (mVGap*rows-1));
        int hIndent=0;
        if (mHAlign == CENTER) {
          hIndent = (width-rowLength)/2;
        }
        else if (mHAlign == RIGHT) {
          hIndent = width-rowLength;
        }

        int vIndent=0;
        if (mVAlign == CENTER) {
            vIndent = (height-rowHeight)/2;
        }
        else if (mVAlign == BOTTOM) {
            vIndent = height-rowHeight;
        }


        for (int i = 0; i<cnt; i++) {
          Component c = container.getComponent(i);
          int x = (i%compsPerRow)*(compDimension.width + mHGap) + hIndent;
          int y = (i/compsPerRow)*(compDimension.height+ mVGap) + vIndent;
          c.setLocation(x, y);
          c.setSize(compDimension);
        }
      }
    }

    public void addLayoutComponent(String arg0, Component arg1) {
    }

    public Dimension minimumLayoutSize(Container c) {
        return new Dimension(0,0);
    }

    public Dimension preferredLayoutSize(Container c) {
        return minimumLayoutSize(c);
    }



}