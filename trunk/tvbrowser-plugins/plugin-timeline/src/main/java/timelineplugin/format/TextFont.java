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

import java.awt.Font;
import java.awt.Graphics;

import devplugin.Program;

public class TextFont implements ITextObject
{
	private Font mFont;
	int mX;
	int mY;

	public TextFont(final String fontName, final int style, final int size)
	{
		mFont = new Font(fontName, style, size);
	}

	public Font getFont()
	{
		return mFont;
	}

	public void print(final TextLine line, final Program p, final Graphics g,
      final int width, final int height, final int x, final int y)
	{
		mX = x;
		mY = y;
		g.setFont(mFont);
	}

	public int getX()
	{
		return mX;
	}

	public int getY()
	{
		return mY;
	}

	public boolean testFormat()
	{
		return true;
	}
}
