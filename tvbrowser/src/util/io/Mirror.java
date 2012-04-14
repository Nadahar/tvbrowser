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
package util.io;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang.math.RandomUtils;

import util.exc.TvBrowserException;
import devplugin.Date;
import devplugin.ProgressMonitor;

/**
 *
 *
 * @author Til Schneider, www.murfman.de
 */
public class Mirror {
  /** The localizer for this class. */
  public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(Mirror.class);

  private static final int MAX_UP_TO_DATE_CHECKS = 10;
  private static final int MAX_LAST_UPDATE_DAYS = 5;

  /** The name extension of mirror files */
  public static final String MIRROR_LIST_FILE_NAME = "mirrorlist.gz";

  private static final Logger mLog = Logger.getLogger(Mirror.class.getName());
  /** The default weight of a mirror */
  public static final int DEFAULT_WEIGHT = 100;

  private String mUrl;

  private int mWeight;

  /** List of blocked Servers */
  private static ArrayList<String> BLOCKEDSERVERS = new ArrayList<String>();

  /** Mirror-Download Running?*/
  private static boolean mMirrorDownloadRunning = true;
  /** Exception on downloading in Thread */
  private static boolean mDownloadException = false;
  /** Data of Mirror-Download*/
  private static byte[] mMirrorDownloadData = null;

  /**
   * @param url
   * @param weight
   */
  public Mirror(String url, int weight) {
    // Escape spaces in the URL
    mUrl = IOUtilities.replace(url, " ", "%20");
    mWeight = weight;
  }

  /**
   * Creates an instance with the given URL
   * and the default weight for this mirror.
   *
   * @param url The URL of the mirror.
   */
  public Mirror(String url) {
    this(url, DEFAULT_WEIGHT);
  }

  /**
   * Gets the URL of this Mirror.
   *
   * @return The URL of this Mirror.
   */
  public String getUrl() {
    return mUrl;
  }

  /**
   * Gets the weight of this Mirror.
   *
   * @return The weight of this Mirror.
   */
  public int getWeight() {
    return mWeight;
  }

  /**
   * Sets the weight of this Mirror.
   *
   * @param weight The new weight of this Mirror.
   */
  public void setWeight(int weight) {
    mWeight = weight;
  }

  /**
   * Reads the mirrors from the given stream.
   *
   * @param stream
   *          The stream to read the mirrors from.
   * @return The mirror array read from the stream.
   * @throws IOException
   *           Thrown if something went wrong.
   * @throws FileFormatException
   *           Thrown if something went wrong.
   */
  private static Mirror[] readMirrorListFromStream(InputStream stream) throws IOException, FileFormatException {
    InputStream gIn = IOUtilities.openSaveGZipInputStream(stream);
    BufferedReader reader = new BufferedReader(new InputStreamReader(gIn));

    ArrayList<Mirror> list = new ArrayList<Mirror>();
    String line;
    int lineCount = 1;
    while ((line = reader.readLine()) != null) {
      line = line.trim();
      if (line.length() > 0) {
        // This is not an empty line -> read it

        StringTokenizer tokenizer = new StringTokenizer(line, ";");
        if (tokenizer.countTokens() < 2) {
          throw new FileFormatException("Syntax error in mirror file line " + lineCount + ": '" + line + "'");
        }

        String url = tokenizer.nextToken();
        String weightAsString = tokenizer.nextToken();
        int weight;
        try {
          weight = Integer.parseInt(weightAsString);
        } catch (Exception exc) {
          throw new FileFormatException("Syntax error in mirror file line " + lineCount + ": weight is not a number: '"
              + weightAsString + "'");
        }

        list.add(new Mirror(url, weight));
      }
      lineCount++;
    }

    gIn.close();

    Mirror[] mirrorArr = new Mirror[list.size()];
    list.toArray(mirrorArr);

    return mirrorArr;
  }

  /**
   * Reads the mirrors in the given file.
   *
   * @param file
   *          The file to read the mirrors from.
   * @return The mirror array read from the file.
   * @throws IOException
   *           Thrown if something went wrong.
   * @throws FileFormatException
   *           Thrown if something went wrong.
   */
  public static Mirror[] readMirrorListFromFile(File file) throws IOException, FileFormatException {
    BufferedInputStream stream = null;
    try {
      stream = new BufferedInputStream(new FileInputStream(file), 0x2000);

      return readMirrorListFromStream(stream);
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException exc) {
        }
      }
    }
  }

  /**
   * Write the mirror array to the given stream.
   *
   * @param stream The stream to write the mirror array to.
   * @param mirrorArr The mirror array to write.
   * @throws IOException Thrown if something went wrong.
   */
  private static void writeMirrorListToStream(OutputStream stream, Mirror[] mirrorArr) throws IOException {
    GZIPOutputStream gOut = new GZIPOutputStream(stream);

    PrintWriter writer = new PrintWriter(gOut);
    for (Mirror mirror : mirrorArr) {
      writer.print(mirror.getUrl());
      writer.print(";");
      writer.println(String.valueOf(mirror.getWeight()));
    }
    writer.close();

    gOut.close();
  }

  /**
   *
   *
   * @param file The file to write the mirror array to.
   * @param mirrorArr The mirror array to write.
   * @throws IOException Thrown if something went wrong.
   */
  public static void writeMirrorListToFile(File file, Mirror[] mirrorArr) throws IOException {
    // NOTE: We need two try blocks to ensure that the file is closed in the
    // outer block.

    try {
      FileOutputStream stream = null;
      try {
        stream = new FileOutputStream(file);

        writeMirrorListToStream(stream, mirrorArr);
      } finally {
        // Close the file in every case
        if (stream != null) {
          try {
            stream.close();
          } catch (IOException exc) {
          }
        }
      }
    } catch (IOException exc) {
      file.delete();
      throw exc;
    }
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = PRIME * result + ((mUrl == null) ? 0 : mUrl.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Mirror other = (Mirror) obj;
    if (mUrl == null) {
      if (other.mUrl != null) {
        return false;
      }
    } else if (!mUrl.equals(other.mUrl)) {
      return false;
    }
    return true;
  }

  /**
   * Loads the mirror lists from the given file
   * and the given server defined mirror array.
   *
   * @param file The file to load the mirrors from.
   * @param mirrorUrlArr The array with the current mirrors urls.
   * @param serverDefindedMirros The array with the server definded mirrors
   * @return The load mirror array.
   */
  public static Mirror[] loadMirrorList(File file, String[] mirrorUrlArr, Mirror[] serverDefindedMirros) {
    try {
      ArrayList<Mirror> mirrorList = new ArrayList<Mirror>(Arrays.asList(Mirror.readMirrorListFromFile(file)));

      for (int i=0;i<mirrorUrlArr.length;i++) {
        Mirror basemirror = mirrorList.get(i);
        if (!mirrorList.contains(basemirror)) {
          mirrorList.add(basemirror);
        }
      }

      if(serverDefindedMirros != null) {
        for(int i = 0; i < serverDefindedMirros.length; i++) {
          if(!mirrorList.contains(serverDefindedMirros[i])) {
            mirrorList.add(serverDefindedMirros[i]);
          }
        }
      }

      return mirrorList.toArray(new Mirror[mirrorList.size()]);
    } catch (Exception exc) {
      ArrayList<Mirror> mirrorList = new ArrayList<Mirror>();

      for (int i = 0; i < mirrorUrlArr.length; i++) {
        if (!BLOCKEDSERVERS.contains(getServerBase(mirrorUrlArr[i])) && mirrorUrlArr[i] != null) {
          mirrorList.add(new Mirror(mirrorUrlArr[i]));
        }
      }

      return mirrorList.toArray(new Mirror[mirrorList.size()]);
    }
  }

  /**
   * Get the Server-Domain of the Url
   * @param url Url to fetch the Server-Domain from
   * @return Server-Domain
   */
  private static String getServerBase(String url) {
    if (url.startsWith("http://")) {
      url = url.substring(7);
    }
    if (url.indexOf('/') >= 0) {
      url = url.substring(0, url.indexOf('/'));
    }

    return url;
  }

  private static Mirror chooseMirror(Mirror[] mirrorArr, Mirror oldMirror, String name, Class caller) throws TvBrowserException {
    Mirror[] oldMirrorArr = mirrorArr;

    /* remove the old mirror from the mirrorlist */
    if (oldMirror != null) {
      ArrayList<Mirror> mirrors = new ArrayList<Mirror>();
      for (Mirror mirror : mirrorArr) {
        if (oldMirror != mirror) {
          mirrors.add(mirror);
        }
      }
      mirrorArr = new Mirror[mirrors.size()];
      mirrors.toArray(mirrorArr);
    }

    // Get the total weight
    int totalWeight = 0;
    for (Mirror mirror : mirrorArr) {
      totalWeight += mirror.getWeight();
    }

    if(totalWeight > 0) {
      // Choose a weight
      int chosenWeight = RandomUtils.nextInt(totalWeight);
  
      // Find the chosen mirror
      int currWeight = 0;
      for (Mirror mirror : mirrorArr) {
        currWeight += mirror.getWeight();
        if (currWeight > chosenWeight) {
          // Check whether this is the old mirror or Mirror is Blocked
          if (((mirror == oldMirror) || BLOCKEDSERVERS.contains(getServerBase(mirror.getUrl()))) && (mirrorArr.length > 1)) {
            // We chose the old mirror -> chose another one
            ArrayList<Mirror> oldList = new ArrayList<Mirror>(oldMirrorArr.length);
            for (Mirror m : oldMirrorArr) {
              oldList.add(m);
            }
            ArrayList<Mirror> currentList = new ArrayList<Mirror>(mirrorArr.length);
            for (Mirror m : mirrorArr) {
              currentList.add(m);
            }
            Comparator<Mirror> comp = new Comparator<Mirror>() {
  
              @Override
              public int compare(Mirror m1, Mirror m2) {
                return m1.getUrl().compareTo(m2.getUrl());
              }
            };
            Collections.sort(oldList, comp );
            Collections.sort(currentList, comp);
            if (oldList.equals(currentList)) {
              // avoid stack overflow
              return mirror;
            }
            return chooseMirror(mirrorArr, oldMirror, name, caller);
          } else {
            return mirror;
          }
        }
      }
    }

    // We didn't find a mirror? This should not happen -> throw exception
    StringBuilder buf = new StringBuilder();
    for (Mirror mirror : oldMirrorArr) {
      buf.append(mirror.getUrl()).append('\n');
    }

    throw new TvBrowserException(caller, "error.2", "No mirror found\ntried following mirrors: ", name, buf.toString());
  }

  /**
   * Chooses a up to date mirror.
   *
   * @param mirrorArr The mirror array to check.
   * @param monitor The progress monitor to use.
   * @param name The name of the file to check.
   * @param id The id of the file to check.
   * @param caller The caller class.
   * @param additionalErrorMsg An additional error message value.
   * @return The choosen mirror or <code>null</code>, if no up to date mirror was found or something went wrong.
   * @throws TvBrowserException
   */
  public static Mirror chooseUpToDateMirror(Mirror[] mirrorArr, ProgressMonitor monitor, String name, String id, Class caller, String additionalErrorMsg) throws TvBrowserException {

    Mirror chosenMirror = null;
    int chosenLastUpdate = Integer.MAX_VALUE;
    
    // Choose a random Mirror
    Mirror mirror = chooseMirror(mirrorArr, null, name, caller);
    
    if (monitor != null) {
      monitor.setMessage(mLocalizer.msg("info.3", "Try to connect to mirror {0}", mirror.getUrl()));
    }
    // Check whether the mirror is up to date and available
    for (int i = 0; i < MAX_UP_TO_DATE_CHECKS; i++) {
      try {
    	int maxDays = (int)(Math.random()*(MAX_LAST_UPDATE_DAYS+1));
    	int currentLastUpdate = daysSinceMirrorLastUpdate(mirror, id);
        if (currentLastUpdate <= maxDays || mirrorArr.length == 1) {
          chosenMirror = mirror;
          break;
        } else {
          // This one is not up to date, remember old one if newest so far
          if (currentLastUpdate < chosenLastUpdate) {
        	chosenLastUpdate = currentLastUpdate;
        	chosenMirror = mirror;
          }          
          // -> choose another one
          Mirror oldMirror = mirror;
          mirror = chooseMirror(mirrorArr, mirror, name, caller);
          mLog.info("Mirror " + oldMirror.getUrl() + " is out of date or down. Choosing " + mirror.getUrl() + " instead.");
          if (monitor != null) {
            monitor.setMessage(mLocalizer.msg("info.4", "Mirror {0} is out of date or down. Choosing {1}", oldMirror.getUrl(), mirror
                    .getUrl()));
          }
        }
      } catch (TvBrowserException exc) {
        String blockedServer = getServerBase(mirror.getUrl());
        BLOCKEDSERVERS.add(blockedServer);
        mLog.info("Server blocked : " + blockedServer);

        if(mirrorArr.length == 1 && mirrorArr[0].equals(mirror)) {
          return chosenMirror;
        }

        // This one is not available -> choose another one
        Mirror oldMirror = mirror;
        mirror = chooseMirror(mirrorArr, mirror, name, caller);
        mLog.info("Mirror " + oldMirror.getUrl() + " is not available. Choosing " + mirror.getUrl() + " instead.");
        if (monitor != null) {
          monitor.setMessage(mLocalizer.msg("info.5", "Mirror {0} is not available. Choosing {1}", oldMirror.getUrl(), mirror
                  .getUrl()));
        }
      }
    }

    // Return the mirror
    return chosenMirror;

  }

  private static int daysSinceMirrorLastUpdate(Mirror mirror, String id) throws TvBrowserException {
    // Load the lastupdate file and parse it
    final String url = mirror.getUrl() + (mirror.getUrl().endsWith("/") ? "" : "/") + id + "_lastupdate";
    Date lastupdated;
    mMirrorDownloadRunning = true;
    mMirrorDownloadData = null;
    mDownloadException = false;

    mLog.info("Loading MirrorDate from " + url);

    new Thread(new Runnable() {
      public void run() {
        try {
          mMirrorDownloadData = IOUtilities.loadFileFromHttpServer(new URL(url), 15000);
        } catch (Exception e) {
          mDownloadException = true;
        }
        mMirrorDownloadRunning = false;
      };
    }, "Load mirror date from "+url).start();

    int num = 0;
    // Wait till second Thread is finished or 15000 ms reached
    while ((mMirrorDownloadRunning) && (num < 150)) {
      num++;
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    if (mMirrorDownloadRunning || mMirrorDownloadData == null || mDownloadException) {
      mLog.info("Server " + url +" is down!");
      return Integer.MAX_VALUE;
    }

    try {
      // Parse is. E.g.: '2003-10-09 11:48:45'
      String asString = new String(mMirrorDownloadData);

      if (asString.length() > 10) {
        int year = Integer.parseInt(asString.substring(0, 4));
        int month = Integer.parseInt(asString.substring(5, 7));
        int day = Integer.parseInt(asString.substring(8, 10));
        lastupdated = new Date(year, month, day);

        mLog.info("Done !");
        
        return new Date().getNumberOfDaysSince(lastupdated);
      }
    }catch(NumberFormatException parseException) {
      mLog.info("The file on the server has the wrong format!");
    }

    return Integer.MAX_VALUE;
  }

  /**
   * Reset the List of banned Servers
   */
  public static void resetBannedServers() {
    BLOCKEDSERVERS.clear();
  }
}
