package tvbrowser.ui.settings.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class LineButton extends JButton {

  public LineButton() {
    super();
    
    BufferedImage image = new BufferedImage(22, 22, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    g2.setColor(new Color(255,0,0,0));
    g2.fillRect(0, 0, 22, 22);
    
    g2.setColor(getForeground());
    g2.drawLine(0, 11, 22, 11);
    setIcon(new ImageIcon(image));
  }
  
}
