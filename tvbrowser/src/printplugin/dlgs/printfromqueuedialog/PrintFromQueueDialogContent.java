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

package printplugin.dlgs.printfromqueuedialog;

import printplugin.dlgs.DialogContent;
import printplugin.settings.*;
import printplugin.printer.*;
import printplugin.printer.PrintJob;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;

import devplugin.Program;


public class PrintFromQueueDialogContent implements DialogContent {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
         = util.ui.Localizer.getLocalizerFor(PrintFromQueueDialogContent.class);

  private GeneralTab mGeneralTab;
  private LayoutTab mLayoutTab;
  private ExtrasTab mExtrasTab;
  private Program[] mPrograms;

  private Frame mParentFrame;

  public PrintFromQueueDialogContent(Program[] programs, Frame parentFrame) {
    mPrograms = programs;
    mParentFrame = parentFrame;
  }

  public Component getContent() {
    JTabbedPane tab = new JTabbedPane();
    mGeneralTab = new GeneralTab();
    mLayoutTab = new LayoutTab();
    mExtrasTab = new ExtrasTab(mParentFrame);
    tab.add(mLocalizer.msg("listingsTab", "Daten"), mGeneralTab);
    tab.add(mLocalizer.msg("layoutTab","Layout"), mLayoutTab);
    tab.add(mLocalizer.msg("miscTab","Extras"), mExtrasTab);
    return tab;

  }

  public String getDialogTitle() {
    return mLocalizer.msg("dialogTitle","Print from queue");
  }

  public Settings getSettings() {
    return new QueuePrinterSettings(mGeneralTab.emptyQueueAfterPrinting(), mLayoutTab.getColumnsPerPage(), mExtrasTab.getProgramIconSettings());
  }

  public void setSettings(Settings s) {
    QueuePrinterSettings settings = (QueuePrinterSettings)s;
    mGeneralTab.setEmptyQueueAfterPrinting(settings.emptyQueueAfterPrinting());
    mLayoutTab.setColumnsPerPage(settings.getColumnsPerPage());
    mExtrasTab.setProgramIconSettings(settings.getProgramIconSettings());
  }

  public PrintJob createPrintJob(PageFormat format) {
    return JobFactory.createPrintJob((QueuePrinterSettings)getSettings(), format, mPrograms);
  }

  public Scheme createNewScheme(String schemeName) {
    return new QueueScheme(schemeName);
  }
}
