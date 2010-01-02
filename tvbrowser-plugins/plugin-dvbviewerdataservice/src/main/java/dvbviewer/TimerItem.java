/*
 * ScheduledRecording.java
 * Copyright (C) 2006 Ullrich Pollaehne (pollaehne@users.sourceforge.net)
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
package dvbviewer;

import java.util.Calendar;
import java.util.Date;

import devplugin.Program;
import dvbviewer.com4j.ITimerItem;
import dvbviewer.com4j.TDVBVShutdown;
import dvbviewer.com4j.TDVBVTimerAction;
import dvbviewer.com4j.TDVBVTunerType;

/**
 * Container for the data that is contained in the DVBViewer scheduled
 * recording.
 * <p>
 * This class contains all data that is stored in one scheduled recording of
 * DVBViewer. Currently this class only handles DVBViewer Pro data.
 *
 * @author pollaehne
 * @version $Revision: $
 */
public final class TimerItem {

  /** the title of the program to record */
  private String programTitle = "";

  /** the DVBViewer ID of the channel */
  private String dvbViewerID;

  /** the tuner type to be used for this recording */
  private TDVBVTunerType tunerType;

  /** the audio ID to be used for this recording */
  private int audioID;

  /** the service ID to be used for this recording */
  private int serviceID;

  /** the channel name to be used for this recording */
  private String channelName = "";

  /** date/time of recording start */
  private Calendar start;

  /** date/time of recording end */
  private Calendar stop;

  /** the action to be executed at start of recording */
  private TDVBVTimerAction recAction;

  /** the days on which this recording should be repeated */
  private String repetitionDays = "-------";

  /** the action to be executed at end of recording */
  private TDVBVShutdown afterAction;

  /** is this recording enabled? */
  private boolean enabled = true;

  /** is audio/video playback during recording enabled? */
  private boolean avDisable;

  /** the ID of the timer (if it is known to DVBViewer) */
  private int timerID = -1;

  /** the program ID of the TV-Browser program */
  private String tvbID;


  /**
   * Initialize an instance without further data initialisation
   */
  public TimerItem() {
  // nothing to do
  }


  /**
   * Initialize an instance with the data of a TV-Browser program
   *
   * @param program the program containing the data
   * @param channel the DVBViewer channel description
   * @param before the time to start the recording ahead of time
   * @param after the time to stop the recording after end
   */
  public TimerItem(Program program, DVBViewerChannel channel, int before, int after,
          int defRecAction, int defAfterAction, boolean isAVDisabled) {

    final String title = program.getTitle().replace('\n', ' ');
    programTitle = title;

    channelName = channel.getName();
    audioID = channel.getAudioID();
    serviceID = channel.getServiceID();
    tunerType = channel.getTunerType();

    // get the start date
    Calendar cal = (Calendar) program.getDate().getCalendar().clone();
    // add the start time
    cal.set(Calendar.HOUR_OF_DAY, program.getHours());
    cal.set(Calendar.MINUTE, program.getMinutes());
    cal.set(Calendar.SECOND, 0);
    // subtract the value of before
    cal.add(Calendar.MINUTE, -before);
    start = cal;

    // get the start date
    cal = (Calendar) program.getDate().getCalendar().clone();
    // add the stop time
    cal.set(Calendar.HOUR_OF_DAY, program.getHours());
    cal.set(Calendar.MINUTE, program.getMinutes());
    // add the duration
    if (program.getLength() <= 0)
      cal.add(Calendar.MINUTE, 1);
    else
      cal.add(Calendar.MINUTE, program.getLength());
    cal.set(Calendar.SECOND, 0);
    // add the value after
    cal.add(Calendar.MINUTE, after);
    stop = cal;

    recAction = TDVBVTimerAction.values()[defRecAction];
    afterAction = TDVBVShutdown.values()[defAfterAction];
    avDisable = isAVDisabled;
    tvbID = program.getID();
  }


  /**
   * initialize an instance with the data read from DVBViewer
   *
   * @param item the TimerItem from DVBViewer
   */
  TimerItem(ITimerItem item) {
    programTitle = item.description();
    channelName = item.channelName();

    dvbViewerID = item.channelID();

    // get the start date/time
    Calendar cal = Calendar.getInstance();
    cal.setTime(item.date());
    cal.set(Calendar.HOUR_OF_DAY, item.startTime().getHours());
    cal.set(Calendar.MINUTE, item.startTime().getMinutes());
    cal.set(Calendar.SECOND, 0);
    start = cal;

    // get the stop date/time
    cal = Calendar.getInstance();
    cal.setTime(item.date());
    cal.set(Calendar.HOUR_OF_DAY, item.endTime().getHours());
    cal.set(Calendar.MINUTE, item.endTime().getMinutes());
    cal.set(Calendar.SECOND, 0);
    stop = cal;

    recAction = item.timerAction();
    repetitionDays = item.days();
    afterAction = item.shutdown();
    enabled = item.enabled();
    avDisable = item.disableAV();
    timerID = item.id();
  }


  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof TimerItem)) { return false; }
    if (obj == this) { return true; }

    TimerItem other = (TimerItem)obj;
    if (programTitle.equals(other.programTitle)) {
      if (channelName.equals(other.channelName)) {
        if (start.equals(other.start)) {
          if (stop.equals(other.stop)) {
            return true;
          }
        }
      }
    }

    return false;
  }


  /**
   * @return Returns the action to be executed at start of recording.
   */
  public final TDVBVTimerAction getRecAction() {
    return recAction;
  }


  /**
   * @param action the action to be executed at start of recording.
   */
  public final void setRecAction(TDVBVTimerAction action) {
    recAction = action;
  }


  /**
   * @return Returns value of avDisable.
   */
  public final boolean isAvDisable() {
    return avDisable;
  }


  /**
   * @param avdisable New value for avDisable.
   */
  public final void setAvDisable(boolean avdisable) {
    avDisable = avdisable;
  }


  /**
   * @return Returns value of dvbViewerChannel.
   */
  public final String getDvbViewerChannel() {
    return channelName;
  }


  /**
   * @param channel New value for dvbViewerChannel.
   */
  public final void setDvbViewerChannel(String channel) {
    if (null == channel) { return; }

    channelName = channel;
  }


  /**
   * @return Returns value of dvbViewerID.
   */
  final String getDvbViewerID() {
    if (null == dvbViewerID || 0 == dvbViewerID.length()) {
      StringBuilder buffer = new StringBuilder(128);
      buffer.append(((tunerType.ordinal() + 1) << 29) + (audioID << 16) + serviceID);
      buffer.append('|');
      buffer.append(channelName);
      dvbViewerID = buffer.toString();
    }

    return dvbViewerID;
  }


  /**
   * @param id New value for dvbViewerID.
   */
  final void setDvbViewerID(String id) {
    if (null == id || 0 == id.length()) { return; }

    dvbViewerID = id;
  }


  /**
   * @return Returns value of audioID.
   */
  final int getAudioID() {
    return audioID;
  }


  /**
   * @param id New value for audioID.
   */
  public final void setAudioID(int id) {
    audioID = id;
  }


  /**
   * @return Returns value of serviceID.
   */
  final int getServiceID() {
    return serviceID;
  }


  /**
   * @param id New value for serviceID.
   */
  public final void setServiceID(int id) {
    serviceID = id;
  }


  /**
   * @return Returns value of tunerType.
   */
  final TDVBVTunerType getTunerType() {
    return tunerType;
  }


  /**
   * @param type New value for tunerType.
   */
  public final void setTunerType(TDVBVTunerType type) {
    tunerType = type;
  }


  /**
   * @return Returns value of programTitle.
   */
  public final String getProgramTitle() {
    return programTitle;
  }


  /**
   * @param title New value for programTitle.
   */
  public final void setProgramTitle(String title) {
    if (null == title) { return; }

    programTitle = title;
  }


  /**
   * @return Returns the action to be executed after recording end.
   */
  public final TDVBVShutdown getAfterAction() {
    return afterAction;
  }


  /**
   * @param action the action to be executed after recording end.
   */
  public final void setAfterAction(TDVBVShutdown action) {
    afterAction = action;
  }


  /**
   * @return Returns value of repetitionDays.
   */
  public final String getRepetitionDays() {
    return repetitionDays;
  }


  /**
   * @param repetitions New value for repetitionDays.
   */
  public final void setRepetitionDays(String repetitions) {
    if (null == repetitions || 0 == repetitions.length()) { return; }

    repetitionDays = repetitions;
  }


  public final void setStart(Date startDate) {
    start.setTime(startDate);
  }


  public final void setStart(Calendar startCal) {
    start = (Calendar)startCal.clone();
  }


  public final Date getStart() {
    return start.getTime();
  }


  public final Calendar getStartAsCalendar() {
    return start;
  }


  public final void setStop(Date stopDate) {
    stop.setTime(stopDate);
  }


  public final void setStop(Calendar stopCal) {
    stop = (Calendar)stopCal.clone();
  }


  public final Date getStop() {
    return stop.getTime();
  }

  /**
   * @return Returns value of enabled.
   */
  final boolean isEnabled() {
    return enabled;
  }


  /**
   * @param enabled New value for enabled.
   */
  final void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }


  /**
   * @return the DVBViewer timer ID
   */
  final int getTimerID() {
    return timerID;
  }


  /**
   * @param id the DVBViewer timer ID
   */
  public final void setTimerID(int id) {
    timerID = id;
  }


  /**
   * @return the TV-Browser program ID
   */
  public final String getTvbID() {
    return tvbID;
  }


  /**
   * @param id the TV-Browser program ID
   */
  public final void setTvbID(String id) {
    tvbID = id;
  }
}
