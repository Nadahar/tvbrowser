package printplugin;

import java.awt.*;

import javax.swing.Icon;

public class ColumnHeader implements Icon {

  private static Font mFont = new Font("Dialog",Font.BOLD,24);
  private int mWidth, mHeight;
  private String mTitle;
  private double mZoom;
  
  public ColumnHeader(String title, int width, int height, double zoom) {
    mTitle = title;
    mWidth = width;
    mHeight = height;
    mZoom = zoom;
  }

	public int getIconHeight() {
		return mHeight;
	}

	
	public int getIconWidth() {
		return mHeight;
	}

	
	public void paintIcon(Component comp, Graphics g, int x, int y) {
    g.translate(x,y);
   // Graphics2D g = (Graphics2D)graphics;
  //  g.scale(mZoom, mZoom);
		FontMetrics metrics = g.getFontMetrics(mFont);
    int width=metrics.stringWidth(mTitle);
    g.setFont(mFont);
    g.drawRect(0,0,mWidth,mHeight);
    g.drawString(mTitle,(mWidth-width)/2,mFont.getSize());
  //  g.scale(1/mZoom, 1/mZoom);
  g.translate(-x,-y);
	}
  
  
  
}