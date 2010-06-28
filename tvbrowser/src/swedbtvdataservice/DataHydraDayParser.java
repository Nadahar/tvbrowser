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
package swedbtvdataservice;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import util.misc.AbstractXmlTvDataHandler;
import util.ui.Localizer;
import devplugin.Channel;
import devplugin.Date;
import devplugin.ProgramFieldType;

/**
 * @author bananeweizen
 *
 */
class DataHydraDayParser extends AbstractXmlTvDataHandler {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(DataHydraDayParser.class);

  private static final Logger mLog = Logger.getLogger(DataHydraDayParser.class.getName());

  private Hashtable<String, MutableChannelDayProgram> mDayProgsHashTable;

  private MutableChannelDayProgram mMcdp;

  private Channel mChannel;

  private SweDBTvDataService mDataService;

  private MutableProgram mProgram;

  /**
   * Creates a new instance of DataHydraDayParser
   */
  private DataHydraDayParser(Channel ch, Hashtable<String, MutableChannelDayProgram> lht, SweDBTvDataService dataService) {
    mChannel = ch;
    mDayProgsHashTable = lht;
    mDataService = dataService;
  }

  public InputSource resolveEntity(String publicId, String systemId) {
    byte[] temp = new byte[0];
    // strib...
    return new InputSource(new ByteArrayInputStream(temp));
  }

  @Override
  protected void startProgram(final Date startDate, final int startTime) {
    if (!mDayProgsHashTable.containsKey(startDate.toString())) {
      mMcdp = new MutableChannelDayProgram(startDate, mChannel);
      mDayProgsHashTable.put(startDate.toString(), mMcdp);
    } else {
      mMcdp = mDayProgsHashTable.get(startDate.toString());
    }
    int startHour = startTime / 60;
    int startMinute = startTime % 60;
    mProgram = new MutableProgram(mMcdp.getChannel(), startDate, startHour, startMinute, true);
  }

  public void fatalError(SAXParseException e) {
    // empty
  }

  public void error(SAXParseException e) {
  }

  public void warning(SAXParseException e) {
  }

  protected static void parseNew(InputStream in, Channel ch, devplugin.Date day,
      Hashtable<String, MutableChannelDayProgram> ht, SweDBTvDataService dataService) throws Exception {
    SAXParserFactory fac = SAXParserFactory.newInstance();
    fac.setValidating(false);
    SAXParser sax = fac.newSAXParser();
    InputSource input = new InputSource(in);
    input.setSystemId(new File("/").toURI().toURL().toString());
    sax.parse(input, new DataHydraDayParser(ch, ht, dataService));
  }

  @Override
  protected void addField(final ProgramFieldType fieldType, final byte[] value) {
    mProgram.setBinaryField(fieldType, value);
  }

  @Override
  protected void addField(final ProgramFieldType fieldType, String value) {
    if (fieldType.equals(ProgramFieldType.DESCRIPTION_TYPE)) {
      if (((DataHydraChannelGroup) mChannel.getGroup()).isShowRegister() && mDataService.getShowRegisterText()) {
        value += "\n\n" + mLocalizer.msg("register", "Please Register at {0}", mChannel.getWebpage());
      }
    }
    mProgram.setTextField(fieldType, value);
    if (fieldType.equals(ProgramFieldType.TITLE_TYPE)) {
      mProgram.setTitle(value);
    }
  }

  @Override
  protected void addField(final ProgramFieldType fieldType, final int value) {
    if (fieldType.getFormat() == ProgramFieldType.TIME_FORMAT) {
      mProgram.setTimeField(fieldType, value);
    }
    else {
      mProgram.setIntField(fieldType, value);
    }
    if (fieldType.equals(ProgramFieldType.END_TIME_TYPE)) {
      int endTime = value;
      int startTime = mProgram.getTimeField(ProgramFieldType.START_TIME_TYPE);
      int progLength = endTime - startTime;
      // Assumption: If the program length is less than 0, the program spans
      // midnight
      if (progLength < 0) {
        progLength += 24 * 60; // adding 24 hours to the length
      }
      // Only allow program length for 12 hours.... This will take care of
      // possible DST problems
      if ((progLength > 0) && (progLength < 12 * 60)) {
        mProgram.setLength(progLength);
      }
    }
  }

  @Override
  protected void addToList(final ProgramFieldType fieldType, String value, final String separator) {
    String currentValue = mProgram.getTextField(fieldType);
    if (currentValue != null && !currentValue.isEmpty()) {
      value = currentValue + separator + value;
    }

    mProgram.setTextField(fieldType, value);
  }

  @Override
  protected void endProgram() {
    // finish the program itself
    mProgram.setProgramLoadingIsComplete();
    mMcdp.addProgram(mProgram);
  }

  @Override
  protected String getChannelCountry() {
    return mProgram.getChannel().getCountry();
  }

  @Override
  protected void logException(final Exception exc) {
    mLog.warning(exc.getMessage());
  }

  @Override
  protected void logMessage(final String message) {
    mLog.info(message);
  }

  @Override
  protected void setInfoBit(final int bit) {
    int infoBits = mProgram.getIntField(ProgramFieldType.INFO_TYPE);
    if (infoBits == -1) {
      infoBits = 0;
    }
    infoBits |= bit;
    mProgram.setIntField(ProgramFieldType.INFO_TYPE, infoBits);
  }

}
