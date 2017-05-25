/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.extras.favoritesplugin.core;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Iterator;

import tvbrowser.core.filters.FilterManagerImpl;
import tvbrowser.extras.common.ChannelItem;
import tvbrowser.extras.common.DayListCellRenderer;
import tvbrowser.extras.common.LimitationConfiguration;
import util.ui.WrapperFilter;
import devplugin.Channel;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.ProgramFilter;
import devplugin.ProgramInfoHelper;

public class Exclusion implements Comparable<Exclusion> {
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(Exclusion.class);
  
  public static final int DAYLIMIT_DAILY = LimitationConfiguration.DAYLIMIT_DAILY;
  private static final int DAYLIMIT_WEEKEND = LimitationConfiguration.DAYLIMIT_WEEKEND;
  private static final int DAYLIMIT_SUNDAY = LimitationConfiguration.DAYLIMIT_SUNDAY;
  private static final int DAYLIMIT_SATURDAY = LimitationConfiguration.DAYLIMIT_SATURDAY;

  private ChannelItem mChannel;
  private String mTopic;
  private String mTitle;
  private String mEpisodeTitle;
  private int mTimeFrom;
  private int mTimeTo;
  private int mDayOfWeek;
  private ProgramFilter mFilter;
  private String mFilterName;
  private int mCategory;
  
  private ProgramFieldExclusion mProgramFieldExclusion;

  /**
   * Creates a new exclusion criteria.
   * @param title null, if any title is allowed
   * @param topic null, if any keyword is allowed
   * @param channel null, if any channel is allowed
   * @param timeFrom lower time limit (or -1, if no lower limit exists)
   * @param timeTo upper time limit (or -1, if no upper limit exists)
   * @param dayOfWeek The day of week to use.
   * @param filterName The name of the filter to use;
   * @param episodeTitle null, if any episode title is allowed
   * @param category the category of the program or 0 if no category should be filtered
   * @param programFieldExclusion a program field to exclude from
   */
  public Exclusion(String title, String topic, Channel channel, int timeFrom, int timeTo, int dayOfWeek, String filterName, String episodeTitle, int category, ProgramFieldExclusion programFieldExclusion) {
    mTitle = title;
    mTopic = topic;
    mChannel = new ChannelItem(channel);
    mTimeFrom = timeFrom;
    mTimeTo = timeTo;
    mDayOfWeek = dayOfWeek;
    mFilterName = filterName;
    mEpisodeTitle = episodeTitle;
    mCategory = category;
    mProgramFieldExclusion = programFieldExclusion;
  }

  public Exclusion(ObjectInputStream in) throws ClassNotFoundException, IOException {
    int version = in.readInt();  // version
    
    boolean hasChannel = in.readBoolean();
    if (hasChannel) {
      if(version < 3) {
        String channelServiceClassName = (String) in.readObject();
        String channelGroupId = null;
      
        if(version >= 2) {
          channelGroupId = (String) in.readObject();
        }
      
        String channelId=(String)in.readObject();
        Channel ch = Channel.getChannel(channelServiceClassName, channelGroupId, null, channelId);
        
        mChannel = new ChannelItem(ch);
      }
      else if (version < 5) {
        Channel ch = Channel.readData(in, true);
        mChannel = new ChannelItem(ch);
      }
      else {
        mChannel = new ChannelItem(in,3);
      }
    }

    boolean hasTitle = in.readBoolean();
    if (hasTitle) {
      mTitle = (String)in.readObject();
    }

    boolean hasTopic = in.readBoolean();
    if (hasTopic) {
      mTopic = (String)in.readObject();
    }
    
    if(version > 3) {
      if(in.readBoolean()) {
        mFilterName = (String)in.readObject();
      }
    }

    mTimeFrom = in.readInt();
    mTimeTo = in.readInt();
    mDayOfWeek = in.readInt();
    
    if(mChannel == null) {
      mChannel = new ChannelItem(null);
    }
    
    if(version > 5) {
      boolean hasEpisodeTitle = in.readBoolean();
      
      if(hasEpisodeTitle) {
        mEpisodeTitle = in.readUTF();
      }
    }
    
    if(version > 6) {
      mCategory = in.readInt();
    }
    
    if(version > 7) {
      if(in.readBoolean()) {
        mProgramFieldExclusion = new ProgramFieldExclusion(in, version);
      }
      else {
        mProgramFieldExclusion = null;
      }
    }
    else {
      mProgramFieldExclusion = null;
    }
  }


  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(8);  // version
    out.writeBoolean(mChannel != null);
    if (mChannel != null) {
      mChannel.saveItem(out);
    }

    out.writeBoolean(mTitle != null);
    if (mTitle != null) {
      out.writeObject(mTitle);
    }

    out.writeBoolean(mTopic != null);
    if (mTopic != null) {
      out.writeObject(mTopic);
    }
    
    out.writeBoolean(mFilterName != null);
    if(mFilterName != null) {
      out.writeObject(mFilterName);
    }

    out.writeInt(mTimeFrom);
    out.writeInt(mTimeTo);
    out.writeInt(mDayOfWeek);
    
    out.writeBoolean(mEpisodeTitle != null);
    
    if(mEpisodeTitle != null) {
      out.writeUTF(mEpisodeTitle);
    }
    
    out.writeInt(mCategory);
    
    out.writeBoolean(mProgramFieldExclusion != null);
    
    if(mProgramFieldExclusion != null) {
      mProgramFieldExclusion.writeData(out);
    }
  }


  public String getTitle() {
    return mTitle;
  }

  public String getTopic() {
    return mTopic;
  }
  
  public String getEpisodeTitle() {
    return mEpisodeTitle;
  }
  
  public ProgramFilter getFilter() {
    if(mFilter == null) {
      ProgramFilter[] filters = FilterManagerImpl.getInstance().getAvailableFilters();
      
      for(ProgramFilter filter : filters) {
        if(filter.getName().equals(mFilterName)) {
          mFilter = filter;
          break;
        }
      }
    }
    
    return mFilter;
  }
  
  public void setFilter(final ProgramFilter filter) {
    mFilter = filter;
    mFilterName = filter.getName();
  }

  public Channel getChannel() {
    return mChannel.getChannel();
  }

  public int getTimeLowerBound() {
    return mTimeFrom;
  }

  public int getTimeUpperBound() {
    return mTimeTo;
  }

  public int getDayOfWeek() {
    return mDayOfWeek;
  }
  
  public int getCategory() {
    return mCategory;
  }
  
  public ProgramFieldExclusion getProgramFieldExclusion() {
    return mProgramFieldExclusion;
  }

  public boolean isProgramExcluded(Program prog) {
    boolean channelExcl = false;
    boolean titleExcl = false;
    boolean topicExcl = false;
    boolean timeExcl = false;
    boolean dayExcl = false;
    boolean filterExclusion = false;
    boolean episodeTitleExcl = false;
    boolean categoryExcl = false;
    boolean programFieldExcl = false;
    
    if(isInvalid()) {
      return false;
    }
    
    if (mChannel != null && !mChannel.isNullChannel()) {
      Channel ch = prog.getChannel();
      if (ch.equals(mChannel.getChannel())) {
        channelExcl = true;
      }
    }
    else {
      channelExcl = true;
    }
    
    if (mTitle != null) {
      if (mTitle.equalsIgnoreCase(prog.getTitle())) {
        titleExcl = true;
      }
    }
    else {
      titleExcl = true;
    }

    if (mTopic != null) {
      Iterator<ProgramFieldType> types = ProgramFieldType.getTypeIterator();
      StringBuilder value = new StringBuilder();
      
      while(types.hasNext()) {
        ProgramFieldType type = types.next();
        
        if(type.getFormat() == ProgramFieldType.FORMAT_TEXT) {
          value.append(prog.getTextField(type)).append(' ');
        }
      }
            
      if (value.toString() != null && value.toString().toLowerCase().indexOf(mTopic.toLowerCase()) >=0) {
        topicExcl = true;
      }
    } else {
      topicExcl = true;
    }
    
    if(mFilterName != null) {
      ProgramFilter filter = getFilter();
      
      if(filter != null) {
        filterExclusion = filter.accept(prog);
      }
      else {
        filterExclusion = true;
      }
    }
    else {
      filterExclusion = true;
    }

    int timeFromParsed = mTimeFrom;
    int progTime = prog.getHours()*60 + prog.getMinutes();

    if(mTimeFrom > mTimeTo) {
      timeFromParsed -= 60*24;
      
      if(progTime > mTimeTo) {
        progTime -= 24*60;
      }
    }
        
    if (mTimeFrom >=0 && mTimeTo >=0) {
      if (progTime >= timeFromParsed && progTime <= mTimeTo) {
        timeExcl = true;
      }
    }
    else if (mTimeFrom >=0) {
      if (progTime >= timeFromParsed) {
        timeExcl = true;
      }
    }
    else if (mTimeTo >=0) {
      if (progTime <= mTimeTo) {
        timeExcl = true;
      }
    }
    else {
      timeExcl = true;
    }

    if (mDayOfWeek != DAYLIMIT_DAILY) {
      int dayOfWeek = prog.getDate().getCalendar().get(Calendar.DAY_OF_WEEK);
      if (mDayOfWeek >=1 && mDayOfWeek <=7) {
        if (dayOfWeek == mDayOfWeek) {
          dayExcl = true;
        }
      } else if (mDayOfWeek == DAYLIMIT_WEEKEND) {
        dayExcl = dayOfWeek == DAYLIMIT_SUNDAY || dayOfWeek == DAYLIMIT_SATURDAY;
      } else /* (mDayOfWeek == DAYLIMIT_WEEKDAY) */ {
        dayExcl = dayOfWeek > 1 && dayOfWeek <7;
      }
    }
    else {
      dayExcl = true;
    }
    
    if(mEpisodeTitle != null) {
      if(prog.getTextField(ProgramFieldType.EPISODE_TYPE) != null && prog.getTextField(ProgramFieldType.EPISODE_TYPE).equalsIgnoreCase(mEpisodeTitle)) {
        episodeTitleExcl = true;
      }
    }
    else {
      episodeTitleExcl = true;
    }
    
    if(mCategory != 0) {
      categoryExcl = ProgramInfoHelper.bitSet(prog.getInfo(),mCategory);
    }
    else {
      categoryExcl = true;
    }
    
    if(mProgramFieldExclusion != null) {
      ProgramFieldType type = mProgramFieldExclusion.getProgramFieldType();
      
      switch(type.getFormat()) {
        case ProgramFieldType.FORMAT_INT: programFieldExcl = prog.getIntFieldAsString(type) != null && prog.getIntFieldAsString(type).equalsIgnoreCase(mProgramFieldExclusion.getProgramFieldText()); break;
        case ProgramFieldType.FORMAT_TIME: programFieldExcl = prog.getTimeFieldAsString(type) != null && prog.getTimeFieldAsString(type).equalsIgnoreCase(mProgramFieldExclusion.getProgramFieldText()); break;
        case ProgramFieldType.FORMAT_TEXT: programFieldExcl = prog.getTextField(type) != null && prog.getTextField(type).equalsIgnoreCase(mProgramFieldExclusion.getProgramFieldText()); break;
        
        default: programFieldExcl = true;break;
      }
    }
    else {
      programFieldExcl = true;
    }
    
    return channelExcl && titleExcl && topicExcl && timeExcl && dayExcl && filterExclusion && episodeTitleExcl && categoryExcl && programFieldExcl;
  }
  
  /**
   * Gets if this Exclusion is invalid.
   * 
   * @return <code>True</code> if this Exclusion is invalid, <code>false</code> otherwise.
   */
  public boolean isInvalid() {
    return (mTitle == null && mTopic == null && mEpisodeTitle == null && !mChannel.isAvailableOrNullChannel() && mFilterName == null && mTimeFrom == -1 &&mTimeTo == -1 && mDayOfWeek == Exclusion.DAYLIMIT_DAILY && mProgramFieldExclusion == null) || !mChannel.isAvailableOrNullChannel();
  }

  @Override
  public int compareTo(Exclusion other) {
    return toString().compareToIgnoreCase(other.toString());
  }
  
  @Override
  public String toString() {
    StringBuilder textValue = new StringBuilder("<html>");
    ProgramFilter filter = getFilter();
    String timeMsg = createTimeMessage(getTimeLowerBound(), getTimeUpperBound(), getDayOfWeek());
    
    if(mTitle != null) {
      textValue.append(mLocalizer.msg("exclude.title","Exclude all programs with title '")).append(mTitle).append("'");
    }
    if(mTitle != null && mTopic != null) {
      textValue.append(" ").append(mLocalizer.msg("exclude.appendTopic","with topic '")).append(mTopic).append("'");
    }
    else if (mTopic != null) {
      textValue.append(mLocalizer.msg("exclude.topic","Exclude all programs with topic '")).append(mTopic).append("'");
    }
    if(mEpisodeTitle != null && mTopic != null && mTitle != null) {
      textValue.append(" ").append(mLocalizer.msg("exclude.appendEpisodeTitle","Exclude all programs with episode '")).append(mEpisodeTitle).append("'");
    }
    else if (mEpisodeTitle != null) {
      textValue.append(mLocalizer.msg("exclude.episodeTitle","Exclude all programs with topic '")).append(mEpisodeTitle).append("'");
    }      
    if(filter != null && (mTitle != null || mTopic != null || mEpisodeTitle != null)) {
      textValue.append(" ").append(mLocalizer.msg("exclude.appendFilter","of the filter '")).append(filter.getName()).append("'");
    }
    else if(filter != null) {
      textValue.append(mLocalizer.msg("exclude.filter","Exclude all programs of the filter '")).append(new WrapperFilter(filter).toString().replaceAll("</*html>", "")).append("'");
    }
    if(mChannel.getChannel() != null && (mTitle != null || mTopic != null || mEpisodeTitle != null || filter != null)) {
      textValue.append(" ").append(mLocalizer.msg("exclude.appendChannel","on channel '")).append(mChannel.getChannel().getName()).append("'");
    }
    else if(mChannel.getChannel() != null) {
      textValue.append(mLocalizer.msg("exclude.channel","Exclude all programs on channel '")).append(mChannel.getChannel().getName()).append("'");
    }
    if(timeMsg != null && (mTitle != null || mTopic != null || mEpisodeTitle != null || filter != null || mChannel.getChannel() != null)) {
      textValue.append(" ").append(timeMsg);
    }
    else if(timeMsg != null) {
      textValue.append(mLocalizer.msg("exclude.time","Exclude all programs ")).append(timeMsg);
    }
    
    if(mCategory != 0) {
      if(timeMsg != null || mTitle != null || mTopic != null || mEpisodeTitle != null || filter != null || mChannel.getChannel() != null) {
        textValue.append(" ").append(mLocalizer.msg("exclude.appendCategory","with category '")).append(ProgramInfoHelper.getMessageForBit(mCategory)).append("'");
      }
      else {
        textValue.append(mLocalizer.msg("exclude.category","Exclude all programs with category '")).append(ProgramInfoHelper.getMessageForBit(mCategory)).append("'");
      }
    }
    
    if(mProgramFieldExclusion != null) {
      ProgramFieldType exclusion = mProgramFieldExclusion.getProgramFieldType();
      
      if(timeMsg != null || mTitle != null || mTopic != null || mEpisodeTitle != null || filter != null || mChannel.getChannel() != null || mCategory != 0) {
        textValue.append(" ").append(mLocalizer.msg("exclude.append","with '"));
      }
      else {
        textValue.append(mLocalizer.msg("exclude.single","Exclude all programs with '"));
      }
      
      textValue.append(exclusion.getLocalizedName()).append("'='").append(mProgramFieldExclusion.getProgramFieldText()).append("'");
    }
    
    if(textValue.length() < 1) {
      textValue.append(mLocalizer.msg("exclude.invalid","<invalid>"));
    }
    else {
      if(mLocalizer.msg("exclude.appendix",".").length() > 1) {
        textValue.append(" ");
      }
      
      textValue.append(mLocalizer.msg("exclude.appendix","."));
    }
    
    textValue.append("</html>");
    
    return textValue.toString();
  }
  
  private static String createTimeMessage(int lowBnd, int upBnd, int dayOfWeek) {
    int mLow = lowBnd % 60;
    int hLow = lowBnd / 60;
    int mUp = upBnd % 60;
    int hUp = upBnd / 60;

    String lowTime = hLow + ":" + (mLow < 10 ? "0" : "") + mLow;
    String upTime = hUp + ":" + (mUp < 10 ? "0" : "") + mUp;

    if (dayOfWeek != Exclusion.DAYLIMIT_DAILY) {
      String dayStr = DayListCellRenderer.getDayString(dayOfWeek);
      if (lowBnd >= 0 && upBnd >= 0) {
        return mLocalizer.msg("datetimestring.between", "on {0} between {1} and {2}", dayStr, lowTime, upTime);
      } else if (lowBnd >= 0) {
        return mLocalizer.msg("datetimestring.after", "on {0} after {1}", dayStr, lowTime);
      } else if (upBnd >= 0) {
        return mLocalizer.msg("datetimestring.before", "on {0} after {1}", dayStr, upTime);
      } else {
        return mLocalizer.msg("datetimestring.on", "on {0}", dayStr);
      }
    } else {
      if (lowBnd >= 0 && upBnd >= 0) {
        return mLocalizer.msg("timestring.between", "on {0} between {1} and {2}", lowTime, upTime);
      } else if (lowBnd >= 0) {
        return mLocalizer.msg("timestring.after", "on {0} after {1}", lowTime);
      } else if (upBnd >= 0) {
        return mLocalizer.msg("timestring.before", "on {0} after {1}", upTime);
      } else {
        return null;
      }
    }
  }
  
  public static final class ProgramFieldExclusion {
    private int mProgramField;
    private String mProgramFieldText;
    
    public ProgramFieldExclusion(int programField, String programFieldText) {
      mProgramField = programField;
      mProgramFieldText = programFieldText;
    }
    
    private ProgramFieldExclusion(ObjectInputStream in, int version) throws IOException {
      mProgramField = in.readInt();
      mProgramFieldText = in.readUTF();
    }
    
    public int getProgramFieldTypeId() {
      return mProgramField;
    }
    
    public ProgramFieldType getProgramFieldType() {
      return ProgramFieldType.getTypeForId(mProgramField);
    }
    
    public String getProgramFieldText() {
      return mProgramFieldText;
    }
    
    public void setmProgramField(int programField) {
      mProgramField = programField;
    }
    
    public void setmProgramFieldText(String programFieldText) {
      mProgramFieldText = programFieldText;
    }
    
    private void writeData(ObjectOutputStream out) throws IOException {
      out.writeInt(mProgramField);
      out.writeUTF(mProgramFieldText);
    }
  }
}
