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

package printplugin.dlgs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;

import printplugin.PrintPlugin;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class PreviewDlg extends JDialog implements ActionListener, WindowClosingIf {
  
  private Printable mPrinter;
  private PageFormat mPageFormat;
  private PreviewComponent mPreviewComponent;
  private JButton mPrevBt, mNextBt;
  private JLabel mSiteLb;
  
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(PreviewDlg.class);

  private Point mMouseDragPoint;
  private JButton mZoomOut;
  private JButton mZoomIn;
  
  public PreviewDlg(Window parent, Printable printer, PageFormat pageFormat,
      int numberOfPages) {
    super(parent);
    setModal(true);
    createGui(printer, pageFormat, numberOfPages);
  }
  
  private void createGui(Printable printer, PageFormat pageFormat, int numberOfPages) {
    setTitle(mLocalizer.msg("preview","preview"));
    mPrinter = printer;
    mPageFormat = pageFormat;
    
    UiUtilities.registerForClosing(this);
    
    JPanel southPn = new JPanel(new FormLayout("left:10dlu:grow, pref, right:10dlu:grow", "pref"));
    CellConstraints cc = new CellConstraints();
    
    mSiteLb = new JLabel();
    mSiteLb.setHorizontalAlignment(SwingConstants.CENTER);
    
    southPn.add(mPrevBt=new JButton(TVBrowserIcons.left(TVBrowserIcons.SIZE_SMALL)), cc.xy(1,1));
    mPrevBt.setToolTipText(mLocalizer.msg("previous", "Previous page"));
    southPn.add(mNextBt=new JButton(TVBrowserIcons.right(TVBrowserIcons.SIZE_SMALL)), cc.xy(3,1));
    mNextBt.setToolTipText(mLocalizer.msg("next", "Next page"));
    southPn.add(mSiteLb, cc.xy(2,1));
    
    mPreviewComponent = new PreviewComponent(mPrinter, mPageFormat, numberOfPages);
    
    mPreviewComponent.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent evt) {
        mMouseDragPoint = evt.getPoint();
      }
      public void mouseReleased(MouseEvent evt) {
        mMouseDragPoint = null;
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      }
    });
    
    mPreviewComponent.addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseDragged(MouseEvent evt) {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        if (mMouseDragPoint != null && !evt.isShiftDown()) {
          int deltaX = mMouseDragPoint.x - evt.getX();
          int deltaY = mMouseDragPoint.y - evt.getY();
          scrollBy(deltaX, deltaY);
        }
      }
    });
    
    JPanel borderPn = new JPanel(new BorderLayout());
    final JScrollPane scrollPane = new JScrollPane(mPreviewComponent);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
    scrollPane.getVerticalScrollBar().setUnitIncrement(20);
    borderPn.add(scrollPane);
    
    mZoomIn = new JButton(TVBrowserIcons.zoomIn(TVBrowserIcons.SIZE_SMALL));
    mZoomIn.setToolTipText(mLocalizer.msg("zoomIn", "Zoom in"));
    mZoomOut = new JButton(TVBrowserIcons.zoomOut(TVBrowserIcons.SIZE_SMALL));
    mZoomOut.setToolTipText(mLocalizer.msg("zoomOut", "Zoom out"));
    
    mZoomIn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mPreviewComponent.zoomIn();
        mZoomIn.setEnabled(!mPreviewComponent.maxZoom());
        mZoomOut.setEnabled(true);
      }
    });
    
    mZoomOut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mPreviewComponent.zoomOut();
        mZoomOut.setEnabled(!mPreviewComponent.minZoom());
        mZoomIn.setEnabled(true);
      }
    });

    JPanel panel = new JPanel(new FormLayout("pref, 3dlu, pref", "pref"));
    
    panel.add(mZoomIn, cc.xy(1,1));
    panel.add(mZoomOut, cc.xy(3,1));
    
    JPanel content = (JPanel)getContentPane();
    content.setBorder(Borders.DLU4_BORDER);
    content.setLayout(new FormLayout("fill:default:grow", "pref, 3dlu, fill:default:grow, 3dlu, pref, 3dlu, pref"));
    
    content.add(panel, cc.xy(1, 1));
    content.add(borderPn, cc.xy(1,3));
    content.add(southPn, cc.xy(1,5));
    
    JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    
    JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    close.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });
    closePanel.add(close);
    
    content.add(closePanel, cc.xy(1,7));
    
    updateDialogState();
    
    pack();    
    
    Properties prop = PrintPlugin.getInstance().getSettings();

    try {
      if ((prop.getProperty("PreviewDlg.Width") != null) && (prop.getProperty("PreviewDlg.Height") != null)) {
        int width = Integer.parseInt(prop.getProperty("PreviewDlg.Width"));
        int height = Integer.parseInt(prop.getProperty("PreviewDlg.Height"));
        setSize(width, height);
      }
          
      if ((prop.getProperty("PreviewDlg.X") != null) && (prop.getProperty("PreviewDlg.Y") != null)){
        int x = Integer.parseInt(prop.getProperty("PreviewDlg.X"));
        int y = Integer.parseInt(prop.getProperty("PreviewDlg.Y"));
        setLocation(x, y);
      } else {
        setLocationRelativeTo(getParent());
      }
      
      if (prop.getProperty("PreviewDlg.Zoom") != null) {
        double zoom = Double.parseDouble(prop.getProperty("PreviewDlg.Zoom"));
        mPreviewComponent.setZoom(zoom);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
   
    mZoomIn.setEnabled(!mPreviewComponent.maxZoom());
    mZoomOut.setEnabled(!mPreviewComponent.minZoom());
    
    mPrevBt.addActionListener(this);
    mNextBt.addActionListener(this); 
  }
  
  public void scrollBy(int deltaX, int deltaY) {
    if (mPreviewComponent.getParent() instanceof JViewport) {
      JViewport viewport = (JViewport) mPreviewComponent.getParent();
      Point viewPos = viewport.getViewPosition();

      if (deltaX!=0){
        viewPos.x += deltaX;

        int maxX = mPreviewComponent.getWidth() - viewport.getWidth();

        viewPos.x = Math.min(viewPos.x, maxX);
        viewPos.x = Math.max(viewPos.x, 0);

        viewport.setViewPosition(viewPos);
      }

      if (deltaY !=0){
        viewPos.y += deltaY;
        int maxY = mPreviewComponent.getHeight() - viewport.getHeight();
        viewPos.y = Math.min(viewPos.y, maxY);
        viewPos.y = Math.max(viewPos.y, 0);

        viewport.setViewPosition(viewPos);
      }
    }
  }
  
  private void updateDialogState() {    
    mSiteLb.setText(mLocalizer.msg("pageInfo", "page {0} of {1}",
        mPreviewComponent.getPageIndex() + 1, mPreviewComponent
            .getNumberOfPages()));
    mPrevBt.setEnabled(mPreviewComponent.getPageIndex() > 0);
    mNextBt.setEnabled(mPreviewComponent.getPageIndex()+1 < mPreviewComponent.getNumberOfPages());
  }
  
  public void actionPerformed(ActionEvent event) {
    if (event.getSource()==mPrevBt) {
      mPreviewComponent.previous();
      updateDialogState();
    }
    else if (event.getSource()==mNextBt) {
      mPreviewComponent.next();
      updateDialogState();
    }
  }

  public void close() {
    Properties prop = PrintPlugin.getInstance().getSettings();
    
    prop.setProperty("PreviewDlg.X", Integer.toString(this.getLocationOnScreen().x));
    prop.setProperty("PreviewDlg.Y", Integer.toString(this.getLocationOnScreen().y));
    prop.setProperty("PreviewDlg.Width", Integer.toString(this.getWidth()));
    prop.setProperty("PreviewDlg.Height", Integer.toString(this.getHeight()));
    prop.setProperty("PreviewDlg.Zoom", Double.toString(mPreviewComponent.getZoom()));
    
    dispose();
  }
  
  
}


class PreviewComponent extends JComponent {
  
  private Printable mPrintable;
  private PageFormat mPageFormat;
  private double mZoom=0.5;
  private int mPageIndex, mNumberOfPages;
  
  public PreviewComponent(Printable printable, PageFormat pageFormat, int numberOfPages) {
    mPrintable = printable;
    mPageFormat = pageFormat;
    mNumberOfPages = numberOfPages;
    setPreferredSize(new Dimension((int)(pageFormat.getWidth()*mZoom), (int)(pageFormat.getHeight()*mZoom)));
    mPageIndex = 0;
  }
  
  public double getZoom() {
    return mZoom;
  }
  
  public void setZoom(double zoom) {
    mZoom = zoom;
    setPreferredSize(new Dimension((int)(mPageFormat.getWidth()*mZoom), (int)(mPageFormat.getHeight()*mZoom)));
    revalidate();
    repaint();
  }
  
  public boolean minZoom() {
    return (mZoom <= 0.5);
  }

  public boolean maxZoom() {
    return (mZoom >= 2.5);
  }

  public void zoomIn() {
    if (mZoom < 2.5) {
      mZoom += 0.2;
      setZoom(mZoom);
    }
  }

  public void zoomOut() {
    if (mZoom > 0.5) {
      mZoom -= 0.2;
      setZoom(mZoom);
    }
  }

  public void next() {
    if (mPageIndex<mNumberOfPages-1) {
      mPageIndex++;
      repaint();
    }
  }
  
  public void previous() {
    if (mPageIndex>0) {
      mPageIndex--;
      repaint();
    }
  }
  
  public int getPageIndex() {
    return mPageIndex;
  }
  
  public int getNumberOfPages() {
    return mNumberOfPages;
  }
  
  public void setPageIndex(int inx) {
    if (inx!=mPageIndex) {
      mPageIndex=inx;
      repaint();
    }
  }
  
  public void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);
    Graphics2D g = (Graphics2D)graphics;
    g.scale(mZoom,mZoom);
    g.setColor(Color.white);
    g.fillRect(0,0,(int)mPageFormat.getWidth(), (int)mPageFormat.getHeight());
    g.setColor(Color.lightGray);
    g.drawRect((int)mPageFormat.getImageableX(), (int)mPageFormat.getImageableY(), (int)mPageFormat.getImageableWidth(), (int)mPageFormat.getImageableHeight());
    g.setColor(Color.black);
    
    try {
      mPrintable.print(g, mPageFormat, mPageIndex);
    } catch (PrinterException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    g.scale(1/mZoom, 1/mZoom);
    
  }
  
}