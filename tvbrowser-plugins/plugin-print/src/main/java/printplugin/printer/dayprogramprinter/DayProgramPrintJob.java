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
 *     $Date: 2008-02-26 21:43:52 +0100 (Di, 26 Feb 2008) $
 *   $Author: bananeweizen $
 * $Revision: 4315 $
 */

package printplugin.printer.dayprogramprinter;


import java.awt.Font;
import java.awt.Graphics;
import java.awt.print.PageFormat;

import printplugin.printer.AbstractPrintJob;
import printplugin.printer.ColumnModel;
import printplugin.printer.Page;
import printplugin.printer.PageModel;
import printplugin.settings.DayProgramPrinterSettings;
import printplugin.settings.ProgramIconSettings;



/**
 * Creates a PrintJob out of a PageModel array.
 * The resulting pages will look like a pages of a paper bases TV guide.
 */
public class DayProgramPrintJob extends AbstractPrintJob {

  private DayProgramPrinterSettings mSettings;

  private static final Font HEADER_FONT = new Font("Dialog",Font.BOLD,24);
  private static final Font FOOTER_FONT = new Font("Dialog",Font.ITALIC,6);

  public DayProgramPrintJob(PageModel[] pageModelArr, DayProgramPrinterSettings settings, PageFormat pageFormat) {
    super(pageModelArr, pageFormat);
    mSettings = settings;
  }

  protected Page[] createPages(PageModel pageModel) {

    int colCnt = pageModel.getColumnCount();

    int channelsPerPage = mSettings.getColumnCount() * mSettings.getChannelsPerColumn();

    int numberOfPages;
    if (colCnt==0) {
      numberOfPages=0;
    }
    else {
      numberOfPages = (colCnt-1) / channelsPerPage +1;
    }

    Page[] result = new Page[numberOfPages];

    for (int i=0;i<result.length;i++) {

      int fromInx = i * channelsPerPage;
      int inxCnt = channelsPerPage;

      if (fromInx+inxCnt >  colCnt) {
        inxCnt = colCnt-fromInx;
      }

      ColumnModel[] cols=getColumns(pageModel, fromInx, inxCnt);
      result[i]=new ChannelPage(cols, getPageFormat(), mSettings.getColumnCount(), mSettings.getChannelsPerColumn(), pageModel.getHeader(), pageModel.getFooter(), mSettings.getDayStartHour(), mSettings.getDayEndHour(), mSettings.getProgramIconSettings()/*PrinterProgramIconSettings.create()*/);
    }

    return result;


}

  /**
   *
   * @param model
   * @param fromInx
   * @param cnt
   * @return cnt columns of the page model.
   */
  private ColumnModel[] getColumns(PageModel model, int fromInx, int cnt) {
    ColumnModel[] result = new ColumnModel[cnt];
    for (int i=fromInx;i<fromInx+cnt;i++) {
      result[i-fromInx]=model.getColumnAt(i);
    }
    return result;
  }


  /**
   * A Page with one column per channel
   */
  private static class ChannelPage implements Page {


    private ProgramTableIcon[] mProgramTableIcons;

    private PageFormat mPageFormat;
    private static final int HEADER_SPACE=30;
    private static final int FOOTER_SPACE=10;
    private static final int TABLE_SPACE=10;
    private String mHeader, mFooter;



    public ChannelPage(ColumnModel[] cols, PageFormat pageFormat, int maxColsPerPage, int channelsPerColumn, String header, String footer, int startHour, int endHour, ProgramIconSettings settings) {

      int tableHeight = ((int)pageFormat.getImageableHeight() - HEADER_SPACE - FOOTER_SPACE - (channelsPerColumn-1)*TABLE_SPACE)/channelsPerColumn;
      mProgramTableIcons = new ProgramTableIcon[channelsPerColumn];
      for (int i=0; i<channelsPerColumn; i++) {
        int fromInx = i*maxColsPerPage;
        int toInx = (i+1)*maxColsPerPage-1;
        if (fromInx >= cols.length) {
          break;
        }
        if (toInx >= cols.length) {
          toInx = cols.length-1;
        }
        ColumnModel[] columns = new ColumnModel[toInx-fromInx+1];

        System.arraycopy(cols, fromInx, columns, 0, columns.length);
        mProgramTableIcons[i] = new ProgramTableIcon(columns, (int)pageFormat.getImageableWidth(), tableHeight , maxColsPerPage, startHour, endHour, settings);
      }

      mPageFormat = pageFormat;
      mHeader = header;
      mFooter = footer;
    }


    public PageFormat getPageFormat() {
      return mPageFormat;
    }

    public void printPage(Graphics graphics) {

      int x0 = (int)mPageFormat.getImageableX();
      int y0 = (int)mPageFormat.getImageableY();


      graphics.setFont(HEADER_FONT);
      graphics.drawString(mHeader, x0, y0+HEADER_FONT.getSize());

      graphics.setFont(FOOTER_FONT);
      graphics.drawString(mFooter, x0, y0 + (int)mPageFormat.getImageableHeight()-3);

      for (int i=0; i<mProgramTableIcons.length; i++) {
        if (mProgramTableIcons[i] != null) {
          mProgramTableIcons[i].paintIcon(null, graphics, x0, y0 + HEADER_SPACE + (mProgramTableIcons[i].getIconHeight()+TABLE_SPACE)*i);
        }
      }

    }
  }



}
