/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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

package tvbrowser.ui.splashscreen;
 
import javax.swing.JWindow;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import tvbrowser.ui.SkinPanel;

public class SplashScreen extends JWindow {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SplashScreen.class);
  
  private Image image;
  private JLabel msgLabel;
  
  
  
  public SplashScreen(String imgName, int width, int height) {  
    super();
    
    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setLayout(new BorderLayout());
    SkinPanel content=new SkinPanel(imgName,SkinPanel.SINGLE);
    
    contentPane.add(content,BorderLayout.CENTER);
    msgLabel = new JLabel(mLocalizer.msg("loading", "Loading..."));
    msgLabel.setHorizontalAlignment(JLabel.CENTER);
    contentPane.add(msgLabel,BorderLayout.CENTER);
    
    this.setSize(width,height);
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    
    this.setBounds((screenSize.width-width)/2,(screenSize.height-height)/2,width,height);
  }



  public void setMessage(String msg) {
    msgLabel.setText(msg);
  }

}