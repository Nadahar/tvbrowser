/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package printplugin.printer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;

import printplugin.PrintPlugin;
import printplugin.settings.PrinterProgramIconSettings;
import printplugin.settings.ProgramIconSettings;
import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.MultipleFieldReader;
import util.ui.TextAreaIcon;
import util.ui.UiUtilities;
import devplugin.Channel;
import devplugin.Marker;
import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.PluginManager;
import devplugin.Program;
import devplugin.ProgramFieldType;


public class ProgramIcon implements Icon {


  private static final Logger mLog
    = Logger.getLogger(ProgramIcon.class.getName());


  private static final Composite NORMAL_COMPOSITE = AlphaComposite.SrcOver;
   private static final Composite PALE_COMPOSITE
     = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F);

  private ProgramIconSettings mSettings;

  private int mHeight = 0;
  private int mWidth;

  /** The start time as String. */
  private String mProgramTimeAsString;
  /** The icon used to render the title. */
  private TextAreaIcon mTitleIcon;

  private TextAreaIcon mEndTimeIcon;

  /** The icon used to render the description. */
  private TextAreaIcon mDescriptionIcon;
  /** The icons to show on the left side under the start time. */
  private Icon[] mIconArr;
  /** The program. */
  private Program mProgram;

  private ChannelIcon mChannelIcon;

  private static final ProgramIconSettings DEFAULT_PROGRAM_ICON_SETTINGS = PrinterProgramIconSettings.create();



  public ProgramIcon(Program prog) {
    this(prog, null, 100, false);
  }

  public ProgramIcon(Program prog, ProgramIconSettings settings, int width, boolean showChannelName, boolean showEndTime) {

    if (settings == null) {
      mSettings = DEFAULT_PROGRAM_ICON_SETTINGS;
    }
    else {
      mSettings = settings;
    }
    mWidth = width;

    int titleWidth = width- mSettings.getTimeFieldWidth() - 5;
    if (showChannelName) {
      mChannelIcon = new ChannelIcon(prog.getChannel(), mSettings.getTitleFont());
      titleWidth-=mChannelIcon.getIconWidth();
    }



    mTitleIcon = new TextAreaIcon(null, mSettings.getTitleFont(), titleWidth);
    int timefieldWidth = mSettings.getTimeFieldWidth();
    mDescriptionIcon = new TextAreaIcon(null,mSettings.getTextFont(), width - timefieldWidth - 5 + timefieldWidth/2);
    mDescriptionIcon.setMaximumLineCount(3);

    if (showEndTime) {
      mEndTimeIcon = new TextAreaIcon(createTimeString(prog.getMinutes()+prog.getHours()*60+prog.getLength()), mSettings.getTextFont().deriveFont(Font.ITALIC), mDescriptionIcon.getIconWidth());
    }


    setProgram(prog, -1);
  }

  private String createTimeString(int minutes) {
    int time = minutes%(60*24);
    int h = time/60;
    int m = time%60;
    String hString = Integer.toString(h);
    String mString = (m<10?"0":"")+m;
    return Localizer.getLocalizerFor(ProgramIcon.class).msg("timeString","",hString,mString);
  }

  public ProgramIcon(Program prog, ProgramIconSettings settings, int width, boolean showChannelName) {
    this(prog, settings, width, showChannelName, false);
  }

  public void setMaximumHeight(int height) {
    setProgram(mProgram, height);
  }

  private Icon[] getPluginIcons(Program program) {
    ArrayList<Icon> list = new ArrayList<Icon>();

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

  private void setProgram(Program program, int maxHeight) {
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
    
    int additionalHeight = Plugin.getPluginManager().getTvBrowserSettings().isUsingExtraSpaceForMarkIcons() && program.getMarkerArr().length > 0 ? 16 : 0;

    // Calculate the maximum description lines
    int titleHeight = mTitleIcon.getIconHeight();
    int maxDescLines = 0; //3;
    if (maxHeight != -1) {
      maxDescLines = (maxHeight - titleHeight - additionalHeight) / mSettings.getTextFont().getSize();
    }

    if (programChanged || (maxDescLines != mDescriptionIcon.getMaximumLineCount())) {
      // (Re)set the description text
      mDescriptionIcon.setMaximumLineCount(maxDescLines);
      if (maxDescLines > 0) {
        ProgramFieldType[] infoFieldArr = mSettings.getProgramInfoFields();
        Reader infoReader = new MultipleFieldReader(program, infoFieldArr);
        try {
          mDescriptionIcon.setText(infoReader);
        }
        catch (IOException exc) {
          mLog.log(Level.WARNING, "Reading program info failed for " + program, exc);
        }
      }
      // Calculate the height
      mHeight = mTitleIcon.getIconHeight() +  mDescriptionIcon.getIconHeight() + additionalHeight;
      
      if (mEndTimeIcon!=null) {
        mHeight+=mEndTimeIcon.getIconHeight();
      }

    }
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

      grp.setColor(Plugin.getPluginManager().getTvBrowserSettings().getProgramPanelOnAirDarkColor());
          grp.fillRect(1, 1, width - 2, progressY - 1);
          grp.setColor(Plugin.getPluginManager().getTvBrowserSettings().getProgramPanelOnAirLightColor());
          grp.fillRect(1, progressY, width - 2, height - progressY - 1);
          grp.draw3DRect(0, 0, width - 1, height - 1, true);
        }

        // If there are plugins that have marked the program -> paint the background
        Marker[] markedByPluginArr = getMarkedByPlugins(mProgram);
        if (mSettings.getPaintPluginMarks() && markedByPluginArr.length != 0) {
          Color c = Plugin.getPluginManager().getTvBrowserSettings().getColorForMarkingPriority(mProgram.getMarkPriority());
          
          if(c != null && mProgram.getMarkPriority() > Program.NO_MARK_PRIORITY) {
            grp.setColor(c);
            
            if(Plugin.getPluginManager().getTvBrowserSettings().isMarkingBorderPainted()) {
              grp.fill3DRect(0, 0, width, height+2, true);
            }
            else {
              grp.fillRect(0,0,width,height+2);
            }
          }
        }


        // Draw all the text
        if (mSettings.getPaintExpiredProgramsPale() && mProgram.isExpired()) {
          grp.setColor(Color.gray);
        } else {
          grp.setColor(Color.black);
        }
        grp.setFont(mSettings.getTimeFont());

        int timeStringY = mSettings.getTimeFont().getSize();
        grp.drawString(mProgramTimeAsString, 1, timeStringY);

//    if (mEndTimeIcon != null) {
//        mEndTimeIcon.paintIcon(component, grp, mSettings.getTimeFieldWidth()+mTitleIcon.getIconWidth()-mEndTimeIcon.getIconWidth(), 0);
//    }

        mTitleIcon.paintIcon(component, grp, mSettings.getTimeFieldWidth(), 0);
        mDescriptionIcon.paintIcon(component, grp, mSettings.getTimeFieldWidth()/2, mTitleIcon.getIconHeight());

        if (mEndTimeIcon != null) {
          mEndTimeIcon.paintIcon(component, grp, mSettings.getTimeFieldWidth()/2, mTitleIcon.getIconHeight()+mDescriptionIcon.getIconHeight());
        }

        if (mChannelIcon != null) {
          mChannelIcon.paintIcon(component, grp, getIconWidth()-mChannelIcon.getIconWidth(), 0);
        }


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
            Icon[] icons = markedByPluginArr[i].getMarkIcons(mProgram);
            if (icons != null) {
              for(Icon icon : icons) {
                x -= icon.getIconWidth();
                icon.paintIcon(component, grp, x, y - icon.getIconHeight());
              }
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


  private Marker[] getMarkedByPlugins(Program prog) {
    Marker[] access = prog.getMarkerArr();
    ArrayList<Marker> list = new ArrayList<Marker>();
    for (int i=0; i<access.length; i++) {
      if (!access[i].getId().equals(PrintPlugin.getInstance().getId())) {
        list.add(access[i]);
      }
    }
    return list.toArray(new Marker[list.size()]);
  }


  private static class ChannelIcon implements Icon {

    private Channel mChannel;
    private Font mFont;
    private int mWidth;
    private int mHeight;

    public ChannelIcon(Channel channel, Font font) {
      mChannel = channel;
      mFont = font;
      mWidth = UiUtilities.getStringWidth(mFont, mChannel.getName());
      mHeight = mFont.getSize();
    }

    public int getIconHeight() {
      return mHeight;
    }

    public int getIconWidth() {
      return mWidth;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.drawString(mChannel.getName(), x, y+mFont.getSize());
    }
  }


}