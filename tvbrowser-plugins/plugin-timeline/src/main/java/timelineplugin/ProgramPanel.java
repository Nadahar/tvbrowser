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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;

import javax.swing.JPanel;
import javax.swing.JViewport;

import devplugin.Date;
import devplugin.Plugin;

public class ProgramPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private int mOffset;
	private int mSizeHour;
	private int mSizeChannel;
	private int mChannelCount;
	private int mClientWidth;
	private int mClientHeigh;
	private int mStartX;
	private Point mDraggingPoint = null;

	private static Stroke hourStroke = new BasicStroke(1.0f,
      BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 15.0f, new float[] { 5.0f,
          5.0f }, 5.0f);
  private static Stroke halfHourStroke = new BasicStroke(1.0f,
      BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 15.0f, new float[] { 1.0f,
          2.0f }, 5.0f);
  private static Stroke nowStroke = new BasicStroke(3);

	public ProgramPanel()
	{
		super(new BorderLayout());

		mChannelCount = Plugin.getPluginManager().getSubscribedChannels().length;

		resize();

		this.setBackground(Color.WHITE);

		addMouseMotionListener(new MouseMotionAdapter()
		{
			public void mouseDragged(final MouseEvent evt)
			{
				if (mDraggingPoint != null && !evt.isShiftDown())
				{
				  final int deltaX = mDraggingPoint.x - evt.getX();
          final int deltaY = mDraggingPoint.y - evt.getY();
					scrollBy(deltaX, deltaY);
				}
			}
		});

		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(final MouseEvent evt)
			{
				mDraggingPoint = evt.getPoint();
			}

			public void mouseReleased(final MouseEvent evt)
			{
				mDraggingPoint = null;
			}
		});
	}

	public void resize()
	{
		mSizeHour = TimelinePlugin.getSettings().getHourWidth();
    mSizeChannel = TimelinePlugin.getSettings().getChannelHeight();
		mOffset = TimelinePlugin.getInstance().getOffset();
		mClientWidth = mSizeHour * 24;
		mClientHeigh = mChannelCount * mSizeChannel;

		mStartX = mOffset - (((mOffset / mSizeHour) + 1) * mSizeHour);

		this.setPreferredSize(new Dimension(mClientWidth + 2 * mOffset, mClientHeigh + 1));
		this.setSize(new Dimension(mClientWidth + 2 * mOffset, mClientHeigh + 1));
	}

	public void scrollBy(final int deltaX, final int deltaY)
	{
		if (getParent() instanceof JViewport)
		{
		  final JViewport viewport = (JViewport) getParent();
			Point viewPos = viewport.getViewPosition();

			if (deltaX != 0)
			{
				viewPos.x += deltaX;
				final int maxX = getWidth() - viewport.getWidth();

				viewPos.x = Math.min(viewPos.x, maxX);
				viewPos.x = Math.max(viewPos.x, 0);
			}
			if (deltaY != 0)
			{
				viewPos.y += deltaY;
				final int maxY = getHeight() - viewport.getHeight();

				viewPos.y = Math.min(viewPos.y, maxY);
				viewPos.y = Math.max(viewPos.y, 0);
			}
			viewport.setViewPosition(viewPos);
		}
	}

	public void paintComponent(final Graphics g)
	{
		super.paintComponent(g);
		final Rectangle drawHere = g.getClipBounds();

    g.setFont(TimelinePlugin.getFont());

		final Graphics2D g2 = (Graphics2D) g;

		final int top = 0;
    final int bottom = drawHere.height;

    final Color oriColor = g.getColor();

		g.setColor(new Color(240, 240, 240));
		for (int i = 0; i < mChannelCount; i++)
		{
			if (i % 2 != 0)
			{
			  final int y = mSizeChannel * i;
				g.fillRect(0, y, this.getSize().width, mSizeChannel);
			}
		}

		g.setColor(Color.LIGHT_GRAY);
		final Stroke oriStroke = g2.getStroke();

		final int halfHourSize = mSizeHour / 2;
		int x = mStartX;

		while (x < this.getSize().width)
		{
		  final int xh = x + halfHourSize;
		  final Line2D l = new Line2D.Double(x, top, x, bottom);
      final Line2D l2 = new Line2D.Double(xh, top, xh, bottom);
			g2.setStroke(hourStroke);
			g2.draw(l);
			g2.setStroke(halfHourStroke);
			g2.draw(l2);
			x += mSizeHour;
		}
		if (TimelinePlugin.getSettings().showBar())
		{
		  final Date d = Date.getCurrentDate();
			if (d.equals(new Date(TimelinePlugin.getInstance().getChoosenDate())))
			{
				g2.setStroke(nowStroke);
				g.setColor(new Color(255, 0, 0, 50));
				x = TimelinePlugin.getInstance().getNowPosition();
				g.drawLine(x, 0, x, this.getSize().height);
			}
		}
		g2.setStroke(oriStroke);
		g.setColor(oriColor);
	}
}
