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
package timelineplugin;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

public class TimeHeader extends JComponent implements MouseListener, MouseInputListener
{
	private static final long serialVersionUID = 1L;
	private static Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);

	private int mOffset;
	private int mSizeHour;
	private int mStartX;
	private int mStartHour;
	private Cursor mLastCursor = null;

	private boolean mResizing = false;
	private int mResizeStartX;
	private int mResizeX;
	private int mReferenceHourX;

	public TimeHeader()
	{
		this.addMouseListener(this);
		this.addMouseMotionListener(this);

		mOffset = TimelinePlugin.getInstance().getOffset();
		mSizeHour = TimelinePlugin.getSettings().getHourWidth();
		final int deltaHour = (mOffset / mSizeHour) + 1;
		mStartX = mOffset - (deltaHour * mSizeHour);
		mStartHour = (24 - deltaHour) % 24;
		if (mStartHour < 0)
		{
			mStartHour += 24;
		}

		this.setOpaque(true);
	}

	public void setPreferredWidth(final int pw)
	{
		setPreferredSize(new Dimension(pw, 30));
	}

	public void paintComponent(final Graphics g)
	{
		super.paintComponent(g);

		final Color c = g.getColor();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.getSize().width, this.getSize().height);
		g.setColor(!mResizing ? c : Color.LIGHT_GRAY);
		g.setFont(TimelinePlugin.getInstance().getFont());

		final int h = g.getFontMetrics().getHeight() + 2;
    final int h5 = h + 5;

		int x = mStartX;
		int hour = mStartHour;

		while (x < this.getSize().width)
		{
		  final String time = formatTime(hour % 24);
		  final int left = x - (g.getFontMetrics().stringWidth(time) / 2);

			g.drawString(time, left, h);
			g.drawLine(x, h5, x, 50);
			x += mSizeHour;
			hour++;
		}

		if (mResizing)
		{
			g.setColor(Color.RED);
			g.drawLine(mReferenceHourX, 0, mReferenceHourX, 50);
			final String diff = Integer
          .toString(Math.abs(mReferenceHourX - mResizeX));
      final int begin = mReferenceHourX + (mResizeX - mReferenceHourX) / 2
          - g.getFontMetrics().stringWidth(diff) / 2;
      final int end = begin + g.getFontMetrics().stringWidth(diff);
			g.drawString(diff, begin, h);
			g.drawLine(mReferenceHourX, h - 5, begin - 2, h - 5);
			g.drawLine(mResizeX, h - 5, end + 2, h - 5);
			g.drawLine(mResizeX, 0, mResizeX, 50);
		}
	}

	private String formatTime(final int hour)
	{
	  final StringBuilder sb = new StringBuilder();
		sb.append("0").append(hour).append(":00");
		if (sb.length() > 5)
		{
			sb.delete(0, 1);
		}
		return sb.toString();
	}

	public void mouseClicked(final MouseEvent e)
	{}

	public void mouseEntered(final MouseEvent e)
	{}

	public void mouseExited(final MouseEvent e)
	{}

	public void mousePressed(final MouseEvent e)
	{
		if (isMouseOverMargin(e.getPoint()))
		{
			mResizeStartX = e.getPoint().x;
			mResizing = true;
			mReferenceHourX = mResizeStartX - mSizeHour;
		}
	}

	public void mouseReleased(final MouseEvent e)
	{
		if (mResizing)
		{
			mResizing = false;
			TimelinePlugin.getSettings().setHourWidth(
          Math.abs(mReferenceHourX - e.getPoint().x));
			TimelinePlugin.getInstance().resize();
		}
	}

	public void mouseDragged(final MouseEvent e)
	{
		if (mResizing)
		{
			mResizeX = e.getPoint().x;
			repaint();
		}
	}

	public void mouseMoved(final MouseEvent e)
	{
		if (isMouseOverMargin(e.getPoint()))
		{
			if (mLastCursor == null)
			{
				mLastCursor = this.getCursor();
			}
			setCursor(resizeCursor);
		}
		else
		{
			setCursor(mLastCursor);
		}
	}

	private boolean isMouseOverMargin(final Point p)
	{
		if (!TimelinePlugin.getSettings().resizeWithMouse())
		{
			return false;
		}
		final int x = p.x - mStartX;
		if ((p.y >= TimelinePlugin.getInstance().getFont().getSize() + 2 + 5 + 4) && (x % mSizeHour == 0))
		{
			return true;
		}
		return false;
	}
}
