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
 *     $Date: 2008-05-27 21:09:30 +0200 (Di, 27 Mai 2008) $
 *   $Author: troggan $
 * $Revision: 4740 $
 */
import java.io.IOException;
import java.util.HashMap;

import primarydatamanager.primarydataservice.ProgramFrameDispatcher;
import tvbrowserdataservice.file.ProgramField;
import tvbrowserdataservice.file.ProgramFrame;
import util.io.FileFormatException;
import util.misc.AbstractXmlTvDataHandler;
import devplugin.Channel;
import devplugin.Date;
import devplugin.ProgramFieldType;

/**
 * A handler that parses TV data from XML.
 *
 * ToDo: The following Elements must be implemented: -  - CAMERA_TYPE - CUTTER_TYPE -
 * ADDITIONAL_PERSONS_TYPE - PRODUCTION_COMPANY_TYPE
 *
 * INFO_CATEGORIE_NEWS -
 * INFO_CATEGORIE_SHOW - INFO_CATEGORIE_MAGAZINE_INFOTAINMENT -
 * INFO_CATEGORIE_DOCUMENTARY - INFO_CATEGORIE_ARTS - INFO_CATEGORIE_SPORTS -
 * INFO_CATEGORIE_CHILDRENS - INFO_CATEGORIE_OTHERS
 */
class XmlTvDataHandler extends AbstractXmlTvDataHandler {

  /**
   * The program dispatchers. (key: channel id, value: ProgramFrameDispatcher)
   */
  private HashMap<String, ProgramFrameDispatcher> mDispatcherHash = new HashMap<String, ProgramFrameDispatcher>();

  /**
   * The country of the current ProgramFrame's channel.
   */
  private String mChannelCountry;

  /**
   * The current ProgramFrame.
   */
  private ProgramFrame mFrame;

  /**
   * The date of the current ProgramFrame
   */
  private Date mDate;

  /**
   * The PDS that is used in this handler
   */
  private XmlTvPDS mXmlTvPDS;

  /**
   * Creates a new instance of TvDataHandler.
   *
   * @param xmlTvPDS
   *          The PDS that is used for the handler
   */
  public XmlTvDataHandler(XmlTvPDS xmlTvPDS) {
    this.mXmlTvPDS = xmlTvPDS;
  }

  private ProgramFrameDispatcher getProgramDispatcher(String channelId) {
    ProgramFrameDispatcher dispatcher = mDispatcherHash.get(channelId);
    if (dispatcher == null) {
      String[] s = channelId.split("_");
      Channel channel;
      if (s.length == 2) {
        channel = new Channel(null, s[0], s[1], java.util.TimeZone.getTimeZone("GMT+1"), "de", "(c) by TV-Browser");
      } else {
        channel = new Channel(null, s[0], s[0], java.util.TimeZone.getTimeZone("GMT+1"), "de", "(c) by TV-Browser");
      }
      dispatcher = new ProgramFrameDispatcher(channel);
      mDispatcherHash.put(channelId, dispatcher);
    }
    return dispatcher;
  }

  protected void setInfoBit(int bit) {
    int info = 0;

    // Try to get the already set info bits
    ProgramField infoField = mFrame.removeProgramFieldOfType(ProgramFieldType.INFO_TYPE);
    if (infoField != null) {
      info = infoField.getIntData();
    }

    // Add the bit
    info |= bit;

    // Set the changed info bits
    addField(ProgramField.create(ProgramFieldType.INFO_TYPE, info));
  }

  private void addField(ProgramField field) {
    if (field == null) {
      return;
    }

    ProgramField existingField = mFrame.getProgramFieldOfType(field.getType());
    if (existingField == null) {
      // There is no such field -> Add the new one
      mFrame.addProgramField(field);
    } else {
      // We already have this kind of field -> log a warning
      mXmlTvPDS.logMessage("WARNING: There is already a field of the type '" + field.getType().getName()
          + "': existing value: " + existingField.getDataAsString() + ", ignored value: " + field.getDataAsString());
    }
  }

  /**
   * Stores the extracted TV data in a directory.
   *
   * @param targetDir
   *          The directory where to write the raw TV data.
   * @throws java.io.IOException
   *           If writing the tv data failed.
   * @throws util.io.FileFormatException
   *           If the extracted TV data has an illegal format.
   */
  public void storeTvData(String targetDir) throws IOException, FileFormatException {
    for (ProgramFrameDispatcher dis : mDispatcherHash.values()) {
      dis.store(targetDir);
    }
  }

  @Override
  protected void logException(final Exception exc) {
    mXmlTvPDS.logException(exc);
  }

  @Override
  protected void logMessage(final String message) {
    mXmlTvPDS.logMessage(message);
  }

  @Override
  protected void addField(final ProgramFieldType fieldType, final int value) {
    addField(ProgramField.create(fieldType, value));
  }

  @Override
  protected void addField(final ProgramFieldType fieldType, final String value) {
    addField(ProgramField.create(fieldType, value));
  }

  @Override
  protected void addField(ProgramFieldType fieldType, byte[] value) {
    addField(ProgramField.create(fieldType, value));
  }

  @Override
  protected boolean isValid() {
    return super.isValid() && mFrame != null;
  }

  @Override
  protected void startProgram(final Date date, final int startTime) {
    ProgramFrameDispatcher dispatcher = getProgramDispatcher(getChannelId());
    mChannelCountry = dispatcher.getChannel().getCountry();

    mFrame = new ProgramFrame();
  }

  @Override
  protected void endProgram() {
    ProgramFrameDispatcher dis = mDispatcherHash.get(getChannelId());
    if (dis != null) {
      dis.dispatchProgramFrame(mFrame, mDate);
    }

    mFrame = null;
  }

  @Override
  protected void addToList(final ProgramFieldType fieldType, String value, final String separator) {
    ProgramField field = mFrame.removeProgramFieldOfType(fieldType);
    if (field != null) {
      String currentValue = field.getTextData();
      if (currentValue.length() > 0) {
        value = currentValue + separator + value;
      }
    }

    // Set the text
    addField(ProgramField.create(fieldType, value));
  }

  @Override
  protected String getChannelCountry() {
    return mChannelCountry;
  }
}
