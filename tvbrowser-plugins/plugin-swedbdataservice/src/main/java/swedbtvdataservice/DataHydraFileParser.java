package swedbtvdataservice;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Logger;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.TvDataUpdateManager;
import util.exc.TvBrowserException;
import util.io.IOUtilities;
import util.ui.Localizer;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.ProgressMonitor;

class DataHydraFileParser {

  private static final Localizer mLocalizer = Localizer.getLocalizerFor(DataHydraFileParser.class);
  private static final Logger mLog = Logger.getLogger(DataHydraFileParser.class.getName());

  protected void loadDataForChannel(SweDBTvDataService service, TvDataUpdateManager updateManager, Date startDate, int dateCount, ProgressMonitor monitor, Date testStart, DataHydraChannelContainer internalChannel, Channel channel) throws TvBrowserException {
    ArrayList<Date> modifiedDates = new ArrayList<Date>();
    monitor.setMessage(mLocalizer.msg("updateTvData.progressmessage.10",
            "{2}: Searching for updated/new programs on {0} for {1} days",
            startDate.toString(), dateCount, channel.getName()));
    
    for (int b = 0; b < dateCount; b++) {
      Date testDay = testStart.addDays(b);
      String fileDate = createFileName(testDay);
      try {
        String urlString = internalChannel.getBaseUrl() + internalChannel.getId() + "_" + fileDate + ".xml.gz";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(Plugin.getPluginManager().getTvBrowserSettings().getDefaultNetworkConnectionTimeout());
        conn.setIfModifiedSince(internalChannel.getLastUpdate(testDay));
        conn.setRequestMethod("HEAD"); // Only make a HEAD request, to
        // see if the file has been
        // changed (only get the HTTP
        // Header fields).
        if (conn.getResponseCode() == 200) {
          modifiedDates.add(testDay);
        }
        mLog.info("mInternalChannel.lastModified=" + internalChannel.getLastUpdate(testDay));
      } catch (Exception e) {
        throw new TvBrowserException(SweDBTvDataService.class,
                "An error occurred in updateTvData",
                "Please report this to the developer", e);
      }
    } // int b
    mLog.info("Number of modified days for channel " + internalChannel.getName() + ":" + modifiedDates.size());
    monitor.setMessage(mLocalizer.msg("updateTvData.progressmessage.20",
            "{0}: Retrieving updated/new programs.", channel.getName()));

    /*********************************************************************
     * IF we found any modified/missing data, we have to download the
     * files before and after each date, since the XMLTV-data files are
     * split in UTC-time while provider is in a different timezone. This
     * procedure ensures that we get all of the data. (There is a small
     * risk that we miss updated data, since we do not verify if the files
     * before and after the actual date has been modified - but we do not
     * care for now)
     ********************************************************************/
    if (modifiedDates.size() > 0) {
      Date prevDate;
      Date currDate;

      Hashtable<String, Date> fileDates = new Hashtable<String, Date>();
      prevDate = new Date((modifiedDates.get(0)).addDays(-1));
      fileDates.put(prevDate.getDateString(), prevDate);
      for (Date modifiedDate : modifiedDates) {
        currDate = modifiedDate;
        if (currDate.equals(prevDate.addDays(1))) {
          if (!fileDates.containsKey(currDate.getDateString())) {
            fileDates.put(currDate.getDateString(), currDate);
          }
        } else {
          Date tempDate = new Date(prevDate.addDays(1));
          if (!fileDates.containsKey(tempDate.getDateString())) {
            fileDates.put(tempDate.getDateString(), tempDate);
          }
          tempDate = new Date(currDate.addDays(-1));
          if (!fileDates.containsKey(tempDate.getDateString())) {
            fileDates.put(tempDate.getDateString(), tempDate);
          }
          if (!fileDates.containsKey(currDate.getDateString())) {
            fileDates.put(currDate.getDateString(), currDate);
          }

        }
        prevDate = currDate;
      }// for j
      currDate = new Date(prevDate.addDays(1));
      if (!fileDates.containsKey(currDate.getDateString())) {
        fileDates.put(currDate.getDateString(), currDate);
      }
      mLog.info(currDate.getDateString());

      /*******************************************************************
       * OK... So now we are ready to start parsing the selected data
       * files
       ******************************************************************/
      Hashtable<String, MutableChannelDayProgram> dataHashtable = new Hashtable<String, MutableChannelDayProgram>();
      Enumeration<Date> en = fileDates.elements();
      monitor.setMessage(mLocalizer.msg(
              "updateTvData.progressmessage.30", "{0}: Reading datafiles",
              channel.getName()));
      while (en.hasMoreElements()) {
        try {
          Date date = (en.nextElement());
          String strFileDate = createFileName(date);
          mLog.info("getting: " + internalChannel.getBaseUrl()
                  + internalChannel.getId() + "_" + strFileDate
                  + ".xml.gz");
          URL url = new URL(internalChannel.getBaseUrl()
                  + internalChannel.getId() + "_" + strFileDate
                  + ".xml.gz");
          HttpURLConnection con = (HttpURLConnection) url
                  .openConnection();
          con.setReadTimeout(Plugin.getPluginManager()
                  .getTvBrowserSettings()
                  .getDefaultNetworkConnectionTimeout());

          if (con.getResponseCode() == 200) {
            DataHydraDayParser.parseNew(IOUtilities.openSaveGZipInputStream(con
                    .getInputStream()), channel, date, dataHashtable, service);
            if (modifiedDates.contains(date)) {
              mLog.info("Updating lastUpdate property for date "
                      + date.toString());
              internalChannel.setLastUpdate(date, con
                      .getLastModified());
            }
          }
        } catch (Exception e) {
          throw new TvBrowserException(SweDBTvDataService.class,
                  "An error occurred in updateTvData",
                  "Please report this to the developer", e);
        }
      }
      mLog.info("All of the files have been parsed");
      /*******************************************************************
       * Now all of the files has been parsed. Time to update the local
       * database with our data...
       */
      for (Date date : modifiedDates) {
        if (dataHashtable.containsKey(date.toString())) {
          mLog.info("Updating database for day " + date.toString());
          monitor.setMessage(mLocalizer.msg(
                  "updateTvData.progressmessage.40",
                  "{0}: Updating database", channel.getName()));
          updateManager.updateDayProgram(dataHashtable.get(date
                  .toString()));
        } else {
          mLog.info("Strange.... Didn't find the data for "
                  + date.toString());
        }
      }
    }
  }

  private String createFileName(devplugin.Date fileDate) {
    String fileName = "";
    fileName = Integer.toString(fileDate.getYear()) + "-";
    // fileName = fileName + "-";
    if (fileDate.getMonth() < 10) {
      fileName = fileName + "0";
    }
    fileName = fileName + Integer.toString(fileDate.getMonth()) + "-";
    // datum = datum + "-";

    if (fileDate.getDayOfMonth() < 10) {
      fileName = fileName + "0";
    }
    fileName = fileName + Integer.toString(fileDate.getDayOfMonth());
    return fileName;
  }


}
