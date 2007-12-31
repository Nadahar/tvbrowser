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
 *     $Date: 2007-09-20 23:45:38 +0200 (Do, 20 Sep 2007) $
 *   $Author: bananeweizen $
 * $Revision: 3894 $
 */
package dreamboxdataservice;

import tvdataservice.TvDataUpdateManager;
import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;

public class DreamboxChannelHandler extends DefaultHandler {
    StringBuffer mCharacters = new StringBuffer();

    /**
     * map of lazily created update channel day programs
     */
    private HashMap<devplugin.Date, MutableChannelDayProgram> mMutMap = new HashMap<devplugin.Date, MutableChannelDayProgram>();
    private Channel mChannel;
    private HashMap<String, String> mCurrentEvent;
    private TvDataUpdateManager mUpdateManager;
    private static final int MAX_SHORT_DESCRIPTION_LENGTH = 200;

    public DreamboxChannelHandler(TvDataUpdateManager updateManager, Channel ch) {
        mChannel = ch;
        mUpdateManager = updateManager;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        mCharacters = new StringBuffer();
        if (qName.equals("e2event")) {
            mCurrentEvent = new HashMap<String, String>();
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("e2event")) {
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new java.util.Date(Long.parseLong(mCurrentEvent.get("e2eventstart")) * 1000));
                Date programDate = new Date(cal);
                MutableProgram prog = new MutableProgram(mChannel, programDate, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
                prog.setTitle(mCurrentEvent.get("e2eventtitle"));

                prog.setDescription(mCurrentEvent.get("e2eventdescriptionextended"));

                String shortDesc = mCurrentEvent.get("e2eventdescription");

                if (shortDesc.equals(prog.getTitle())) {
                    shortDesc = "";
                }

                if (shortDesc.length() == 0) {
                    shortDesc = mCurrentEvent.get("e2eventdescriptionextended");
                }
                if (shortDesc.length() > MAX_SHORT_DESCRIPTION_LENGTH) {
                    int lastSpacePos = shortDesc.lastIndexOf(' ',
                            MAX_SHORT_DESCRIPTION_LENGTH - 3);
                    if (lastSpacePos == -1) {
                        shortDesc = shortDesc
                                .substring(0, MAX_SHORT_DESCRIPTION_LENGTH - 3)
                                + "...";
                    } else {
                        shortDesc = shortDesc.substring(0, lastSpacePos) + "...";
                    }
                }

                prog.setShortInfo(shortDesc);
                prog.setLength(Integer.parseInt(mCurrentEvent.get("e2eventduration")) / 60);

                prog.setProgramLoadingIsComplete();

                MutableChannelDayProgram mutDayProg = getMutableDayProgram(programDate);
                mutDayProg.addProgram(prog);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else if (qName.equals("e2eventlist")) {
            storeDayPrograms(mUpdateManager);
        } else {
            mCurrentEvent.put(qName, mCharacters.toString());
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        mCharacters.append(ch, start, length);
    }

    /**
     * @param date Date to search for
     * @return MutableChannelDayProgram that fits to the Date, a new one is created if needed
     */
    private MutableChannelDayProgram getMutableDayProgram(devplugin.Date date) {
        MutableChannelDayProgram dayProgram = mMutMap.get(date);

        if (dayProgram == null) {
            dayProgram = new MutableChannelDayProgram(date, mChannel);
            mMutMap.put(date, dayProgram);
        }

        return dayProgram;
    }

    private void storeDayPrograms(TvDataUpdateManager updateManager) {
        for (MutableChannelDayProgram newDayProg : getAllMutableDayPrograms()) {
            // compare new and existing programs to avoid unnecessary updates
            boolean update = true;

            Iterator<Program> itCurrProg = DreamboxDataService.getPluginManager().getChannelDayProgram(newDayProg.getDate(), mChannel);
            Iterator<Program> itNewProg = newDayProg.getPrograms();
            if (itCurrProg != null && itNewProg != null) {
                update = false;
                while (itCurrProg.hasNext() && itNewProg.hasNext()) {
                    MutableProgram currProg = (MutableProgram) itCurrProg.next();
                    MutableProgram newProg = (MutableProgram) itNewProg.next();
                    if (!currProg.equalsAllFields(newProg)) {
                        update = true;
                    }
                }
                // not the same number of programs ?
                if (itCurrProg.hasNext() != itNewProg.hasNext()) {
                    update = true;
                }
            }
            if (update) {
                updateManager.updateDayProgram(newDayProg);
            }
        }
    }

    /**
     * @return all MutableChannelDayPrograms
     */
    private Collection<MutableChannelDayProgram> getAllMutableDayPrograms() {
        return mMutMap.values();
    }

}
