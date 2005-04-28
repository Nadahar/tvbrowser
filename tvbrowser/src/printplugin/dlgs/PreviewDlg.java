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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

public class PreviewDlg extends JDialog implements ActionListener {
  
  private Printable mPrinter;
  private PageFormat mPageFormat;
  private PreviewComponent mPreviewComponent;
  private JButton mPrevBt, mNextBt;
  private JLabel mSiteLb;
  
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(PreviewDlg.class);

  
  public PreviewDlg(Frame parent, Printable printer, PageFormat pageFormat, int numberOfPages) {
  
    super(parent, true);
    setTitle(mLocalizer.msg("preview","preview"));
    mPrinter = printer;
    mPageFormat = pageFormat;
    
    
    JPanel content = (JPanel)getContentPane();
    
    content.setLayout(new BorderLayout());
    
    JPanel southPn = new JPanel(new BorderLayout());
    southPn.setBorder(BorderFactory.createEmptyBorder(10,20,10,20));
    
    mSiteLb = new JLabel();
    mSiteLb.setHorizontalAlignment(SwingConstants.CENTER);
    
    southPn.add(mPrevBt=new JButton("<"), BorderLayout.WEST);
    southPn.add(mNextBt=new JButton(">"), BorderLayout.EAST);
    southPn.add(mSiteLb, BorderLayout.CENTER);
    
    mPreviewComponent = new PreviewComponent(mPrinter, mPageFormat, numberOfPages);
    JPanel borderPn = new JPanel(new BorderLayout());
    borderPn.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
    borderPn.add(mPreviewComponent);
    
    content.add(borderPn, BorderLayout.CENTER);
    content.add(southPn, BorderLayout.SOUTH);
    updateSiteLabelText();
    
    pack();    
   
    mPrevBt.addActionListener(this);
    mNextBt.addActionListener(this); 
              
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
  
  
}


class PreviewComponent extends JComponent {
  
  private Printable mPrintable;
  private PageFormat mPageFormat;
  private static final double ZOOM=0.5;
  private int mPageIndex, mNumberOfPages;
  
  public PreviewComponent(Printable printable, PageFormat pageFormat, int numberOfPages) {
    mPrintable = printable;
    mPageFormat = pageFormat;
    mNumberOfPages = numberOfPages;
    setPreferredSize(new Dimension((int)(pageFormat.getWidth()*ZOOM), (int)(pageFormat.getHeight()*ZOOM)));
    mPageIndex = 0;
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
    g.scale(ZOOM,ZOOM);
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
    g.scale(1/ZOOM, 1/ZOOM);
    
  }
  
}