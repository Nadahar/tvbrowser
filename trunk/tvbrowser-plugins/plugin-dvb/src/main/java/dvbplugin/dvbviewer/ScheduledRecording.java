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
package dvbplugin.dvbviewer;

import devplugin.Program;
import dvbplugin.HelperClass;
import dvbplugin.Settings;
import dvbplugin.Settings.TvbDvbVChannel;

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
public final class ScheduledRecording {

  /** separator used in DVBViewer's scheduled recordings to separate the fields */
  private static final String VALUE_SEP = ";";

  /** index of field title */
  private static final int FIELD_TITLE = 0;

  /** index of field channel */
  private static final int FIELD_CHANNEL = 1;

  /** sub-index of field channel ID */
  private static final int FIELD_CHANNEL_ID = 0;

  /** sub-index of field channel name */
  private static final int FIELD_CHANNEL_NAME = 1;

  /** index of field start date */
  private static final int FIELD_STARTDATE = 2;

  /** index of field start time */
  private static final int FIELD_STARTTIME = 3;

  /** index of field stoptime */
  private static final int FIELD_STOPTIME = 4;

  /** index of field after record action */
  private static final int FIELD_RECORDINGACTION = 5;

  /** index of field repetition days */
  private static final int FIELD_REPETITION = 6;

  /** index of field action */
  private static final int FIELD_ACTION = 7;

  /** index of field enabled */
  private static final int FIELD_ENABLED = 8;

  /** index of field A/V disable */
  private static final int FIELD_AVDISABLE = 9;

  /** the title of the program to record */
  private String programTitle = Settings.EMPTYSTRING;

  /** the raw (calculated) ID of the channel */
  private String rawDvbViewerID;

  /** the tuner type to be used for this recording */
  private int tunerType;

  /** the audio ID to be used for this recording */
  private int audioID;

  /** the service ID to be used for this recording */
  private int serviceID;

  /** the channel name to be used for this recording */
  private String channelName = Settings.EMPTYSTRING;

  /** the start date of this recording */
  private String startDate = Settings.EMPTYSTRING;

  /** the start time of this recording */
  private String startTime = Settings.EMPTYSTRING;

  /** the stop time of this recording */
  private String stopTime = Settings.EMPTYSTRING;

  /** the action to be executed at start of recording */
  private int recAction;

  /** the days on which this recording should be repeated */
  private String repetitionDays = "-------";

  /** the action to be executed at end of recording */
  private int afterAction;

  /** is this recording enabled? */
  private boolean enabled = true;

  /** is audio/video playback during recording enabled? */
  private boolean avDisable;

  /** the program ID of the TV-Browser program */
  private String tvbID;


  /**
   * Initialize an instance without further data initialisation
   */
  public ScheduledRecording() {
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
  public ScheduledRecording(Program program, TvbDvbVChannel channel, int before, int after,
          int defRecAction, int defAfterAction, boolean isAVDisabled) {

    final String title = program.getTitle().replace('\n', ' ');
    programTitle = title;

    channelName = channel.getDVBChannel().name;
    audioID = Integer.parseInt(channel.getDVBChannel().audioID);
    serviceID = Integer.parseInt(channel.getDVBChannel().serviceID);
    tunerType = Integer.parseInt(channel.getDVBChannel().tunerType);
    startDate = HelperClass.date(program);
    startTime = HelperClass.calcStartTime(program, before);
    stopTime = HelperClass.calcEndTime(program, after);
    recAction = defRecAction;
    afterAction = defAfterAction;
    avDisable = isAVDisabled;
    tvbID = program.getID();
  }


  /**
   * initialize an instance with the data read from DVBViewer
   *
   * @param entry the scheduled recording line from DVBViewer
   */
  ScheduledRecording(String entry) {
    String[] fields = entry.split(VALUE_SEP);
    String tmp;

    tmp = fields[FIELD_TITLE];
    if (null != tmp) {
      programTitle = tmp;
    }

    tmp = fields[FIELD_CHANNEL];
    if (null != tmp) {
      String[] chnl = tmp.split("\\|");

      tmp = chnl[FIELD_CHANNEL_ID];
      if (tmp != null) {
        if (1 == chnl.length) {
          channelName = tmp;
        } else {
          setRawDvbViewerID(tmp);
        }
      }

      if (1 < chnl.length) {
        tmp = chnl[FIELD_CHANNEL_NAME];
        if (null != tmp) {
          channelName = tmp;
        }
      }
    }

    tmp = fields[FIELD_STARTDATE];
    if (null != tmp) {
      startDate = tmp;
    }

    tmp = fields[FIELD_STARTTIME];
    if (null != tmp) {
      startTime = tmp;
    }

    tmp = fields[FIELD_STOPTIME];
    if (null != tmp) {
      stopTime = tmp;
    }

    tmp = fields[FIELD_ACTION];
    if (null != tmp && 0 < tmp.length()) {
      recAction = Integer.parseInt(tmp);
    }

    tmp = fields[FIELD_REPETITION];
    if (null != tmp) {
      repetitionDays = tmp;
    }

    tmp = fields[FIELD_RECORDINGACTION];
    if (null != tmp && 0 < tmp.length()) {
      afterAction = Integer.parseInt(tmp);
    }

    tmp = fields[FIELD_ENABLED];
    if (null != tmp) {
      enabled = Boolean.valueOf(tmp).booleanValue();
    }

    tmp = fields[FIELD_AVDISABLE];
    if (null != tmp) {
      avDisable = Boolean.valueOf(tmp).booleanValue();
    }
  }


  /**
   * Returns the data in the format used by DVBViewer
   *
   * @return scheduled recording data in DVBViewer format
   */
  final String getEntry() {
    // create the output
    StringBuffer buf = new StringBuffer(128);
    buf.append(programTitle).append(VALUE_SEP);
    buf.append(getRawDvbViewerID()).append('|');
    buf.append(channelName).append(VALUE_SEP);
    buf.append(startDate).append(VALUE_SEP);
    buf.append(startTime).append(VALUE_SEP);
    buf.append(stopTime).append(VALUE_SEP);
    buf.append(String.valueOf(afterAction)).append(VALUE_SEP);
    buf.append(repetitionDays).append(VALUE_SEP);
    buf.append(String.valueOf(recAction)).append(VALUE_SEP);
    buf.append(String.valueOf(enabled)).append(VALUE_SEP);
    buf.append(String.valueOf(avDisable));

    return buf.toString();
  }


  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof ScheduledRecording)) { return false; }
    if (obj == this) { return true; }

    ScheduledRecording other = (ScheduledRecording)obj;
    if (programTitle.equals(other.programTitle)) {
      if (channelName.equals(other.channelName)) {
        if (startDate.equals(other.startDate)) {
          if (startTime.equals(other.startTime)) {
            if (stopTime.equals(other.stopTime)) { return true; }
          }
        }
      }
    }

    return false;
  }


  /**
   * @return Returns the action to be executed at start of recording.
   */
  public final int getRecAction() {
    return recAction;
  }


  /**
   * @param action the action to be executed at start of recording.
   */
  public final void setRecAction(int action) {
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
   * @return Returns value of rawDvbViewerID.
   */
  final String getRawDvbViewerID() {
    if (null == rawDvbViewerID || 0 == rawDvbViewerID.length()) {
      long id = 0x10000 * audioID + serviceID;
      id |= tunerType << 29;
      rawDvbViewerID = String.valueOf(id);
    }

    return rawDvbViewerID;
  }


  /**
   * @param id New value for rawDvbViewerID.
   */
  final void setRawDvbViewerID(String id) {
    if (null == id || 0 == id.length()) { return; }

    rawDvbViewerID = id;
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
  final int getTunerType() {
    return tunerType;
  }


  /**
   * @param type New value for tunerType.
   */
  public final void setTunerType(int type) {
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
  public final int getAfterAction() {
    return afterAction;
  }


  /**
   * @param action the action to be executed after recording end.
   */
  public final void setAfterAction(int action) {
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


  /**
   * @return Returns value of startDate.
   */
  public final String getStartDate() {
    return startDate;
  }


  /**
   * @param start New value for startDate.
   */
  public final void setStartDate(String start) {
    if (null == start) { return; }

    startDate = start;
  }


  /**
   * @return Returns value of startTime.
   */
  public final String getStartTime() {
    return startTime;
  }


  /**
   * @param start New value for startTime.
   */
  public final void setStartTime(String start) {
    if (null == start) { return; }

    startTime = start;
  }


  /**
   * @return Returns value of stopTime.
   */
  public final String getStopTime() {
    return stopTime;
  }


  /**
   * @param stop New value for stopTime.
   */
  public final void setStopTime(String stop) {
    if (null == stop) { return; }

    stopTime = stop;
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
   * @return the TV-Browser program ID
   */
  final String getTvbID() {
    return tvbID;
  }


  /**
   * @param id the TV-Browser program ID
   */
  public final void setTvbID(String id) {
    tvbID = id;
  }
}
