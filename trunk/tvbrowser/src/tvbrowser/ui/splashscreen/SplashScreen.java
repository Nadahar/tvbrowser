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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JWindow;

import util.ui.ImageUtilities;

public class SplashScreen extends JWindow {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(SplashScreen.class);
  
  private static final Font MESSAGE_FONT = new Font("Dialog", Font.BOLD, 12);
  
  private Image mImage;
  private String mMessage;
  private int mMsgX, mMsgY;
  private Color mBackground, mForeground;
  
  
  
  public SplashScreen(String imgFileName, int msgX, int msgY,
    Color background, Color foreground)
  {
    super();
    
    setFont(MESSAGE_FONT);
    
    mImage = ImageUtilities.createImage(imgFileName);
    if (mImage != null) {
      ImageUtilities.waitForImageData(mImage, null);
      setSize(mImage.getWidth(null), mImage.getHeight(null));
    } else {
      setSize(100, 50);
    }
    
    mMessage = mLocalizer.msg("loading", "Loading...");
    
    mMsgX = msgX;
    mMsgY = msgY;
    
    mBackground = background;
    mForeground = foreground;
  }

  
  
  public void paint(Graphics grp) {
    if (mImage != null) {
      grp.drawImage(mImage, 0, 0, null);
    }
    
    // Draw the message border
    grp.setColor(mBackground);
    for (int x = -1; x <= 1; x++) {
      for (int y = -1; y <= 1; y++) {
        grp.drawString(mMessage, mMsgX + x, mMsgY + y);
      }
    }

    // Draw the message itself
    grp.setColor(mForeground);
    grp.drawString(mMessage, mMsgX, mMsgY);
  }
  

  
  public void setMessage(String msg) {
    mMessage = msg;
    repaint();
  }

}