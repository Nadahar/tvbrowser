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
package util.ui;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.io.IOUtilities;

import devplugin.Plugin;
import devplugin.Program;

import tvbrowser.core.*;

/**
 * A ProgramPanel is a JComponent representing a single program.
 *
 * @author Martin Oberhauser
 */
public class ProgramPanel extends JComponent /*tvbrowser.ui.SkinPanel*/ implements ChangeListener {
  
  private static final boolean USE_FULL_HEIGHT = true;
  private static final boolean PAINT_EXPIRED_PROGRAMS_PALE = true;
  
  private static final Color COLOR_ON_AIR_DARK  = new Color(128, 128, 255, 80);
  private static final Color COLOR_ON_AIR_LIGHT = new Color(128, 128, 255, 40);
  private static final Color COLOR_MARKED       = new Color(255, 0, 0, 40);
  private static final Composite NORMAL_COMPOSITE = AlphaComposite.SrcOver;
  private static final Composite PALE_COMPOSITE
    = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);

  /** The title font. */
  private static Font TITLE_FONT = Settings.getProgramTitleFont();
  /** The time font. */
  private static Font TIME_FONT = Settings.getProgramTimeFont();
  /** The normal font */ 
  private static Font NORMAL_FONT = Settings.getProgramInfoFont();
  /** The width of the left part (the time). */  
  private static final int WIDTH_LEFT = 40;
  /** The width of the left part (the title and short info). */  
  private static int WIDTH_RIGHT = Settings.getColumnWidth() - WIDTH_LEFT;
  /** The total width. */  
  private static int WIDTH = WIDTH_LEFT + WIDTH_RIGHT;
  
  /** The height. */  
  private int mHeight = 0;
  /**
   * The preferred height.
   * <p>
   * It's the height the panel has with a maximum of 3 information rows.
   */  
  private int mPreferredHeight = 0;
  /** The icon used to render the title. */  
  private TextAreaIcon mTitleIcon;
  /** The icon used to render the description. */  
  private TextAreaIcon mDescriptionIcon;
  /** The start time as String. */  
  private String mProgramTimeAsString;
  /** The list of the icons to show on the left side. */
  private ArrayList mIconList;
  /** The program. */  
  private Program mProgram;

  
  
  /**
   * Creates a new instance of ProgramPanel.
   *
   * @param prog The program to show in this panel.
   */  
  public ProgramPanel() {
    mTitleIcon = new TextAreaIcon(null, TITLE_FONT, WIDTH_RIGHT - 5);
    mDescriptionIcon = new TextAreaIcon(null, NORMAL_FONT, WIDTH_RIGHT - 5);
    mDescriptionIcon.setMaximumLineCount(3);    

    mIconList = new ArrayList();
  }
  

  
  /**
   * Creates a new instance of ProgramPanel.
   *
   * @param prog The program to show in this panel.
   */  
  public ProgramPanel(devplugin.Program prog) {
    this();
    setProgram(prog);
  }


  public static void updateFonts() {
    TITLE_FONT = Settings.getProgramTitleFont();
    TIME_FONT = Settings.getProgramTimeFont();
    NORMAL_FONT = Settings.getProgramInfoFont();  
  }
  
  public static void updateColumnWidth() {
    WIDTH_RIGHT = Settings.getColumnWidth() - WIDTH_LEFT;
    WIDTH = WIDTH_LEFT + WIDTH_RIGHT;
  }


  /**
   * Gets the preferred height.
   * <p>
   * It's the height the panel has with a maximum of 3 information rows.
   * 
   * @return The preferred height.
   */  
  public int getPreferredHeight() {
    return mPreferredHeight;
  }

  
  /**
   * Sets the height of this panel
   * 
   * @param height
   */
  public void setHeight(int height) {
    if (mHeight != height) {
      setProgram(getProgram(), height);
      mHeight = height;
    }
  }
  
  
  /**
   * Gets the height.
   */
  public int getHeight() {
    return mHeight;
  }

  
  /**
   * Sets the program this panel shows.
   *
   * @param program The program to show in this panel.
   */  
  public void setProgram(devplugin.Program program) {
    setProgram(program, -1);    
  }
  
  
  /**
   * Sets the program this panel shows.
   *
   * @param program The program to show in this panel.
   */  
  public void setProgram(devplugin.Program program, int maxHeight) {
    Program oldProgram = mProgram;
    mProgram = program;
    
    boolean programChanged = (oldProgram != program);
    if (programChanged) {
      // Get the icons from the plugins
      mIconList.clear();
      Plugin[] pluginArr = PluginManager.getInstance().getInstalledPlugins();
      for (int i = 0; i < pluginArr.length; i++) {
        Icon[] iconArr = pluginArr[i].getProgramTableIcons(program);
        if (iconArr != null) {
          for (int j = 0; j < iconArr.length; j++) {
            mIconList.add(iconArr[j]);
          }
        }
      }
      mIconList.trimToSize();
      
      // Get the start time
      mProgramTimeAsString = program.getTimeString();
      
      // Set the new title
      mTitleIcon.setText(program.getTitle());
    }
    
    // Calculate the maximum description lines
    int titleHeight = mTitleIcon.getIconHeight();
    int maxDescLines = 3;
    if (maxHeight != -1) {
      maxDescLines = (maxHeight - titleHeight - 10) / NORMAL_FONT.getSize();
    }
    
    if (programChanged || (maxDescLines != mDescriptionIcon.getMaximumLineCount())) {
      // (Re)set the description text
      mDescriptionIcon.setMaximumLineCount(maxDescLines);
      String shortInfo = program.getShortInfo();
      if ((shortInfo == null) || shortInfo.endsWith("...")) {
        mDescriptionIcon.setText(program.getDescription());
      } else {
        mDescriptionIcon.setText(shortInfo);
      }

      // Calculate the height
      mHeight = mTitleIcon.getIconHeight() + 10 + mDescriptionIcon.getIconHeight();
      setPreferredSize(new Dimension(WIDTH, mHeight));

      // Calculate the preferred height
      mPreferredHeight = titleHeight + (3 * NORMAL_FONT.getSize()) + 10;
      if (mHeight < mPreferredHeight) {
        mPreferredHeight = mHeight;
      }
    }

    if (isShowing()) {
      oldProgram.removeChangeListener(this);
      mProgram.addChangeListener(this);
      revalidate();
      repaint();
    }
  }


  
  /**
   * Paints the component.
   *
   * @param grp The graphics context to paint to.
   */  
  public void paintComponent(Graphics g) {
    int width = getWidth();
    int height = USE_FULL_HEIGHT ? getHeight() : mHeight;
    Graphics2D grp = (Graphics2D) g;

    // Draw the background if this program is on air
    if (mProgram.isOnAir()) {
      int minutesAfterMidnight = IOUtilities.getMinutesAfterMidnight();
      int progLength = mProgram.getLength();
      int startTime = mProgram.getHours() * 60 + mProgram.getMinutes();
      int elapsedMinutes;
      if (minutesAfterMidnight < startTime) {
        // The next day has begun -> we have to add 24 * 60 minutes
        // Example: Start time was 23:50 = 1430 minutes after midnight
        //          now it is       0:03 = 3 minutes after midnight
        //          elapsedMinutes = (24 * 60) + 3 - 1430 = 13 minutes
        elapsedMinutes = (24 * 60) + minutesAfterMidnight - startTime;
      } else {
        elapsedMinutes = minutesAfterMidnight - startTime;
      }
      int progressY = 0;
      if (progLength > 0) {
        progressY = elapsedMinutes * height / progLength;
      }

      grp.setColor(COLOR_ON_AIR_DARK);
      grp.fillRect(1, 1, width - 2, progressY - 1);
      grp.setColor(COLOR_ON_AIR_LIGHT);
      grp.fillRect(1, progressY, width - 2, height - progressY - 1);
      grp.draw3DRect(0, 0, width - 1, height - 1, true);
    }

    // If there are plugins that have marked the program -> paint the background
    Plugin[] markedByPluginArr = mProgram.getMarkedByPlugins();
    if (markedByPluginArr.length != 0) {
      grp.setColor(COLOR_MARKED);
      grp.fill3DRect(0, 0, width, height, true);
    }

    // Draw all the text
    if (PAINT_EXPIRED_PROGRAMS_PALE && mProgram.isExpired()) {
      grp.setColor(Color.gray);
    } else {
      grp.setColor(Color.black);
    }
    grp.setFont(ProgramPanel.TIME_FONT);
    grp.drawString(mProgramTimeAsString, 1, TIME_FONT.getSize());
    mTitleIcon.paintIcon(this, grp, WIDTH_LEFT, 0);
    mDescriptionIcon.paintIcon(this, grp, WIDTH_LEFT, mTitleIcon.getIconHeight());

    // Paint the icons pale if the program is expired
    if (PAINT_EXPIRED_PROGRAMS_PALE && mProgram.isExpired()) {
      grp.setComposite(PALE_COMPOSITE);
    }

    // paint the icons of the plugins that have marked the program
    int x = width - 1;
    int y = mTitleIcon.getIconHeight() + mDescriptionIcon.getIconHeight() + 18;
    y = Math.min(y, height - 1);
    for (int i = 0; i < markedByPluginArr.length; i++) {
      Icon icon = markedByPluginArr[i].getMarkIcon();
      if (icon != null) {
        x -= icon.getIconWidth();
        icon.paintIcon(this, grp, x, y - icon.getIconHeight());
      }
    }
    
    // Paint the icons on the left side
    x = 2;
    y = TIME_FONT.getSize() + 3;
    for (int i = 0; i < mIconList.size(); i++) {
      Icon icon = (Icon) mIconList.get(i);
      int iconHeight = icon.getIconHeight();
      if ((y + iconHeight) < mHeight) {
        icon.paintIcon(this, grp, x, y);
        y += iconHeight + 2;
      }
    }

    // Reset the old composite
    if (PAINT_EXPIRED_PROGRAMS_PALE && mProgram.isExpired()) {
      grp.setComposite(NORMAL_COMPOSITE);
    }
  }

  
  
  /**
   * Called when the panel is added to a container.
   * <p>
   * registers the panel as ChangeListener at the program.
   */  
  public void addNotify() {
    super.addNotify();
    mProgram.addChangeListener(this);
  }

  
  
  /**
   * Called when the panel is added to a container.
   * <p>
   * removes the panel as ChangeListener from the program.
   */  
  public void removeNotify() {
    super.removeNotify();
    mProgram.removeChangeListener(this);
  }
  


  /**
   * Gets the program object of this ProgramPanel.
   *
   * @return the program object of this ProgramPanel.
   */
  public Program getProgram() {
    return mProgram;
  }
  
  
  // implements ChangeListener
  
  
  /**
   * Called when the state of the program has changed.
   * <p>
   * repaints the panel.
   *
   * @param evt The event describing the change.
   */  
  public void stateChanged(ChangeEvent evt) {
    if (evt.getSource() == mProgram) {
      repaint();
    }
  }

}