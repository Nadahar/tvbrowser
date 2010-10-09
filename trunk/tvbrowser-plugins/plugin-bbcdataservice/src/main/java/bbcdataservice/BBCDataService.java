/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package bbcdataservice;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.TimeZone;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.SettingsPanel;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import devplugin.AbstractTvDataService;
import devplugin.Channel;
import devplugin.ChannelGroup;
import devplugin.ChannelGroupImpl;
import devplugin.Date;
import devplugin.PluginInfo;
import devplugin.ProgressMonitor;
import devplugin.Version;

/**
 * @author bananeweizen
 *
 */
public final class BBCDataService extends AbstractTvDataService {

  private static final boolean IS_STABLE = false;
  private static final Version mVersion = new Version(2, 70, 0, IS_STABLE);

  /**
   * created lazily on first access
   */
  private PluginInfo mPluginInfo = null;
  private File mWorkingDir;
  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(BBCDataService.class);

  private static ChannelGroup CHANNEL_GROUP = new ChannelGroupImpl("bbc programmes", mLocalizer.msg("group.name",
      "BBC programmes"), mLocalizer.msg("group.description", "BBC programmes data"), mLocalizer.msg("group.provider",
      "BBC programmes"));

  public static Version getVersion() {
    return mVersion;
  }

  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      final String name = mLocalizer.msg("name", "BBC Data Service");
      final String desc = mLocalizer.msg("description", "Loads BBC program data.");
      mPluginInfo = new PluginInfo(BBCDataService.class, name, desc, "Michael Keppler");
    }

    return mPluginInfo;
  }

  public ChannelGroup[] checkForAvailableChannelGroups(ProgressMonitor arg0) throws TvBrowserException {
    return getAvailableGroups();
  }

  public Channel[] checkForAvailableChannels(ChannelGroup arg0, ProgressMonitor arg1) throws TvBrowserException {
    return getAvailableChannels();
  }

  public Channel[] getAvailableChannels(ChannelGroup arg0) {
    return new Channel[] { new Channel(this, "BBC One London", "bbc one london", TimeZone.getTimeZone("GMT"), "gb",
        "(c) BBC", "http://www.bbc.co.uk/bbcone/programmes/schedules/london", CHANNEL_GROUP, null, Channel.CATEGORY_TV) };
  }

  public ChannelGroup[] getAvailableGroups() {
    return new ChannelGroup[] { CHANNEL_GROUP };
  }

  public SettingsPanel getSettingsPanel() {
    return null;
  }

  public boolean hasSettingsPanel() {
    return false;
  }

  public void loadSettings(Properties arg0) {
    // TODO Auto-generated method stub

  }

  public void setWorkingDirectory(File dir) {
    mWorkingDir = dir;
  }

  public Properties storeSettings() {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean supportsDynamicChannelGroups() {
    return false;
  }

  public boolean supportsDynamicChannelList() {
    return true;
  }

  public void updateTvData(final TvDataUpdateManager updateManager, final Channel[] channels, final Date startDate,
      final int days, final ProgressMonitor monitor) throws TvBrowserException {
    // // Check for connection
    // if (!updateManager.checkConnection()) {
    // return;
    // }
    monitor.setMessage(mLocalizer.msg("update", "Updating BBC data"));
    monitor.setMaximum(channels.length);
    int progress = 0;
    for (Channel channel : channels) {
      HashMap<Date, MutableChannelDayProgram> dayPrograms = new HashMap<Date, MutableChannelDayProgram>();
      monitor.setValue(progress++);
      for (int i = 0; i < days; i++) {
        Date date = startDate.addDays(i);
        String year = String.valueOf(date.getYear());
        String month = String.valueOf(date.getMonth());
        String day = String.valueOf(date.getDayOfMonth());
        String schedulePath = "/" + year + "/" + month + "/" + day + ".xml";
        String url = channel.getWebpage() + schedulePath;
        File file = new File(mWorkingDir, "bbc.xml");
        try {
          IOUtilities.download(new URL(url), file);
        } catch (MalformedURLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        boolean continueWithNextDay = false;
        try {
          continueWithNextDay = BBCProgrammesParser.parse(dayPrograms, file, channel, date);
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        file.delete();
        if (!continueWithNextDay) {
          break;
        }
      }
      // store the received programs
      for (MutableChannelDayProgram dayProgram : dayPrograms.values()) {
        updateManager.updateDayProgram(dayProgram);
      }
    }
  }

}
