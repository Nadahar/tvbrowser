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

import java.util.*;
import java.util.List;
import java.awt.*;
import devplugin.*;

public class TextFormatter
{
	private int mPadding;
	private String mFormat;
	private Font mFont;
	private List<TextLine> mLines;
	private Boolean mInitialiseMaxLine;

	public TextFormatter()
	{
		mPadding = 4;
		mFormat = "";
		mFont = null;
		mLines = new ArrayList<TextLine>();
		mInitialiseMaxLine = false;
	}

	public String getFormat()
	{
		return mFormat;
	}

	public void setFormat(String format)
	{
		mFormat = format;
		parseFormat();
	}

	public int getPadding()
	{
		return mPadding;
	}

	public void setPadding(int padding)
	{
		mPadding = padding;
	}

	public Font getFont()
	{
		return mFont;
	}

	public void setFont(Font font)
	{
		mFont = font;
	}

	public Boolean getInitialiseMaxLine()
	{
		return mInitialiseMaxLine;
	}

	public void setInitialiseMaxLine(Boolean value)
	{
		mInitialiseMaxLine = value;
	}

	public boolean testFormat()
	{
		for (TextLine line : mLines)
		{
			if (!line.testFormat())
			{
				return false;
			}
		}
		return true;
	}

	public void paint(Program p, Graphics g, int width, int height)
	{
		paint(p, g, mPadding, width, height);
	}

	public void paint(Program p, Graphics g, int x, int width, int height)
	{
		int y = mPadding;
		int h = height - mPadding;
		int w = width - mPadding;

		for (TextLine line : mLines)
		{
			line.print(p, g, w, h, x, y);
			y += line.getY();
		}
	}

	private void parseFormat()
	{
		String[] lines = mFormat.split("\n");
		for (String line : lines)
		{
			mLines.add(new TextLine(line, mPadding, mInitialiseMaxLine));
		}
	}
}
