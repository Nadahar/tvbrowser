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


import devplugin.Program;
import java.util.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;

import javax.swing.Icon;

public class TimePageRenderer implements PageRenderer {

	private PageFormat mPageFormat;
  private int mNumOfColumns;
  private ProgramIconSettings mProgramIconSettings;
  private static final int COLUMN_WIDTH=180;
  private static final int FOOTER_SPACE=10;
  public static final Font HEADER_FONT = new Font("Dialog",Font.BOLD,32);
  public static final Font FOOTER_FONT = new Font("Dialog",Font.ITALIC,6);
  public static final Font COL_HEADER_FONT = new Font("Dialog",Font.BOLD,18);
  public static final Font CHANNEL_FONT = new Font("Dialog",Font.ITALIC,14);

  public TimePageRenderer(PageFormat pageFormat, int numOfColumns, ProgramIconSettings programIconSettings) {
    mPageFormat = pageFormat;
    mNumOfColumns = numOfColumns;
    mProgramIconSettings = programIconSettings;
  }
  
	public Page[] createPages(PageModel model) {
    ArrayList pages = new ArrayList();
    
    TimePage curPage = new TimePage(model.getHeader(), model.getFooter());
    pages.add(curPage);
    
    for (int i=0;i<model.getColumnCount();i++) {
      ColumnModel col = model.getColumnAt(i);
      if (!curPage.newColumn(col.getTitle())) {
        curPage = new TimePage(model.getHeader(), model.getFooter());
        pages.add(curPage);
        curPage.newColumn(col.getTitle());
      }
      
      for (int j=0; j<col.getProgramCount(); j++) {
        Program prog = col.getProgramAt(j);
        if (!curPage.addProgram(prog)) {
          curPage = new TimePage(model.getHeader(), model.getFooter());
          pages.add(curPage);
          curPage.newColumn(col.getTitle());
          curPage.addProgram(prog);
          
        }
      }      
    }
    
    Page[] result = new Page[pages.size()];
    pages.toArray(result);
		return result;
	}
 
 
  class TimePage implements Page {

    private int mCurY, mCurColumn, mColWidth;
    private ArrayList[] mItems;
    private double mZoom;
    private String mCurColumnTitle;
    private String mHeader, mFooter;
	  public TimePage(String header, String footer) {
      
      mHeader = header;
      mFooter = footer;
      //mHeight = (int)mPageFormat.getImageableHeight() - FOOTER_SPACE;
      mZoom = mPageFormat.getImageableWidth() / (mNumOfColumns * TimePageRenderer.COLUMN_WIDTH);
      mCurY = 0;
      mCurColumn = 0;
      mItems = new ArrayList[mNumOfColumns];
      for (int i=0;i<mItems.length;i++) {
        mItems[i] = new ArrayList();
      }
	  }
    
    
    public boolean newColumn(String title) {
      mCurColumnTitle = title;
      if (mCurY>0) {
        mCurColumn++;
        mCurY=0;
      }
      if (mCurColumn>=mNumOfColumns) {
        return false;
      }
      HeaderItem header = new HeaderItem(mCurColumnTitle);
      mItems[mCurColumn].add(header);
      header.setPos(COLUMN_WIDTH*mCurColumn, mCurY);
      mCurY+=header.getIconHeight();
      return true;
    }
    
  
    public boolean addProgram(Program prog) {      
      
      if (mCurY>mPageFormat.getImageableHeight()/mZoom) {
        // break column
        //mCurColumn++;
        //mCurY=0;
        
        if (mCurColumn+1>=mNumOfColumns) {
          return false;
        }        
        newColumn(mCurColumnTitle);
      }
      
      ProgramItem item = new ProgramItem(prog);
      mItems[mCurColumn].add(item);
      item.setPos(COLUMN_WIDTH*mCurColumn, mCurY);
      mCurY+=item.getIconHeight();
      
      // System.out.println("mCurY: "+mCurY+"; imageableHeight: "+mPageFormat.getImageableHeight()+"; mZoom: "+mZoom);
      
     
      
      return true;
    }
  
		public void printPage(Graphics graphics) {
      
      int x0 = (int)mPageFormat.getImageableX();
      int y0 = (int)mPageFormat.getImageableY();
      int w = (int)mPageFormat.getImageableWidth();
    
      if (mHeader!=null && mHeader.length()>0) {
        graphics.setFont(ChannelPageRenderer.HEADER_FONT);
        graphics.drawString(mHeader, x0, y0+ChannelPageRenderer.HEADER_FONT.getSize());
      }    
      
      if (mFooter!=null && mFooter.length()>0) {
        graphics.setFont(ChannelPageRenderer.FOOTER_FONT);
        graphics.drawString(mFooter, x0, y0 + (int)mPageFormat.getImageableHeight());
      }
      
      Graphics2D g = (Graphics2D)graphics;
      g.scale(mZoom, mZoom);
      g.translate(mPageFormat.getImageableX()/mZoom, mPageFormat.getImageableY()/mZoom);
      
      for (int col = 0; col<mItems.length; col++) {
      	for (int i=0;i<mItems[col].size(); i++) {
          Icon item = (Icon)mItems[col].get(i);
          item.paintIcon(null, g, 0, 0);
			  }
      }
      
      g.translate(mPageFormat.getImageableX()*mZoom, mPageFormat.getImageableY()*mZoom);
      g.scale(1/mZoom, 1/mZoom);
		}
    
     
  }
  
  class HeaderItem implements Icon {

    private int mX, mY;
    private String mTitle;
    

    public HeaderItem(String title) {
      mTitle = title;
    }

		public int getIconHeight() {
			return 22;
		}

		public int getIconWidth() {
			return COLUMN_WIDTH;
		}
    
    public void setPos(int x, int y) {
      mX = x;
      mY = y;
    }

		public void paintIcon(Component comp, Graphics g, int x, int y) {
      
      g.setColor(Color.lightGray);
      g.fillRect(x+mX, y+mY, getIconWidth(), getIconHeight());  
      
      g.setColor(Color.black);      
      g.setFont(COL_HEADER_FONT);
			g.drawString(mTitle,x+mX, y+mY+getIconHeight()-3);
      g.drawRect(x+mX, y+mY, getIconWidth(), getIconHeight());
		}
    
    
  }
  
  class ProgramItem implements Icon {
    
    private Icon mIcon;
    private int mX, mY;
    private Program mProgram;
    
    public ProgramItem(Program prog) {
      mProgram = prog;
      ProgramIcon ico = new ProgramIcon(prog, mProgramIconSettings, COLUMN_WIDTH);
      ico.setMaximumHeight(300);
      mIcon = ico;
    }

		
		public int getIconHeight() {
			return mIcon.getIconHeight()+CHANNEL_FONT.getSize()+10;
		}

		
		public int getIconWidth() {
			return mIcon.getIconWidth();
		}

    public void setPos(int x, int y) {
      mX = x;
      mY = y;
    }
		
		public void paintIcon(Component comp, Graphics g, int x, int y) {
      g.setFont(CHANNEL_FONT);
      g.drawString(mProgram.getChannel().getName()+":",x+mX,y+mY+2+CHANNEL_FONT.getSize());
			mIcon.paintIcon(null, g, x+mX, y+mY+CHANNEL_FONT.getSize()+4);
		}
  }
  
}