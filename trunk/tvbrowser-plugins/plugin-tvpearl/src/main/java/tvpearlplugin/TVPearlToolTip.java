/*
 * TV-Pearl by Reinhard Lehrbaum
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
 */
package tvpearlplugin;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.plaf.ComponentUI;

public class TVPearlToolTip extends JToolTip
{
	private static final long serialVersionUID = 1L;

	public TVPearlToolTip()
	{
		setUI(new TVPearlToolTipUI());
	}

	private static class TVPearlToolTipUI extends ComponentUI
	{
		private final static int mMaxWidth = 400;
		private final static int mPadding = 5;

		public void paint(Graphics g, JComponent c)
		{
			Font font = c.getFont();
			FontMetrics metrics = c.getFontMetrics(font);
			g.setFont(font);

			Dimension size = c.getSize();
			g.setColor(c.getBackground());
			g.fillRect(0, 0, size.width, size.height);

			g.setColor(c.getForeground());
			String tipText = ((JToolTip) c).getTipText();
			if (tipText != null)
			{
				g.drawRect(0, 0, size.width - 1, size.height - 1);

				int y = metrics.getAscent() + mPadding;
				for (String line : tipText.split("\n"))
				{
					int lineWidth = metrics.stringWidth(line);
					if (lineWidth > mMaxWidth)
					{
						int pos = line.lastIndexOf(" ");

						while (pos > 0)
						{
							if (metrics.stringWidth(line.substring(0, pos).trim()) <= mMaxWidth)
							{
								g.drawString(line.substring(0, pos).trim(), mPadding, y);
								y += metrics.getHeight();
								line = line.substring(pos).trim();
								if (metrics.stringWidth(line) > mMaxWidth)
								{
									pos = line.lastIndexOf(" ");
								}
								else
								{
									pos = -1;
								}
							}
							else
							{
								pos = line.lastIndexOf(" ", pos - 1);
							}
						}
					}
					g.drawString(line, mPadding, y);
					y += metrics.getHeight();
				}
			}
		}

		public Dimension getPreferredSize(JComponent c)
		{
			Font font = c.getFont();
			FontMetrics metrics = c.getFontMetrics(font);

			String tipText = ((JToolTip) c).getTipText();
			if (tipText == null)
			{
				return new Dimension(0, 0);
			}
			else
			{
				int height = 0;
				int width = 0;
				for (String line : tipText.split("\n"))
				{
					height += metrics.getHeight();
					int lineWidth = metrics.stringWidth(line);
					width = Math.max(width, lineWidth);
					if (lineWidth > mMaxWidth)
					{
						int pos = line.lastIndexOf(" ");

						while (pos > 0)
						{
							if (metrics.stringWidth(line.substring(0, pos).trim()) <= mMaxWidth)
							{
								height += metrics.getHeight();
								line = line.substring(pos).trim();
								if (metrics.stringWidth(line) > mMaxWidth)
								{
									pos = line.lastIndexOf(" ");
								}
								else
								{
									pos = -1;
								}
							}
							else
							{
								pos = line.lastIndexOf(" ", pos - 1);
							}
						}
					}
				}
				return new Dimension(Math.min(width, mMaxWidth) + mPadding * 2, height + mPadding * 2);
			}
		}
	}
}
