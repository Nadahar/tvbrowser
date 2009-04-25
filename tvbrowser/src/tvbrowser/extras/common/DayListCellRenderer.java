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
 *     $Date: 2006-04-16 11:33:06 +0200 (So, 16 Apr 2006) $
 *   $Author: darras $
 * $Revision: 2177 $
 */

package tvbrowser.extras.common;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;


public class DayListCellRenderer extends DefaultListCellRenderer {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(DayListCellRenderer.class);

  public DayListCellRenderer() {
    super();
  }

  public static String getDayString(int dayOfWeek) {
    String str;
      switch (dayOfWeek) {
        case LimitationConfiguration.DAYLIMIT_DAILY:
          str = mLocalizer.msg("day.daily", "Daily");
          break;
        case LimitationConfiguration.DAYLIMIT_WEEKDAY:
          str = mLocalizer.msg("day.weekday", "weekday");
          break;
        case LimitationConfiguration.DAYLIMIT_WEEKEND:
          str = mLocalizer.msg("day.weekend", "weekend");
          break;
        case LimitationConfiguration.DAYLIMIT_MONDAY:
          str = mLocalizer.msg("day.monday", "monday");
          break;
        case LimitationConfiguration.DAYLIMIT_TUESDAY:
          str = mLocalizer.msg("day.tuesday", "tuesday");
          break;
        case LimitationConfiguration.DAYLIMIT_WEDNESDAY:
          str = mLocalizer.msg("day.wednesday", "wednesday");
          break;
        case LimitationConfiguration.DAYLIMIT_THURSDAY:
          str = mLocalizer.msg("day.thursday", "thursday");
          break;
        case LimitationConfiguration.DAYLIMIT_FRIDAY:
          str = mLocalizer.msg("day.friday", "friday");
          break;
        case LimitationConfiguration.DAYLIMIT_SATURDAY:
          str = mLocalizer.msg("day.saturday", "saturday");
          break;
        case LimitationConfiguration.DAYLIMIT_SUNDAY:
          str = mLocalizer.msg("day.sunday", "sunday");
          break;
        default:
          str = "<unknown>";
      }
    return str;
  }

  public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                boolean cellHasFocus) {
    JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

    if (value instanceof Integer) {
     label.setText(getDayString(((Integer) value).intValue()));

    }

    return label;
  }
}




