package printplugin;

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
  
  public PreviewDlg(Frame parent, Printable printer, PageFormat pageFormat, int numberOfPages) {
  
    super(parent, true);
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
    
    
    content.add(mPreviewComponent, BorderLayout.CENTER);
    content.add(southPn, BorderLayout.SOUTH);
    updateSiteLabelText();
    
    pack();    
   
    mPrevBt.addActionListener(this);
    mNextBt.addActionListener(this); 
              
  }
  
  private void updateSiteLabelText() {
    mSiteLb.setText((mPreviewComponent.getPageIndex()+1)+" of "+mPreviewComponent.getNumberOfPages());
    System.out.println(mSiteLb.getText());
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
    Graphics2D g = (Graphics2D)graphics;
    g.scale(ZOOM,ZOOM);
    g.drawRect(0,0,(int)mPageFormat.getWidth(), (int)mPageFormat.getHeight());
    g.drawRect((int)mPageFormat.getImageableX(), (int)mPageFormat.getImageableY(), (int)mPageFormat.getImageableWidth(), (int)mPageFormat.getImageableHeight());
    
    try {
      mPrintable.print(g, null, mPageIndex);
    } catch (PrinterException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    g.scale(1/ZOOM, 1/ZOOM);
    
  }
  
}