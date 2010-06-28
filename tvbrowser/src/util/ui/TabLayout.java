/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * A layout similar to the {@link java.awt.GridLayout}, whose column widths and
 * row heights can variate. so a column with only narrow components is narrower
 * than one with large components.
 *
 * @author Til Schneider, www.murfman.de
 */
public class TabLayout implements LayoutManager {
  // Anzahl der Spalten, Abstand zwischen den Komponenten in x- und
  // y-Richtung
  protected int anzahlSpalten, xAbs, yAbs;
  protected boolean dontMaximizeComponents;



  /**
   * Constructs a TabLayout.
   *
   * @param columns The number of columns.
   */
  public TabLayout(int columns) {
    this(columns, false);
  }


  /**
   * Constructs a TabLayout.
   *
   * @param columns The number of columns.
   * @param xAbs horizontal gap between the components
   * @param yAbs vertical gap between the components
   */
  public TabLayout(int columns, int xAbs, int yAbs) {
    this(columns, xAbs, yAbs, false);
  }


  /**
   * Constructs a TabLayout.
   *
   * @param columns The number of columns.
   * @param dontMaximizeComponents If true the Components of a Container using this
   *        TabLayout are not maximized to fit in their cell but have a maximum
   *        size equal to their preferred size.
   */
  public TabLayout(int columns, boolean dontMaximizeComponents) {
    this(columns, 3, 3, dontMaximizeComponents);
  }


  /**
   * Constructs a TabLayout.
   *
   * @param columns The number of columns.
   * @param xAbs horizontal gap between the components
   * @param yAbs vertical gap between the components
   * @param dontMaximizeComponents If true the Components of a Container using this
   *        TabLayout are not maximized to fit in their cell but have a maximum
   *        size equal to their preferred size.
   */
  public TabLayout(int columns, int xAbs, int yAbs, boolean dontMaximizeComponents) {
    if (columns < 1) {
      columns = 1;
    }
    this.anzahlSpalten = columns;
    this.dontMaximizeComponents = dontMaximizeComponents;
    this.xAbs = xAbs;
    this.yAbs = yAbs;
  }



  Component[][] getComponentFeld(Container c) {
    int anzahlComp = c.getComponentCount();
    int hoehe = (int)Math.ceil((double)anzahlComp/anzahlSpalten);
    Component[][] comp = new Component[anzahlSpalten][hoehe>0 ? hoehe : 1];

    int aktSpalte = 0, aktZeile = 0;
    for (int i = 0; i<anzahlComp; i++) {
      comp[aktSpalte][aktZeile] = c.getComponent(i);
      if (++aktSpalte>=anzahlSpalten) { aktSpalte = 0; aktZeile++; }
    }
    return comp;
  }



  int[] getBreiten(int gesBreite, Component[][] comp) {
    int[] breite = new int[comp.length];
    for (int i = 0;i<breite.length;i++) {
      breite[i] = 0;
    }

    for (int y = 0;y<comp[0].length;y++) {
      for (int x = 0;x<comp.length;x++) {
        if (comp[x][y]!=null && comp[x][y].isVisible()) {
          Dimension d = comp[x][y].getPreferredSize();
          breite[x] = Math.max(breite[x], d.width);
        }
      }
    }

    // Skalierung
    if (gesBreite>0) {
      int prefGesBreite = -xAbs;
      for (int element : breite) {
        prefGesBreite += element + xAbs;
      }
      double skal = ((double)gesBreite)/prefGesBreite;
      for (int i = 0;i<breite.length;i++) {
        breite[i] = (int)(breite[i]*skal);
      }
    }

    return breite;
  }



  int[] getHoehen(int gesHoehe, Component[][] comp) {
    int[] hoehe = new int[comp[0].length];
    for (int i = 0;i<hoehe.length;i++) {
      hoehe[i] = 0;
    }

    for (int y = 0;y<comp[0].length;y++) {
      for (Component[] element : comp) {
        if (element[y]!=null && element[y].isVisible()) {
          Dimension d = element[y].getPreferredSize();
          hoehe[y] = Math.max(hoehe[y], d.height);
        }
      }
    }

    // Skalierung
    if (gesHoehe>0) {
      int prefGesHoehe = -yAbs;
      for (int element : hoehe) {
        prefGesHoehe += element + yAbs;
      }
      double ySkal = ((double)gesHoehe)/prefGesHoehe;
      for (int i = 0;i<hoehe.length;i++) {
        hoehe[i] = (int)(hoehe[i]*ySkal);
      }
    }

    return hoehe;
  }


  // implements LayoutManager


  public void addLayoutComponent(String name, Component comp) {
  }



  public void removeLayoutComponent(Component comp) {
  }



  public Dimension preferredLayoutSize(Container c) {
    synchronized (c.getTreeLock()) {
      Component[][] comp = getComponentFeld(c);
      int[] breite = getBreiten(0, comp);
      int[] hoehe = getHoehen(0, comp);

      Dimension dim = new Dimension(-xAbs, -yAbs);
      for (int element : breite) {
        dim.width += element + xAbs;
      }
      for (int element : hoehe) {
        dim.height += element + yAbs;
      }
      Insets insets = c.getInsets();
      dim.width += insets.left + insets.right;
      dim.height += insets.top + insets.bottom;
      return dim;
    }
  }



  public Dimension minimumLayoutSize(Container target) {
    return preferredLayoutSize(target);
  }



  public void layoutContainer(Container target) {
    synchronized (target.getTreeLock()) {
      Insets insets = target.getInsets();
      Component[][] comp = getComponentFeld(target);
      int[] breite = getBreiten(target.getSize().width
                    -insets.left-insets.right,comp);
      int[] hoehe = getHoehen(target.getSize().height
                  -insets.top-insets.bottom,comp);

      int[] xPos = new int[comp.length];
      int[] yPos = new int[comp[0].length];
      xPos[0] = insets.left; yPos[0] = insets.top;
      for (int i = 1; i < xPos.length; i++) {
        xPos[i] = xPos[i-1] + breite[i-1] + xAbs;
      }
      for (int i = 1; i < yPos.length; i++) {
        yPos[i] = yPos[i-1] + hoehe[i-1] + yAbs;
      }

      int currBreite, currHoehe;
      Dimension currPreferredSize;
      for (int y = 0; y < comp[0].length; y++) {
        for (int x = 0; x < comp.length; x++) {
          if ((comp[x][y] != null) && comp[x][y].isVisible()) {
            currBreite = breite[x];
            currHoehe = hoehe[y];
            if (dontMaximizeComponents) {
              currPreferredSize = comp[x][y].getPreferredSize();
              currBreite = Math.min(currBreite, currPreferredSize.width);
              currHoehe = Math.min(currHoehe, currPreferredSize.height);
            }
            comp[x][y].setSize(currBreite, currHoehe);
            comp[x][y].setLocation(xPos[x], yPos[y]);
          }
        }
      }
    }
  }

}
