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
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.settings.channel;

import java.util.ArrayList;

import devplugin.Channel;
import devplugin.ChannelGroup;

/**
 * Utils for Channel Handling
 */
public class ChannelUtil {
  /** Translation */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ChannelUtil.class);

  /**
   * Returns a Text representation for a Channel Category
   * @param category Category to convert
   * @return Text for Category
   */
  public static String getNameForCategories(int category) {
    if ((category & Channel.CATEGORY_CINEMA) > 0) {
      return mLocalizer.msg("categoryCinema", "Kino");
    } else if ((category & Channel.CATEGORY_EVENTS) > 0) {
      return mLocalizer.msg("categoryEvents", "Events");
    } else if ((category & Channel.CATEGORY_RADIO) > 0) {
      return mLocalizer.msg("categoryRadio", "Radio");
    } else if ((category & Channel.CATEGORY_TV) > 0) {

      ArrayList<String> categories = new ArrayList<String>();

      if ((category & Channel.CATEGORY_DIGITAL) > 0) {
        categories.add(mLocalizer.msg("categoryDigital", "Digitale"));
      }

      if ((category & Channel.CATEGORY_SPECIAL_MUSIC) > 0) {
        categories.add(mLocalizer.msg("categoryMusic", "Musik"));
      } else if ((category & Channel.CATEGORY_SPECIAL_NEWS) > 0) {
        categories.add(mLocalizer.msg("categoryNews", "Nachrichten"));
      } else if ((category & Channel.CATEGORY_SPECIAL_SPORT) > 0) {
        categories.add(mLocalizer.msg("categorySport", "Sport"));
      } else if ((category & Channel.CATEGORY_SPECIAL_OTHER) > 0) {
        categories.add(mLocalizer.msg("categoryOthers", "Sonstige Sparten"));
      }

      StringBuilder buf = new StringBuilder(mLocalizer.msg("categoryTV", "TV"));

      if (categories.size() > 0) {
        buf.append(" (");

        for (int i = 0; i < categories.size(); i++) {
          buf.append(categories.get(i));
          if (i < categories.size() - 1) {
            buf.append(", ");
          }
        }

        buf.append(')');
      }

      return buf.toString();
    } else {
      return mLocalizer.msg("categoryNone", "Not categorized");
    }

  }
 
  /**
   * Get the Providername of a Channel
   * @param ch Channel
   * @return Name of Provider
   */
  public static String getProviderName(Channel ch) {
    ChannelGroup group = ch.getGroup();
    if (group == null) {
      return ch.getDataServiceProxy().getInfo().getName();
    }

    return group.getProviderName();
  }
}