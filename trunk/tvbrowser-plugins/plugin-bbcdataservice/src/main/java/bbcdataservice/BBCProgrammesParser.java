/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package bbcdataservice;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import tvdataservice.TvDataUpdateManager;
import devplugin.Channel;
import devplugin.Date;
import devplugin.ProgramFieldType;

public class BBCProgrammesParser extends DefaultHandler {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); // 2010-10-03T01:10:00+01:00
  private static final SimpleDateFormat DATE_FORMAT_ZULU = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  private static final int PROGRAMME_TYPE_EPISODE = 1;

  private static final int PROGRAMME_TYPE_SERIES = 2;

  private static final int PROGRAMME_TYPE_BRAND = 3;

  private static StringBuilder mText;

  private static boolean mHasNextDay = false;
  private Channel mChannel;

  private MutableProgram mProgram;

  private int mProgramType = 0;

  private boolean mIgnoreElements;

  private Date mDate;

  private TvDataUpdateManager mUpdateManager;

  private HashMap<Date, MutableChannelDayProgram> mDayPrograms;

  public BBCProgrammesParser(HashMap<Date, MutableChannelDayProgram> dayPrograms, Channel channel, Date date) {
    mChannel = channel;
    mDate = date;
    mDayPrograms = dayPrograms;
    mText = new StringBuilder();
  }

  protected static boolean parse(HashMap<Date,MutableChannelDayProgram> dayPrograms, File file, Channel channel, Date date)
      throws Exception {
    mHasNextDay = false;
    SAXParserFactory fac = SAXParserFactory.newInstance();
    fac.setValidating(false);
    SAXParser sax = fac.newSAXParser();
    sax.parse(file, new BBCProgrammesParser(dayPrograms, channel, date));
    return mHasNextDay;
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    mText.append(ch, start, length);
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    try {
      mText.setLength(0);
      if ("programme".equalsIgnoreCase(qName)) {
        String programType = attributes.getValue("type");
        if ("episode".equalsIgnoreCase(programType)) {
          mProgramType = PROGRAMME_TYPE_EPISODE;
        } else if ("series".equalsIgnoreCase(programType)) {
          mProgramType = PROGRAMME_TYPE_SERIES;
        } else if ("brand".equalsIgnoreCase(programType)) {
          mProgramType = PROGRAMME_TYPE_BRAND;
        }
      } else if ("ownership".equalsIgnoreCase(qName) || "display_titles".equalsIgnoreCase(qName)) {
        mIgnoreElements = true;
      } else if ("day".equalsIgnoreCase(qName)) {
        mHasNextDay = attributes.getValue("has_next").equals("1");
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if ("ownership".equalsIgnoreCase(qName) || "display_titles".equalsIgnoreCase(qName)) {
      mIgnoreElements = false;
    }
    if (mIgnoreElements) {
      return;
    }
    try {
      String text = mText.toString().trim();
      if ("start".equalsIgnoreCase(qName)) {
        mProgram = null;
        Date startDate = extractDate(text);
        int startTime = extractTime(text);
        if (startDate != null && startTime >= 0) {
          mProgram = new MutableProgram(mChannel, startDate, startTime / 60, startTime % 60, true);
          mProgramType = 0;
        }
      } else if ("end".equalsIgnoreCase(qName)) {
        mProgram.setTimeField(ProgramFieldType.END_TIME_TYPE, extractTime(text));
      } else if ("duration".equalsIgnoreCase(qName)) {
        if (text.length() > 0) {
          mProgram.setLength(Integer.valueOf(text) / 60);
        }
      } else if ("title".equalsIgnoreCase(qName)) {
        if (mProgramType == PROGRAMME_TYPE_EPISODE) {
          mProgram.setTextField(ProgramFieldType.EPISODE_TYPE, text);
          // some episodes don't list their series, so use the episode title as program title
          if (StringUtils.isEmpty(mProgram.getTitle())) {
            mProgram.setTitle(text);
          }
        } else if (mProgramType == PROGRAMME_TYPE_SERIES || mProgramType == PROGRAMME_TYPE_BRAND) {
          mProgram.setTitle(text);
        }
      } else if ("position".equalsIgnoreCase(qName) && text.length() > 0) {
        if (mProgramType == PROGRAMME_TYPE_EPISODE) {
          mProgram.setIntField(ProgramFieldType.EPISODE_NUMBER_TYPE, Integer.valueOf(text));
        } else if (mProgramType == PROGRAMME_TYPE_SERIES) {
          mProgram.setIntField(ProgramFieldType.SEASON_NUMBER_TYPE, Integer.valueOf(text));
        }
      } else if ("expected_child_count".equalsIgnoreCase(qName)) {
        if (text.length() > 0) {
          if (mProgramType == PROGRAMME_TYPE_SERIES) {
            mProgram.setIntField(ProgramFieldType.EPISODE_TOTAL_NUMBER_TYPE, Integer.valueOf(text));
          }
        }
      } else if ("short_synopsis".equalsIgnoreCase(qName)) {
        mProgram.setShortInfo(text);
      } else if ("pid".equalsIgnoreCase(qName)) {
        if (text.length() > 0) {
          String url = "http://www.bbc.co.uk/programmes/" + text;
          String oldUrl = mProgram.getTextField(ProgramFieldType.URL_TYPE);
          if (StringUtils.isEmpty(oldUrl)) {
            mProgram.setTextField(ProgramFieldType.URL_TYPE, url);
          }
        }
      } else if ("broadcast".equalsIgnoreCase(qName)) {
        // finish the program itself
        mProgram.setProgramLoadingIsComplete();
        Date date = mProgram.getDate();
        MutableChannelDayProgram dayProgram = mDayPrograms.get(date);
        if (dayProgram == null) {
          dayProgram = new MutableChannelDayProgram(date, mChannel);
          mDayPrograms.put(date, dayProgram);
        }
        dayProgram.addProgram(mProgram);
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private int extractTime(final String dateTime) {
    Calendar cal = parseDateTime(dateTime);
    if (cal == null) {
      return -1;
    }
    int hour = cal.get(Calendar.HOUR_OF_DAY);
    int minute = cal.get(Calendar.MINUTE);
    return hour * 60 + minute;
  }

  /**
   * Extracts the date from a XMLTV time value.
   *
   * @param dateTime
   *          The value to extract the date from.
   * @return The date.
   */
  private Date extractDate(final String dateTime) {
    Calendar cal = parseDateTime(dateTime);
    if (cal == null) {
      return null;
    }
    return new devplugin.Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
  }

  private synchronized Calendar parseDateTime(final String time) {
    try {
      if (time.endsWith("Z")) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DATE_FORMAT_ZULU.parse(time.substring(0, time.indexOf("Z"))));
        return calendar;
      }
      String withoutSeparator = time.substring(0, 22) + time.substring(23);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(DATE_FORMAT.parse(withoutSeparator));
      //calendar.setTimeInMillis(calendar.getTimeInMillis() - calendar.getTimeZone().getRawOffset());
      return calendar;
    } catch (ParseException e) {
      // logMessage("invalid time format: " + time);
      return null;
    }
  }

}
