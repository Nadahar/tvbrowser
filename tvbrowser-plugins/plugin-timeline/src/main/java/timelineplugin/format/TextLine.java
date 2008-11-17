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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import devplugin.*;

public class TextLine
{
	private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(timelineplugin.TimelinePlugin.class.getName());

	private List<ITextObject> mList;
	private int mY;
	private int mMaxSize;
	private int mSizeIndex;
	private int mMaxLines;
	private int mAvailableLines;
	private int mPadding;

	public TextLine(String format, int padding, Boolean setMaxline)
	{
		mList = new ArrayList<ITextObject>();
		mMaxSize = 0;
		mSizeIndex = -1;
		mMaxLines = setMaxline ? 1 : -1;
		parseFormat(format);
		mPadding = padding;
	}

	private void parseFormat(String format)
	{
		format = parseMaxLines(format);
		mSizeIndex = format.length();

		Pattern pattern = Pattern.compile("\\{ *setfont *\\( *([^,]+) *, *([^,]+) *, *([^,]+) *\\) *\\}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(format);
		int index = 0;
		while (matcher.find())
		{
			addString(format.substring(index, matcher.start()));
			index = matcher.end();
			mMaxSize = Math.max(mMaxSize, addFont(matcher.group(1), matcher.group(2), matcher.group(3)));
			mSizeIndex = Math.min(mSizeIndex, matcher.start());
		}
		addString(format.substring(index));
		if (mSizeIndex == format.length())
		{
			mSizeIndex = -1;
		}
	}

	private String parseMaxLines(String format)
	{
		Pattern pattern = Pattern.compile("\\{ *multiline *\\( *([0-9]+) *\\) *}", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(format);

		if (matcher.find())
		{
			mMaxLines = Integer.parseInt(matcher.group(1));
		}
		return matcher.replaceAll("");
	}

	private void addString(String value)
	{
		if (value.length() > 0)
		{
			mList.add(new TextString(value));
		}
	}

	private int addFont(String fontName, String style, String size)
	{
		int resultSize = -1;
		try
		{
			int fontStyle = Integer.parseInt(style);
			int fontSize = Integer.parseInt(size);
			TextFont f = new TextFont(fontName, fontStyle, fontSize);
			resultSize = f.getFont().getSize();
			mList.add(f);
		}
		catch (Exception ex)
		{
			mLog.warning("SetFont Error (" + fontName + ", " + style + ", " + size + ")");
		}
		return resultSize;
	}

	public boolean testFormat()
	{
		for (ITextObject item : mList)
		{
			if (!item.testFormat())
			{
				return false;
			}
		}
		return true;
	}

	public int getY()
	{
		return mY;
	}

	public void print(Program p, Graphics g, int width, int height, int x, int y)
	{
		mAvailableLines = mMaxLines;
		mY = y;

		if (mSizeIndex < 0)
		{
			mY += g.getFont().getSize();
		}
		else if (mSizeIndex > 0)
		{
			mY += Math.max(g.getFont().getSize(), mMaxSize);
		}
		else
		{
			mY += mMaxSize;
		}
		int dx = x;
		int dy = mY;

		for (ITextObject item : mList)
		{
			item.print(this, p, g, width, height, dx, dy);
			dx = item.getX();
			dy = item.getY();
		}
		mY = dy;
	}

	int getPadding()
	{
		return mPadding;
	}

	int getLines()
	{
		return mAvailableLines;
	}

	void setLines(int value)
	{
		mAvailableLines = value;
	}
}
