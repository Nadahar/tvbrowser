/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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

package printplugin;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.print.PageFormat;



public class ChannelPageRenderer implements PageRenderer {

  static int COLUMN_WIDTH = 180;
  private PageFormat mPageFormat;
  private int mColumnsPerPage;
  private int mStartHour, mEndHour;
  private ProgramIconSettings mProgramIconSettings;
  
  public static final Font HEADER_FONT = new Font("Dialog",Font.BOLD,32);
  public static final Font FOOTER_FONT = new Font("Dialog",Font.ITALIC,6);
  public static final Font COL_HEADER_FONT = new Font("Dialog",Font.BOLD,18);

	public ChannelPageRenderer(PageFormat pageFormat, int columnsPerPage, int startHour, int endHour, ProgramIconSettings programIconSettings) {
    
    mPageFormat = pageFormat;
    mColumnsPerPage = columnsPerPage;
    mProgramIconSettings = programIconSettings;
    
    mStartHour = startHour;
    mEndHour = endHour;
	}
  
  
  private ColumnModel[] getColumns(PageModel model, int fromInx, int cnt) {
    ColumnModel[] result = new ColumnModel[cnt];
    for (int i=fromInx;i<fromInx+cnt;i++) {
      result[i-fromInx]=model.getColumnAt(i);
    }
    return result;
  }
  
	public Page[] createPages(PageModel model) {
    
    int colCnt = model.getColumnCount();
    
    int numberOfPages;
    if (colCnt==0) {
      numberOfPages=0;
    }
    else {
      numberOfPages = (colCnt-1) / mColumnsPerPage +1;
    }
    
    Page[] result = new Page[numberOfPages];
    
    for (int i=0;i<result.length;i++) {
      
      int fromInx = i * mColumnsPerPage;
      int inxCnt = mColumnsPerPage;
      
      if (fromInx+inxCnt >  colCnt) {
        inxCnt = colCnt-fromInx;
      }
      
      ColumnModel[] cols=getColumns(model, fromInx, inxCnt); 
      result[i]=new ChannelPage(cols, mPageFormat, mColumnsPerPage, model.getHeader(), model.getFooter(), mStartHour, mEndHour, mProgramIconSettings);
    }
    
    return result;
    
  
}


class ChannelPage implements Page {


  private ProgramTableIcon mProgramTableIcon;
  private PageFormat mPageFormat;
  private static final int HEADER_SPACE=30;
  private static final int FOOTER_SPACE=10;
  private String[] mColumnHeaders;
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
    int w = (int)mPageFormat.getImageableWidth();
    
    graphics.setFont(ChannelPageRenderer.HEADER_FONT);
    graphics.drawString(mHeader, x0, y0+ChannelPageRenderer.HEADER_FONT.getSize());
    
    graphics.setFont(ChannelPageRenderer.FOOTER_FONT);
    graphics.drawString(mFooter, x0, y0 + (int)mPageFormat.getImageableHeight());
    
    mProgramTableIcon.paintIcon(null, graphics, x0 , y0 + HEADER_SPACE);
    
	}
} 
  
  
  
 
  
}
