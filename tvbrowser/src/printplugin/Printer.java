package printplugin;

import java.awt.Graphics;
import java.awt.print.*;
import java.util.ArrayList;


public class Printer implements Printable {
  
  private Page[] mPages;
  
  public Printer(PageModel[] pageModelArr, PageRenderer pageRenderer) {
  
    System.out.println("creating pages for "+pageModelArr.length+" virtual pages");
  
    ArrayList pages = new ArrayList();
    for (int i=0;i<pageModelArr.length;i++) {
      Page[] p = pageRenderer.createPages(pageModelArr[i]);
      for (int j=0;j<p.length;j++) {
        pages.add(p[j]);
      }
    }
    mPages = new Page[pages.size()];
    pages.toArray(mPages);   
  }

  public int getNumberOfPages() {
    return mPages.length;
  }

	public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
    System.out.println("need to print page "+pageIndex);
    
    if (pageIndex>=mPages.length) {
      return NO_SUCH_PAGE;
    }
    mPages[pageIndex].printPage(g);
    
		return PAGE_EXISTS;
	}
  
  
}