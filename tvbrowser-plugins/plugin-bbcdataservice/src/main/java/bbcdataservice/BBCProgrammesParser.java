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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"); //2010-10-03T01:10:00+01:00

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

  private MutableChannelDayProgram mChannelDayProgram;

  private TvDataUpdateManager mUpdateManager;

  public BBCProgrammesParser(TvDataUpdateManager updateManager, Channel channel, Date date) {
    mChannel = channel;
    mDate = date;
    mChannelDayProgram = new MutableChannelDayProgram(date, mChannel);
    mUpdateManager = updateManager;
    mText = new StringBuilder();
  }

  protected static boolean parse(TvDataUpdateManager updateManager, File file, Channel channel, Date date) throws Exception {
    mHasNextDay = false;
    SAXParserFactory fac = SAXParserFactory.newInstance();
    fac.setValidating(false);
    SAXParser sax = fac.newSAXParser();
    sax.parse(file, new BBCProgrammesParser(updateManager, channel, date));
    return mHasNextDay;
  }

  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    mText.append(ch, start, length);
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    mText.setLength(0);
    if ("programme".equalsIgnoreCase(qName)) {
      String programType = attributes.getValue("type");
      if ("episode".equalsIgnoreCase(programType)) {
        mProgramType = PROGRAMME_TYPE_EPISODE;
      }
      else if ("series".equalsIgnoreCase(programType)) {
        mProgramType = PROGRAMME_TYPE_SERIES;
      }
      else if ("brand".equalsIgnoreCase(programType)) {
        mProgramType = PROGRAMME_TYPE_BRAND;
      }
    }
    else if ("ownership".equalsIgnoreCase(qName) || "display_titles".equalsIgnoreCase(qName)) {
      mIgnoreElements = true;
    }
    else if ("day".equalsIgnoreCase(qName)) {
      mHasNextDay = attributes.getValue("has_next").equals("1");
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {
    if (mIgnoreElements) {
      return;
    }
    String text = mText.toString().trim();
    if ("start".equalsIgnoreCase(qName)) {
      Date startDate = extractDate(text);
      int startTime = extractTime(text);
      mProgram = new MutableProgram(mChannel, startDate, startTime /60, startTime % 60, true);
      mProgramType = 0;
    }
    else if ("end".equalsIgnoreCase(qName)) {
      mProgram.setTimeField(ProgramFieldType.END_TIME_TYPE, extractTime(text));
    }
    else if ("duration".equalsIgnoreCase(qName)) {
      mProgram.setLength(Integer.valueOf(text) / 60);
    }
    else if ("title".equalsIgnoreCase(qName)) {
      if (mProgramType == PROGRAMME_TYPE_EPISODE) {
        mProgram.setTextField(ProgramFieldType.EPISODE_TYPE, text);
      }
      else if (mProgramType == PROGRAMME_TYPE_SERIES || mProgramType == PROGRAMME_TYPE_BRAND) {
        mProgram.setTitle(text);
      }
    }
    else if ("position".equalsIgnoreCase(qName)) {
      if (mProgramType == PROGRAMME_TYPE_EPISODE) {
        mProgram.setIntField(ProgramFieldType.EPISODE_NUMBER_TYPE, Integer.valueOf(text));
      }
      else if (mProgramType == PROGRAMME_TYPE_SERIES && text.length() > 0) {
        mProgram.setIntField(ProgramFieldType.SEASON_NUMBER_TYPE, Integer.valueOf(text));
      }
    }
    else if ("expected_child_count".equalsIgnoreCase(qName)) {
      if (mProgramType == PROGRAMME_TYPE_SERIES) {
        mProgram.setIntField(ProgramFieldType.EPISODE_TOTAL_NUMBER_TYPE, Integer.valueOf(text));
      }
    }
    else if ("short_synopsis".equalsIgnoreCase(qName)) {
      mProgram.setShortInfo(text);
    }
    else if ("ownership".equalsIgnoreCase(qName) || "display_titles".equalsIgnoreCase(qName)) {
      mIgnoreElements = false;
    }
    else if ("broadcast".equalsIgnoreCase(qName)) {
      // finish the program itself
      mProgram.setProgramLoadingIsComplete();
      mChannelDayProgram.addProgram(mProgram);
    }
    else if ("schedule".equalsIgnoreCase(qName)) {
      // finish the file
      mUpdateManager.updateDayProgram(mChannelDayProgram);
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
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(DATE_FORMAT.parse(time.substring(0, 20)));
      calendar.setTimeInMillis(calendar.getTimeInMillis() - calendar.getTimeZone().getRawOffset());
      return calendar;
    } catch (ParseException e) {
//      logMessage("invalid time format: " + time);
      return null;
    }
  }

}
