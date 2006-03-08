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
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.Timer;

import tvbrowser.core.Settings;
import tvbrowser.extras.programinfo.ProgramInfo;
import util.ui.Localizer;
import util.ui.TextAreaIcon;
import util.ui.UiUtilities;
import devplugin.Program;

/**
 * A class that contains a program in a JMenuItem.
 * 
 * @author René Mach
 * 
 */
public class ProgramMenuItem extends JMenuItem implements ActionListener {

  private static final long serialVersionUID = 1L;
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(ProgramMenuItem.class);

  private Program mProgram;
  private Color mBackground, mFill = null;
  private Insets mInsets;
  boolean mSelected;
  private Timer mTimer;
  private Font mPlainFont, mBoldFont;
  private int mIconHeight = 0;
  private boolean mShowStartTime, mShowDate;
  private Icon mIcon = null;
  private TextAreaIcon mChannelName;
  
  protected final static int CHANNEL_WIDTH = 72;
  protected final static int TIME_WIDTH = 42;
  protected final static int DATE_WIDTH = 70;

  /**
   * Creates the JMenuItem.
   * 
   * @param p
   *          The program to show
   * @param showStartTime
   *          If the start time of the program should be shown.
   * @param showDate If the date of the program should be shown.
   * @param time The time of the time button.
   * @param n A value represents the position of this MenuItem.
   */
  public ProgramMenuItem(Program p, boolean showStartTime, boolean showDate, int time, int n) {
    mProgram = p;
    mBackground = getBackground();
    mShowStartTime = showStartTime;
    mShowDate = showDate;
    mPlainFont = getFont();
    mBoldFont = mPlainFont.deriveFont(Font.BOLD);
    mChannelName = new TextAreaIcon(p.getChannel().getName(), mBoldFont,CHANNEL_WIDTH);

    if(n % 2 == 1 && n != -1) {
      Color temp = mBackground.darker();
      mFill = new Color(temp.getRed(),temp.getGreen(),temp.getBlue(),145);
      setBackground(mFill);
    }
   
    if (Settings.propProgramsInTrayContainsChannelIcon.getBoolean()) {
      mIcon = UiUtilities.createChannelIcon(p.getChannel().getIcon());
      mIconHeight = mIcon.getIconHeight();
      setMargin(new Insets(1,getMargin().left,1,getMargin().right));
    }

    if (p.getMarkerArr().length > 0)
      setForeground(Color.red);
    
    mSelected = false;

    addActionListener(this);
      
    mInsets = getMargin();
    setUI(new ProgramMenuItemUI(p, mChannelName,mIcon,showStartTime,showDate, time));
    
    if (Settings.propProgramsInTrayShowTooltip.getBoolean()) {
      int end = p.getStartTime() + p.getLength();

      if (end > 1440)
        end -= 1440;

      String hour = String.valueOf(end / 60);
      String minute = String.valueOf(end % 60);

      StringBuffer info = new StringBuffer(p.getShortInfo() == null ? "" : p
          .getShortInfo());

      if (minute.length() == 1)
        minute = "0" + minute;

      for (int i = 20; i < info.length(); i += 24) {
        int index = info.indexOf(" ", i);
        if (index == -1)
          index = info.indexOf("\n", i);
        if (index != -1) {
          info.deleteCharAt(index);
          info.insert(index, "<br>");
          i += index - i;
        }

      }

      StringBuffer toolTip = new StringBuffer(mLocalizer.msg("to", "To: "))
          .append(hour).append(":").append(minute).append(
              info.length() > 0 ? "<br>" : "").append(info);

      setToolTipText("<html>" + toolTip + "</html>");
    }
    
    mTimer = new Timer(10000, new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        repaint();
      }

    });
    startTimer();
  }

  public void actionPerformed(ActionEvent e) {
    ProgramInfo.getInstance().getContextMenuActions(mProgram).getAction()
        .actionPerformed(e);
  }
  
  public Dimension getPreferredSize() {
    FontMetrics fmBold = getFontMetrics(mBoldFont);
    FontMetrics fmPlain = getFontMetrics(mPlainFont);
    
    int height = mIconHeight;
    int width = fmPlain.stringWidth(mProgram.getTitle()) + mInsets.left + mInsets.right + 10;
    
    if(height != 0)
      width += mIcon.getIconWidth() + getIconTextGap();
    else
      width += 30;
    
    if(Settings.propProgramsInTrayContainsChannel.getBoolean())
      width += CHANNEL_WIDTH;
    if(mShowStartTime)
      width += TIME_WIDTH;
    if(mShowDate)
      width += DATE_WIDTH;
    
    if(height == 0) {
      if(mShowStartTime || mShowDate)
        height = fmBold.getHeight();
      else
        height = fmPlain.getHeight();
      
      height += mInsets.top + mInsets.bottom;
    }
    else    
      height += 2;

    if(mChannelName.getIconHeight() > height && Settings.propProgramsInTrayContainsChannel.getBoolean())
      height = mChannelName.getIconHeight() + mInsets.top + mInsets.bottom + 2;

    return new Dimension(width,height);
  }  
  
  /**
   * Sets the backgound:
   * n == -1 The default background
   * n % 2 == 1 The default background a little brighter
   * 
   * @param n The Background color flag.
   */
  public void setBackground(int n) {
    if(n == -1)
      setBackground(mBackground);
    else if(n % 2 == 1) {
        Color temp = mBackground.darker();
        mFill = new Color(temp.getRed(),temp.getGreen(),temp.getBlue(),145);
        setBackground(mFill);
      }
  }
  
  private void startTimer() {
    if(!mTimer.isRunning())
      mTimer.start();
  }
  
  protected void stopTimer() {
    if(mTimer.isRunning())
      mTimer.stop();
    setForeground(Color.gray);
  }
  
  protected Color getDefaultBackground() {
    return mBackground;
  }
}
