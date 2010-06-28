/*
 * This Class was found in the Sun Java-Forum. It was heavily modified to work with
 * Images and the Preview-Dialog of the TV-Browser
 */
package printplugin.printer.singleprogramprinter;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.util.ArrayList;

import javax.swing.JEditorPane;
import javax.swing.text.ComponentView;
import javax.swing.text.Document;
import javax.swing.text.View;

/**
 * DocumentRenderer prints objects of type Document. Text attributes, including
 * fonts, color, and small icons, will be rendered to a printed page.
 * DocumentRenderer computes line breaks, paginates, and performs other
 * formatting.
 * 
 * An HTMLDocument is printed by sending it as an argument to the
 * print(HTMLDocument) method. A PlainDocument is printed the same way. Other
 * types of documents must be sent in a JEditorPane as an argument to the
 * print(JEditorPane) method. Printing Documents in this way will automatically
 * display a print dialog.
 * 
 * As objects which implement the Printable Interface, instances of the
 * DocumentRenderer class can also be used as the argument in the setPrintable
 * method of the PrinterJob class. Instead of using the print() methods detailed
 * above, a programmer may gain access to the formatting capabilities of this
 * class without using its print dialog by creating an instance of
 * DocumentRenderer and setting the document to be printed with the
 * setDocument(). The Document may then be printed by setting the instance of
 * DocumentRenderer in any PrinterJob.
 */
public class DocumentRenderer implements Printable {
  /** Used to keep track of when the page to print changes. */
  private int mCurrentPage = -1;

  /**
   * Container to hold the Document. This object will be used to lay out the
   * Document for printing.
   */
  private JEditorPane mJEditorPane;

  /** Location of the current page end */
  private double mPageEndY = 0;

  /** Location of the current page start */
  private double mPageStartY = 0;

  /**
   * boolean to allow control over whether pages too wide to fit on a page will
   * be scaled.
   */
  private boolean mScaleWidthToFit = true;

  /**
   * The DocumentRenderer class uses pFormat and pJob in its methods. Note that
   * pFormat is not the variable name used by the print method of the
   * DocumentRenderer. Although it would always be expected to reference the
   * pFormat object, the print method gets its PageFormat as an argument.
   */
  private PageFormat mPageFormat;

  /**
   * The PrintJob
   */
  private PrinterJob mPrintJob;

  /**
   * Stores the Start-Position for each Page
   */
  private ArrayList<Double> mPageStarts = new ArrayList<Double>();
  

  /**
   * The constructor initializes the pFormat and PJob variables.
   * 
   * @param pageFormat Format of the Page
   */
  public DocumentRenderer(PageFormat pageFormat) {
    mPageFormat = pageFormat;
    mPrintJob = PrinterJob.getPrinterJob();
  }

  /**
   * pageDialog() displays a page setup dialog.
   */
  public void pageDialog() {
    mPageFormat = mPrintJob.pageDialog(mPageFormat);
  }

  /**
   * The print method implements the Printable interface. Although Printables
   * may be called to render a page more than once, each page is painted in
   * order. We may, therefore, keep track of changes in the page being rendered
   * by setting the currentPage variable to equal the pageIndex, and then
   * comparing these variables on subsequent calls to this method. When the two
   * variables match, it means that the page is being rendered for the second or
   * third time. When the currentPage differs from the pageIndex, a new page is
   * being requested.
   * 
   * The highlights of the process used print a page are as follows:
   * 
   * I. The Graphics object is cast to a Graphics2D object to allow for scaling.
   * 
   * II. The JEditorPane is laid out using the width of a printable page. This
   * will handle line breaks. If the JEditorPane cannot be sized at the width of
   * the graphics clip, scaling will be allowed.
   * 
   * III. The root view of the JEditorPane is obtained. By examining this root
   * view and all of its children, printView will be able to determine the
   * location of each printable element of the document.
   * 
   * IV. If the scaleWidthToFit option is chosen, a scaling ratio is determined,
   * and the graphics2D object is scaled.
   * 
   * V. The Graphics2D object is clipped to the size of the printable page.
   * 
   * VI. currentPage is checked to see if this is a new page to render. If so,
   * pageStartY and pageEndY are reset.
   * 
   * VII. To match the coordinates of the printable clip of graphics2D and the
   * allocation rectangle which will be used to lay out the views, graphics2D is
   * translated to begin at the printable X and Y coordinates of the graphics
   * clip.
   * 
   * VIII. An allocation Rectangle is created to represent the layout of the
   * Views.
   * 
   * The Printable Interface always prints the area indexed by reference to the
   * Graphics object. For instance, with a standard 8.5 x 11 inch page with 1
   * inch margins the rectangle X = 72, Y = 72, Width = 468, and Height = 648,
   * the area 72, 72, 468, 648 will be painted regardless of which page is
   * actually being printed.
   * 
   * To align the allocation Rectangle with the graphics2D object two things are
   * done. The first step is to translate the X and Y coordinates of the
   * graphics2D object to begin at the X and Y coordinates of the printable
   * clip, see step VII. Next, when printing other than the first page, the
   * allocation rectangle must start laying out in coordinates represented by
   * negative numbers. After page one, the beginning of the allocation is
   * started at minus the page end of the prior page. This moves the part which
   * has already been rendered to before the printable clip of the graphics2D
   * object.
   * 
   * X. The printView method is called to paint the page. Its return value will
   * indicate if a page has been rendered.
   * 
   * Although public, print should not ordinarily be called by programs other
   * than PrinterJob.
   */
  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
    double scale = 1.0;
    Graphics2D graphics2D;

    View rootView;
    // I
    graphics2D = (Graphics2D) graphics;

    // II
    mJEditorPane.setSize((int) pageFormat.getImageableWidth(), Integer.MAX_VALUE);
    mJEditorPane.validate();
    // III
    rootView = mJEditorPane.getUI().getRootView(mJEditorPane);
    // IV
    if ((mScaleWidthToFit) && (mJEditorPane.getMinimumSize().getWidth() > pageFormat.getImageableWidth())) {
      scale = pageFormat.getImageableWidth() / mJEditorPane.getMinimumSize().getWidth();
      graphics2D.scale(scale, scale);
    }
    // V
    int x = (int) (pageFormat.getImageableX() / scale);
    int y = (int) (pageFormat.getImageableY() / scale);
    int width = (int) (pageFormat.getImageableWidth() / scale);
    int height = (int) (pageFormat.getImageableHeight() / scale);
    
    Rectangle bounds = graphics2D.getClipBounds();

    int cx = x;
    int cy = y;
    int cheight = height;
    int cwidth = width;

    if (bounds != null) {
      if (cy < bounds.y) {
        cy = bounds.y;
      }

      if (cx < bounds.x) {
        cx = bounds.x;
      }
      
      if (height+cy > bounds.y + bounds.height) {
        cheight = (bounds.y + bounds.height) - cy;
      }
      
      if (cy + cheight > y+height) {
        cheight = (y+height)-cy;
      }

      if (width+cx > bounds.x + bounds.width) {
        cwidth = (bounds.x + bounds.width) - cx;
      }
      
      if (cx + cwidth > y+width) {
        cwidth = (x+width)-cx;
      }
    }
    
    graphics2D.setClip(cx, cy, cwidth, cheight);
    
    // VI
    if (pageIndex != mCurrentPage) {
      mCurrentPage = pageIndex;
      
      if (pageIndex > mPageStarts.size()-1) {
        double lastSize = 0;
        if (mPageStarts.size() > 0) {
          lastSize = (mPageStarts.get(mPageStarts.size()-1)).doubleValue();
        }
        if (pageIndex > 0) {
          mPageStartY = lastSize + height;
        } else {
          mPageStartY = lastSize;
        }
        mPageStarts.add(mPageStartY);
      } else {
        mPageStartY = mPageStarts.get(pageIndex);
      }
      
      mPageEndY = height;
    }
    // VII
    graphics2D.translate(x, y);
    // VIII
    
    Rectangle allocation = new Rectangle(0, (int) -mPageStartY, (int) (mJEditorPane.getMinimumSize().getWidth()),
        (int) (mJEditorPane.getPreferredSize().getHeight()));
    // X
    if (printView(graphics2D, allocation, rootView)) {
      graphics2D.setClip(bounds);
      // Add Position of next Page
      if (pageIndex >= mPageStarts.size()-1) {
        mPageStarts.add(Double.valueOf(mPageStartY+mPageEndY));
      }
      return Printable.PAGE_EXISTS;
    } else {
      mPageStartY = 0;
      mPageEndY = 0;
      mCurrentPage = -1;
      graphics2D.setClip(bounds);
      return Printable.NO_SUCH_PAGE;
    }
  }

  /**
   * printView is a recursive method which iterates through the tree structure
   * of the view sent to it. If the view sent to printView is a branch view,
   * that is one with children, the method calls itself on each of these
   * children. If the view is a leaf view, that is a view without children which
   * represents an actual piece of text to be painted, printView attempts to
   * render the view to the Graphics2D object.
   * 
   * I. When any view starts after the beginning of the current printable page,
   * this means that there are pages to print and the method sets pageExists to
   * true.
   * 
   * II. When a leaf view is taller than the printable area of a page, it
   * cannot, of course, be broken down to fit a single page. Such a View will be
   * printed whenever it intersects with the Graphics2D clip.
   * 
   * III. If a leaf view intersects the printable area of the graphics clip and
   * fits vertically within the printable area, it will be rendered.
   * 
   * IV. If a leaf view does not exceed the printable area of a page but does
   * not fit vertically within the Graphics2D clip of the current page, the
   * method records that this page should end at the start of the view. This
   * information is stored in pageEndY.
   */
  protected boolean printView(Graphics2D graphics2D, Shape allocation, View view) {
    boolean pageExists = false;
    Rectangle clipRectangle = graphics2D.getClipBounds();
    Shape childAllocation;
    View childView;

    if (view.getViewCount() > 0) {
      for (int i = 0; i < view.getViewCount(); i++) {
        childAllocation = view.getChildAllocation(i, allocation);
        if (childAllocation != null) {
          childView = view.getView(i);
          if (printView(graphics2D, childAllocation, childView)) {
            pageExists = true;
          }
        }
      }
    } else {
      // I
      if (allocation.getBounds().getMaxY() >= clipRectangle.getY()) {
        pageExists = true;
        // II
        if ((allocation.getBounds().getHeight() > clipRectangle.getHeight()) && (allocation.intersects(clipRectangle))) {
          view.paint(graphics2D, allocation);
        } else {
          // III
          if (view instanceof ComponentView) {
            Component comp = ((ComponentView) view).getComponent();
            comp.setBounds(allocation.getBounds());
            graphics2D.translate(allocation.getBounds().getX(), allocation.getBounds().getY());
            comp.paint(graphics2D);
            graphics2D.translate(-allocation.getBounds().getX(), -allocation.getBounds().getY());
          } else {
            if (allocation.getBounds().getY() >= clipRectangle.getY()) {
              if (allocation.getBounds().getMaxY() <= clipRectangle.getMaxY()) {
                view.paint(graphics2D, allocation);
              } else {
                // IV
                if (allocation.getBounds().getY() < mPageEndY) {
                  mPageEndY = allocation.getBounds().getY();
                }
              }
            }
          }
        }
      }
    }
    return pageExists;
  }

  /**
   * Method to set the content type and document of the JEditorPane.
   */
  protected void setDocument(String type, Document document) {
    setContentType(type);
    mJEditorPane.setDocument(document);
  }

  /**
   * Method to set the content type the JEditorPane.
   */
  protected void setContentType(String type) {
    mJEditorPane.setContentType(type);
  }

  /**
   * Method to set the JEditorPane that will be printed
   */
  public void setEditorPane(JEditorPane jedPane) {
    mJEditorPane = jedPane;
  }

  /**
   * Method to set the current choice of the width scaling option.
   */
  public void setScaleWidthToFit(boolean scaleWidth) {
    mScaleWidthToFit = scaleWidth;
  }

  /**
   * Method to get the current choice the width scaling option.
   */
  public boolean getScaleWidthToFit() {
    return mScaleWidthToFit;
  }
  
  /**
   * Calculates the Number of Pages
   * @return Number of Pages
   */
  public int getPageCount() {
     int count = 0;
     
     int width = (int) (mPageFormat.getImageableWidth());
     int height = (int) (mPageFormat.getImageableHeight());

     BufferedImage bufferedImage = new BufferedImage (width, height, BufferedImage.TYPE_4BYTE_ABGR_PRE );
     Graphics2D g2d = ( bufferedImage.createGraphics() );
     
     while (print(g2d, mPageFormat, count) == PAGE_EXISTS) {
       count++;
     }
    
     return count;
  }
}