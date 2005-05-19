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

package printplugin.printer.queueprinter;

import printplugin.settings.QueuePrinterSettings;
import printplugin.printer.*;

import java.awt.print.PageFormat;
import java.awt.*;
import java.util.ArrayList;

import devplugin.Program;



public class QueuePrintJob extends AbstractPrintJob {

  private static final Font HEADER_FONT = new Font("Dialog",Font.BOLD,32);
  private static final Font FOOTER_FONT = new Font("Dialog",Font.ITALIC,6);

  private static final int HEADER_SPACE=30;
  private static final int FOOTER_SPACE=10;

  private QueuePrinterSettings mSettings;
  private PageFormat mPageFormat;

  public QueuePrintJob(PageModel pageModel, QueuePrinterSettings settings, PageFormat pageFormat) {
    super(new PageModel[]{pageModel});
    mSettings = settings;
    mPageFormat = pageFormat;
  }

  protected Page[] createPages(PageModel pageModel) {
    ArrayList pages = new ArrayList();
    QueuePage currentPage=new QueuePage(mSettings, mPageFormat);
    pages.add(currentPage);
    for (int i=0; i<pageModel.getColumnCount(); i++) {
      ColumnModel column = pageModel.getColumnAt(i);
      for (int k=0; k<column.getProgramCount();k++) {
        Program program = column.getProgramAt(k);
        if (!currentPage.addProgram(program)) {
          currentPage = new QueuePage(mSettings, mPageFormat);
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
      int height = (int)pageFormat.getImageableHeight()-HEADER_SPACE-FOOTER_SPACE;
      mTableIcon = new ProgramTableIcon(settings.getProgramIconSettings(), width, height, settings.getColumnsPerPage());
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


      graphics.setFont(HEADER_FONT);
      graphics.drawString("Titel", x0, y0+HEADER_FONT.getSize());

      graphics.setFont(FOOTER_FONT);
      graphics.drawString("Copyright (c) by TV-Browser", x0, y0 + (int)mPageFormat.getImageableHeight());

//      for (int i=0; i<mProgramTableIcons.length; i++) {
//        if (mProgramTableIcons[i] != null) {
//          mProgramTableIcons[i].paintIcon(null, graphics, x0, y0 + HEADER_SPACE + (mProgramTableIcons[i].getIconHeight()+TABLE_SPACE)*i);
//        }
//      }
      mTableIcon.paintIcon(null, graphics, x0, y0 + HEADER_SPACE);
    }
  }

}
