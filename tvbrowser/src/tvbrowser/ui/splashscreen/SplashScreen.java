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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JWindow;
import javax.swing.SwingUtilities;

import tvbrowser.TVBrowser;
import util.ui.ImageUtilities;
import util.ui.UiUtilities;

public class SplashScreen extends JWindow implements Splash {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(SplashScreen.class);

  private static final Font MESSAGE_FONT = new Font("Dialog", Font.BOLD, 16);
  private static final Font VERSION_FONT = new Font("Dialog", Font.BOLD, 16);
  private static final Font DOMAIN_FONT = new Font("Dialog", Font.PLAIN, 10);

  private static final String DOMAIN = "tvbrowser.org";
  private static final String VERSION = TVBrowser.VERSION.toString();

  private Image mImage;
  private String mMessage;
  private int mMsgX, mMsgY;
  private int mVersionX, mVersionY;
  private int mDomainX, mDomainY;
  private Point mDraggingPoint;

  private Color mForeground;

  protected String mImgFileName;

  public SplashScreen(final String imgFileName, final int msgX, int msgY,
      final Color foreground) {
    super();
    mImgFileName = imgFileName;
    mMessage = mLocalizer.ellipsisMsg("loading", "Loading");

    mMsgX = msgX;

    mForeground = foreground;

    this.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        mDraggingPoint = e.getPoint();
      }

      public void mouseReleased(MouseEvent e) {
        mDraggingPoint = null;
      }
    });
    this.addMouseMotionListener(new MouseMotionListener() {

      public void mouseDragged(MouseEvent e) {
        if (mDraggingPoint != null) {
          int xP = e.getX();
          int yP = e.getY();
          int x = mDraggingPoint.x - xP;
          int y = mDraggingPoint.y - yP;

          if (x != 0 || y != 0) {
            setLocation(getX() - x, getY() - y);
          }
        }
      }

      public void mouseMoved(MouseEvent e) {
      }
    });
  }

  public void paint(Graphics grp) {
    if (mImage != null) {
      grp.drawImage(mImage, 0, 0, null);
    }

    // enable anti-aliasing for progress texts
    Graphics2D graphics = (Graphics2D) grp;
    graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    grp.setFont(MESSAGE_FONT);

    // Draw the message itself
    grp.setColor(mForeground);
    grp.drawString(mMessage, mMsgX, mMsgY);

    grp.setFont(VERSION_FONT);
    grp.drawString(VERSION, mVersionX, mVersionY);

    grp.setFont(DOMAIN_FONT);
    grp.drawString(DOMAIN, mDomainX, mDomainY);

  }

  public void setMessage(final String msg) {
    SwingUtilities.invokeLater(new Runnable() {

      public void run() {
        mMessage = msg;
        repaint(0, getHeight() - 40, getWidth(), 40);
      }

    });
  }

  public void hideSplash() {
    setVisible(false);
  }

  @Override
  public void showSplash() {

    Thread thread = new Thread("Splash screen creation") {
      @Override
      public void run() {
        mImage = ImageUtilities.createImage(mImgFileName);
        if (mImage != null) {
          ImageUtilities.waitForImageData(mImage, null);
          setSize(mImage.getWidth(null), mImage.getHeight(null));
        } else {
          setSize(100, 50);
        }

        mMsgY = getHeight() - 9;

        mDomainX = getWidth() - UiUtilities.getStringWidth(DOMAIN_FONT, DOMAIN)
            - 10;
        mDomainY = getHeight() - 7;

        mVersionX = getWidth() - UiUtilities.getStringWidth(VERSION_FONT, VERSION)
            - 10;
        mVersionY = getHeight() - 20;

        // have window opening in UI thread
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            UiUtilities.centerAndShow(SplashScreen.this);
          }});
      }
    };
    thread.setPriority(Thread.NORM_PRIORITY);
    thread.start();
  }

}