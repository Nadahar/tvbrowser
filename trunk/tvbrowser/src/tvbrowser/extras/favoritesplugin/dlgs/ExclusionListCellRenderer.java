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
import devplugin.ProgramFilter;

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
      String episodeTitle = excl.getEpisodeTitle();
      ProgramFilter filter = excl.getFilter();
      Channel channel = excl.getChannel();
      String timeMsg = createTimeMessage(excl.getTimeLowerBound(), excl.getTimeUpperBound(), excl.getDayOfWeek());
      
      StringBuilder textValue = new StringBuilder();
      
      if(title != null) {
        textValue.append(mLocalizer.msg("exclude.title","Exclude all programs with title '")).append(title).append("'");
      }
      if(topic != null && title != null) {
        textValue.append(" ").append(mLocalizer.msg("exclude.appendTopic","with topic '")).append(topic).append("'");
      }
      else if (topic != null) {
        textValue.append(mLocalizer.msg("exclude.topic","Exclude all programs with topic '")).append(topic).append("'");
      }
      if(episodeTitle != null && topic != null && title != null) {
        textValue.append(" ").append(mLocalizer.msg("exclude.appendEpisodeTitle","Exclude all programs with episode '")).append(episodeTitle).append("'");
      }
      else if (episodeTitle != null) {
        textValue.append(mLocalizer.msg("exclude.episodeTitle","Exclude all programs with topic '")).append(episodeTitle).append("'");
      }      
      if(filter != null && (title != null || topic != null || episodeTitle != null)) {
        textValue.append(" ").append(mLocalizer.msg("exclude.appendFilter","of the filter '")).append(filter.getName()).append("'");
      }
      else if(filter != null) {
        textValue.append(mLocalizer.msg("exclude.filter","Exclude all programs of the filter '")).append(filter.getName()).append("'");
      }
      if(channel != null && (title != null || topic != null || episodeTitle != null || filter != null)) {
        textValue.append(" ").append(mLocalizer.msg("exclude.appendChannel","on channel '")).append(channel.getName()).append("'");
      }
      else if(channel != null) {
        textValue.append(mLocalizer.msg("exclude.channel","Exclude all programs on channel '")).append(channel.getName()).append("'");
      }
      if(timeMsg != null && (title != null || topic != null || episodeTitle != null || filter != null || channel != null)) {
        textValue.append(" ").append(timeMsg);
      }
      else if(timeMsg != null) {
        textValue.append(mLocalizer.msg("exclude.time","Exclude all programs ")).append(timeMsg);
      }
      
      if(textValue.length() < 1) {
        textValue.append(mLocalizer.msg("exclude.invalid","<invalid>"));
      }
      else {
        if(mLocalizer.msg("exclude.appendix",".").length() > 1) {
          textValue.append(" ");
        }
        
        textValue.append(mLocalizer.msg("exclude.appendix","."));
      }
      
      defaultLabel.setText(textValue.toString());

    }
    return defaultLabel;
  }
}
