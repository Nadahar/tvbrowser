/*
* TV-Browser
* Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
*     $Date$
*   $Author$
* $Revision$
*/
package util.ui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalToolTipUI;

public class ToolTipWithIcon extends JToolTip {
  ImageIcon icon;

  public ToolTipWithIcon(ImageIcon icon) {
    this.icon = icon;
    setUI(new IconToolTipUI());
  }

  public ToolTipWithIcon(ImageIcon icon, String text) {
    this(icon);
    setTipText(text);
  }

  public ToolTipWithIcon(MetalToolTipUI toolTipUI) {
    setUI(toolTipUI);
  }

  private class IconToolTipUI extends MetalToolTipUI {
    public void paint(Graphics g, JComponent c) {
      FontMetrics metrics = c.getFontMetrics(c.getFont());
      Dimension size = c.getSize();
      g.setColor(c.getBackground());
      g.fillRect(0, 0, size.width, size.height);
      int x = 3;
      if (icon != null) {
        icon.paintIcon(c, g, 1, 1);
        x += icon.getIconWidth() + 1;
      }
      g.setColor(c.getForeground());
      String text = ((JToolTip) c).getTipText();
      if (text != null && text.length() > 0) {
        g.drawString(text, x, metrics.getHeight());
      }
    }

    public Dimension getPreferredSize(JComponent c) {
      String tipText = ((JToolTip) c).getTipText();
      // avoid painting by returning empty dimension for no text and no icon
      if (icon == null && (tipText == null || tipText.length() == 0)) {
        return new Dimension(0, 0);
      }
      FontMetrics metrics = c.getFontMetrics(c.getFont());
      if (tipText == null) {
        tipText = "";
      }
      int width = SwingUtilities.computeStringWidth(metrics, tipText);
      int height = metrics.getHeight() + 4;
      if (icon != null) {
        width += icon.getIconWidth() + 2;
        int iconHeight = icon.getIconHeight() + 2;
        if (iconHeight > height) {
          height = iconHeight;
        }
      }
      // make some space between icon and text
      if (icon != null && tipText.length() > 0) {
        width += 6;
      }
      if (ToolTipWithIcon.this.isMinimumSizeSet()) {
        Dimension minSize = ToolTipWithIcon.this.getMinimumSize();
        if (width < minSize.width) {
          width = minSize.width;
        }
        if (height < minSize.height) {
          height = minSize.height;
        }
      }
      return new Dimension(width, height);
    }
  }
}