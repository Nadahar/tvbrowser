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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import util.browserlauncher.Launch;
import util.ui.UiUtilities;
import devplugin.Channel;
import devplugin.Plugin;

public class ChannelHeader extends JComponent
{
	private static final long serialVersionUID = 1L;

	private static final Cursor linkCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	private static final Cursor normalCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	private static final Cursor resizeRowCursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
	private static final Cursor resizeColumnCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);

	private int mChannelCount;
	transient private Channel[] mChannels;
	private int mChannelHeight;
	private boolean mShowName;
  private boolean mShowIcon;

	private Map<Channel, ImageIcon> mIcons;

	private boolean mRowResizing = false;
	private int mResizeStartY;
	private int mResizeY;
	private int mReferenceY;

	private boolean mColumnResizing = false;
	private int mResizeX;

	public ChannelHeader(final int channelHeight)
	{
		mIcons = new HashMap<Channel, ImageIcon>();

		mChannelHeight = channelHeight;
		mChannels = Plugin.getPluginManager().getSubscribedChannels();
		mChannelCount = mChannels.length;

		mShowName = TimelinePlugin.getSettings().showChannelName();
    mShowIcon = TimelinePlugin.getSettings().showChannelIcon();

		this.setOpaque(true);

		addMouseMotionListener(new MouseMotionListener()
		{
			public void mouseDragged(final MouseEvent e)
			{
				if (mRowResizing)
				{
					mResizeY = e.getPoint().y;
					repaint();
				}
				else if (mColumnResizing)
				{
					mResizeX = e.getPoint().x;
					repaint();
				}
			}

			public void mouseMoved(final MouseEvent e)
			{

				if (isMouseOverRowMargin(e.getPoint()))
				{
					setCursor(resizeRowCursor);
				}
				else if (isMouseOverColumnMargin(e.getPoint()))
				{
					setCursor(resizeColumnCursor);
					mResizeY = e.getPoint().y;
				}
				else if (isMouseOverIcon(e.getPoint()))
				{
					setCursor(linkCursor);
				}
				else
				{
					setCursor(normalCursor);
				}
			}
		});

		addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(final MouseEvent e)
			{
				if (getCursor() == linkCursor && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1)
				{
				  final int index = (int) ((double) e.getY() / mChannelHeight);
					Launch.openURL(mChannels[index].getWebpage());
				}
			}

			public void mousePressed(final MouseEvent e)
			{
				if (isMouseOverRowMargin(e.getPoint()))
				{
					mResizeStartY = e.getPoint().y;
					mRowResizing = true;
					mReferenceY = mResizeStartY - mChannelHeight;
				}
				else if (isMouseOverColumnMargin(e.getPoint()))
				{
					mColumnResizing = true;
				}
			}

			public void mouseReleased(final MouseEvent e)
			{
				if (mRowResizing)
				{
					mRowResizing = false;
					TimelinePlugin.getSettings().setChannelHeight(
              Math.abs(mReferenceY - e.getPoint().y));
					TimelinePlugin.getInstance().resize();
				}
				if (mColumnResizing)
				{
					mColumnResizing = false;
					TimelinePlugin.getInstance().setChannelWidth(mResizeX);
					TimelinePlugin.getInstance().resize();
				}
			}
		});
	}

	public void setPreferredHeight(final int ph)
	{
		setPreferredSize(new Dimension(TimelinePlugin.getInstance().getChannelWidth(), ph));
		setSize(new Dimension(TimelinePlugin.getInstance().getChannelWidth(), ph));
	}

	public void paintComponent(final Graphics g)
	{
		super.paintComponent(g);

		final Color c = g.getColor();
    final Color cr = new Color(240, 240, 240);
		g.setFont(TimelinePlugin.getInstance().getFont());
		g.setColor(c);

		int delta = 0;
		int h = g.getFontMetrics().getHeight();
		if (mChannelHeight > 20)
		{
			h += (mChannelHeight - h) / 2;
			delta = (mChannelHeight - 20) / 2;
		}
		final int w = this.getSize().width;
		g.setColor(Color.WHITE);
		g.fillRect(1, 0, w - 1, this.getSize().height);
		final int textBegin = mShowIcon ? 45 : 5;
		for (int i = 0; i < mChannelCount; i++)
		{
		  final int y = mChannelHeight * i;
			g.setColor(i % 2 == 0 ? Color.WHITE : cr);
			g.fillRect(0, y, w, mChannelHeight);
			g.setColor((!mRowResizing && !mColumnResizing) ? c : Color.LIGHT_GRAY);
			if (mShowName)
			{
				g.drawString(mChannels[i].getName(), textBegin, y + h);
			}
			if (mShowIcon)
			{
				getIcon(mChannels[i]).paintIcon(this, g, 0, y + delta);
			}
		}

		if (mRowResizing)
		{
			g.setColor(Color.RED);
			final int diff = mResizeY - mReferenceY;
      final String text = Integer.toString(Math.abs(diff));
      final int x = getWidth() / 2;
      final int end = mReferenceY + (diff + g.getFont().getSize()) / 2;
      final int begin = end - g.getFont().getSize();
			g.drawString(text, (getWidth() - g.getFontMetrics().stringWidth(text)) / 2, end);
			g.drawLine(0, mReferenceY, getSize().width, mReferenceY);
			g.drawLine(x, mReferenceY, x, begin - 2);
			g.drawLine(x, mResizeY, x, end + 2);
			g.drawLine(0, mResizeY, getSize().width, mResizeY);
		}
		else if (mColumnResizing)
		{
		  final String text = Integer.toString(mResizeX);
      final int textWidth = g.getFontMetrics().stringWidth(text);
      final int x = (mResizeX - textWidth) / 2;
			g.setColor(Color.WHITE);
			g.fillRect(x - 1, mResizeY - 6, textWidth + 2, g.getFont().getSize() + 2);
			g.setColor(Color.RED);
			final int begin = x - 2;
      final int end = begin + textWidth + 2;
			g.drawLine(0, mResizeY, begin, mResizeY);
			g.drawLine(end, mResizeY, mResizeX, mResizeY);
			g.drawString(text, x, mResizeY + 5);
			g.drawLine(mResizeX, 0, mResizeX, getSize().height);
		}
		g.setColor(c);
	}

	private ImageIcon getIcon(final Channel channel)
	{
		if (mIcons.containsKey(channel))
		{
			return mIcons.get(channel);
		}
		final ImageIcon icon = UiUtilities.createChannelIcon(channel.getIcon());
		mIcons.put(channel, icon);
		return icon;
	}

	private boolean isMouseOverIcon(final Point p)
	{
	  final int logoBegin = ((int) ((double) p.getY() / mChannelHeight))
        * mChannelHeight
        + (mChannelHeight > 20 ? (mChannelHeight - 20) / 2 : 0);
    final int maxHeight = mChannelCount * mChannelHeight;

		if (p.getY() < maxHeight && p.getX() <= 42 && p.getY() > logoBegin && p.getY() < logoBegin + 22)
		{
			return true;
		}
		return false;
	}

	private boolean isMouseOverRowMargin(final Point p)
	{
		if (!TimelinePlugin.getSettings().resizeWithMouse())
		{
			return false;
		}
		if ((p.y % mChannelHeight == 0) && (p.y > 0))
		{
			return true;
		}
		return false;
	}

	private boolean isMouseOverColumnMargin(final Point p)
	{
		if (!TimelinePlugin.getSettings().resizeWithMouse())
		{
			return false;
		}
		if ((p.x == getSize().width - 1))
		{
			return true;
		}
		return false;
	}
}
