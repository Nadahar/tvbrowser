package printplugin;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;

import javax.swing.Icon;

import devplugin.Program;

public class DefaultPageRenderer implements PageRenderer {

	private PageFormat mPageFormat;
  private int mColumnsPerPage;
  private static int COLUMN_WIDTH = 180;
  private static int HEADER_SPACE = 55;
  private static int FOOTER_SPACE = 10;
  private double mZoom;
  
  private ProgramItem[][] mProgramItems;
  
  public DefaultPageRenderer(PageFormat pageFormat, double zoom) {
    mPageFormat = pageFormat;
    mZoom = zoom;
    mColumnsPerPage = (int)(pageFormat.getImageableWidth()/mZoom/COLUMN_WIDTH);
  }
  
 
  
  private ProgramItem[][] createProgramIcons(PageModel model) {
    
    ProgramItem[][] result = new ProgramItem[model.getColumnCount()][];
    
    for (int i=0;i<result.length;i++) {
      int rowCnt = model.getColumnAt(i).getProgramCount()+1;      
      result[i]=new ProgramItem[rowCnt];
      result[i][0] = new ProgramItem(new ColumnHeader(model.getColumnAt(i).getTitle(),COLUMN_WIDTH, 30, mZoom));
      for (int j=0;j<rowCnt-1;j++) {
        Program prog = model.getColumnAt(i).getProgramAt(j);
        result[i][j+1]=new ProgramItem(new ProgramIcon(prog, null, COLUMN_WIDTH));
             
      }      
    }
    
    return result;
  }
  
  private void doLayout() {
    int x=0,y=HEADER_SPACE;
    for (int col=0;col<mProgramItems.length;col++) {
      
      for (int raw=0;raw<mProgramItems[col].length;raw++) {
        mProgramItems[col][raw].setPos(x,y);
        y+=mProgramItems[col][raw].getH();         
      }     
      y=HEADER_SPACE;
      if (col%mColumnsPerPage==mColumnsPerPage-1) {
        x=0;
      }
      else {
        x+=COLUMN_WIDTH;
      }
      
    }
    
  }
  
	public Page[] createPages(PageModel model) {
    
    mProgramItems = createProgramIcons(model);
    System.out.println("page contains "+mProgramItems.length+" programs");
    doLayout();
    
    int numberOfPages;
    if (mProgramItems.length>0) {
      numberOfPages = (mProgramItems.length-1)/mColumnsPerPage + 1;
    }
    else {
      numberOfPages = 0;
    }
    
    
    
    System.out.println("==>"+numberOfPages+" pages   "+mColumnsPerPage);
    Page[] result = new Page[numberOfPages];
    for (int i=0;i<numberOfPages;i++) {
      result[i]=new DefaultPage(mProgramItems, i*mColumnsPerPage, (i+1)*mColumnsPerPage, mPageFormat, mZoom, model.getHeader());
    }
    
		return result;
	}
  
  
  
}

class ProgramItem {
  
  private Icon mIcon;
  private int mX, mY, mW, mH;
  
  public ProgramItem(Icon icon) {
    mIcon = icon;  
  }
  
  public Icon getIcon() {
    return mIcon;
  }
  
  public void setPos(int x, int y) {
    mX=x;
    mY=y;
  }
  
/*  public int getX() {
    return mX;
  }
  
  public int getY() {
    return mY;
  }*/
  public int getW() {
    return mIcon.getIconWidth();
  }
  public int getH() {
    return mIcon.getIconHeight();
  }
  public void paint(Graphics g) {
    mIcon.paintIcon(null,g,mX,mY);
    
  }
  
}

class DefaultPage implements Page {

  private ProgramItem[][] mItems;
  private PageFormat mPageFormat;
  private int mFromInx, mToInx;
  private double mZoom;
  private String mHeader;
  
  private Font HEADER_FONT = new Font("Dialog",Font.BOLD,48);

  public DefaultPage(ProgramItem[][] items, int from, int to, PageFormat pageFormat, double zoom, String header) {
    mItems = items;   
    mPageFormat = pageFormat;
    mFromInx = from;
    mToInx = to;
    mZoom = zoom;
    mHeader = header;
    
    System.out.println("create page col "+from+" to "+to);
    
  }

	public void printPage(Graphics graphics) {
  
    Graphics2D g = (Graphics2D)graphics;
    g.scale(mZoom, mZoom);
    g.translate(mPageFormat.getImageableX()/mZoom, mPageFormat.getImageableY()/mZoom);

    g.setFont(HEADER_FONT);
    g.drawString(mHeader,0,HEADER_FONT.getSize());

   
    for (int col=mFromInx;col<mToInx;col++) {
      
      for (int raw=0;raw<mItems[col].length;raw++) {
        mItems[col][raw].paint(g);
      }     
      
    }
    g.translate(-mPageFormat.getImageableX()/mZoom, -mPageFormat.getImageableY()/mZoom);
    g.scale(1/mZoom,1/mZoom);
	}
  
}

