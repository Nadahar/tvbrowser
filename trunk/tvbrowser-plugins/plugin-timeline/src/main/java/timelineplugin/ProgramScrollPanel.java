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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFilter;

public class ProgramScrollPanel extends JScrollPane implements
		MouseWheelListener {
	private static final long serialVersionUID = 1L;

	private ProgramPanel mProgramPanel;
	private int mOffset;

	private int mLabelHeight = TimelinePlugin.getSettings().getChannelHeight() + 1;

	public ProgramScrollPanel() {
		super();

		mOffset = TimelinePlugin.getInstance().getOffset();

		initPanel();

		setBackground(Color.WHITE);
		setWheelScrollingEnabled(false);
		addMouseWheelListener(this);
	}

	private void initPanel() {
		mProgramPanel = new ProgramPanel();
		addProgramList();
		setViewportView(mProgramPanel);

		final TimeHeader th = new TimeHeader();
		th.setPreferredWidth(mProgramPanel.getPreferredSize().width);
		setColumnHeaderView(th);

		final ChannelHeader ch = new ChannelHeader(TimelinePlugin.getSettings()
				.getChannelHeight());
		ch.setPreferredHeight(mProgramPanel.getPreferredSize().height);
		setRowHeaderView(ch);

		getVerticalScrollBar().setUnitIncrement(
				TimelinePlugin.getSettings().getChannelHeight());
		getHorizontalScrollBar().setUnitIncrement(
				TimelinePlugin.getSettings().getHourWidth() / 4);
	}

	private void addProgramList() {
		mProgramPanel.removeAll();

		int channelTop = 0;
		final Channel[] channels = Plugin.getPluginManager()
				.getSubscribedChannels();
		final Date selectedDay = TimelinePlugin.getInstance().getChoosenDate();
		final Date nextDay = selectedDay.addDays(1);
		final int dayWidth = 24 * TimelinePlugin.getSettings().getHourWidth();

		final ProgramFilter filter = TimelinePlugin.getInstance().getFilter();
		int channelHeight = TimelinePlugin.getSettings().getChannelHeight();

		for (int channelIndex = 0; channelIndex < channels.length; channelIndex++) {
			channelTop = channelIndex * channelHeight;

			// today
			Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(
					selectedDay, channels[channelIndex]);
			if (it != null) {
				while (it.hasNext()) {
					final Program p = it.next();
					if (p.getStartTime() + p.getLength() >= mProgramPanel.mStartOfDay) {
						if (filter.accept(p)) {
							addProgram(p, channelTop, 0);
						}
					}
				}
			}
			
			// next day
			if (mProgramPanel.mEndOfDay > 0) {
				it = Plugin.getPluginManager().getChannelDayProgram(nextDay,
						channels[channelIndex]);
				if (it != null) {
					while (it.hasNext()) {
						final Program p = it.next();
						if (p.getStartTime() < mProgramPanel.mEndOfDay) {
							if (filter.accept(p)) {
								addProgram(p, channelTop, dayWidth);
							}
						}
					}
				}
			}
		}
		final JLabel dummy = new JLabel("");
		dummy.setBounds(0, 0, 1, 1);
		mProgramPanel.add(dummy);
		mProgramPanel.repaint();
	}

	private void addProgram(final Program program, final int channelTop,
			final int delta) {
		double sizePerMinute = TimelinePlugin.getInstance().getSizePerMinute();
		int startTime = program.getStartTime();
		int x = mOffset + (int) Math.round(startTime * sizePerMinute);
		int w = mOffset
				+ (int) Math.round((startTime + program.getLength()) * sizePerMinute);
		w = Math.abs(w - x) + 1;
		x += delta;
		if (x + w < 0) {
			return;
		}

		final ProgramLabel lbl = new ProgramLabel(program);
		lbl.setBounds(x, channelTop, w, mLabelHeight);
		mProgramPanel.add(lbl);
	}

	public void mouseWheelMoved(final MouseWheelEvent e) {
		if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
			if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0) {
				final int amount = e.getUnitsToScroll()
						* getHorizontalScrollBar().getUnitIncrement();
				getHorizontalScrollBar().setValue(
						getHorizontalScrollBar().getValue() + amount);
			} else {
				final int amount = e.getUnitsToScroll()
						* getVerticalScrollBar().getUnitIncrement();
				getVerticalScrollBar().setValue(
						getVerticalScrollBar().getValue() + amount);
			}
		}
	}

	public void updateProgram() {
		addProgramList();
	}

	public void gotoTime(int minutes) {
		if (minutes < 0) {
			minutes = 0;
		}
		int x = TimelinePlugin.getSettings().getHourWidth() * (minutes / 60) + mOffset;
		Rectangle viewPosAndSize = getViewport().getViewRect();
		x = Math.min(x, getViewport().getViewSize().width - viewPosAndSize.width - mOffset);
		getViewport().setViewPosition(new Point(x, viewPosAndSize.y));
	}

	public void addTime(final int minute) {
		final JScrollBar sb = getHorizontalScrollBar();

		final int value = (int) Math.round((sb.getMaximum() - 2 * mOffset) * minute
				/ (24.0 * 60.0));
		sb.setValue(sb.getValue() + value);
	}

	public void update() {
		mProgramPanel.repaint();
	}

	public void resize() {
		mOffset = TimelinePlugin.getInstance().getOffset();
		mProgramPanel.resize();
		final TimeHeader th = new TimeHeader();
		th.setPreferredWidth(mProgramPanel.getPreferredSize().width);
		setColumnHeaderView(th);
		final ChannelHeader ch = new ChannelHeader(TimelinePlugin.getSettings()
				.getChannelHeight());
		ch.setPreferredHeight(mProgramPanel.getPreferredSize().height);
		setRowHeaderView(ch);
		addProgramList();
	}

	public int getShownHours() {
		return (mProgramPanel.mEndOfDay-mProgramPanel.mStartOfDay) / 60 + 24;
	}
}
