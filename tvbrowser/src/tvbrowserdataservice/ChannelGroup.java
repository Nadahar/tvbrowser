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

package tvbrowserdataservice;

import devplugin.Date;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;

import devplugin.Channel;
import devplugin.ProgressMonitor;

import tvbrowserdataservice.file.*;
import util.exc.TvBrowserException;
import util.io.IOUtilities;

public class ChannelGroup implements devplugin.ChannelGroup {

    private String mID;

    private String[] mMirrorUrlArr;

    private File mDataDir;

    private Channel[] mAvailableChannelArr;

    private Mirror mCurMirror;

    private SummaryFile mSummary;

    private TvDataBaseUpdater mUpdater;

    private String mGroupName = null;

    private String mDescription;

    private int mDirectlyLoadedBytes;

    private TvBrowserDataService mDataService;

    private HashSet mChannels;

    private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(ChannelGroup.class.getName());

    /** The localizer for this class. */
    public static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ChannelGroup.class);

    private static final int MAX_META_DATA_AGE = 2;

    private static final int MAX_UP_TO_DATE_CHECKS = 10;

    private static final int MAX_LAST_UPDATE_DAYS = 5;

    public ChannelGroup(TvBrowserDataService dataservice, String id, String[] mirrorUrls) {
        mID = id;
        mDataService = dataservice;
        mMirrorUrlArr = mirrorUrls;
        mChannels = new HashSet();
        mDataDir = dataservice.getDataDir();
    }

    public String[] getMirrorArr() {
        return mMirrorUrlArr;
    }

    public boolean isGroupMember(Channel ch) {
        return ch.getGroup() != null && ch.getGroup().getId() != null && ch.getGroup().getId().equalsIgnoreCase(mID);
    }

    public void setWorkingDirectory(File dataDir) {
        mDataDir = dataDir;
    }

    public void addChannel(Channel ch) {
        mChannels.add(ch);
    }

    public Iterator getChannels() {
        return mChannels.iterator();
    }

    private String getLocaleProperty(Properties prop, String key, String defaultValue) {
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        String result = prop.getProperty(key + "_" + language);
        if (result == null) {
            result = prop.getProperty(key + "_default", defaultValue);
        }
        return result;

    }

    public String getDescription() {
        if (mDescription != null) { return mDescription; }
        File file = new File(mDataDir, mID + "_info");
        if (!file.exists()) { return ""; }
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(file));
            return getLocaleProperty(prop, "description", "");
        } catch (IOException e) {
            return "";
        }

    }

    public String getName() {
        if (mGroupName != null) { return mGroupName; }

        File file = new File(mDataDir, mID + "_info");
        if (!file.exists()) { return mID; }

        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(file));
            Locale locale = Locale.getDefault();
            String language = locale.getLanguage();
            String result = prop.getProperty(language);
            if (result == null) {
                result = prop.getProperty("default", mID);
            }
            return result;

        } catch (IOException e) {
            return mID;
        }

    }

    public String toString() {
        return getName();
    }

    public void chooseMirrors() throws TvBrowserException {
        // load the mirror list
        Mirror[] mirrorArr = loadMirrorList();

        // Get a random Mirror that is up to date
        mCurMirror = chooseUpToDateMirror(mirrorArr, null);

        mLog.info("Using mirror " + mCurMirror.getUrl());
        // monitor.setMessage(mLocalizer.msg("info.1","Downloading from mirror
        // {0}",mirror.getUrl()));

        // Update the mirrorlist (for the next time)
        updateMetaFile(mCurMirror.getUrl(), mID + "_" + Mirror.MIRROR_LIST_FILE_NAME);

        // Update the channel list
        // NOTE: We have to load the channel list before the programs, because
        // we need it for the programs.
        updateChannelList(mCurMirror);

        try {
            mSummary = loadSummaryFile(mCurMirror);
        } catch (Exception exc) {
            mLog.log(Level.WARNING, "Getting summary file from mirror " + mCurMirror.getUrl() + " failed.", exc);

            mSummary = null;
        }
    }

    public SummaryFile getSummary() {
        return mSummary;
    }

    public Mirror getMirror() {
        return mCurMirror;
    }

    private SummaryFile loadSummaryFile(Mirror mirror) throws IOException, FileFormatException {
        String url = mirror.getUrl() + "/" + mID + "_" + SummaryFile.SUMMARY_FILE_NAME;

        InputStream stream = null;
        try {
            stream = IOUtilities.getStream(new URL(url));

            SummaryFile summary = new SummaryFile();
            summary.readFromStream(stream);

            return summary;
        } finally {
            if (stream != null) {
                stream.close();
            }
        }
    }

    private Mirror[] loadMirrorList() throws TvBrowserException {
        File file = new File(mDataDir, mID + "_" + Mirror.MIRROR_LIST_FILE_NAME);
        try {
            return Mirror.readMirrorListFromFile(file);
        } catch (Exception exc) {

            Mirror[] mirrorList = new Mirror[mMirrorUrlArr.length];
            for (int i = 0; i < mMirrorUrlArr.length; i++) {
                mirrorList[i] = new Mirror(mMirrorUrlArr[i]);
            }
            return mirrorList;

        }
    }

    private Mirror chooseMirror(Mirror[] mirrorArr, Mirror oldMirror) throws TvBrowserException {

        /* remove the old mirror from the mirrorlist */
        if (oldMirror != null) {
            ArrayList mirrors = new ArrayList();
            for (int i = 0; i < mirrorArr.length; i++) {
                if (oldMirror != mirrorArr[i]) {
                    mirrors.add(mirrorArr[i]);
                }
            }
            mirrorArr = new Mirror[mirrors.size()];
            mirrors.toArray(mirrorArr);
        }

        // Get the total weight
        int totalWeight = 0;
        for (int i = 0; i < mirrorArr.length; i++) {
            totalWeight += mirrorArr[i].getWeight();
        }

        // Choose a weight
        int chosenWeight = (int) (Math.random() * totalWeight);

        // Find the chosen mirror
        int currWeight = 0;
        for (int i = 0; i < mirrorArr.length; i++) {
            currWeight += mirrorArr[i].getWeight();
            if (currWeight > chosenWeight) {
                Mirror mirror = mirrorArr[i];
                // Check whether this is the old mirror
                if ((mirror == oldMirror) && (mirrorArr.length > 1)) {
                    // We chose the old mirror -> chose another one
                    return chooseMirror(mirrorArr, oldMirror);
                } else {
                    return mirror;
                }
            }
        }

        // We didn't find a mirror? This should not happen -> throw exception
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < mMirrorUrlArr.length; i++) {
            buf.append(mMirrorUrlArr[i]).append("\n");
        }

        throw new TvBrowserException(getClass(), "error.2", "No mirror found\ntried following mirrors: ", buf.toString());
    }

    private boolean mirrorIsUpToDate(Mirror mirror) throws TvBrowserException {
        // Load the lastupdate file and parse it
        String url = mirror.getUrl() + "/" + mID + "_lastupdate";
        Date lastupdated;
        try {
            byte[] data = IOUtilities.loadFileFromHttpServer(new URL(url));
            mDirectlyLoadedBytes += data.length;

            // Parse is. E.g.: '2003-10-09 11:48:45'
            String asString = new String(data);
            int year = Integer.parseInt(asString.substring(0, 4));
            int month = Integer.parseInt(asString.substring(5, 7));
            int day = Integer.parseInt(asString.substring(8, 10));
            lastupdated = new Date(year, month, day);
        } catch (Exception exc) {
            throw new TvBrowserException(getClass(), "error.3", "Loading lastupdate file failed: {0}", url, exc);
        }

        return lastupdated.compareTo(new Date().addDays(-MAX_LAST_UPDATE_DAYS)) >= 0;
    }

    private Mirror chooseUpToDateMirror(Mirror[] mirrorArr, ProgressMonitor monitor) throws TvBrowserException {
        // Choose a random Mirror
        Mirror mirror = chooseMirror(mirrorArr, null);
        if (monitor != null) {
            monitor.setMessage(mLocalizer.msg("info.3", "Try to connect to mirror {0}", mirror.getUrl()));
        }
        // Check whether the mirror is up to date and available
        for (int i = 0; i < MAX_UP_TO_DATE_CHECKS; i++) {
            try {
                if (mirrorIsUpToDate(mirror)) {
                    break;
                } else {
                    // This one is not up to date -> choose another one
                    Mirror oldMirror = mirror;
                    mirror = chooseMirror(mirrorArr, mirror);
                    mLog.info("Mirror " + oldMirror.getUrl() + " is out of date. Choosing " + mirror.getUrl() + " instead.");
                    if (monitor != null) {
                        monitor.setMessage(mLocalizer.msg("info.4", "Mirror {0} is out of date. Choosing {1}", oldMirror.getUrl(), mirror
                                .getUrl()));
                    }
                }
            } catch (TvBrowserException exc) {
                // This one is not available -> choose another one
                Mirror oldMirror = mirror;
                mirror = chooseMirror(mirrorArr, mirror);
                mLog.info("Mirror " + oldMirror.getUrl() + " is not available. Choosing " + mirror.getUrl() + " instead.");
                if (monitor != null) {
                    monitor.setMessage(mLocalizer.msg("info.5", "Mirror {0} is not available. Choosing {1}", oldMirror.getUrl(), mirror
                            .getUrl()));
                }
            }
        }

        // Return the mirror
        return mirror;
    }

    private boolean needsUpdate(File file) {
        if (!file.exists()) {
            return true;
        } else {
            long minLastModified = System.currentTimeMillis() - ((long) MAX_META_DATA_AGE * 24L * 60L * 60L * 1000L);
            return file.lastModified() < minLastModified;
        }
    }

    private void updateMetaFile(String serverUrl, String metaFileName) throws TvBrowserException {
        File file = new File(mDataDir, metaFileName);

        // Download the new file if needed
        if (needsUpdate(file)) {
            String url = serverUrl + "/" + metaFileName;
            try {
                IOUtilities.download(new URL(url), file);

                mDirectlyLoadedBytes += (int) file.length();
            } catch (IOException exc) {
                throw new TvBrowserException(getClass(), "error.1", "Downloading file from '{0}' to '{1}' failed", url, file
                        .getAbsolutePath(), exc);
            }
        }
    }

    public int getDirectlyLoadedBytes() {
        return mDirectlyLoadedBytes;
    }

    public void resetDirectlyLoadedBytes() {
        mDirectlyLoadedBytes = 0;
    }

    private void updateChannelList(Mirror mirror) throws TvBrowserException {
        updateChannelList(mirror, false);
    }

    private void updateChannelList(Mirror mirror, boolean forceUpdate) throws TvBrowserException {
        File file = new File(mDataDir, mID + "_" + ChannelList.FILE_NAME);
        if (forceUpdate || needsUpdate(file)) {
            String url = mirror.getUrl() + "/" + mID + "_" + ChannelList.FILE_NAME;
            try {
                IOUtilities.download(new URL(url), file);
            } catch (Exception exc) {
                throw new TvBrowserException(getClass(), "error.4", "Server has no channel list: {0}", mirror.getUrl(), exc);
            }

            // Invalidate the channel list
            mAvailableChannelArr = null;
        }
    }

    public Channel[] checkForAvailableChannels(ProgressMonitor monitor) throws TvBrowserException {

        // load the mirror list
        Mirror[] mirrorArr = loadMirrorList();

        // Get a random Mirror that is up to date
        Mirror mirror = chooseUpToDateMirror(mirrorArr, monitor);
        mLog.info("Using mirror " + mirror.getUrl());

        // Update the mirrorlist (for the next time)
        updateMetaFile(mirror.getUrl(), mID + "_" + Mirror.MIRROR_LIST_FILE_NAME);

        // Update the groupname file
        updateMetaFile(mirror.getUrl(), mID + "_info");

        // Update the channel list
        updateChannelList(mirror, true);
        return getAvailableChannels();
    }

    /**
     * Gets the list of the channels that are available by this data service.
     */
    public Channel[] getAvailableChannels() {
        if (mAvailableChannelArr == null) {
            File channelFile = new File(mDataDir, mID + "_" + ChannelList.FILE_NAME);
            if (channelFile.exists()) {
                try {
                    final String groupName = getName();
                    devplugin.ChannelGroup group = new devplugin.ChannelGroup() {

                        public String getName() {
                            return groupName;
                        }

                        public String getId() {
                            return mID;
                        }

                        public String getDescription() {
                            return mDescription;
                        }
                    };

                    ChannelList channelList = new ChannelList(group);
                    channelList.readFromFile(channelFile, mDataService);
                    mAvailableChannelArr = channelList.createChannelArray();
                } catch (Exception exc) {
                    mLog.log(Level.WARNING, "Loading channellist failed: " + channelFile.getAbsolutePath(), exc);
                }
            }

            if (mAvailableChannelArr == null) {
                // There is no channel file or loading failed
                // -> create a list without any channels
                mAvailableChannelArr = new Channel[] {};
            }
        }

        return mAvailableChannelArr;
    }

    public String getId() {
        return mID;
    }

    public boolean equals(Object obj) {

        if (obj instanceof devplugin.ChannelGroup) {
            devplugin.ChannelGroup group = (devplugin.ChannelGroup) obj;
            return group.getId().equalsIgnoreCase(mID);
        }
        return false;

    }

}