/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
 *
 * This mProgram is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This mProgram is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this mProgram; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.programtable;


import java.util.Iterator;



import java.awt.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import util.ui.TextAreaIcon;
import util.io.IOUtilities;

import devplugin.Plugin;

import tvbrowser.core.*;

/**
 * A ProgramPanel is a JComponent representing a single mProgram.
 *
 * @author Martin Oberhauser
 */
public class ProgramPanel extends JComponent implements ChangeListener {
  
  private static final Color COLOR_ON_AIR_DARK  = new Color(128, 128, 255, 80);
  private static final Color COLOR_ON_AIR_LIGHT = new Color(128, 128, 255, 40);
  private static final Color COLOR_MARKED       = new Color(255, 0, 0, 40);

  /** The bold font. */
  private static final Font bold;
  /** The italic font */  
  private static final Font italic;
  /** The width of the left part (the time). */  
  private static final int WIDTH_LEFT = 40;;
  /** The width of the left part (the title and short info). */  
  private static final int WIDTH_RIGHT = Settings.getColumnWidth() - WIDTH_LEFT;
  /** The total width. */  
  private static final int WIDTH = WIDTH_LEFT + WIDTH_RIGHT;
  
  /** The height. */  
  private int mHeight = 0;
  /** The icon used to render the title. */  
  private TextAreaIcon mTitleIcon;
  /** The icon used to render the description. */  
  private TextAreaIcon mDescriptionIcon;
  /** The start time as String. */  
  private String mProgramTimeAsString;
  /** The program. */  
  private devplugin.Program mProgram;

  static {
   // Font f=new JTextArea().getFont();
   // bold=new Font(f.getName(),Font.BOLD,f.getSize());
   // italic=new Font(f.getName(),Font.PLAIN,f.getSize()-2);
   bold=Settings.getProgramTitleFont();
   italic=Settings.getProgramInfoFont();
   
  }

  
  
  /**
   * Creates a new instance of ProgramPanel.
   *
   * @param prog The program to show in this panel.
   */  
  public ProgramPanel() {
  }
  

  
  /**
   * Creates a new instance of ProgramPanel.
   *
   * @param prog The program to show in this panel.
   */  
  public ProgramPanel(devplugin.Program prog) {
    setProgram(prog);
  }
  
  
  
  /**
   * Sets the program this panel shows.
   *
   * @param program The program to show in this panel.
   */  
  public void setProgram(devplugin.Program program) {
    devplugin.Program oldProgram = mProgram;

    mProgram = program;

    char[] title = program.getTitle().toCharArray();
    mTitleIcon = new TextAreaIcon(title, bold, WIDTH_RIGHT - 10);
    char[] shortInfo = program.getShortInfo().toCharArray();
    mDescriptionIcon = new TextAreaIcon(shortInfo, italic, WIDTH_RIGHT - 5);
    mProgramTimeAsString = program.getTimeString();

    mHeight = mTitleIcon.getIconHeight() + 10 + mDescriptionIcon.getIconHeight();
    setPreferredSize(new Dimension(WIDTH, mHeight));

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
  public void paintComponent(Graphics grp) {
    // Draw the background if this program is on air
    if (mProgram.isOnAir()) {
      int minutesAfterMidnight = IOUtilities.getMinutesAfterMidnight();
      int length = mProgram.getLength();
      int startTime = mProgram.getHours() * 60 + mProgram.getMinutes();
      int elapsedMinutes = minutesAfterMidnight - startTime;
      int progressX = elapsedMinutes * WIDTH / length;

      grp.setColor(COLOR_ON_AIR_DARK);
      grp.fillRect(1, 1, progressX - 1, mHeight - 2);
      grp.setColor(COLOR_ON_AIR_LIGHT);
      grp.fillRect(progressX, 1, WIDTH - progressX - 1, mHeight - 2);
      grp.draw3DRect(0, 0, WIDTH - 1, mHeight - 1, true);
    }

    // If there are plugins that have marked the program -> paint the background
    Iterator pluginIter = mProgram.getMarkedByIterator();
    if (pluginIter.hasNext()) {
      grp.setColor(COLOR_MARKED);
      grp.fill3DRect(0, 0, WIDTH, mHeight, true);
    }

    // Draw all the text
  	grp.setFont(bold);
    grp.setColor(Color.black);
    grp.drawString(mProgramTimeAsString, 1, bold.getSize());
    mTitleIcon.paintIcon(this, grp, WIDTH_LEFT, 0);
    mDescriptionIcon.paintIcon(this, grp, WIDTH_LEFT, mTitleIcon.getIconHeight());

    // paint the icons of the plugins that have marked the program
    if (pluginIter.hasNext()) {
      int x = WIDTH - 1;
      int y = mHeight - 1;
      while (pluginIter.hasNext()) {
        Plugin plugin = (Plugin) pluginIter.next();
        Icon icon = plugin.getMarkIcon();
        if (icon != null) {
          x -= icon.getIconWidth();
          icon.paintIcon(this, grp, x, y - icon.getIconHeight());
        }
      }
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
  public devplugin.Program getProgram() {
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