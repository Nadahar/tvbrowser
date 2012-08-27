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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import util.ui.persona.Persona;

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
	
	private ProgramLabel mSelectedProgram;

	public ProgramScrollPanel() {
		super();

		mOffset = TimelinePlugin.getInstance().getOffset();
		mSelectedProgram = null;

		initPanel();
		
		setWheelScrollingEnabled(false);
		addMouseWheelListener(this);
		setOpaque(false);
	}

	private void initPanel() {
		mProgramPanel = new ProgramPanel();
		addProgramList();
		setViewportView(mProgramPanel);

    JPanel leftUpper = new JPanel() {
      protected void paintComponent(Graphics g) {
        if(Persona.getInstance().getAccentColor() != null && Persona.getInstance().getHeaderImage() != null) {
          TimelinePlugin.paintComponentInternal(g,this);
        }
        else {
          super.paintComponent(g);
        }
      }
    };

    JPanel rightUpper = new JPanel() {
      protected void paintComponent(Graphics g) {
        if(Persona.getInstance().getAccentColor() != null && Persona.getInstance().getHeaderImage() != null) {
          TimelinePlugin.paintComponentInternal(g,this);
        }
        else {
          super.paintComponent(g);
        }
      }
    };
    
    setCorner(UPPER_LEFT_CORNER, leftUpper);
    setCorner(UPPER_RIGHT_CORNER, rightUpper);
		
		final TimeHeader th = new TimeHeader();
		th.setPreferredWidth(mProgramPanel.getPreferredSize().width);
		setColumnHeaderView(th);
		getColumnHeader().setOpaque(false);

		final ChannelHeader ch = new ChannelHeader(TimelinePlugin.getSettings()
				.getChannelHeight());
		ch.setPreferredHeight(mProgramPanel.getPreferredSize().height);
		setRowHeaderView(ch);
		getRowHeader().setOpaque(false);

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
		
    if(mSelectedProgram != null) {
      if(mSelectedProgram.containsProgram(program)) {
        mSelectedProgram = lbl;
        lbl.setSelected(true);
        getViewport().setViewPosition(lbl.getLocation());
      }
    }
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
	  mLabelHeight = TimelinePlugin.getSettings().getChannelHeight() + 1;
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
	
	void scrollToChannel(Channel channel) {
	  Channel[] shownChannelArr = ((ChannelHeader)getRowHeader().getComponent(0)).getChannels();
	  int rowHeight = ((ChannelHeader)getRowHeader().getComponent(0)).getRowHeight();
	  
    for (int row = 0; row < shownChannelArr.length; row++) {
      if (channel.equals(shownChannelArr[row])) {
        Point scrollPos = getViewport().getViewPosition();
        
        if (scrollPos != null) {
          int visibleRows = getViewport().getHeight() / rowHeight;
          
          scrollPos.y = (row - visibleRows/2) * rowHeight;
          if (scrollPos.y < 0) {
            scrollPos.y = 0;
          }
          int max = rowHeight * shownChannelArr.length;
          
          if (scrollPos.y > max) {
            scrollPos.y = max;
          }
          getViewport().setViewPosition(scrollPos);
        }
      }
    }
	}
	
	void selectProgram(Program prog) {
	  if(mSelectedProgram != null) {
	    mSelectedProgram.setSelected(false);
	    mSelectedProgram.repaint();
	  }
	  
	  if(prog != null) {
	    scrollToChannel(prog.getChannel());
	    
  	  for(int i = 0; i < mProgramPanel.getComponentCount(); i++) {
  	    if(mProgramPanel.getComponent(i) instanceof ProgramLabel) {
  	      if(((ProgramLabel)mProgramPanel.getComponent(i)).containsProgram(prog)) {
  	        mSelectedProgram = (ProgramLabel)mProgramPanel.getComponent(i);
  	        mSelectedProgram.setSelected(true);
  	        getViewport().setViewPosition(mSelectedProgram.getLocation());
  	        break;
  	      }
  	    }
  	  }
	  }
	}
}
