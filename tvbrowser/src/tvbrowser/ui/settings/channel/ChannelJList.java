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

import java.awt.event.MouseEvent;
import java.util.Locale;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import tvbrowser.TVBrowser;
import util.ui.Localizer;
import devplugin.Channel;

/**
 * A special List that shows details for a Channel as a Tooltip
 */
public class ChannelJList extends JList {
  /** Localizer */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannelJList.class);

  public ChannelJList() { }

  public ChannelJList(DefaultListModel model) {
    super(model);
  }

  public String getToolTipText(MouseEvent evt) {
    // Get item index
    int index = locationToIndex(evt.getPoint());

    if (index < 0) {
      return null;
    }
    
    // Get item
    Object item = getModel().getElementAt(index);

    if (item instanceof Channel) {
      Channel channel = (Channel) item;

      StringBuilder buf = new StringBuilder();

      Locale loc = new Locale(Locale.getDefault().getLanguage(), channel.getCountry());
      buf.append("<html>");
      buf.append("<b>").append(Localizer.getLocalization(Localizer.I18N_CHANNEL)).append(" :</b> ").append(channel.getName()).append("<br>");
      if (!TVBrowser.isStable()) {
        buf.append("<b>ID (dev. only) :</b> ").append(channel.getUniqueId())
            .append("<br>");
      }
      buf.append("<b>").append(mLocalizer.msg("country", "Country")).append(" :</b> ").append(loc.getDisplayCountry()).append("<br>");
      buf.append("<b>").append(mLocalizer.msg("timezone", "Timezone")).append(" :</b> ").append(channel.getTimeZone().getDisplayName()).append("<br>");
      buf.append("<b>").append(mLocalizer.msg("category", "Category")).append(" :</b> ").append(ChannelUtil.getNameForCategories(channel.getCategories())).append("<br><br>");
      buf.append(mLocalizer.msg("provided", "provided by")).append("<br><center>").append(ChannelUtil.getProviderName(channel)).append("</center>");
      buf.append("</html>");
      return buf.toString();
    }

    // Return the tool tip text
    return null;
  }
 
}