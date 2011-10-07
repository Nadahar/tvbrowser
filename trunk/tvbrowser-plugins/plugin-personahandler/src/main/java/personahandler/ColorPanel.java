package personahandler;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class ColorPanel extends JPanel {
  private Color mColor;
  
  public ColorPanel(Color color) {
    super();
    mColor = color;
    setBackground(Color.green);
  }
  
  public void setColor(Color color) {
    mColor = color;
    repaint();
  }
  
  public Color getColor() {
    return mColor;
  }
  
  protected void paintComponent(Graphics g) {
    g.setColor(Color.white);
    g.clearRect(0,0,getWidth()-1,getHeight()-1);
    
    g.setColor(Color.black);
    g.drawRect(0,0,getWidth()-1,getHeight()-1);
    
    g.setColor(mColor);
    g.fillRect(1,1,getWidth()-2,getHeight()-2);
  }
}
