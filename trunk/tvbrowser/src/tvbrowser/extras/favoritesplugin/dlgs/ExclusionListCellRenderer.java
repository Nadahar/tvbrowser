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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.extras.favoritesplugin.dlgs;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import tvbrowser.extras.common.DayListCellRenderer;
import tvbrowser.extras.favoritesplugin.core.Exclusion;
import devplugin.Channel;

/**
 * A renderer class for rendering the exclusion list. 
 */
public class ExclusionListCellRenderer extends DefaultListCellRenderer {
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ExclusionListCellRenderer.class);
  
  private String createTimeMessage(int lowBnd, int upBnd, int dayOfWeek) {
    int mLow = lowBnd % 60;
    int hLow = lowBnd / 60;
    int mUp = upBnd % 60;
    int hUp = upBnd / 60;

    String lowTime = hLow + ":" + (mLow < 10 ? "0" : "") + mLow;
    String upTime = hUp + ":" + (mUp < 10 ? "0" : "") + mUp;

    if (dayOfWeek != Exclusion.DAYLIMIT_DAILY) {
      String dayStr = DayListCellRenderer.getDayString(dayOfWeek);
      if (lowBnd >= 0 && upBnd >= 0) {
        return mLocalizer.msg("datetimestring.between", "on {0} between {1} and {2}", dayStr, lowTime, upTime);
      } else if (lowBnd >= 0) {
        return mLocalizer.msg("datetimestring.after", "on {0} after {1}", dayStr, lowTime);
      } else if (upBnd >= 0) {
        return mLocalizer.msg("datetimestring.before", "on {0} after {1}", dayStr, upTime);
      } else {
        return mLocalizer.msg("datetimestring.on", "on {0}", dayStr);
      }
    } else {
      if (lowBnd >= 0 && upBnd >= 0) {
        return mLocalizer.msg("timestring.between", "on {0} between {1} and {2}", lowTime, upTime);
      } else if (lowBnd >= 0) {
        return mLocalizer.msg("timestring.after", "on {0} after {1}", lowTime);
      } else if (upBnd >= 0) {
        return mLocalizer.msg("timestring.before", "on {0} after {1}", upTime);
      } else {
        return null;
      }
    }
  }

  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
      boolean cellHasFocus) {

    JLabel defaultLabel = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    if (value instanceof Exclusion) {
      Exclusion excl = (Exclusion) value;

      String title = excl.getTitle();
      String topic = excl.getTopic();
      Channel channel = excl.getChannel();
      String timeMsg = createTimeMessage(excl.getTimeLowerBound(), excl.getTimeUpperBound(), excl.getDayOfWeek());

      String text;
      if (title == null) {
        if (topic == null) {
          if (channel == null) {
            if (timeMsg == null) {
              text = "<invalid>";
            } else { // timeMsg != null
              text = mLocalizer.msg("exclude.time", "", timeMsg);
            }
          } else { // channel != null
            if (timeMsg == null) {
              text = mLocalizer.msg("exclude.channel", "", channel.getName());
            } else { // timeMsg != null
              text = mLocalizer.msg("exclude.channel-time", "", channel.getName(), timeMsg);
            }
          }

        } else { // topic != null
          if (channel == null) {
            if (timeMsg == null) {
              text = mLocalizer.msg("exclude.topic", "", topic);
            } else { // timeMsg != null
              text = mLocalizer.msg("exclude.topic-time", "", topic, timeMsg);
            }
          } else { // channel != null
            if (timeMsg == null) {
              text = mLocalizer.msg("exclude.topic-channel", "", topic, channel.getName());
            } else { // timeMsg != null
              text = mLocalizer.msg("exclude.topic-channel-time", "", topic, channel.getName(), timeMsg);
            }
          }
        }

      } else { // title != null
        if (topic == null) {
          if (channel == null) {
            if (timeMsg == null) {
              text = mLocalizer.msg("exclude.title", "", title);
            } else { // timeMsg != null
              text = mLocalizer.msg("exclude.title-time", "", title, timeMsg);
            }
          } else { // channel != null
            if (timeMsg == null) {
              text = mLocalizer.msg("exclude.title-channel", "", title, channel.getName());
            } else {
              text = mLocalizer.msg("exclude.title-channel-time", "", title, channel.getName(), timeMsg);
            }
          }
        } else { // topic != null
          if (channel == null) {
            if (timeMsg == null) {
              text = mLocalizer.msg("exclude.title-topic", "", title, topic);
            } else { // timeMsg != null
              text = mLocalizer.msg("exclude.title-topic-time", "", title, topic, timeMsg);
            }
          } else { // channel != null
            if (timeMsg == null) {
              text = mLocalizer.msg("exclude.title-topic-channel", "", title, topic, channel.getName());
            } else { // timeMsg != null
              text = mLocalizer.msg("exclude.title-topic-channel-time", "", new Object[] { title, topic,
                  channel.getName(), timeMsg });
            }
          }
        }
      }
      defaultLabel.setText(text);

    }
    return defaultLabel;
  }
}
