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
package tvbrowser.ui.tray;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import tvbrowser.core.Settings;
import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.core.contextmenu.DoNothingContextMenuItem;
import tvbrowser.core.plugin.PluginProxyManager;
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
  
  private boolean mShowToolTip = true;
  private String mToolTipTextBuffer;
  
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
    boolean showIcon = true;
    
    if(type == NOW_TYPE) {
      mShowStartTime = Settings.propTrayNowProgramsContainsTime.getBoolean();
      mShowDate = false;
      mShowName = Settings.propTrayNowProgramsContainsName.getBoolean();
      showIcon = Settings.propTrayNowProgramsContainsIcon.getBoolean();
      mShowToolTip = Settings.propTrayNowProgramsContainsToolTip.getBoolean();
    }
    else if(type == SOON_TYPE) {
      mShowStartTime = Settings.propTraySoonProgramsContainsTime.getBoolean();
      mShowDate = false;
      mShowName = Settings.propTraySoonProgramsContainsName.getBoolean();
      showIcon = Settings.propTraySoonProgramsContainsIcon.getBoolean();
      mShowToolTip = Settings.propTraySoonProgramsContainsToolTip.getBoolean();
    }
    else if(type == ON_TIME_TYPE) {
      mShowStartTime = Settings.propTrayOnTimeProgramsContainsTime.getBoolean();
      mShowDate = false;
      mShowName = Settings.propTrayOnTimeProgramsContainsName.getBoolean();
      showIcon = Settings.propTrayOnTimeProgramsContainsIcon.getBoolean();
      mShowToolTip = Settings.propTrayOnTimeProgramsContainsToolTip.getBoolean();
      
      if(!Settings.propTrayOnTimeProgramsShowProgress.getBoolean()) {
        time = -1;
      }
    }
    else if (type == IMPORTANT_TYPE) {
      mShowStartTime = Settings.propTrayImportantProgramsContainsTime.getBoolean();
      mShowDate = Settings.propTrayImportantProgramsContainsDate.getBoolean();
      mShowName = Settings.propTrayImportantProgramsContainsName.getBoolean();
      showIcon = Settings.propTrayImportantProgramsContainsIcon.getBoolean();
      mShowToolTip = Settings.propTrayImportantProgramsContainsToolTip.getBoolean();
    }
    else {
      mShowStartTime = true;
      mShowDate = false;
      mShowName = true;
    }
        
    mChannelName = new TextAreaIcon(p.getChannel().getName(), mBoldFont, Settings.propTrayChannelWidth.getInt());
    mChannelName.setMaximumLineCount(2);

    if(mShowToolTip) {
      ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
      
      toolTipManager.registerComponent(this);
    }

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
    
    addMouseListener(new MouseAdapter() {
      @Override
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
        } else if (SwingUtilities.isRightMouseButton(e)) {
          Point point = e.getPoint();
          SwingUtilities.convertPointToScreen(point,e.getComponent());
          showPopup(point,PluginProxyManager.createPluginContextMenu(mProgram));
          e.consume();
        }
      }
    });
      
    mInsets = getMargin();
    setUI(new ProgramMenuItemUI(p, mChannelName,mIcon,mShowStartTime,mShowDate,showIcon,mShowName,time));
    
    mTimer = new Timer(10000, new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        repaint();
      }

    });
    startTimer();
  }
  
  @Override
  public void setPreferredSize(Dimension dim) {
    mPreferredSize = dim;
  }
  
  @Override
  public Dimension getPreferredSize() {
    if(mPreferredSize != null) {
      return mPreferredSize;
    }
    
    FontMetrics fmBold = getFontMetrics(mBoldFont);
    FontMetrics fmPlain = getFontMetrics(mPlainFont);
    
    int height = mIconHeight;
    int width = fmPlain.stringWidth(mProgram.getTitle().length() > 70 ? mProgram.getTitle().substring(0,67) + "..." : mProgram.getTitle()) + mInsets.left + mInsets.right + 10;
    
    if(height != 0) {
      width += mIcon.getIconWidth() + getIconTextGap();
    } else {
      width += 30;
    }
    
    if(mShowName) {
      width += Settings.propTrayChannelWidth.getInt() + getIconTextGap();
    }
    if(mShowStartTime) {
      width += TIME_WIDTH;
    }
    if(mShowDate) {
      width += DATE_WIDTH;
    }
    
    if(height == 0) {
      if(mShowStartTime || mShowDate) {
        height = fmBold.getHeight();
      } else {
        height = fmPlain.getHeight();
      }
      
      height += mInsets.top + mInsets.bottom;
    } else {
      height += 2;
    }

    if(mChannelName.getIconHeight() > height && mShowName) {
      height = mChannelName.getIconHeight() + mInsets.top + mInsets.bottom + 2;
    }

    return new Dimension(width,height);
  }

  /**
   * Sets the background: n == -1 The default background n % 2 == 1 The default
   * background a little brighter
   * 
   * @param n
   *          The Background color flag.
   */
  public void setBackground(int n) {
    if(n == -1) {
      setBackground(mBackground);
    } else if((n & 1) == 1) {
        Color temp = mBackground.darker();
        mFill = new Color(temp.getRed(),temp.getGreen(),temp.getBlue(),145);
        setBackground(mFill);
      }
  }
  
  private void startTimer() {
    if(!mTimer.isRunning()) {
      mTimer.start();
    }
  }
  
  protected void stopTimer() {
    if(mTimer.isRunning()) {
      mTimer.stop();
    }
    setForeground(Color.gray);
  }
  
  protected Color getDefaultBackground() {
    return mBackground;
  }
  
  @Override
  public String getToolTipText() {
    if (mShowToolTip) {
      if(mToolTipTextBuffer == null) {
        String episodeText = CompoundedProgramFieldType.EPISODE_COMPOSITION.getFormattedValueForProgram(mProgram);
        
        StringBuilder episode = new StringBuilder(
            episodeText != null ? episodeText : "");
        breakLines(episode);
        
        StringBuilder info;
        if (mProgram.getShortInfo() == null) {
          String desc = mProgram.getDescription();

          if (desc != null) {
            if (desc.length() > 197) {
              desc = desc.substring(0, 197) + "...";
            }
            info = new StringBuilder(desc);
          }
          else {
            info = new StringBuilder();
          }
        }
        else {
          info = new StringBuilder(mProgram.getShortInfo());
        }
        breakLines(info);
  
        StringBuilder toolTip = new StringBuilder("<html>");
        if (episode.length() > 0) {
          toolTip.append(episode.insert(0, "<b>").append("</b><br>"));
        }
        toolTip.append(mLocalizer.msg("to", "To ")).append(
            mProgram.getEndTimeString());
        if (info.length() > 0) {
          toolTip.append("<br>").append(info);
        }
        toolTip.append("</html>");
  
        mToolTipTextBuffer = toolTip.toString();
      }
      
      return mToolTipTextBuffer;
    }
    
    return "";
  }

  private void breakLines(StringBuilder info) {
    for (int i = 38; i < info.length(); i += 38) {
      int index = info.indexOf(" ", i);
      if (index == -1) {
        index = info.indexOf("\n", i);
      }
      if (index != -1) {
        info.deleteCharAt(index);
        info.insert(index, "<br>");
        i += index - i;
      }
    }
  }
  
  @Override
  public String getToolTipText(MouseEvent e) {
    return getToolTipText();
  }
  
  private void showPopup(final Point p, final JPopupMenu menu) {
    final JDialog popupParent = SystemTray.getProgamPopupParent();
    menu.addPopupMenuListener(new PopupMenuListener() {
      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        popupParent.setVisible(false);
      }

      public void popupMenuCanceled(PopupMenuEvent e) {}
    });
    
    popupParent.setVisible(true);
    popupParent.toFront();
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        menu.show(popupParent,p.x - popupParent.getLocation().x,p.y - popupParent.getLocation().y);
      };
    });
  }

  public Program getProgram() {
    return mProgram;
  }
}
