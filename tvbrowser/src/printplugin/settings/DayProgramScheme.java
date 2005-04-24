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

package printplugin.settings;

import devplugin.Date;
import devplugin.Channel;
import devplugin.Plugin;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.awt.print.PageFormat;
import java.util.ArrayList;

public class DayProgramScheme extends Scheme {


  private DayProgramPrinterSettings mSettings;

  public DayProgramScheme(String name) {
    super(name);
  }

  public void store(ObjectOutputStream out) throws IOException {
    out.writeInt(1);  // version
    Date today = new Date();
    int day = mSettings.getFromDay().getNumberOfDaysSince(today);
    out.writeInt(day);
    out.writeInt(mSettings.getNumberOfDays());
    writeChannels(out, mSettings.getChannelList());
    out.writeInt(mSettings.getDayStartHour());
    out.writeInt(mSettings.getDayEndHour());
    out.writeInt(mSettings.getColumnCount());
    out.writeInt(mSettings.getChannelsPerColumn());

  }

  public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt();  // version
    int day = in.readInt();
    Date fromDay = new Date().addDays(day);
    int numberOfDays = in.readInt();
    Channel[] channelArr = readChannels(in);
    int dayStartHour = in.readInt();
    int dayEndHour = in.readInt();
    PageFormat pageFormat = null;
    int colCount = in.readInt();
    int channelsPerColumn = in.readInt();

    DayProgramPrinterSettingsImpl settings = new DayProgramPrinterSettingsImpl(fromDay, numberOfDays, channelArr, dayStartHour, dayEndHour, pageFormat, colCount, channelsPerColumn);
    setSettings(settings);
  }


  private void writeChannels(ObjectOutputStream out, Channel[] channels) throws IOException {
    if (channels == null) {
      out.writeInt(-1);
    }
    else {
      out.writeInt(channels.length);
      for (int i=0; i<channels.length; i++) {
        out.writeObject(channels[i].getId());
      }
    }
  }

  private Channel[] readChannels(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int cnt = in.readInt();
    if (cnt < 0) {
      return null;
    }
    Channel[] subscribedChannels = Plugin.getPluginManager().getSubscribedChannels();

    ArrayList list = new ArrayList();
    for (int i=0; i<cnt; i++) {
      String channelId = (String)in.readObject();
      for (int chInx = 0; chInx<subscribedChannels.length; chInx++) {
        if (channelId.equals(subscribedChannels[chInx].getId())) {
          list.add(subscribedChannels[chInx]);
          break;
        }
      }
    }
    Channel[] result = new Channel[list.size()];
    list.toArray(result);
    return result;
  }


  public void setSettings(DayProgramPrinterSettings settings) {
    mSettings = settings;
  }

  public DayProgramPrinterSettings getSettings() {
    return mSettings;
  }

}
