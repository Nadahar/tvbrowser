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
 *     $Date: 2007-01-03 09:06:40 +0100 (Mi, 03 Jan 2007) $
 *   $Author: bananeweizen $
 * $Revision: 2979 $
 */

package printplugin.printer.queueprinter;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.print.PageFormat;
import java.util.ArrayList;

import printplugin.printer.AbstractPrintJob;
import printplugin.printer.ColumnModel;
import printplugin.printer.Page;
import printplugin.printer.PageModel;
import printplugin.settings.QueuePrinterSettings;
import devplugin.Program;



public class QueuePrintJob extends AbstractPrintJob {

  private static final Font FOOTER_FONT = new Font("Dialog",Font.ITALIC,6);

  private static final int FOOTER_SPACE=10;

  private QueuePrinterSettings mSettings;
  private String mFooterString;

  public QueuePrintJob(PageModel pageModel, QueuePrinterSettings settings, PageFormat pageFormat) {
    super(new PageModel[]{pageModel}, pageFormat);
    mSettings = settings;
    mFooterString = pageModel.getFooter();
  }

  protected Page[] createPages(PageModel pageModel) {
    ArrayList<QueuePage> pages = new ArrayList<QueuePage>();
    QueuePage currentPage=new QueuePage(mSettings, getPageFormat());
    pages.add(currentPage);
    for (int i=0; i<pageModel.getColumnCount(); i++) {
      ColumnModel column = pageModel.getColumnAt(i);
      for (int k=0; k<column.getProgramCount();k++) {
        Program program = column.getProgramAt(k);
        if (!currentPage.addProgram(program)) {
          currentPage = new QueuePage(mSettings, getPageFormat());
          pages.add(currentPage);
          currentPage.addProgram(program, true);
        }
      }
    }

    Page[] pageArr = new Page[pages.size()];
    pages.toArray(pageArr);
    return pageArr;
  }


  class QueuePage implements Page {

    private PageFormat mPageFormat;
    private ProgramTableIcon mTableIcon;

    public QueuePage(QueuePrinterSettings settings, PageFormat pageFormat) {
      mSettings = settings;
      mPageFormat = pageFormat;
      int width = (int)pageFormat.getImageableWidth();
      int height = (int)pageFormat.getImageableHeight()-FOOTER_SPACE;
      mTableIcon = new ProgramTableIcon(settings.getProgramIconSettings(), settings.getDateFont(), width, height, settings.getColumnsPerPage());
    }


    public PageFormat getPageFormat() {
      return mPageFormat;
    }

    public boolean addProgram(Program prog) {
      return addProgram(prog, false);
    }
    
    public boolean addProgram(Program prog, boolean forceAdding) {
      return mTableIcon.add(prog, forceAdding);
    }

    public void printPage(Graphics graphics) {
      int x0 = (int)mPageFormat.getImageableX();
      int y0 = (int)mPageFormat.getImageableY();

      graphics.setFont(FOOTER_FONT);
      graphics.drawString(mFooterString, x0, y0 + (int)mPageFormat.getImageableHeight()-3);

      mTableIcon.paintIcon(null, graphics, x0, y0);
    }
  }

}
