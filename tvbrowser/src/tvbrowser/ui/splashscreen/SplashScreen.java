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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.splashscreen;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JWindow;

import tvbrowser.TVBrowser;
import util.io.IOUtilities;
import util.ui.UiUtilities;

public class SplashScreen implements Splash {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(SplashScreen.class);

  private static final Font MESSAGE_FONT = new Font("Dialog", Font.BOLD, 15);
  private static final Font VERSION_FONT = new Font("Dialog", Font.BOLD, 12);
  private static final Font DOMAIN_FONT = new Font("Dialog", Font.PLAIN, 10);

  private static final String DOMAIN = "tvbrowser.org";
  private static final String VERSION = TVBrowser.VERSION.toString();
  private static final String INFO = mLocalizer.msg("info", "The free program guide");

  private String mMessage;
  private int mMsgX, mMsgY;
  private int mVersionX, mVersionY;
  private int mDomainX, mDomainY;

  protected String mImgFileName;
  
  private java.awt.SplashScreen mSplashScreen;
  
  private Image mImage;
  private JWindow mSplashWindow;

  public SplashScreen() {
    super();
    mSplashScreen = java.awt.SplashScreen.getSplashScreen();
    mMessage = mLocalizer.ellipsisMsg("loading", "Loading");
    
    if(mSplashScreen == null) {
      try {
        byte[] image = IOUtilities.loadFileFromJar("splash.png", SplashScreen.class);
        mImage = new ImageIcon(image).getImage();
        mSplashWindow = new JWindow() {
          public void paint(Graphics g) {
            ((Graphics2D)g).setBackground(new Color(0,0,0,0));
            g.clearRect(0, 0, getWidth(), getHeight());
            if (mImage != null) {
              g.drawImage(mImage, 0, 0, null);
            }
            
            paintOnce((Graphics2D)g);
          }
        };
        mSplashWindow.setSize(mImage.getWidth(null),mImage.getHeight(null));
        
        setTransparentBackground(true);
        ((JPanel)mSplashWindow.getContentPane()).setOpaque(false);
        ((JPanel)mSplashWindow.getContentPane()).setBackground(new Color(0,0,0,0));
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }
  
  

  public void paintOnce(Graphics2D grp) {
    // enable anti-aliasing for progress texts
    Graphics2D graphics = (Graphics2D) grp;
   graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    grp.setFont(MESSAGE_FONT);

    // Draw the message itself
    grp.setColor(new Color(0,0,0,128));
    grp.drawString(mMessage, mMsgX+2, mMsgY+38);
    grp.drawString(INFO, mMsgX+2, mMsgY+2);
    
    grp.setColor(Color.white);
    grp.drawString(mMessage, mMsgX, mMsgY+36);
    grp.drawString(INFO, mMsgX, mMsgY);

    grp.setFont(VERSION_FONT);
    
    grp.setColor(Color.darkGray);
    grp.drawString(VERSION, mVersionX, mVersionY);
    
    grp.setFont(DOMAIN_FONT);
    grp.setColor(Color.darkGray);
    grp.drawString(DOMAIN, mDomainX, mDomainY);
    grp.dispose();
  }

  public void setMessage(final String msg) {
    new Thread() {
      public void run() {
        mMessage = msg;
        
        if(mSplashScreen != null && mSplashScreen.isVisible()) {
          Graphics2D g2d2 = mSplashScreen.createGraphics();
          g2d2.setColor(Color.white);
          g2d2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
          
          Graphics2D g2d = mSplashScreen.createGraphics();
          g2d.setComposite(AlphaComposite.Clear);
          g2d.fillRect(0, 0, getWidth(), getHeight());          
          g2d.dispose();
          
          paintOnce(g2d2);
          
          g2d2.dispose();
          
          mSplashScreen.update();
        }
        else if(mSplashWindow != null){
          mSplashWindow.repaint();
        }
      }

    }.start();
  }
  
  private int getWidth() {
    if(mSplashScreen != null) {
      return mSplashScreen.getSize().width;
    }
    else if(mSplashWindow != null) {
      return mSplashWindow.getWidth();
    }
    
    return 0;
  }

  private int getHeight() {
    if(mSplashScreen != null) {
      return mSplashScreen.getSize().height;
    }
    else if(mSplashWindow != null) {
      return mSplashWindow.getHeight();
    }
    
    return 0;
  }
  
  public void hideSplash() {
    if(mSplashScreen != null && mSplashScreen.isVisible()) {
      mSplashScreen.close();
    }
    else if(mSplashWindow != null) {
      mSplashWindow.dispose();
    }
  }

  @Override
  public void showSplash() {

    Thread thread = new Thread("Splash screen creation") {
      @Override
      public void run() {
        mMsgY = 100;

        mDomainX = getWidth() - UiUtilities.getStringWidth(DOMAIN_FONT, DOMAIN)-45;
        mDomainY = getHeight() - 30;

        mMsgX = 24;
        
        mVersionX = 24;
        mVersionY = getHeight() - 30;
        
        if(mSplashWindow != null) {
          mSplashWindow.setLocationRelativeTo(null);
          mSplashWindow.setVisible(true);
        }
      }
    };
    thread.setPriority(Thread.NORM_PRIORITY);
    thread.start();
  }
  
  /**
   * Sets the if the clock background should be transparent.
   * Copied from ClockPlugin.
   * <p>
   * @param value <code>true</code> if the clock background should be transparent.
   */
  public void setTransparentBackground(boolean value) {
    GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    GraphicsConfiguration config = devices[0].getDefaultConfiguration();
    
    try {
      Class<?> awtUtilities = Class.forName("com.sun.awt.AWTUtilities");
      Method m = awtUtilities.getMethod("isTranslucencyCapable",new Class<?>[] {GraphicsConfiguration.class});
      
      if((Boolean)m.invoke(awtUtilities, new Object[] {config})) {
        m = awtUtilities.getMethod("setWindowOpaque",new Class<?>[] {Window.class,boolean.class});
        m.invoke(awtUtilities, new Object[] {mSplashWindow,!value});
      }
    } catch (Exception e) {e.printStackTrace();
      
      
      try {
        Method m = config.getClass().getMethod("isTranslucencyCapable()",new Class<?>[] {GraphicsConfiguration.class});
        
        if((Boolean)m.invoke(config,new Object[0])) {
          m = this.getClass().getMethod("setOpacity",new Class<?>[] {float.class});
          m.invoke(mSplashWindow,new Object[] {(float)(value ? 0 : 1)});
        }
      } catch (Exception e1) {}
    }    
  }

}