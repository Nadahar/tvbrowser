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
 *     $Date: 2006-10-18 16:47:20 +0200 (Mi, 18 Okt 2006) $
 *   $Author: ds10 $
 * $Revision: 2755 $
 */

package printplugin.dlgs.printdayprogramsdialog;

import java.awt.Component;
import java.awt.Frame;
import java.awt.print.PageFormat;

import javax.swing.JTabbedPane;

import printplugin.dlgs.DialogContent;
import printplugin.printer.JobFactory;
import printplugin.printer.PrintJob;
import printplugin.settings.DayProgramPrinterSettings;
import printplugin.settings.DayProgramScheme;
import printplugin.settings.Scheme;
import printplugin.settings.Settings;
import devplugin.Channel;
import devplugin.Date;


public class PrintDayProgramsDialogContent implements DialogContent {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(PrintDayProgramsDialogContent.class);
  private Frame mParentFrame;

  private LayoutTab mLayoutTab;
  private ListingsTab mListingsTab;
  private ExtrasTab mExtrasTab;

  public PrintDayProgramsDialogContent(Frame parent) {
    mParentFrame = parent;

  }

  public void printingDone() {

  }

  public Component getContent() {

    JTabbedPane tab = new JTabbedPane();

    mListingsTab = new ListingsTab(mParentFrame);
    mLayoutTab = new LayoutTab();
    mExtrasTab = new ExtrasTab(mParentFrame);
    tab.add(mLocalizer.msg("listingsTab", "Daten"), mListingsTab);
    tab.add(mLocalizer.msg("layoutTab","Layout"), mLayoutTab);
    tab.add(mLocalizer.msg("miscTab","Extras"), mExtrasTab);
    return tab;
  }

  public String getDialogTitle() {
    return mLocalizer.msg("dialogTitle","Tagesprogramme drucken");
  }

  public Settings getSettings() {
    return new DayProgramPrinterSettings(mListingsTab.getDateFrom(),
        mListingsTab.getNumberOfDays(),
        mListingsTab.getChannels(),
        mListingsTab.getFromTime(),
        mListingsTab.getToTime(),
        mLayoutTab.getColumnsPerPage(),
        mLayoutTab.getChannelsPerColumn(),
        mExtrasTab.getProgramIconSettings(),
        mListingsTab.getSelectedFilter()
        );
  }

  public void setSettings(Settings s) {
    DayProgramPrinterSettings settings = (DayProgramPrinterSettings)s;
    Channel[] ch = settings.getChannelList();
    mListingsTab.setChannels(ch);
    int start = settings.getDayStartHour();
    int end = settings.getDayEndHour();
    mListingsTab.setTimeRange(start, end);
    Date from = settings.getFromDay();
    mListingsTab.setDateFrom(from);
    mListingsTab.setDayCount(settings.getNumberOfDays());
    mLayoutTab.setColumnLayout(settings.getColumnCount(), settings.getChannelsPerColumn());
    mExtrasTab.setProgramIconSettings(settings.getProgramIconSettings());
  }


  public PrintJob createPrintJob(PageFormat format) {
    return JobFactory.createPrintJob((DayProgramPrinterSettings)getSettings(), format);
  }


  public Scheme createNewScheme(String schemeName) {
    return new DayProgramScheme(schemeName);
  }


}
