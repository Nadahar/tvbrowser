/*
 * Timeline by Reinhard Lehrbaum
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
package timelineplugin.format;

import java.awt.FontMetrics;
import java.awt.Graphics;

import util.paramhandler.ParamParser;
import devplugin.Plugin;
import devplugin.Program;

public class TextString implements ITextObject
{
	private int mX;
	private int mY;
	private String mValue;

	public TextString(final String value)
	{
		mValue = value;
	}

	public int getX()
	{
		return mX;
	}

	public int getY()
	{
		return mY;
	}

	public void print(final TextLine textLine, final Program p, final Graphics g,
      final int width, final int height, final int x, final int y)
	{
	  final ParamParser parser = new ParamParser();
		String value = parser.analyse(mValue, p);
		if (value == null)
		{
			value = "";
		}
		final FontMetrics metric = g.getFontMetrics();

		mX = x;
		mY = y;

		if (textLine.getLines() >= 0)
		{
			if (mY <= height)
			{
				int lines = textLine.getLines();

				for (int i = 0; i < textLine.getLines(); i++)
				{
				  final int w = metric.stringWidth(value);
					if (w + mX > width)
					{
						lines--;
						if (isLastLine(lines, mY, height, g.getFont().getSize()))
						{
							printString(shrinkString(value, metric, width - mX, "…"), g, mX, mY);
							value = "";
							mX = width + 1;
							break;
						}
						else
						{
						  final String text = shrinkString(value, metric, width - mX, "");
              final int index = text.lastIndexOf(' ');
							if (index > 0)
							{
								printString(text.substring(0, index), g, mX, mY);
								value = value.substring(index).trim();
								mX = textLine.getPadding();
								mY += g.getFont().getSize();
							}
							else
							{
								printString(shrinkString("", metric, width - mX, "…"), g, mX, mY);
								value = "";
								mX = width + 1;
								break;
							}
						}
					}
					else
					{
						printString(value, g, mX, mY);
						break;
					}
				}
				textLine.setLines(lines);
			}
		}
		else
		{
			printString(value, g, mX, mY);
		}
		mX += metric.stringWidth(value);
	}

	private static void printString(final String value, final Graphics g, final int x,
      final int y)
	{
		g.drawString(value, x, y);
	}

	private static String shrinkString(String value, final FontMetrics metric,
      final int width, final String trailer)
	{
	  final int usedWidth = width - metric.stringWidth(trailer);
		if (usedWidth > 0)
		{
			Boolean finishCut = false;
			final int l = value.length();
			for (int i = l - 1; i > 0; i--)
			{
			  final int w = metric.stringWidth(value.substring(0, i));
				if (w <= usedWidth)
				{
					value = value.substring(0, i) + trailer;
					finishCut = true;
					break;
				}
			}
			if (!finishCut)
			{
				value = trailer;
			}
		}
		else
		{
			value = "";
		}
		return value;
	}

	private static boolean isLastLine(final int lines, final int y, final int height,
      final int fontSize)
	{
		if (lines <= 0)
		{
			return true;
		}
		if (y + fontSize > height)
		{
			return true;
		}
		return false;
	}

	public boolean testFormat()
	{
	  final ParamParser parser = new ParamParser();
		return parser.analyse(mValue, Plugin.getPluginManager().getExampleProgram()) != null;
	}
}
