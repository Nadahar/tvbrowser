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

package printplugin.printer;

import printplugin.settings.DayProgramPrinterSettings;
import printplugin.settings.ProgramIconSettings;
import printplugin.settings.PrinterProgramIconSettings;


import java.awt.print.PageFormat;

import java.awt.*;



public class DayProgramPrintJob extends AbstractPrintJob {

  private DayProgramPrinterSettings mSettings;

  private static final Font HEADER_FONT = new Font("Dialog",Font.BOLD,32);
  private static final Font FOOTER_FONT = new Font("Dialog",Font.ITALIC,6);

  public DayProgramPrintJob(PageModel[] pageModelArr, DayProgramPrinterSettings settings) {
    super(pageModelArr);
    mSettings = settings;
  }

  protected Page[] createPages(PageModel pageModel) {

    int colCnt = pageModel.getColumnCount();

    int numberOfPages;
    if (colCnt==0) {
      numberOfPages=0;
    }
    else {
      numberOfPages = (colCnt-1) / mSettings.getColumnCount() +1;
    }

    Page[] result = new Page[numberOfPages];

    for (int i=0;i<result.length;i++) {

      int fromInx = i * mSettings.getColumnCount();
      int inxCnt = mSettings.getColumnCount();

      if (fromInx+inxCnt >  colCnt) {
        inxCnt = colCnt-fromInx;
      }

      ColumnModel[] cols=getColumns(pageModel, fromInx, inxCnt);
      result[i]=new ChannelPage(cols, mSettings.getPageFormat(), mSettings.getColumnCount(), pageModel.getHeader(), pageModel.getFooter(), mSettings.getDayStartHour(), mSettings.getDayEndHour(), PrinterProgramIconSettings.create());
    }

    return result;


}

  private ColumnModel[] getColumns(PageModel model, int fromInx, int cnt) {
    ColumnModel[] result = new ColumnModel[cnt];
    for (int i=fromInx;i<fromInx+cnt;i++) {
      result[i-fromInx]=model.getColumnAt(i);
    }
    return result;
  }



  class ChannelPage implements Page {


    private ProgramTableIcon mProgramTableIcon;
    private PageFormat mPageFormat;
    private static final int HEADER_SPACE=30;
    private static final int FOOTER_SPACE=10;
    private String mHeader, mFooter;



    public ChannelPage(ColumnModel[] cols, PageFormat pageFormat, int maxColsPerPage, String header, String footer, int startHour, int endHour, ProgramIconSettings settings) {

      mProgramTableIcon = new ProgramTableIcon(cols, (int)pageFormat.getImageableWidth(), (int)pageFormat.getImageableHeight() - HEADER_SPACE - FOOTER_SPACE, maxColsPerPage, startHour, endHour, settings);
      mPageFormat = pageFormat;
      mHeader = header;
      mFooter = footer;
    }


    public void printPage(Graphics graphics) {

      int x0 = (int)mPageFormat.getImageableX();
      int y0 = (int)mPageFormat.getImageableY();
      
      graphics.setFont(HEADER_FONT);
      graphics.drawString(mHeader, x0, y0+HEADER_FONT.getSize());

      graphics.setFont(FOOTER_FONT);
      graphics.drawString(mFooter, x0, y0 + (int)mPageFormat.getImageableHeight());

      mProgramTableIcon.paintIcon(null, graphics, x0 , y0 + HEADER_SPACE);

    }
  }



}
