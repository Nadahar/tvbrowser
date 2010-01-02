/*
 * ChannelGrabber.java
 * Copyright (C) 2006 Probum
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
 *     $Date: $
 *   $Author: $
 * $Revision: $
 */

package dvbplugin;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import dvbplugin.Settings.DVBViewerChannel;
import dvbplugin.dvbviewer.ChannelList;
import dvbplugin.dvbviewer.ProcessHandler;
import dvbplugin.dvbviewer.ChannelList.Channel;

import util.exc.ErrorHandler;
import util.ui.Localizer;

/**
 * @author Probum
 */
public class ChannelGrabber {

  private static final String PROPSHEADER = "Format: ChannelName = ServiceID | AudioID | TunerType";

  private static final String CHANNELS_DAT = "channels.dat";

  private static final String CHANNEL_PROPERTIES = ProcessHandler.DVBVIEWERPLUGIN_USER_PATH + File.separator
                                                       + "DVBPluginChannels.properties";


  /** Translator */
  private static final Localizer localizer = Localizer.getLocalizerFor(ChannelGrabber.class);

  /** the usual logger */
  private static Logger logger = Logger.getLogger(ChannelGrabber.class.getName());


  public static List<DVBViewerChannel> readChannels() {
    List<DVBViewerChannel> channels = new ArrayList<DVBViewerChannel>();
    try {
      ChannelList list = ChannelList.getChannelList(Settings.getSettings().getViewerTimersPath() + CHANNELS_DAT);
      for (Iterator<Channel> it = list.iterator(); it.hasNext();) {
        ChannelList.Channel ch = it.next();
        ChannelList.Tuner tuner = ch.getTuner();

        channels.add(new Settings.DVBViewerChannel(ch.getName(), String.valueOf(tuner.getSId()),
                                                   String.valueOf(tuner.getAudioPid()),
                                                   String.valueOf(tuner.getTunerType())));
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return channels;
  }


  public static Properties readChannelProperties() {
    Properties dvbChannels = new Properties();

    File props = new File(CHANNEL_PROPERTIES);
    InputStream is = null;
    try {
      // check for the channel list
      if (props.exists()) {
        // ok, load it
        is = new FileInputStream(props);
        dvbChannels.load(is);
      }
    } catch (IOException e) {
      ErrorHandler.handle(localizer.msg("error_read_channels",
                          "Unable to read the channels assignments from '{0}'",
                          props.getAbsoluteFile()), e);
    } finally {
      if (null != is) {
        try {
          is.close();
        } catch (IOException e) {
          // ok, at least we tried to
          logger.log(Level.SEVERE, "Could not close the file " + props.getAbsolutePath(), e);
        }
      }
    }

    return dvbChannels;
  }


  static void writeChannelProperties(String path) {
    if (!path.endsWith(File.separator)) {
      path += File.separator;
    }

    try {
      write(ChannelList.getChannelList(path + CHANNELS_DAT));
    } catch (Exception e) {
      ErrorHandler.handle(localizer.msg("error_channelsdat_path",
              "Cannot find file {0}{1}", path, CHANNELS_DAT), e);
    }
  }


  private static void write(ChannelList list) {
    BufferedOutputStream buffOut = null;
    Properties channels = new Properties();
    try {
      for (Iterator<Channel> it = list.iterator(); it.hasNext();) {
        ChannelList.Channel ch = it.next();
        ChannelList.Tuner tuner = ch.getTuner();

        StringBuilder builder = new StringBuilder(128);
        builder.append(tuner.getSId()).append('|');
        builder.append(tuner.getAudioPid()).append('|');
        builder.append(tuner.getTunerType() + 1);

        channels.setProperty(ch.getName(), builder.toString());
      }
      buffOut = new BufferedOutputStream(new FileOutputStream(CHANNEL_PROPERTIES));
      channels.store(buffOut, PROPSHEADER);
    } catch (IOException e) {
      ErrorHandler.handle(localizer.msg("error_channelproperties",
              "Could not write file {0}, Reason {1}", CHANNEL_PROPERTIES), e);
    }
    finally {
      if (null != buffOut) {
        try {
          buffOut.close();
        } catch (IOException e) {
          // at least we tried to close it
          logger.log(Level.SEVERE, "Could not close the file " + CHANNEL_PROPERTIES, e);
        }
      }
    }
  }
}
