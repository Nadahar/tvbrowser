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

import java.awt.*;
import util.paramhandler.*;
import devplugin.*;

public class TextString implements ITextObject
{
	private int mX;
	private int mY;
	private String mValue;

	public TextString(String value)
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

	public void print(TextLine textLine, Program p, Graphics g, int width, int height, int x, int y)
	{
		ParamParser parser = new ParamParser();
		String value = parser.analyse(mValue, p);
		if (value == null)
		{
			value = "";
		}
		FontMetrics metric = g.getFontMetrics();

		mX = x;
		mY = y;

		if (textLine.getLines() >= 0)
		{
			if (mY <= height)
			{
				int lines = textLine.getLines();

				for (int i = 0; i < textLine.getLines(); i++)
				{
					int w = metric.stringWidth(value);
					if (w + mX > width)
					{
						lines--;
						if (lastLine(lines, mY, height, g.getFont().getSize()))
						{
							printString(shrinkString(value, metric, width - mX, "..."), g, mX, mY);
							value = "";
							mX = width + 1;
							break;
						}
						else
						{
							String text = shrinkString(value, metric, width - mX, "");
							int index = text.lastIndexOf(" ");
							if (index > 0)
							{
								printString(text.substring(0, index), g, mX, mY);
								value = value.substring(index).trim();
								mX = textLine.getPadding();
								mY += g.getFont().getSize();
							}
							else
							{
								printString(shrinkString("", metric, width - mX, "..."), g, mX, mY);
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

	private void printString(String value, Graphics g, int x, int y)
	{
		g.drawString(value, x, y);
	}

	private String shrinkString(String value, FontMetrics metric, int width, String trailer)
	{
		int usedWidth = width - metric.stringWidth(trailer);
		if (usedWidth > 0)
		{
			Boolean finishCut = false;
			int l = value.length();
			for (int i = l - 1; i > 0; i--)
			{
				int w = metric.stringWidth(value.substring(0, i));
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

	private Boolean lastLine(int lines, int y, int height, int fontSize)
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

	public Boolean testFormat()
	{
		ParamParser parser = new ParamParser();
		return parser.analyse(mValue, Plugin.getPluginManager().getExampleProgram()) != null;
	}
}
