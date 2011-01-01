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
import javax.swing.JPanel;
import javax.swing.JViewport;

import devplugin.Date;
import devplugin.Plugin;

public class ProgramPanel extends JPanel {
	private static final Color SECOND_ROW_COLOR = new Color(240, 240, 240);

	private static final long serialVersionUID = 1L;

	private int mOffsetX;
	private int mHourWidth;
	private int mChannelHeight;
	private int mChannelCount;
	private int mClientWidth;
	private int mClientHeight;
	private int mStartX;
	private Point mDraggingPoint = null;

	int mStartOfDay;

	int mEndOfDay;

	private static Stroke hourStroke = new BasicStroke(1.0f,
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 15.0f, new float[] { 5.0f,
					5.0f }, 5.0f);
	private static Stroke halfHourStroke = new BasicStroke(1.0f,
			BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 15.0f, new float[] { 1.0f,
					2.0f }, 5.0f);
	private static Stroke nowStroke = new BasicStroke(3);

	public ProgramPanel() {
		super(new BorderLayout());
		setOpaque(true);
		this.setBackground(Color.WHITE);

		mChannelCount = Plugin.getPluginManager().getSubscribedChannels().length;
		resize();

		addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseDragged(final MouseEvent evt) {
				if (mDraggingPoint != null && !evt.isShiftDown()) {
					final int deltaX = mDraggingPoint.x - evt.getX();
					final int deltaY = mDraggingPoint.y - evt.getY();
					scrollBy(deltaX, deltaY);
				}
			}
		});

		addMouseListener(new MouseAdapter() {
			public void mousePressed(final MouseEvent evt) {
				mDraggingPoint = evt.getPoint();
			}

			public void mouseReleased(final MouseEvent evt) {
				mDraggingPoint = null;
			}
		});
	}

	public void resize() {
		mStartOfDay = (Plugin.getPluginManager().getTvBrowserSettings()
				.getProgramTableStartOfDay() / 60) * 60;
		mEndOfDay = (Plugin.getPluginManager().getTvBrowserSettings()
				.getProgramTableEndOfDay() / 60) * 60;
		mHourWidth = TimelinePlugin.getSettings().getHourWidth();
		mChannelHeight = TimelinePlugin.getSettings().getChannelHeight();
		mOffsetX = TimelinePlugin.getInstance().getOffset();
		int numHours = (mEndOfDay - mStartOfDay) / 60 + 24;
		mClientWidth = mHourWidth * numHours;
		mClientHeight = mChannelCount * mChannelHeight;

		mStartX = mOffsetX - (((mOffsetX / mHourWidth) + 1) * mHourWidth);

		Dimension size = new Dimension(mClientWidth + 2 * mOffsetX,
				mClientHeight + 1);
		this.setPreferredSize(size);
		this.setSize(size);
	}

	public void scrollBy(final int deltaX, final int deltaY) {
		if (getParent() instanceof JViewport) {
			final JViewport viewport = (JViewport) getParent();
			Point viewPos = viewport.getViewPosition();

			if (deltaX != 0) {
				viewPos.x += deltaX;
				final int maxX = getWidth() - viewport.getWidth();

				viewPos.x = Math.min(viewPos.x, maxX);
				viewPos.x = Math.max(viewPos.x, 0);
			}
			if (deltaY != 0) {
				viewPos.y += deltaY;
				final int maxY = getHeight() - viewport.getHeight();

				viewPos.y = Math.min(viewPos.y, maxY);
				viewPos.y = Math.max(viewPos.y, 0);
			}
			viewport.setViewPosition(viewPos);
		}
	}

	public void paintComponent(final Graphics g) {
		paintProgramPanel(g);
	}

	public void paintProgramPanel(final Graphics g) {
		super.paintComponent(g);
		final Rectangle clipBounds = g.getClipBounds();

		g.setFont(TimelinePlugin.getFont());

		final Graphics2D g2 = (Graphics2D) g;

		final int top = clipBounds.y;
		final int bottom = top + clipBounds.height;
		final int left = clipBounds.x;
		final int right = left + clipBounds.width;

		final Color oriColor = g.getColor();

		g.setColor(secondRowColor());
		for (int i = 1; i < mChannelCount; i += 2) {
			final int y = mChannelHeight * i;
			if (y < bottom && y + mChannelHeight >= top) {
				g.fillRect(left, y, clipBounds.width, mChannelHeight);
			}
		}

		g.setColor(Color.LIGHT_GRAY);
		final Stroke oriStroke = g2.getStroke();

		final int halfHourSize = mHourWidth / 2;
		int xFullHour = mStartX;

		while (xFullHour < right) {
			if (xFullHour >= left) {
				g2.setStroke(hourStroke);
				g2.drawLine(xFullHour, top, xFullHour, bottom);
			}
			final int xHalfHour = xFullHour + halfHourSize;
			if (xHalfHour > left) {
				g2.setStroke(halfHourStroke);
				g2.drawLine(xHalfHour, top, xHalfHour, bottom);
			}
			xFullHour += mHourWidth;
		}
		if (TimelinePlugin.getSettings().showBar()) {
			if (Date.getCurrentDate().equals(
					TimelinePlugin.getInstance().getChoosenDate())) {
				int xNow = TimelinePlugin.getInstance().getNowPosition();
				if (xNow >= left - 5 && xNow < right + 5) {
					g2.setStroke(nowStroke);
					g.setColor(new Color(255, 0, 0, 50));
					g.drawLine(xNow, top, xNow, bottom);
				}
			}
		}
		g2.setStroke(oriStroke);
		g.setColor(oriColor);
	}

	public static Color secondRowColor() {
		return SECOND_ROW_COLOR;
	}
}
