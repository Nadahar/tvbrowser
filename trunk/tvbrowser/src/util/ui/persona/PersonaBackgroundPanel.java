package util.ui.persona;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class PersonaBackgroundPanel extends JPanel {
  protected void paintComponent(Graphics g) {
    if(Persona.getInstance().getAccentColor() != null) {
      g.setColor(Persona.getInstance().getAccentColor());
      g.fillRect(0,0,getWidth(),getHeight());
    }
    else {
      super.paintComponent(g);
    }
    
    BufferedImage headerImage = Persona.getInstance().getHeaderImage();
    BufferedImage footerImage = Persona.getInstance().getFooterImage();
    
    if(headerImage != null) {
      g.drawImage(headerImage,0,0,getWidth(),headerImage.getHeight(),headerImage.getWidth()-getWidth(),0,headerImage.getWidth(),headerImage.getHeight(),null);
    }
    if(footerImage != null) {
      g.drawImage(footerImage,0,getHeight()-footerImage.getHeight(),footerImage.getWidth(),getHeight(),0,0,footerImage.getWidth(),footerImage.getHeight(),null);
    }
  }
}
