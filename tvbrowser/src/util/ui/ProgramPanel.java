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
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.logging.Level;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.io.IOUtilities;

import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFieldType;

import tvbrowser.core.*;

/**
 * A ProgramPanel is a JComponent representing a single program.
 *
 * @author Martin Oberhauser
 * @author Til Schneider, www.murfman.de
 */
public class ProgramPanel extends JComponent implements ChangeListener {

  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(TextAreaIcon.class.getName());
  
  private static final boolean USE_FULL_HEIGHT = true;
  private static final boolean PAINT_EXPIRED_PROGRAMS_PALE = true;
  
  private static final Color COLOR_ON_AIR_DARK  = new Color(128, 128, 255, 80);
  private static final Color COLOR_ON_AIR_LIGHT = new Color(128, 128, 255, 40);
  private static final Color COLOR_MARKED       = new Color(255, 0, 0, 40);
  private static final Composite NORMAL_COMPOSITE = AlphaComposite.SrcOver;
  private static final Composite PALE_COMPOSITE
    = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);

  /** The title font. */
  private static Font mTitleFont = Settings.getProgramTitleFont();
  /** The time font. */
  private static Font mTimeFont = Settings.getProgramTimeFont();
  /** The normal font */ 
  private static Font mNormalFont = Settings.getProgramInfoFont();

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
  /** The icons to show on the left side under the start time. */
  private Icon[] mIconArr;
  /** The program. */  
  private Program mProgram;

  
  
  /**
   * Creates a new instance of ProgramPanel.
   *
   * @param prog The program to show in this panel.
   */  
  public ProgramPanel() {
    mTitleIcon = new TextAreaIcon(null, mTitleFont, WIDTH_RIGHT - 5);
    mDescriptionIcon = new TextAreaIcon(null, mNormalFont, WIDTH_RIGHT - 5);
    mDescriptionIcon.setMaximumLineCount(3);    
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


  /**
   * (Re)Loads the font settings.
   */
  public static void updateFonts() {
    mTitleFont = Settings.getProgramTitleFont();
    mTimeFont = Settings.getProgramTimeFont();
    mNormalFont = Settings.getProgramInfoFont();  
  }
  

  /**
   * (Re)Loads the column width settings.
   */
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
      mIconArr = getPluginIcons(program);
      
      // Get the start time
      mProgramTimeAsString = program.getTimeString();
      
      // Set the new title
      mTitleIcon.setText(program.getTitle());
    }
    
    // Calculate the maximum description lines
    int titleHeight = mTitleIcon.getIconHeight();
    int maxDescLines = 3;
    if (maxHeight != -1) {
      maxDescLines = (maxHeight - titleHeight - 10) / mNormalFont.getSize();
    }
    
    if (programChanged || (maxDescLines != mDescriptionIcon.getMaximumLineCount())) {
      // (Re)set the description text
      mDescriptionIcon.setMaximumLineCount(maxDescLines);
      ProgramFieldType[] infoFieldArr = Settings.getProgramInfoFields();
      Reader infoReader = new MultipleFieldReader(program, infoFieldArr);
      try {
        mDescriptionIcon.setText(infoReader);
      }
      catch (IOException exc) {
        mLog.log(Level.WARNING, "Reading program info failed for " + program, exc);
      }

      // Calculate the height
      mHeight = mTitleIcon.getIconHeight() + 10 + mDescriptionIcon.getIconHeight();
      setPreferredSize(new Dimension(WIDTH, mHeight));

      // Calculate the preferred height
      mPreferredHeight = titleHeight + (3 * mNormalFont.getSize()) + 10;
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
   * Gets the plugin icons for a program.
   * 
   * @param program The program to get the icons for.
   * @return The icons for the program.
   */
  private Icon[] getPluginIcons(Program program) {
    ArrayList list = new ArrayList();
    
    String[] iconPluginArr = Settings.getProgramTableIconPlugins();
    Plugin[] pluginArr = PluginManager.getInstance().getInstalledPlugins();
    for (int i = 0; i < iconPluginArr.length; i++) {
      // Find the plugin with this class name and add its icons
      for (int j = 0; j < pluginArr.length; j++) {
        String className = pluginArr[j].getClass().getName();
        if (iconPluginArr[i].equals(className)) {
          // This is the right plugin -> Add its icons
          Icon[] iconArr = pluginArr[j].getProgramTableIcons(program);
          if (iconArr != null) {
            for (int k = 0; k < iconArr.length; k++) {
              list.add(iconArr[k]);
            }
          }
        }
      }
    }
    
    Icon[] asArr = new Icon[list.size()];
    list.toArray(asArr);
    return asArr;
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
    grp.setFont(ProgramPanel.mTimeFont);
    grp.drawString(mProgramTimeAsString, 1, mTimeFont.getSize());
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
    if (mIconArr != null) {
      x = 2;
      y = mTimeFont.getSize() + 3;
      for (int i = 0; i < mIconArr.length; i++) {
        int iconHeight = mIconArr[i].getIconHeight();
        if ((y + iconHeight) < mHeight) {
          mIconArr[i].paintIcon(this, grp, x, y);
          y += iconHeight + 2;
        }
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