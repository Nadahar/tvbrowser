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

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import devplugin.*;
import util.browserlauncher.Launch;
import util.ui.*;

public class ChannelHeader extends JComponent
{
	private static final long serialVersionUID = 1L;

	private static final Cursor linkCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
	private static final Cursor normalCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	private static final Cursor resizeRowCursor = Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
	private static final Cursor resizeColumnCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);

	private int mChannelCount;
	private Channel[] mChannels;
	private int mChannelHeight;
	private Boolean mShowName;
	private Boolean mShowIcon;

	private Map<Channel, ImageIcon> mIcons;

	private Boolean mRowResizeing = false;
	private int mResizeStartY;
	private int mResizeY;
	private int mReferenceY;

	private Boolean mColumnResizeing = false;
	private int mResizeX;

	public ChannelHeader(int channelHeight)
	{
		mIcons = new HashMap<Channel, ImageIcon>();

		mChannelHeight = channelHeight;
		mChannels = Plugin.getPluginManager().getSubscribedChannels();
		mChannelCount = mChannels.length;

		mShowName = TimelinePlugin.getInstance().showChannelName();
		mShowIcon = TimelinePlugin.getInstance().showChannelIcon();

		this.setOpaque(true);

		addMouseMotionListener(new MouseMotionListener()
		{
			public void mouseDragged(MouseEvent e)
			{
				if (mRowResizeing)
				{
					mResizeY = e.getPoint().y;
					repaint();
				}
				else if (mColumnResizeing)
				{
					mResizeX = e.getPoint().x;
					repaint();
				}
			}

			public void mouseMoved(MouseEvent e)
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

		addMouseListener(new MouseListener()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (getCursor() == linkCursor && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1)
				{
					int index = (int) ((double) e.getY() / mChannelHeight);
					Launch.openURL(mChannels[index].getWebpage());
				}
			}

			public void mouseEntered(MouseEvent e)
			{}

			public void mouseExited(MouseEvent e)
			{}

			public void mousePressed(MouseEvent e)
			{
				if (isMouseOverRowMargin(e.getPoint()))
				{
					mResizeStartY = e.getPoint().y;
					mRowResizeing = true;
					mReferenceY = mResizeStartY - mChannelHeight;
				}
				else if (isMouseOverColumnMargin(e.getPoint()))
				{
					mColumnResizeing = true;
				}
			}

			public void mouseReleased(MouseEvent e)
			{
				if (mRowResizeing)
				{
					mRowResizeing = false;
					TimelinePlugin.getInstance().setProperty("ChannelHeight", Integer.toString(Math.abs(mReferenceY - e.getPoint().y)));
					TimelinePlugin.getInstance().resize();
				}
				if (mColumnResizeing)
				{
					mColumnResizeing = false;
					TimelinePlugin.getInstance().setChannelWidth(mResizeX);
					TimelinePlugin.getInstance().resize();
				}
			}
		});
	}

	public void setPreferredHeight(int ph)
	{
		setPreferredSize(new Dimension(TimelinePlugin.getInstance().getChannelWidth(), ph));
		setSize(new Dimension(TimelinePlugin.getInstance().getChannelWidth(), ph));
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Color c = g.getColor();
		Color cr = new Color(240, 240, 240);
		g.setFont(TimelinePlugin.getInstance().getFont());
		g.setColor(c);

		int delta = 0;
		int h = g.getFontMetrics().getHeight();
		if (mChannelHeight > 20)
		{
			h += (mChannelHeight - h) / 2;
			delta = (mChannelHeight - 20) / 2;
		}
		int w = this.getSize().width;
		g.setColor(Color.WHITE);
		g.fillRect(1, 0, w - 1, this.getSize().height);
		int textBegin = mShowIcon ? 45 : 5;
		for (int i = 0; i < mChannelCount; i++)
		{
			int y = mChannelHeight * i;
			g.setColor(i % 2 == 0 ? Color.WHITE : cr);
			g.fillRect(0, y, w, mChannelHeight);
			g.setColor((!mRowResizeing && !mColumnResizeing) ? c : Color.LIGHT_GRAY);
			if (mShowName)
			{
				g.drawString(mChannels[i].getName(), textBegin, y + h);
			}
			if (mShowIcon)
			{
				getIcon(mChannels[i]).paintIcon(this, g, 0, y + delta);
			}
		}

		if (mRowResizeing)
		{
			g.setColor(Color.RED);
			int diff = mResizeY - mReferenceY;
			String text = Integer.toString(Math.abs(diff));
			int x = getWidth() / 2;
			int end = mReferenceY + (diff + g.getFont().getSize()) / 2;
			int begin = end - g.getFont().getSize();
			g.drawString(text, (getWidth() - g.getFontMetrics().stringWidth(text)) / 2, end);
			g.drawLine(0, mReferenceY, getSize().width, mReferenceY);
			g.drawLine(x, mReferenceY, x, begin - 2);
			g.drawLine(x, mResizeY, x, end + 2);
			g.drawLine(0, mResizeY, getSize().width, mResizeY);
		}
		else if (mColumnResizeing)
		{
			String text = Integer.toString(mResizeX);
			int textWidth = g.getFontMetrics().stringWidth(text);
			int x = (mResizeX - textWidth) / 2;
			g.setColor(Color.WHITE);
			g.fillRect(x - 1, mResizeY - 6, textWidth + 2, g.getFont().getSize() + 2);
			g.setColor(Color.RED);
			int begin = x - 2;
			int end = begin + textWidth + 2;
			g.drawLine(0, mResizeY, begin, mResizeY);
			g.drawLine(end, mResizeY, mResizeX, mResizeY);
			g.drawString(text, x, mResizeY + 5);
			g.drawLine(mResizeX, 0, mResizeX, getSize().height);
		}
		g.setColor(c);
	}

	private ImageIcon getIcon(Channel channel)
	{
		if (mIcons.containsKey(channel))
		{
			return mIcons.get(channel);
		}
		ImageIcon icon = UiUtilities.createChannelIcon(channel.getIcon());
		mIcons.put(channel, icon);
		return icon;
	}

	private boolean isMouseOverIcon(Point p)
	{
		int logoBegin = ((int) ((double) p.getY() / mChannelHeight)) * mChannelHeight + (mChannelHeight > 20 ? (mChannelHeight - 20) / 2 : 0);
		int maxHeight = mChannelCount * mChannelHeight;

		if (p.getY() < maxHeight && p.getX() <= 42 && p.getY() > logoBegin && p.getY() < logoBegin + 22)
		{
			return true;
		}
		return false;
	}

	private boolean isMouseOverRowMargin(Point p)
	{
		if (!TimelinePlugin.getInstance().resizeWithMouse())
		{
			return false;
		}
		if ((p.y % mChannelHeight == 0) && (p.y > 0))
		{
			return true;
		}
		return false;
	}

	private boolean isMouseOverColumnMargin(Point p)
	{
		if (!TimelinePlugin.getInstance().resizeWithMouse())
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
