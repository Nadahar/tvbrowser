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

package printplugin;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.*;

import javax.swing.*;

import util.io.IOUtilities;
import util.ui.MultipleFieldReader;
import util.ui.TextAreaIcon;

import devplugin.*;

public class ProgramIcon implements Icon {


  private static java.util.logging.Logger mLog
    = java.util.logging.Logger.getLogger(ProgramIcon.class.getName());


  private static final Composite NORMAL_COMPOSITE = AlphaComposite.SrcOver;
   private static final Composite PALE_COMPOSITE
     = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);

  private ProgramIconSettings mSettings;

  private int mHeight = 0;
  private int mWidth;

  private int mPreferredHeight = 0;
  /** The start time as String. */  
  private String mProgramTimeAsString;
  /** The icon used to render the title. */  
  private TextAreaIcon mTitleIcon;
  /** The icon used to render the description. */  
  private TextAreaIcon mDescriptionIcon;
  /** The icons to show on the left side under the start time. */
  private Icon[] mIconArr;
  /** The program. */  
  private Program mProgram;
  
  private static final ProgramIconSettings DEFAULT_PROGRAM_ICON_SETTINGS = PrinterProgramIconSettings.create();


 
  public ProgramIcon(Program prog) {
    this(prog, null, 100);
  }
  
  public ProgramIcon(Program prog, ProgramIconSettings settings, int width) {
    
    if (settings == null) {
      mSettings = DEFAULT_PROGRAM_ICON_SETTINGS;
    }
    else {
      mSettings = settings;
    }
    mWidth = width;
    
    mTitleIcon = new TextAreaIcon(null, mSettings.getTitleFont(), width- mSettings.getTimeFieldWidth() - 5);
    mDescriptionIcon = new TextAreaIcon(null,mSettings.getTextFont(), width - mSettings.getTimeFieldWidth() - 5);
    mDescriptionIcon.setMaximumLineCount(3);
    
    setProgram(prog, -1);
  }
  
  public void setMaximumHeight(int height) {
    setProgram(mProgram, height);
    
    //System.out.println(height+" <--> "+mHeight);
    //if (mHeight > height) {
    //  System.out.println("ALARM!");
    //}
  }
  
  private Icon[] getPluginIcons(Program program) {
    ArrayList list = new ArrayList();
    
    String[] iconPluginArr = mSettings.getProgramTableIconPlugins();
    for (int i = 0; i < iconPluginArr.length; i++) {
      // Add the icons of this plugin
      PluginManager mng = Plugin.getPluginManager();
      PluginAccess plugin = mng.getActivatedPluginForId(iconPluginArr[i]);
      if (plugin != null) {
        Icon[] iconArr = plugin.getProgramTableIcons(program);
        if (iconArr != null) {
          for (int j = 0; j < iconArr.length; j++) {
            list.add(iconArr[j]);
          }
        }
      }
    }
    
    Icon[] asArr = new Icon[list.size()];
    list.toArray(asArr);
    return asArr;
  }
  
  private void setProgram(devplugin.Program program, int maxHeight) {
    Program oldProgram = mProgram;
    mProgram = program;
    
    boolean programChanged = (oldProgram != program);
    if (programChanged) {
      // Get the start time
      mProgramTimeAsString = program.getTimeString();

      // Get the icons from the plugins
      mIconArr = getPluginIcons(program);

      // Set the new title
      mTitleIcon.setText(program.getTitle());
    }
    
    // Calculate the maximum description lines
    int titleHeight = mTitleIcon.getIconHeight();
    int maxDescLines = 0; //3;
    if (maxHeight != -1) {
      maxDescLines = (maxHeight - titleHeight /*- 10*/) / mSettings.getTextFont().getSize();
    }
    
    if (programChanged || (maxDescLines != mDescriptionIcon.getMaximumLineCount())) {
      // (Re)set the description text
      mDescriptionIcon.setMaximumLineCount(maxDescLines);
      ProgramFieldType[] infoFieldArr = mSettings.getProgramInfoFields();
      Reader infoReader = new MultipleFieldReader(program, infoFieldArr);
      try {
        mDescriptionIcon.setText(infoReader);
      }
      catch (IOException exc) {
        mLog.log(Level.WARNING, "Reading program info failed for " + program, exc);
      }

      // Calculate the height
      mHeight = mTitleIcon.getIconHeight() + /*10 +*/ mDescriptionIcon.getIconHeight();
      //setPreferredSize(new Dimension(WIDTH, mHeight));

      // Calculate the preferred height
      mPreferredHeight = titleHeight + (3 * mSettings.getTextFont().getSize()) + 10;
      if (mHeight < mPreferredHeight) {
        mPreferredHeight = mHeight;
      }
    }
/*
      if (isShowing()) {
        oldProgram.removeChangeListener(this);
        mProgram.addChangeListener(this);
        revalidate();
        repaint();
      }
      */
  }
  
  
	public int getIconHeight() {
    return mHeight;
	}

	
	public int getIconWidth() {
		return mWidth;
	}

	
	public void paintIcon(Component component, Graphics g, int posX, int posY) {
    
    g.translate(posX, posY);
    
    int width = getIconWidth();
    int height = mHeight;
    Graphics2D grp = (Graphics2D) g;
    
   // grp.draw3DRect(0, 0, width - 1, height - 1, true);

    // Draw the background if this program is on air
    if (mSettings.getPaintProgramOnAir() && mProgram.isOnAir()) {
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

      grp.setColor(mSettings.getColorOnAir_dark());
          grp.fillRect(1, 1, width - 2, progressY - 1);
          grp.setColor(mSettings.getColorOnAir_light());
          grp.fillRect(1, progressY, width - 2, height - progressY - 1);
          grp.draw3DRect(0, 0, width - 1, height - 1, true);
        }

        // If there are plugins that have marked the program -> paint the background
        PluginAccess[] markedByPluginArr = mProgram.getMarkedByPlugins();
        if (mSettings.getPaintPluginMarks() && markedByPluginArr.length != 0) {
          grp.setColor(mSettings.getColorMarked());
          grp.fill3DRect(0, 0, width, height, true);
        }

        // Draw all the text
        if (mSettings.getPaintExpiredProgramsPale() && mProgram.isExpired()) {
          grp.setColor(Color.gray);
        } else {
          grp.setColor(Color.black);
        }
        grp.setFont(mSettings.getTimeFont());
        
        grp.drawString(mProgramTimeAsString, 1, mSettings.getTimeFont().getSize());
        mTitleIcon.paintIcon(component, grp, mSettings.getTimeFieldWidth(), 0);
        mDescriptionIcon.paintIcon(component, grp, mSettings.getTimeFieldWidth(), mTitleIcon.getIconHeight());

        // Paint the icons pale if the program is expired
        if (mSettings.getPaintExpiredProgramsPale() && mProgram.isExpired()) {
          grp.setComposite(PALE_COMPOSITE);
        }

        // paint the icons of the plugins that have marked the program
        int x = width - 1;
        int y = mTitleIcon.getIconHeight() + mDescriptionIcon.getIconHeight() + 18;
        y = Math.min(y, height - 1);
        
        if (mSettings.getPaintPluginMarks()) {
          for (int i = 0; i < markedByPluginArr.length; i++) {
            Icon icon = markedByPluginArr[i].getMarkIcon();
            if (icon != null) {
              x -= icon.getIconWidth();
              icon.paintIcon(component, grp, x, y - icon.getIconHeight());
            }
          }
        }        
    
        // Paint the icons on the left side
        if (mIconArr != null) {
          x = 2;
          y = mSettings.getTimeFont().getSize() + 3;
          for (int i = 0; i < mIconArr.length; i++) {
            int iconHeight = mIconArr[i].getIconHeight();
            if ((y + iconHeight) < mHeight) {
              mIconArr[i].paintIcon(component, grp, x, y);
              y += iconHeight + 2;
            }
          }
        }

        // Reset the old composite
        if (mSettings.getPaintExpiredProgramsPale() && mProgram.isExpired()) {
          grp.setComposite(NORMAL_COMPOSITE);
        }
        
    g.translate(-posX, -posY);
    
	}
  
  
}