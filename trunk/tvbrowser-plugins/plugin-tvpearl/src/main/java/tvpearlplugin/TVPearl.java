/*
 * TV-Pearl by Reinhard Lehrbaum
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
 */
package tvpearlplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import tvbrowser.TVBrowser;
import devplugin.Channel;
import devplugin.Plugin;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;

public class TVPearl {
  /**
   * maximum deviation in minutes which is accepted when searching for the
   * matching program
   */
  private static final int ALLOWED_DEVIATION_MINUTES = 10;

  /**
   * minimum time in hours before the next online update can be done
   */
  private static final int UPDATE_WAIT_HOURS = 12;

  private static final Logger mLog = java.util.logging.Logger.getLogger(TVPearlPlugin.class.getName());

  private String mUrl;
  private List<TVPProgram> mProgramList;
  private Calendar mLastUpdate;
  private boolean mReindexAll = true;
  private static final TVPProgram EXAMPLE_PEARL = new TVPProgram("Me", "http://hilfe.tvbrowser.org", Calendar
      .getInstance(), "Example", "Channel", Calendar.getInstance(), "Info", "ID");

  public TVPearl() {
    mProgramList = new ArrayList<TVPProgram>();
    mLastUpdate = Calendar.getInstance();
    mLastUpdate.set(Calendar.HOUR_OF_DAY, mLastUpdate.get(Calendar.HOUR_OF_DAY) - 13);
  }

  public boolean getReindexAll() {
    return mReindexAll;
  }

  public void setReindexAll(final boolean reindexAll) {
    mReindexAll = reindexAll;
  }

  public void setUrl(final String url) {
    mUrl = url;
  }

  public String getUrl() {
    return mUrl;
  }

  public void update() {
    if (canUpdate()) {
      Thread pearlThread = new Thread("TV pearl update") {
        @Override
        public void run() {
          mLastUpdate = Calendar.getInstance();

          final TVPGrabber grabber = new TVPGrabber();
          final List<TVPProgram> programList = grabber.parse(mUrl);
          mUrl = grabber.getLastUrl();

          for (TVPProgram program : programList) {
            addProgram(program);
          }
          final Calendar limit = getViewLimit();
          int i = 0;
          while (i < mProgramList.size()) {
            final TVPProgram p = mProgramList.get(i);
            if (p.getStart().compareTo(limit) < 0) {
              mProgramList.remove(i);
              i--;
            }
            i++;
          }
          Collections.sort(mProgramList);
          // TODO: change to UIThreadRunner after 3.0
          try {
            SwingUtilities.invokeAndWait(new Runnable() {
              public void run() {
                updateTVB();
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
      };
      pearlThread.setPriority(Thread.MIN_PRIORITY);
      pearlThread.start();
    }
  }

  private synchronized void addProgram(final TVPProgram program) {
    int index = indexOf(program);
    if (index == -1 || mProgramList.get(index).getProgramID().length() == 0) {
      if (index != -1) {
        mProgramList.remove(index);
      }
      setProgramID(program, false);
      mProgramList.add(program);
    }
  }

  private void setProgramID(final TVPProgram pearl, final boolean reindex) {
    if (pearl.getProgramID().length() == 0 || reindex) {
      pearl.resetStatus();

      final List<Channel> channelList = getChannelsFromName(pearl.getChannel());
      if (!channelList.isEmpty()) {
        pearl.setStatus(IProgramStatus.STATUS_FOUND_CHANNEL);
      }
      if (pearl.getStart().compareTo(getViewLimit()) <= 0) {
        return;
      }
      for (Channel channel : channelList) {
        Iterator<Program> it = Plugin.getPluginManager().getChannelDayProgram(pearl.getDate(), channel);
        while ((it != null) && (it.hasNext())) {
          Program program = it.next();
          if (compareTitle(program.getTitle(), pearl.getTitle())
              && Math.abs(program.getStartTime() - pearl.getStartTime()) <= ALLOWED_DEVIATION_MINUTES) {
            pearl.setProgram(program);
            return;
          }
          if (program.getStartTime() - pearl.getStartTime() > ALLOWED_DEVIATION_MINUTES) {
            // we are beyond the pearl start time
            it = Plugin.getPluginManager().getChannelDayProgram(pearl.getDate().addDays(-1), channel);
            while ((it != null) && (it.hasNext())) {
              program = it.next();
              if (compareTitle(program.getTitle(), pearl.getTitle())
                  && Math.abs(program.getStartTime() - pearl.getStartTime()) <= ALLOWED_DEVIATION_MINUTES) {
                pearl.setProgram(program);
                return;
              }
            }
            break;
          }
        }
        // search on next day (for programs shortly after midnight)
        it = Plugin.getPluginManager().getChannelDayProgram(pearl.getDate().addDays(1), channel);
        while ((it != null) && (it.hasNext())) {
          final Program program = it.next();
          if (compareTitle(program.getTitle(), pearl.getTitle())
              && Math.abs((program.getStartTime() + 24 * 60) - pearl.getStartTime()) <= ALLOWED_DEVIATION_MINUTES) {
            pearl.setProgram(program);
            return;
          }
        }
      }
    }
  }

  private boolean compareTitle(final String title1, final String title2) {
    final String t1 = title1.toLowerCase();
    final String t2 = title2.toLowerCase();

    return t1.equals(t2) || t1.indexOf(t2) >= 0 || t2.indexOf(t1) >= 0;
  }

  private Calendar getViewLimit() {
    final Calendar limit = Calendar.getInstance();
    limit.set(Calendar.DAY_OF_MONTH, limit.get(Calendar.DAY_OF_MONTH) - 1);
    return limit;
  }

  private int indexOf(final TVPProgram program) {
    for (int i = 0; i < mProgramList.size(); i++) {
      final TVPProgram p = mProgramList.get(i);
      if (p.getAuthor().equals(program.getAuthor()) && p.getStart().equals(program.getStart())
          && p.getChannel().equals(program.getChannel()) && p.getTitle().equals(program.getTitle())) {
        return i;
      }
    }
    return -1;
  }

  /**
   * get all subscribed channels which match the given channel name
   *
   * @param channelName
   * @return
   */
  private List<Channel> getChannelsFromName(final String channelName) {
    final List<Channel> result = new ArrayList<Channel>();
    final Pattern pattern = Pattern.compile("^(.*[ ()])?" + Pattern.quote(channelName.trim()) + "([ ()].*)?$");
    for (Channel channel : Plugin.getPluginManager().getSubscribedChannels()) {
      // first search default name
      Matcher matcher = pattern.matcher(channel.getDefaultName());
      if (matcher.find()) {
        result.add(channel);
      } else {
        // afterwards search user defined name
        matcher = pattern.matcher(channel.getName());
        if (matcher.find()) {
          result.add(channel);
        }
      }
    }
    return result;
  }

  private boolean canUpdate() {
    final Calendar now = Calendar.getInstance();

    final long hours = Math.round((double) (now.getTimeInMillis() - mLastUpdate.getTimeInMillis()) / (60 * 60 * 1000));

    // always allow update in developer version
    return hours > UPDATE_WAIT_HOURS || !TVBrowser.isStable();
  }

  public synchronized TVPProgram getPearl(final Program program) {
    if (program.equals(TVPearlPlugin.getPluginManager().getExampleProgram())) {
      return EXAMPLE_PEARL;
    }
    for (TVPProgram p : mProgramList) {
      if (p.getProgramID().equalsIgnoreCase(program.getID()) && program.getDate().equals(p.getDate())) {
        return p;
      }
    }
    return null;
  }

  public TVPProgram[] getPearlList() {
    final List<TVPProgram> result = new ArrayList<TVPProgram>();
    result.addAll(mProgramList);

    final Calendar limit = getViewLimit();
    final boolean filterEnabled = TVPearlPlugin.getSettings().getFilterEnabled();
    final boolean showSubscribed = TVPearlPlugin.getSettings().getShowSubscribedChannels();
    final boolean showFound = TVPearlPlugin.getSettings().getShowFoundPearls();

    for (int i = result.size() - 1; i >= 0; i--) {
      final TVPProgram program = result.get(i);
      if ((showSubscribed && !program.isSubscribedChannel()) || (showFound && !program.wasFound())
          || (program.getStart().compareTo(limit) < 0) || (filterEnabled && !TVPProgramFilter.showProgram(program))) {
        result.remove(i);
      }
    }

    return result.toArray(new TVPProgram[result.size()]);
  }

  public synchronized void recheckProgramID() {
    for (TVPProgram program : mProgramList) {
      setProgramID(program, mReindexAll);
    }
  }

  public void readData(final ObjectInputStream in) throws IOException, ClassNotFoundException {
    final Calendar limit = getViewLimit();

    final int version = in.readInt();

    if (version == 1 || version == 2) {
      mLastUpdate = Calendar.getInstance();
      mLastUpdate.setTime((Date) in.readObject());

      final int size = in.readInt();
      for (int i = 0; i < size; i++) {
        final String author = (String) in.readObject();
        final Calendar cal = Calendar.getInstance();
        final Date time = (Date) in.readObject();
        cal.setTime(time);
        final String url = (String) in.readObject();
        final String channel = (String) in.readObject();
        final Calendar calStart = Calendar.getInstance();
        final Date startTime = (Date) in.readObject();
        calStart.setTime(startTime);
        final String title = (String) in.readObject();
        final String info = (String) in.readObject();
        final String programID = (String) in.readObject();
        final TVPProgram p = new TVPProgram(author, url, cal, title, channel, calStart, info, programID);
        if (p.getStart().compareTo(limit) > 0) {
          addProgram(p);
        }
      }
      if (version == 2) {
        TVPearlPlugin.getInstance().setComposers((Vector<String>) in.readObject());
      }
    } else if (version == 3) {
      mLastUpdate = Calendar.getInstance();
      mLastUpdate.setTime((Date) in.readObject());

      final int size = in.readInt();
      for (int i = 0; i < size; i++) {
        final String author = (String) in.readObject();
        final Calendar cal = Calendar.getInstance();
        final Date time = (Date) in.readObject();
        cal.setTime(time);
        final String url = (String) in.readObject();
        final String channel = (String) in.readObject();
        final Date startTime = (Date) in.readObject();
        final Calendar calStart = Calendar.getInstance();
        calStart.setTime(startTime);
        final String title = (String) in.readObject();
        final String info = (String) in.readObject();
        final String programID = (String) in.readObject();
        final TVPProgram p = new TVPProgram(author, url, cal, title, channel, calStart, info, programID);
        final boolean sendTo = in.readBoolean();
        p.setSendTo(sendTo);
        if (p.getStart().compareTo(limit) > 0) {
          addProgram(p);
        }
      }
      final int countComposers = in.readInt();
      final Vector<String> composers = new Vector<String>();
      for (int i = 0; i < countComposers; i++) {
        composers.add((String) in.readObject());
      }
      TVPearlPlugin.getInstance().setComposers(composers);

      final int count = in.readInt();
      if (count > 0) {
        ProgramReceiveTarget[] targets = new ProgramReceiveTarget[count];

        for (int i = 0; i < count; i++) {
          targets[i] = new ProgramReceiveTarget(in);
        }
        TVPearlPlugin.getInstance().setClientPluginsTargets(targets);
      }
    }
    // updateProgramMark();
  }

  public void writeData(final ObjectOutputStream out) throws IOException {
    out.writeInt(3); // version
    out.writeObject(mLastUpdate.getTime());
    out.writeInt(mProgramList.size());
    for (TVPProgram program : mProgramList) {
      out.writeObject(program.getAuthor());
      out.writeObject(program.getCreateDate().getTime());
      out.writeObject(program.getContentUrl());
      out.writeObject(program.getChannel());
      out.writeObject(program.getStart().getTime());
      out.writeObject(program.getTitle());
      out.writeObject(program.getInfo());
      out.writeObject(program.getProgramID());
      out.writeBoolean(program.getSendTo());
    }
    final Vector<String> composers = TVPearlPlugin.getInstance().getComposers();
    out.writeInt(composers.size());
    for (String composer : composers) {
      out.writeObject(composer);
    }

    final ProgramReceiveTarget[] targets = TVPearlPlugin.getInstance().getClientPluginsTargets();
    out.writeInt(targets.length);

    for (ProgramReceiveTarget target : targets) {
      target.writeData(out);
    }
  }

  /**
   * send update to the TV-Browser
   */
  public void updateTVB() {
    updateProgramMark();
    sendToPlugin();
    updateTreeView();
  }

  /**
   * mark TV pearls in the TV-Browser
   */
  private void updateProgramMark() {
    try {
      for (TVPProgram program : mProgramList) {
        if (program.wasFound()) {
          final Program p = program.getProgram();
          if (p != null) {
            markProgram(p, TVPearlPlugin.getSettings().getMarkPearls() && TVPProgramFilter.showProgram(program));
          }
        }
      }
    } catch (Exception ex) {
      mLog.warning(ex.getMessage());
      mLog.warning("Additional Info:\nProgram list:" + (mProgramList != null ? mProgramList.size() : "null"));
    }
  }

  /**
   * mark or unmark the program (and repetitions or continuations)
   *
   * @param program
   * @param setMark
   *          whether to mark or unmark the program
   */
  private void markProgram(final Program program, final boolean setMark) {
    // set or remove mark
    if (setMark) {
      program.mark(TVPearlPlugin.getInstance());
    } else {
      program.unmark(TVPearlPlugin.getInstance());
    }
    program.validateMarking();
    // now find repetitions or continuations
    final Iterator<Program> dayProg = Plugin.getPluginManager().getChannelDayProgram(program.getDate(),
        program.getChannel());
    if (dayProg != null) {
      while (dayProg.hasNext()) {
        final Program nextProg = dayProg.next();
        if (nextProg.getStartTime() > program.getStartTime()
            && nextProg.getTitle().length() >= program.getTitle().length()
            && nextProg.getTitle().substring(0, program.getTitle().length()).equalsIgnoreCase(program.getTitle())) {
          if (setMark) {
            nextProg.mark(TVPearlPlugin.getInstance());
          } else {
            nextProg.unmark(TVPearlPlugin.getInstance());
          }
          nextProg.validateMarking();
        }
      }
    }
  }

  /**
   * send new TV Pearls to other plugins
   */
  private void sendToPlugin() {
    if (TVPearlPlugin.getInstance().hasPluginTarget()) {
      final ArrayList<Program> programList = new ArrayList<Program>();
      for (TVPProgram p : getPearlList()) {
        if (p.wasFound() && !p.getSendTo()) {
          programList.add(p.getProgram());
          p.setSendTo(true);
        }
      }
      Program[] prArray = new Program[programList.size()];
      prArray = programList.toArray(prArray);

      final ProgramReceiveTarget[] targets = TVPearlPlugin.getInstance().getClientPluginsTargets();
      for (ProgramReceiveTarget target : targets) {
        final ProgramReceiveIf plugin = target.getReceifeIfForIdOfTarget();
        if (plugin != null && plugin.canReceiveProgramsWithTarget()) {
          plugin.receivePrograms(prArray, target);
        }
      }
    }
  }

  /**
   * update the TV-Browser tree view
   */
  private void updateTreeView() {
    final PluginTreeNode root = TVPearlPlugin.getInstance().getRootNode();
    root.removeAllActions();
    root.removeAllChildren();

    for (TVPProgram p : getPearlList()) {
      if (p.wasFound()) {
        Program program = p.getProgram();
        if (program != null) {
          root.addProgram(program);
        }
      }
    }
    root.update();
  }

  public void logInfo() {
    mLog.info("Url: " + mUrl);
    mLog.info("Program count: " + mProgramList.size());
    if (mLastUpdate != null) {
      mLog.info("Last update: " + mLastUpdate.getTime().toString());
    }
  }
}
