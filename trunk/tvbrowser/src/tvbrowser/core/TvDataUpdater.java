/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package tvbrowser.core;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.TvDataUpdateManager;
import util.exc.ErrorHandler;
import util.ui.progress.ProgressMonitorGroup;
import devplugin.Channel;
import devplugin.Date;
import devplugin.ProgressMonitor;
import devplugin.ChannelGroup;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;

/**
 * Updates the TV data.
 * 
 * @author Til Schneider, www.murfman.de
 */
public class TvDataUpdater {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(TvDataUpdater.class);

  /** The logger for this class. */
  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(TvDataUpdater.class.getName());

  /** The singleton. */
  private static TvDataUpdater mSingleton;

  private boolean mIsDownloading;

  /** Set to true if Stop was forced */
  private boolean mStopDownloading = false;

  private ArrayList mListenerList;


  private TvDataUpdater() {
    mListenerList = new ArrayList();
  }


  public static TvDataUpdater getInstance() {
    if (mSingleton == null) {
      mSingleton = new TvDataUpdater();
    }

    return mSingleton;
  }


  public void addTvDataUpdateListener(TvDataUpdateListener listener) {
    synchronized(mListenerList) {
      mListenerList.add(listener);
    }
  }


  public void removeTvDataUpdateListener(TvDataUpdateListener listener) {
    synchronized(mListenerList) {
      mListenerList.remove(listener);
    }
  }


  /**
   * Gets whether the TV data updater is currently downloading data.
   *
   * @return <CODE>true</CODE>, if the TV data updater is currently downloading
   *         data.
   */
  public boolean isDownloading() {
    return mIsDownloading;
  }


  /**
   * Download the new TV data
   *
   * @param daysToDownload The number of days until today to download the
   *        program for.
   */
  public void downloadTvData(int daysToDownload, TvDataServiceProxy[] services, JProgressBar progressBar, JLabel label) {
    if (! TvDataServiceProxyManager.getInstance().licensesAccepted(services)) {
      return;
    }

    // Set the download flag
    mIsDownloading = true;
    mStopDownloading = false;

    // Inform the listeners
    fireTvDataUpdateStarted();

    // Add a day to the daysToDownload for today
    daysToDownload ++;

    // Ensure that the tvdata directory exists
    File tvdataDir = new File(Settings.propTVDataDirectory.getString());
    if (! tvdataDir.exists()) {
      tvdataDir.mkdir();
    }

    // Create a interactor that translates the database orders
    TvDataUpdateManager updateManager = new TvDataUpdateManager() {
      public void updateDayProgram(MutableChannelDayProgram program) {
        doUpdateDayProgram(program);
      }

      public boolean isDayProgramAvailable(Date date, Channel channel) {
        return TvDataBase.getInstance().isDayProgramAvailable(date, channel);
      }

      public boolean cancelDownload() {
        return mStopDownloading;
      }
    };

    // Get the start date
    devplugin.Date startDate = new Date();

    // Split the subsribed channels by data service
    Channel[] subscribedChannels = ChannelList.getSubscribedChannels();
    UpdateJob[] jobArr = toUpdateJobArr(subscribedChannels, services);

    // Create the ProgressMonitorGroup
    ProgressMonitorGroup monitorGroup
      = new ProgressMonitorGroup(progressBar, label, subscribedChannels.length);

    // Work on the job list
    Throwable downloadException = null;
    for (int i = 0; (i < jobArr.length) && (!mStopDownloading); i++) {
      TvDataServiceProxy dataService = jobArr[i].getDataService();
      Channel[] channelArr = jobArr[i].getChannelList();
      ProgressMonitor monitor = monitorGroup.getNextProgressMonitor(channelArr.length);
      try {
        dataService.updateTvData(updateManager, channelArr, startDate,
                                 daysToDownload, monitor);
      }
      catch (Throwable thr) {
        mLog.log(Level.WARNING, "Updating the TV data for TV data service "
          + dataService.getInfo().getName() + " failed", thr);

        downloadException = thr;
      }
    }

    // Show the exception if there was one
    if (downloadException != null) {
      String msg = mLocalizer.msg("error.1", "Couldn't download the whole program!");
      ErrorHandler.handle(msg, downloadException);
    }

    // Reset the download flag
    mIsDownloading = false;

    // Inform the listeners
    fireTvDataUpdateFinished();
  }


  void fireTvDataUpdateStarted() {
    synchronized(mListenerList) {
      for (int i = 0; i < mListenerList.size(); i++) {
        TvDataUpdateListener lst = (TvDataUpdateListener) mListenerList.get(i);
        try {
          lst.tvDataUpdateStarted();
        } catch(Throwable thr) {
          mLog.log(Level.WARNING, "Fireing event 'TV data update started' failed", thr);
        }
      }
    }
  }


  void fireTvDataUpdateFinished() {
    synchronized(mListenerList) {
      for (int i = 0; i < mListenerList.size(); i++) {
        TvDataUpdateListener lst = (TvDataUpdateListener) mListenerList.get(i);
        try {
          lst.tvDataUpdateFinished();
        } catch(Throwable thr) {
          mLog.log(Level.WARNING, "Fireing event 'TV data update finished' failed", thr);
        }
      }
    }
  }


  /**
   * Stopps the current download.
   */
  public void stopDownload() {
    mStopDownloading = true;
  }


  private void doUpdateDayProgram(MutableChannelDayProgram program) {
    correctChannelDayProgram(program, false);

    // Pass the new ChannelDayProgram to the data base
    TvDataBase.getInstance().setDayProgram(program);
  }


  /**
   * Corrects the ChannelDayProgram.
   * <p>
   * Checks whether all programs have a length. If not the length will be
   * calculated.
   * 
   * @param channelProg The program to correct
   */
  private void correctChannelDayProgram(MutableChannelDayProgram channelProg,
                                        boolean updateOnChange)
  {
    boolean somethingChanged = TvDataBase.calculateMissingLengths(channelProg);

    if (somethingChanged && updateOnChange) {
      // Pass the new ChannelDayProgram to the data base
      TvDataBase.getInstance().setDayProgram(channelProg);
    }
  }


  private UpdateJob[] toUpdateJobArr(Channel[] subscribedChannels, TvDataServiceProxy[] services) {

    ArrayList jobList = new ArrayList();
    for (int channelIdx = 0; channelIdx < subscribedChannels.length; channelIdx++) {
      Channel channel = subscribedChannels[channelIdx];

      // Get the UpdateJob for this channel
      UpdateJob job = null;
      for (int i = 0; i < jobList.size(); i++) {
        UpdateJob currJob = (UpdateJob) jobList.get(i);
        if (currJob.getDataService().getId().equals(channel.getDataServiceProxy().getId())) {
          job = currJob;
          break;
        }
      }
      if (job == null) {
        // There is no job fo this channel -> create one

          // check, if we can use this dataservice
          TvDataServiceProxy service = channel.getDataServiceProxy();
          boolean useService = false;
          for (int k = 0; k<services.length; k++) {
            if (services[k].getId().equals(service.getId())) {
              useService = true;
              break;
            }
          }
          if (!useService) {
            continue;
          }

        job = new UpdateJob(channel.getDataServiceProxy());
        jobList.add(job);
      }

      // Add the channel to the UpdateJob
      job.addChannel(channel);
    }

    // Convert the list into an array and return it
    UpdateJob[] jobArr = new UpdateJob[jobList.size()];
    jobList.toArray(jobArr);
    return jobArr;
  }


  // inner class UpdateJob


  private class UpdateJob {

    private TvDataServiceProxy mTvDataServiceProxy;
    private ArrayList mChannelList;


    public UpdateJob(TvDataServiceProxy dataService) {
      mTvDataServiceProxy = dataService;
      mChannelList = new ArrayList();
    }


    public void addChannel(Channel channel) {
      mChannelList.add(channel);
    }


    public TvDataServiceProxy getDataService() {
      return mTvDataServiceProxy;
    }


    public Channel[] getChannelList() {
      Channel[] channelArr = new Channel[mChannelList.size()];
      mChannelList.toArray(channelArr);
      return channelArr;
    }

  }

}
