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
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * An icon having a fixed size. This icon wrappes a nested icon. If the nested
 * Icon is smaller than the defined size, it will be shown in the center of this
 * icon (The nested icon will get a transparent border).
 *
 * @author Til Schneider, www.murfman.de
 * @see devplugin.Plugin#getButtonAction()
 */
public class FixedSizeIcon implements Icon {
  
  /** The width the icon should have. */
  private int mWidth;

  /** The height the icon should have. */
  private int mHeight;
  
  /** The nested icon to show. */
  private Icon mNestedIcon;
  
  
  /**
   * Creates a new instance of FixedSizeIcon.
   * 
   * @param width The width the icon should have.
   * @param height The height the icon should have.
   * @param nestedIcon The nested icon to show.
   */
  public FixedSizeIcon(int width, int height, Icon nestedIcon) {
    if (nestedIcon == null) {
      throw new NullPointerException("nestedIcon is null");
    }
    
    mWidth = width;
    mHeight = height;
    mNestedIcon = nestedIcon;
  }
  

  /**
   * Gets the icon's width.
   * 
   * @return the icon's width
   */
  public int getIconWidth() {
    return mWidth;
  }
  
  
  /**
   * Gets the icon's height.
   * 
   * @return the icon's height
   */
  public int getIconHeight() {
    return mHeight;
  }
  

  /**
   * Paints the icon.
   * 
   * @param c May be used to get properties useful for painting, e.g. the
   *        foreground or background color.
   * @param g The graphics context to paint to.
   * @param x The x position where to paint the icon
   * @param y The y position where to paint the icon
   */
  public void paintIcon(Component c, Graphics g, int x, int y) {
    // Paint the nested icon in the middle of this icon
    int nestedWidth = mNestedIcon.getIconWidth();
    int nestedHeight = mNestedIcon.getIconHeight();
    
    mNestedIcon.paintIcon(c, g, x + (mWidth - nestedWidth) / 2,
                                y + (mHeight - nestedHeight) / 2);
  }

}
