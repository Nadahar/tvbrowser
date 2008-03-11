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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import tvbrowser.core.Settings;
import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.contextmenu.DoNothingContextMenuItem;
import util.program.CompoundedProgramFieldType;
import util.ui.Localizer;
import util.ui.TextAreaIcon;
import util.ui.UiUtilities;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;

/**
 * A class that contains a program in a JMenuItem.
 * 
 * @author René Mach
 * 
 */
public class ProgramMenuItem extends JMenuItem {

  private static final long serialVersionUID = 1L;
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(ProgramMenuItem.class);

  private Dimension mPreferredSize = null;
  private Program mProgram;
  private Color mBackground, mFill = null;
  private Insets mInsets;
  boolean mSelected;
  private Timer mTimer; 
  private static Font mPlainFont = (new JMenuItem()).getFont();
  private static Font mBoldFont = mPlainFont.deriveFont(Font.BOLD);
  private int mIconHeight = 0;
  private boolean mShowStartTime, mShowDate, mShowName;
  private Icon mIcon = null;
  private TextAreaIcon mChannelName;
    
  protected static final int TIME_WIDTH = (new JMenuItem()).getFontMetrics(mBoldFont).stringWidth(Plugin.getPluginManager().getExampleProgram().getTimeString()) + 10;
  protected static final int DATE_WIDTH = (new JMenuItem()).getFontMetrics(mBoldFont).stringWidth(Plugin.getPluginManager().getExampleProgram().getDateString()) + (Date.getCurrentDate().getDayOfMonth() < 10 ? 15 : 9);
  
  protected static final int NOW_TYPE = 0;
  protected static final int SOON_TYPE = 1;
  protected static final int ON_TIME_TYPE = 2;
  protected static final int IMPORTANT_TYPE = 3;

  /**
   * Creates the JMenuItem.
   * 
   * @param p
   *          The program to show
   * @param type The type of this program menu item.
   * @param time The time after midnight of the menu entry for ON_TIME programs.
   * @param n A value represents the position of this MenuItem.
   */
  public ProgramMenuItem(Program p, int type, int time, int n) {
    mProgram = p;
    mBackground = getBackground();
    boolean showToolTip = true, showIcon = true;
    
    if(type == NOW_TYPE) {
      mShowStartTime = Settings.propTrayNowProgramsContainsTime.getBoolean();
      mShowDate = false;
      mShowName = Settings.propTrayNowProgramsContainsName.getBoolean();
      showIcon = Settings.propTrayNowProgramsContainsIcon.getBoolean();
      showToolTip = Settings.propTrayNowProgramsContainsToolTip.getBoolean();
    }
    else if(type == SOON_TYPE) {
      mShowStartTime = Settings.propTraySoonProgramsContainsTime.getBoolean();
      mShowDate = false;
      mShowName = Settings.propTraySoonProgramsContainsName.getBoolean();
      showIcon = Settings.propTraySoonProgramsContainsIcon.getBoolean();
      showToolTip = Settings.propTraySoonProgramsContainsToolTip.getBoolean();      
    }
    else if(type == ON_TIME_TYPE) {
      mShowStartTime = Settings.propTrayOnTimeProgramsContainsTime.getBoolean();
      mShowDate = false;
      mShowName = Settings.propTrayOnTimeProgramsContainsName.getBoolean();
      showIcon = Settings.propTrayOnTimeProgramsContainsIcon.getBoolean();
      showToolTip = Settings.propTrayOnTimeProgramsContainsToolTip.getBoolean();
      
      if(!Settings.propTrayOnTimeProgramsShowProgress.getBoolean())
        time = -1;
    }
    else if (type == IMPORTANT_TYPE) {
      mShowStartTime = Settings.propTrayImportantProgramsContainsTime.getBoolean();
      mShowDate = Settings.propTrayImportantProgramsContainsDate.getBoolean();
      mShowName = Settings.propTrayImportantProgramsContainsName.getBoolean();
      showIcon = Settings.propTrayImportantProgramsContainsIcon.getBoolean();
      showToolTip = Settings.propTrayImportantProgramsContainsToolTip.getBoolean();
    }
    else {
      mShowStartTime = true;
      mShowDate = false;
      mShowName = true;
    }
        
    mChannelName = new TextAreaIcon(p.getChannel().getName(), mBoldFont, Settings.propTrayChannelWidth.getInt());

    if((n & 1) == 1 && n != -1) {
      Color temp = mBackground.darker();
      mFill = new Color(temp.getRed(),temp.getGreen(),temp.getBlue(),145);
      setBackground(mFill);
    }
   
    if (showIcon) {
      mIcon = UiUtilities.createChannelIcon(p.getChannel().getIcon());
      mIconHeight = mIcon.getIconHeight();
      setMargin(new Insets(1,getMargin().left,1,getMargin().right));
    }
    
    mSelected = false;

    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e)) {
          if(!ContextMenuManager.getInstance().getLeftSingleClickIf().equals(DoNothingContextMenuItem.getInstance())) {
            Plugin.getPluginManager().handleProgramSingleClick(mProgram);
          }
          else {
            Plugin.getPluginManager().handleProgramDoubleClick(mProgram);
          }
        }
        else if(SwingUtilities.isMiddleMouseButton(e)) {
          Plugin.getPluginManager().handleProgramMiddleClick(mProgram);
        }        
      }
    });
      
    mInsets = getMargin();
    setUI(new ProgramMenuItemUI(p, mChannelName,mIcon,mShowStartTime,mShowDate,showIcon,mShowName,time));
    
    if (showToolTip) {
      int end = p.getStartTime() + p.getLength();

      if (end > 1440)
        end -= 1440;

      String hour = String.valueOf(end / 60);
      String minute = String.valueOf(end % 60);

      String episodeText = CompoundedProgramFieldType.EPISODE_COMPOSITION.getFormatedValueForProgram(mProgram);
      
      StringBuffer episode = new StringBuffer(episodeText != null ? episodeText : "");
      
      for (int i = 20; i < episode.length(); i += 28) {
        int index = episode.indexOf(" ", i);
        if (index == -1)
          index = episode.indexOf("\n", i);
        if (index != -1) {
          episode.deleteCharAt(index);
          episode.insert(index, "<br>");
          i += index - i;
        }
      }
      
      String desc = p.getDescription();
      
      if(desc != null) {
        if(desc.length() > 197) {
          desc = desc.substring(0,197) + "..."; 
        }
        else {
          desc += "...";
        }
      }
      else {
        desc = "";
      }
      
      StringBuffer info = new StringBuffer(p.getShortInfo() == null ? desc : p
          .getShortInfo());
      
      if (minute.length() == 1)
        minute = "0" + minute;
      
      for (int i = 20; i < info.length(); i += 28) {
        int index = info.indexOf(" ", i);
        if (index == -1)
          index = info.indexOf("\n", i);
        if (index != -1) {
          info.deleteCharAt(index);
          info.insert(index, "<br>");
          i += index - i;
        }
      }

      StringBuffer toolTip = new StringBuffer(episode.length() > 0 ? episode.insert(0,"<b>").append("</b><br>") : "").append(mLocalizer.msg("to", "To: "))
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
  
  public void setPreferredSize(Dimension dim) {
    mPreferredSize = dim;
  }
  
  public Dimension getPreferredSize() {
    if(mPreferredSize != null)
      return mPreferredSize;
    
    FontMetrics fmBold = getFontMetrics(mBoldFont);
    FontMetrics fmPlain = getFontMetrics(mPlainFont);
    
    int height = mIconHeight;
    int width = fmPlain.stringWidth(mProgram.getTitle().length() > 70 ? mProgram.getTitle().substring(0,67) + "..." : mProgram.getTitle()) + mInsets.left + mInsets.right + 10;
    
    if(height != 0)
      width += mIcon.getIconWidth() + getIconTextGap();
    else
      width += 30;
    
    if(mShowName)
      width += Settings.propTrayChannelWidth.getInt();
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

    if(mChannelName.getIconHeight() > height && mShowName)
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
    else if((n & 1) == 1) {
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
