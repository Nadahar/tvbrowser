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
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;

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
  
  public PreviewDlg(Frame parent, Printable printer, PageFormat pageFormat, int numberOfPages) {
  
    super(parent, true);
    setTitle(mLocalizer.msg("preview","preview"));
    mPrinter = printer;
    mPageFormat = pageFormat;
    
    UiUtilities.registerForClosing(this);
    
    JPanel southPn = new JPanel(new BorderLayout());
    
    mSiteLb = new JLabel();
    mSiteLb.setHorizontalAlignment(SwingConstants.CENTER);
    
    southPn.add(mPrevBt=new JButton("<"), BorderLayout.WEST);
    southPn.add(mNextBt=new JButton(">"), BorderLayout.EAST);
    southPn.add(mSiteLb, BorderLayout.CENTER);
    
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
    
    // TODO: Add Zoom-Icons!!
    final JButton zoomIn = new JButton("IN");
    final JButton zoomOut = new JButton("OUT");

    zoomIn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mPreviewComponent.zoomIn();
        zoomIn.setEnabled(!mPreviewComponent.maxZoom());
        zoomOut.setEnabled(true);
      }
    });
    
    zoomOut.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mPreviewComponent.zoomOut();
        zoomOut.setEnabled(!mPreviewComponent.minZoom());
        zoomIn.setEnabled(true);
      }
    });

    zoomOut.setEnabled(false);
    
    JPanel panel = new JPanel(new FormLayout("pref, 3dlu, pref", "pref"));
    CellConstraints cc = new CellConstraints();
    
    panel.add(zoomIn, cc.xy(1,1));
    panel.add(zoomOut, cc.xy(3,1));
    
    JPanel content = (JPanel)getContentPane();
    content.setBorder(Borders.DLU4_BORDER);
    content.setLayout(new FormLayout("fill:default:grow", "pref, 3dlu, fill:default:grow, 3dlu, pref"));
    
    content.add(panel, cc.xy(1, 1));
    content.add(borderPn, cc.xy(1,3));
    content.add(southPn, cc.xy(1,5));
    updateSiteLabelText();
    
    pack();    
   
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
  
  private void updateSiteLabelText() {    
    mSiteLb.setText(mLocalizer.msg("pageInfo","page {0} of {1}",""+(mPreviewComponent.getPageIndex()+1), ""+mPreviewComponent.getNumberOfPages()));
  }
  
  public void actionPerformed(ActionEvent event) {
    if (event.getSource()==mPrevBt) {
      mPreviewComponent.previous();
      updateSiteLabelText();
    }
    else if (event.getSource()==mNextBt) {
      mPreviewComponent.next();
      updateSiteLabelText();
    }
  }

  public void close() {
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
  
  public boolean minZoom() {
    return (mZoom <= 0.5);
  }

  public boolean maxZoom() {
    return (mZoom >= 2.5);
  }

  public void zoomIn() {
    if (mZoom < 2.5)
      mZoom += 0.2;
    setPreferredSize(new Dimension((int)(mPageFormat.getWidth()*mZoom), (int)(mPageFormat.getHeight()*mZoom)));
    revalidate();
    repaint();
  }

  public void zoomOut() {
    if (mZoom > 0.5)
      mZoom -= 0.2;
    setPreferredSize(new Dimension((int)(mPageFormat.getWidth()*mZoom), (int)(mPageFormat.getHeight()*mZoom)));
    revalidate();
    repaint();
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
      mPrintable.print(g, null, mPageIndex);
    } catch (PrinterException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    g.scale(1/mZoom, 1/mZoom);
    
  }
  
}