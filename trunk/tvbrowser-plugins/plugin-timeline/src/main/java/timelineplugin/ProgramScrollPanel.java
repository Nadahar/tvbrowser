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

public class ProgramScrollPanel extends JScrollPane implements MouseWheelListener
{
	private static final long serialVersionUID = 1L;

	private ProgramPanel mProgramPanel;
	private int mOffset;

	public ProgramScrollPanel()
	{
		super();

		mOffset = TimelinePlugin.getInstance().getOffset();

		initPanel();

		setBackground(Color.WHITE);
		setWheelScrollingEnabled(false);
		addMouseWheelListener(this);
	}

	private void initPanel()
	{
		mProgramPanel = new ProgramPanel();
		addProgramList();
		setViewportView(mProgramPanel);

		TimeHeader th = new TimeHeader();
		th.setPreferredWidth(mProgramPanel.getPreferredSize().width);
		setColumnHeaderView(th);

		ChannelHeader ch = new ChannelHeader(TimelinePlugin.getSettings()
        .getChannelHeight());
		ch.setPreferredHeight(mProgramPanel.getPreferredSize().height);
		setRowHeaderView(ch);

		getVerticalScrollBar().setUnitIncrement(
        TimelinePlugin.getSettings().getChannelHeight());
    getHorizontalScrollBar().setUnitIncrement(
        TimelinePlugin.getSettings().getHourWidth() / 4);
	}

	private void addProgramList()
	{
		mProgramPanel.removeAll();

		int channelTop = 0;
		Channel[] channels = Plugin.getPluginManager().getSubscribedChannels();
		Date choosenDay = TimelinePlugin.getInstance().getChoosenDate();
		int delta = 24 * TimelinePlugin.getSettings().getHourWidth();
		double deltaHours = mOffset / TimelinePlugin.getInstance().getSizePerMinute() / 60;
		int deltaDay = (int) Math.round(deltaHours / 24) + 1;

		ProgramFilter filter = TimelinePlugin.getInstance().getFilter();

		for (int i = 0; i < channels.length; i++)
		{
			channelTop = i * TimelinePlugin.getSettings().getChannelHeight();

			for (int j = deltaDay * -1; j <= deltaDay; j++)
			{
				Date dayToShow = choosenDay.addDays(j);
				Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(dayToShow, channels[i]);
				while ((it != null) && (it.hasNext()))
				{
					Program p = (Program) it.next();
					if (filter.accept(p))
					{
						addProgram(p, channelTop, delta * (j + p.getDate().compareTo(dayToShow)));
					}
				}
			}
		}
		JLabel dummy = new JLabel("");
		dummy.setBounds(0, 0, 1, 1);
		mProgramPanel.add(dummy);
		mProgramPanel.repaint();
	}

	private void addProgram(Program p, int channelTop, int delta)
	{
		int x = mOffset + (int) Math.round(p.getStartTime() * TimelinePlugin.getInstance().getSizePerMinute());
		int w = mOffset + (int) Math.round((p.getStartTime() + p.getLength()) * TimelinePlugin.getInstance().getSizePerMinute());
		w = Math.abs(w - x);

		ProgramLabel lbl = new ProgramLabel();
		lbl.setBounds(x + delta, channelTop, w + 1, TimelinePlugin.getSettings()
        .getChannelHeight() + 1);
		lbl.setProgram(p);
		mProgramPanel.add(lbl);
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
		if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL)
		{
			if ((e.getModifiersEx() & MouseWheelEvent.SHIFT_DOWN_MASK) == 0)
			{
				int amount = e.getUnitsToScroll() * getHorizontalScrollBar().getUnitIncrement();
				getHorizontalScrollBar().setValue(getHorizontalScrollBar().getValue() + amount);
			}
			else
			{
				int amount = e.getUnitsToScroll() * getVerticalScrollBar().getUnitIncrement();
				getVerticalScrollBar().setValue(getVerticalScrollBar().getValue() + amount);
			}
		}
	}

	public void updateProgram()
	{
		addProgramList();
	}

	public void gotoTime(int minute)
	{
		gotoTime(minute, 0);
	}

	public void gotoTime(int minute, int delta)
	{
		JScrollBar sb = getHorizontalScrollBar();

		int value = (int) Math.round(((sb.getMaximum() - 2 * mOffset) / (24.0 * 60.0)) * minute + mOffset - (sb.getSize().width / 2));
		sb.setValue(value + delta);
	}

	public void addTime(int minute)
	{
		JScrollBar sb = getHorizontalScrollBar();

		int value = (int) Math.round(((sb.getMaximum() - 2 * mOffset) / (24.0 * 60.0)) * minute);
		sb.setValue(sb.getValue() + value);
	}

	//	public int getMinutes()
	//	{
	//		JScrollBar sb = getHorizontalScrollBar();
	//		
	//		//return (int) Math.round(24 * 60 * ((double) (sb.getValue() - mOffset) / (double) (sb.getMaximum() - 2 * mOffset)));
	//		//return (int) Math.round((double) ((sb.getValue() - mOffset + (sb.getSize().width / 2)) * 24 * 60) / (double) (sb.getMaximum() - 2 + mOffset));		
	//		return (int) Math.round(((double) (sb.getValue() - mOffset + (sb.getSize().width / 2)) / (double) mSizeHour) * 60);
	//	}

	public void update()
	{
		mProgramPanel.repaint();
	}

	public void resize()
	{
		mOffset = TimelinePlugin.getInstance().getOffset();
		mProgramPanel.resize();
		TimeHeader th = new TimeHeader();
		th.setPreferredWidth(mProgramPanel.getPreferredSize().width);
		setColumnHeaderView(th);
		ChannelHeader ch = new ChannelHeader(TimelinePlugin.getSettings()
        .getChannelHeight());
		ch.setPreferredHeight(mProgramPanel.getPreferredSize().height);
		setRowHeaderView(ch);
		addProgramList();
	}
}
