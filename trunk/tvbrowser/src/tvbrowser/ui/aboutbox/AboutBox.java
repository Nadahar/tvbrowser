/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
 */
 
package tvbrowser.ui.aboutbox;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;
import java.awt.Dimension;

import tvbrowser.TVBrowser;

/**
 * 
 * @author Martin Oberhauser (darras@users.sourceforge.net)
 *
 */
public class AboutBox extends JDialog {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(AboutBox.class);
  
  
  
  class InfoEntry extends JPanel {
    
    public InfoEntry(String key, String value) {
      setLayout(new BorderLayout(11,0));
      JLabel lLabel=new JLabel(key);
      JLabel rLabel=new JLabel(value,JLabel.RIGHT);
      lLabel.setFont(AboutBox.boldFont);
      rLabel.setFont(AboutBox.normalFont);
      add(lLabel,BorderLayout.WEST);
      add(rLabel,BorderLayout.EAST);
    }
  }
  
  
  
  class PicturePanel extends JPanel {
    private Icon image;
    public PicturePanel(String picName) {
      image=new ImageIcon(picName);
    }
    public void paintComponent(java.awt.Graphics g) {
      java.awt.Rectangle rect=g.getClipBounds();
      image.paintIcon(this,g,(rect.width-image.getIconWidth())/2,(rect.height-image.getIconHeight())/2);
    }
    
    public Dimension getPreferredSize() {
      return new Dimension(image.getIconWidth(),image.getIconHeight());
    }
  }
  
  
  private static Font smallFont=new Font("Dialog", Font.PLAIN, 10);
  private static Font bigFont=new Font("Dialog", Font.PLAIN, 24);
  private static Font normalFont=new Font("Dialog", Font.PLAIN, 12);
  private static Font boldFont=new Font("Dialog",Font.BOLD, 12);
  
  
  
  public AboutBox(Frame parent) {
    super(parent,true);
    
    setTitle(mLocalizer.msg("about", "About"));
    
    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,11,11));
    
    JPanel btnPanel=new JPanel(new BorderLayout());
    JButton closeBtn=new JButton(mLocalizer.msg("close", "Close"));
    final JDialog parentFrame=this;
    closeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        parentFrame.hide();
      }
    }
    );
    btnPanel.add(closeBtn,BorderLayout.EAST);
    btnPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
    contentPane.add(btnPanel,BorderLayout.SOUTH);
    
    JPanel titlePanel=new JPanel(new BorderLayout());
    JLabel titleLabel=new JLabel("TV-Browser",JLabel.CENTER);
    JLabel subTitleLabel=new JLabel("http://tvbrowser.sourceforge.net",JLabel.CENTER);
    titleLabel.setFont(bigFont);
    subTitleLabel.setFont(smallFont);
    titlePanel.add(titleLabel,BorderLayout.NORTH);
    titlePanel.add(subTitleLabel,BorderLayout.SOUTH);
    
    contentPane.add(titlePanel,BorderLayout.NORTH);
    
    JPanel content=new JPanel(new BorderLayout());
    Font font;
    String copyrightText = mLocalizer.msg("copyrightText",
      "Copyright (c) 04/2003 by Martin Oberhauser, Til Schneider under the"
      + "GNU General Public License");
    JTextArea copyrightArea=new JTextArea(copyrightText);
    
    copyrightArea.setFont(smallFont);
    copyrightArea.setWrapStyleWord(true);
    copyrightArea.setEditable(false);
    copyrightArea.setLineWrap(true);
    copyrightArea.setOpaque(false);
    copyrightArea.setFocusable(false);
    
    content.add(copyrightArea,BorderLayout.SOUTH);
    contentPane.add(content,BorderLayout.CENTER);
    
    
    
    JPanel infoPanel=new JPanel();
    infoPanel.setLayout(new BoxLayout(infoPanel,BoxLayout.Y_AXIS));
    
    JPanel panel2=new JPanel(new BorderLayout());
    
    JPanel panel1=new JPanel(new BorderLayout());
    infoPanel.add(new InfoEntry(mLocalizer.msg("version", "Version"),
      TVBrowser.VERSION));
    infoPanel.add(new InfoEntry(mLocalizer.msg("platform", "Platform"),
      System.getProperty("os.name") + " " + System.getProperty("os.version")));
    infoPanel.add(new InfoEntry(mLocalizer.msg("system", "System"),
      System.getProperty("os.arch")));
    infoPanel.add(new InfoEntry(mLocalizer.msg("javaVersion", "Java Version"),
      System.getProperty("java.version")));
    infoPanel.add(new InfoEntry(mLocalizer.msg("javaVM", "Java VM"),
      System.getProperty("java.vm.name")));
    infoPanel.add(new InfoEntry(mLocalizer.msg("javaVendor", "Java vendor"),
      System.getProperty("java.vendor")));
    infoPanel.add(new InfoEntry(mLocalizer.msg("javaHome", "Java home"),
      System.getProperty("java.home")));
    infoPanel.add(new InfoEntry(mLocalizer.msg("location", "Location"),
      System.getProperty("user.country") + "," + System.getProperty("user.language")));
    infoPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
    
    panel1.add(infoPanel,BorderLayout.NORTH);
    
    panel2.add(new PicturePanel("imgs/icon.gif"),BorderLayout.WEST);
    panel2.add(panel1,BorderLayout.EAST);
    panel2.setBorder(BorderFactory.createEmptyBorder(0,0,15,0));
    
    content.add(panel2,BorderLayout.CENTER);
  }
  
}