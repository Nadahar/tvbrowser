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

package printplugin.settings;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import printplugin.util.IO;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;

public class DayProgramScheme extends Scheme {


  public DayProgramScheme(String name) {
    super(name);
  }

  public void store(ObjectOutputStream out) throws IOException {
    out.writeInt(1);  // version
    Date today = new Date();
    DayProgramPrinterSettings settings = (DayProgramPrinterSettings)getSettings();
    int day = settings.getFromDay().getNumberOfDaysSince(today);
    out.writeInt(day);
    out.writeInt(settings.getNumberOfDays());
    writeChannels(out, settings.getChannelList());
    out.writeInt(settings.getDayStartHour());
    out.writeInt(settings.getDayEndHour());
    out.writeInt(settings.getColumnCount());
    out.writeInt(settings.getChannelsPerColumn());
    IO.writeProgramIconSettings(settings.getProgramIconSettings(), out);

  }

  public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt();  // version
    int day = in.readInt();
    Date fromDay = new Date().addDays(day);
    int numberOfDays = in.readInt();
    Channel[] channelArr = readChannels(in);
    int dayStartHour = in.readInt();
    int dayEndHour = in.readInt();
    int colCount = in.readInt();
    int channelsPerColumn = in.readInt();
    ProgramIconSettings programItemSettings = IO.readProgramIconSettings(in);

    DayProgramPrinterSettings settings = new DayProgramPrinterSettings(fromDay, numberOfDays, channelArr, dayStartHour, dayEndHour, colCount, channelsPerColumn, programItemSettings, Plugin.getPluginManager().getFilterManager().getCurrentFilter());
    setSettings(settings);
  }


  private void writeChannels(ObjectOutputStream out, Channel[] channels) throws IOException {
    if (channels == null) {
      out.writeInt(-1);
    }
    else {
      out.writeInt(channels.length);
      for (Channel channel : channels) {
        out.writeObject(channel.getId());
      }
    }
  }

  private Channel[] readChannels(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int cnt = in.readInt();
    if (cnt < 0) {
      return null;
    }
    Channel[] subscribedChannels = Plugin.getPluginManager().getSubscribedChannels();

    ArrayList<Channel> list = new ArrayList<Channel>();
    for (int i=0; i<cnt; i++) {
      String channelId = (String)in.readObject();
      for (Channel subscribedChannel : subscribedChannels) {
        if (channelId.equals(subscribedChannel.getId())) {
          list.add(subscribedChannel);
          break;
        }
      }
    }
    Channel[] result = new Channel[list.size()];
    list.toArray(result);
    return result;
  }

   /*
  public void setSettings(DayProgramPrinterSettingsOLD settings) {
    mSettings = settings;
  }

  public DayProgramPrinterSettingsOLD getSettings() {
    return mSettings;
  }   */

}
