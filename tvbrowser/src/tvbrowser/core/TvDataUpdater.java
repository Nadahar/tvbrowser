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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import org.apache.commons.lang.math.RandomUtils;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.core.plugin.PluginProxyManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import tvbrowser.core.tvdataservice.TvDataServiceProxyManager;
import tvbrowser.extras.common.InternalPluginProxyIf;
import tvbrowser.extras.common.InternalPluginProxyList;
import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import tvbrowser.ui.DontShowAgainOptionBox;
import tvbrowser.ui.mainframe.MainFrame;
import tvdataservice.MarkedProgramsList;
import tvdataservice.MutableChannelDayProgram;
import tvdataservice.TvDataUpdateManager;
import util.exc.ErrorHandler;
import util.io.NetworkUtilities;
import util.ui.EnhancedPanelBuilder;
import util.ui.Localizer;
import util.ui.UIThreadRunner;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.progress.ProgressMonitorGroup;
import devplugin.AfterDataUpdateInfoPanel;
import devplugin.AfterDataUpdateInfoPanel.AfterDataUpdateInfoPanelListener;
import devplugin.Channel;
import devplugin.ChannelDayProgram;
import devplugin.Date;
import devplugin.Program;
import devplugin.ProgressMonitor;

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
  private static final Logger mLog
    = Logger.getLogger(TvDataUpdater.class.getName());

  /** The singleton. */
  private static TvDataUpdater mSingleton;

  private boolean mIsDownloading;
  private boolean mIsUpdating;
  
  /** Set to true if Stop was forced */
  private boolean mStopDownloading = false;

  /**
   * set to true if some changed data was given to the TV database
   */
  private boolean mTvDataWasChanged = false;

  private ArrayList<TvDataUpdateListener> mListenerList;


  private TvDataUpdater() {
    mListenerList = new ArrayList<TvDataUpdateListener>();
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
  public void downloadTvData(int daysToDownload, TvDataServiceProxy[] services, final JProgressBar progressBar, final JLabel label) {
    if (! TvDataServiceProxyManager.getInstance().licensesAccepted(services)) {
      return;
    }

    // Set the download flag
    mIsDownloading = true;
    mStopDownloading = false;
    mTvDataWasChanged = false;
    mIsUpdating = true;
    
    // Inform the listeners
    fireTvDataUpdateStarted();

    // Add two day to the daysToDownload for yesterday and today
    daysToDownload += 2;

    // Ensure that the tvdata directory exists
    File tvdataDir = new File(Settings.propTVDataDirectory.getString());
    if (! tvdataDir.exists()) {
      tvdataDir.mkdir();
    }

    // Create a interactor that translates the database orders
    TvDataUpdateManager updateManager = new TvDataUpdateManager() {

      private boolean mMessageShown = false;

      public void updateDayProgram(MutableChannelDayProgram program) {
        mTvDataWasChanged = true;
        doUpdateDayProgram(program);
      }

      public boolean isDayProgramAvailable(Date date, Channel channel) {
        return TvDataBase.getInstance().isDayProgramAvailable(date, channel);
      }

      public boolean cancelDownload() {
        return mStopDownloading;
      }

      @Override
      public boolean checkConnection() {
        boolean result = NetworkUtilities.checkConnection();
        if (!result && !mMessageShown ) {
          mMessageShown = true;
          try {
            UIThreadRunner.invokeAndWait(new Runnable() {

              @Override
              public void run() {
                JOptionPane.showMessageDialog(null,
                    mLocalizer.msg("noConnectionMessage", "No connection!"),
                    mLocalizer.msg("noConnectionTitle", "No connection!"),
                    JOptionPane.ERROR_MESSAGE);
              }
            });
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
        return result;
      }
    };

    // Get the start date
    devplugin.Date startDate = new Date().addDays(-1);

    // Split the subsribed channels by data service
    Channel[] subscribedChannels = ChannelList.getSubscribedChannels();
    UpdateJob[] jobArr = toUpdateJobArr(subscribedChannels, services);

    // Create the ProgressMonitorGroup
    ProgressMonitorGroup monitorGroup
      = new ProgressMonitorGroup(progressBar, label, subscribedChannels.length + 1);

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

    checkLocalDateUsingNTP();

    ProgressMonitor monitor = monitorGroup.getNextProgressMonitor(subscribedChannels.length+1);
    monitor.setMessage(mLocalizer.msg("calculateEntries","Calculating new entries in the database"));
    TvDataBase.getInstance().reCalculateTvData(daysToDownload, monitor);
    TvDataBase.getInstance().updateTvDataBase();
    
    mIsUpdating = false;
    
    MarkedProgramsList.getInstance().revalidatePrograms();

    TvDataBase.getInstance().sendNewProgramsToTvDataListener();
    
    FavoritesPlugin.getInstance().waitForFinishingUpdateThreads();
    
    // Inform the listeners
    fireTvDataUpdateFinished();
    monitor.setMessage("");

    checkLocalTime();

    // reset flag to avoid unnecessary favorite updates
    mTvDataWasChanged = false;
    showInfoDialog();
  }
  
  private void showInfoDialog() {
    new Thread("After update info showing") {
      public void run() {
        EnhancedPanelBuilder pb = new EnhancedPanelBuilder("5dlu,min:grow,5dlu");
        final JPanel centerPanel = pb.getPanel();
        
        AfterDataUpdateInfoPanelListener infoPanelListener = new AfterDataUpdateInfoPanelListener() {
          @Override
          public void remove(AfterDataUpdateInfoPanel infoPanel) {
            for(int i = centerPanel.getComponentCount()-1; i >= 0; i--) {
              Component c = centerPanel.getComponent(i);
              
              if(c != null && c.equals(infoPanel)) {
                centerPanel.remove(i);
                centerPanel.remove(i-1);
                break;
              }
            }
            
            infoPanel.closed();
            centerPanel.updateUI();
          }
        };

        CellConstraints cc = new CellConstraints();
                
        InternalPluginProxyIf[] internalPlugins = InternalPluginProxyList.getInstance().getAvailableProxys();
        
        boolean showDialog = false;
        
        for(InternalPluginProxyIf internalPlugin : internalPlugins) {
          AfterDataUpdateInfoPanel test = internalPlugin.getAfterDataUpdateInfoPanel();
          
          if(test != null) {
            test.setAfterDataUpdateInfoPanelListener(infoPanelListener);
            pb.addParagraph(internalPlugin.getName());
            pb.addGrowingRow();
            pb.add(test, cc.xy(2, pb.getRowCount()));
            showDialog = true;
          }
        }
        
        PluginProxy[] pluginProxys = PluginProxyManager.getInstance().getActivatedPlugins();
        
        for(PluginProxy pluginProxy : pluginProxys) {
          AfterDataUpdateInfoPanel test = pluginProxy.getAfterDataUpdateInfoPanel();
          
          if(test != null) {
            test.setAfterDataUpdateInfoPanelListener(infoPanelListener);
            pb.addParagraph(pluginProxy.getInfo().getName());
            pb.addGrowingRow();
            pb.add(test, cc.xy(2, pb.getRowCount()));
            showDialog = true;
          }
        }
        
        if(showDialog) {
          final JDialog infoDialog = new JDialog(UiUtilities.getLastModalChildOf(MainFrame.getInstance()));
          infoDialog.setTitle(mLocalizer.msg("afterUpdateInfo","Informations of data update"));
          
          JScrollPane scrollPane = new JScrollPane(centerPanel);
          scrollPane.setBorder(BorderFactory.createEmptyBorder());
          
          JPanel center = new JPanel(new BorderLayout());
          center.setBorder(Borders.DIALOG_BORDER);
          center.add(scrollPane, BorderLayout.CENTER);
          
          JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
          close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              infoDialog.dispose();
            }
          });
          
          JPanel buttons = new JPanel(new FormLayout("min:grow,default,min:grow","5dlu,default,5dlu,default"));
          buttons.add(new JSeparator(JSeparator.HORIZONTAL), cc.xyw(1, 2, 3));
          buttons.add(close, cc.xy(2, 4));
          
          center.add(buttons, BorderLayout.SOUTH);
                    
          infoDialog.setContentPane(center);
          infoDialog.addWindowListener(new WindowAdapter() {            
            @Override
            public void windowClosing(WindowEvent e) {
              for(int i = 0; i < centerPanel.getComponentCount(); i++) {
                Component c = centerPanel.getComponent(i);
                
                if(c instanceof AfterDataUpdateInfoPanel) {
                  ((AfterDataUpdateInfoPanel)c).closed();
                }
              }
            }
          });

          WindowClosingIf closing = new WindowClosingIf() {
            @Override
            public JRootPane getRootPane() {
              return infoDialog.getRootPane();
            }
            
            @Override
            public void close() {
              infoDialog.dispose();
            }
          };
          
          UiUtilities.registerForClosing(closing);

          Settings.layoutWindow("AfterTvDataUpdateInfoDialog", infoDialog, new Dimension(900,500));
          infoDialog.setModal(true);
          infoDialog.setVisible(true);          
        }
      }
    }.start();
  }
  
  public boolean isUpdating() {
    return mIsUpdating;
  }

  private void checkLocalTime() {
    if (tvDataWasChanged()) {
      int count = 0;
      count += wrongTimeZone("Das Erste (ARD)", "Tagesschau", 20 * 60);
      count += wrongTimeZone("ZDF", "ZDF-Morgenmagazin", 5 * 60 + 30);
      count += wrongTimeZone("Sat.1", "Sat.1 Nachrichten", 20 * 60);
      count += wrongTimeZone("RTL2", "RTL II News", 20 * 60);
      // require at least 2 differences, so a program move on one channel does
      // not yet trigger the message
      if (count >= 2) {
        DontShowAgainOptionBox.showOptionDialog("wrongTimeZone", null,
            mLocalizer.msg("timezone", ""));
      }
    }
  }

  /**
   * Check if a known program is found with an offset of exactly 1 or 2 hours
   * for the known start time (for 3 days starting today). This is a sign for
   * incorrect time zone configuration.
   *
   * @param channelName
   * @param programTitle
   * @param minutesAfterMidnight
   * @return The return value is an integer instead of a boolean to make the
   *         counting of programs easier
   */
  private int wrongTimeZone(String channelName, String programTitle,
      int minutesAfterMidnight) {
    final Date currentDate = Date.getCurrentDate();
    for (Channel channel : ChannelList.getSubscribedChannels()) {
      if (channel.getDefaultName().equalsIgnoreCase(channelName)) {
        int wrongDays = 0;
        for (int days = 0; days < 3; days++) {
          ChannelDayProgram dayProgram = TvDataBase.getInstance()
              .getDayProgram(currentDate.addDays(days), channel);
          if (dayProgram != null) {
            Iterator<Program> it = dayProgram.getPrograms();
            if (it != null) {
              while (it.hasNext()) {
                Program program = it.next();
                if (program.getTitle().equals(programTitle)) {
                  int delta = Math.abs(program.getStartTime()
                      - minutesAfterMidnight);
                  if (delta == 60 || delta == 120) {
                    wrongDays++;
                    break;
                  }
                }
              }
            }
          }
        }
        if (wrongDays >= 3) {
          return 1;
        } else {
          return 0;
        }
      }
    }
    return 0;
  }


  private void checkLocalDateUsingNTP() {
    if (!tvDataWasChanged() && Settings.propNTPTimeCheck.getBoolean()) {
      if (Settings.propLastNTPCheck.getDate() == null || Settings.propLastNTPCheck.getDate().compareTo(Date.getCurrentDate()) < 0) {
        Settings.propLastNTPCheck.setDate(Date.getCurrentDate());
        int serverNum = RandomUtils.nextInt(4);
        int differenceSecs = NetworkUtilities
            .getTimeDifferenceSeconds(Integer.toString(serverNum) + ".tvbrowser.pool.ntp.org");
        if (Math.abs(differenceSecs) >= 86400) {
          DateFormat dateFormat = DateFormat.getDateTimeInstance();
          Calendar date = Calendar.getInstance();
          String localTime = dateFormat.format(date.getTime());
          date.add(Calendar.SECOND, differenceSecs);
          String internetTime = dateFormat.format(date.getTime());
          JOptionPane
              .showMessageDialog(
                  null,
                  mLocalizer
                      .msg(
                          "downloadFailed",
                          "TV-Browser could not download any data. A check with an internet time server showed that your local computer time differs more than a day from the official time.\n\nPlease check the date and time settings of your computer.\n\nYour date and time: {0}\nInternet date and time: {1}",
                          localTime, internetTime), Localizer.getLocalization(Localizer.I18N_ERROR),
                  JOptionPane.WARNING_MESSAGE);
        }
      }
    }
  }


  void fireTvDataUpdateStarted() {
    synchronized(mListenerList) {
      for (int i = 0; i < mListenerList.size(); i++) {
        TvDataUpdateListener lst = mListenerList.get(i);
        try {
          lst.tvDataUpdateStarted();
        } catch(Throwable thr) {
          mLog.log(Level.WARNING, "Firing event 'TV data update started' failed", thr);
        }
      }
    }
  }


  void fireTvDataUpdateFinished() {
    synchronized(mListenerList) {
      for (int i = 0; i < mListenerList.size(); i++) {
        TvDataUpdateListener lst = mListenerList.get(i);
        try {
          lst.tvDataUpdateFinished();
        } catch(Throwable thr) {
          mLog.log(Level.WARNING, "Firing event 'TV data update finished' failed", thr);
        }
      }

    }
  }


  /**
   * Stops the current download.
   */
  public void stopDownload() {
    mStopDownloading = true;
  }


  private void doUpdateDayProgram(MutableChannelDayProgram program) {

    // Pass the new ChannelDayProgram to the data base
    TvDataBase.getInstance().setDayProgram(program);
  }

  private UpdateJob[] toUpdateJobArr(Channel[] subscribedChannels, TvDataServiceProxy[] services) {

    ArrayList<UpdateJob> jobList = new ArrayList<UpdateJob>();
    for (Channel channel : subscribedChannels) {
      // Get the UpdateJob for this channel
      UpdateJob job = null;
      for (int i = 0; i < jobList.size(); i++) {
        UpdateJob currJob = jobList.get(i);
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
          for (TvDataServiceProxy service2 : services) {
            if (service2.getId().equals(service.getId())) {
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


  private static class UpdateJob {

    private TvDataServiceProxy mTvDataServiceProxy;
    private ArrayList<Channel> mChannelList;


    public UpdateJob(TvDataServiceProxy dataService) {
      mTvDataServiceProxy = dataService;
      mChannelList = new ArrayList<Channel>();
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

  /**
   * check whether the last TV data update changed some data in the TV database
   * @return <code>true</code>, if data was changed during last update
   * @since 2.6
   */
  public boolean tvDataWasChanged() {
    return mTvDataWasChanged;
  }

}
