/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */

package printplugin.dlgs.printdayprogramsdialog;

import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import printplugin.dlgs.components.ChannelSelectionPanel;
import printplugin.dlgs.components.DateRangePanel;
import printplugin.dlgs.components.FilterSelectionPanel;
import printplugin.dlgs.components.TimeRangePanel;

import com.jgoodies.forms.factories.Borders;

import devplugin.Channel;
import devplugin.Date;
import devplugin.ProgramFilter;


public class ListingsTab extends JPanel {

  private DateRangePanel mDatePanel;
  private TimeRangePanel mTimePanel;
  private ChannelSelectionPanel mChannelPanel;
  private FilterSelectionPanel mFilterPanel;

    public ListingsTab(Frame parentFrame) {
      super();
      setLayout(new BorderLayout());
      setBorder(Borders.DIALOG_BORDER);

      JPanel content = new JPanel();
      content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
      mDatePanel = new DateRangePanel();
      mTimePanel = new TimeRangePanel();
      mChannelPanel = new ChannelSelectionPanel(parentFrame, new Channel[]{});
      mFilterPanel = new FilterSelectionPanel();

      content.add(mDatePanel);
      content.add(mTimePanel);
      content.add(mChannelPanel);
      content.add(mFilterPanel);

      add(content, BorderLayout.NORTH);
    }

  public void setChannels(Channel[] channelArr) {
    mChannelPanel.setChannels(channelArr);
  }

  public Channel[] getChannels() {
    return mChannelPanel.getChannels();
  }

  public void setTimeRange(int from, int to) {
    mTimePanel.setRange(from, to);
  }

  public int getFromTime() {
    return mTimePanel.getFromTime();
  }

  public int getToTime() {
    return mTimePanel.getToTime();
  }

  public int getNumberOfDays() {
    return mDatePanel.getNumberOfDays();
  }

  public void setDateFrom(Date from) {
   mDatePanel.setFromDate(from);
  }

  public Date getDateFrom() {
    return mDatePanel.getFromDate();
  }

  public void setDayCount(int cnt) {
    mDatePanel.setNumberOfDays(cnt);
  }
  
  public ProgramFilter getSelectedFilter() {
    return mFilterPanel.getSelectedFilter();
  }

  }
