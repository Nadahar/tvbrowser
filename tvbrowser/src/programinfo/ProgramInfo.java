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

package programinfo;

import java.awt.event.ComponentEvent;
import java.util.ArrayList;

import javax.swing.Icon;

import util.ui.ImageUtilities;
import util.ui.UiUtilities;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.Version;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ProgramInfo extends devplugin.Plugin {

  private static final util.ui.Localizer mLocalizer =
    util.ui.Localizer.getLocalizerFor(ProgramInfo.class);

  private java.awt.Point location = null;
  private java.awt.Dimension size = null;

  private int[]    mInfoBitArr;
  private Icon[]   mInfoIconArr;
  private String[] mInfoMsgArr;


  public ProgramInfo() {
    mInfoBitArr = new int[] {
      Program.INFO_VISION_BLACK_AND_WHITE,
      Program.INFO_VISION_4_TO_3,
      Program.INFO_VISION_16_TO_9,
      Program.INFO_AUDIO_MONO,
      Program.INFO_AUDIO_STEREO,
      Program.INFO_AUDIO_DOLBY_SURROUND,
      Program.INFO_AUDIO_DOLBY_DIGITAL_5_1,
      Program.INFO_AUDIO_TWO_CHANNEL_TONE,
      Program.INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED,
      Program.INFO_LIVE,
      Program.INFO_ORIGINAL_WITH_SUBTITLE,
    };
    
    mInfoIconArr = new Icon[] {
      createIcon("Info_BlackAndWhite.gif"),  // INFO_VISION_BLACK_AND_WHITE
      null,                                  // INFO_VISION_4_TO_3
      createIcon("Info_16to9.gif"),          // INFO_VISION_16_TO_9
      createIcon("Info_Mono.gif"),           // INFO_AUDIO_MONO
      createIcon("Info_Stereo.gif"),         // INFO_AUDIO_STEREO
      createIcon("Info_DolbySurround.gif"),  // INFO_AUDIO_DOLBY_SURROUND
      createIcon("Info_DolbyDigital51.gif"),  // INFO_AUDIO_DOLBY_DIGITAL_5_1
      createIcon("Info_TwoChannelTone.gif"), // INFO_AUDIO_TWO_CHANNEL_TONE
      createIcon("Info_SubtitleForAurallyHandicapped.gif"), // INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED
      null,                                  // INFO_LIVE
      createIcon("Info_OriginalWithSubtitle.gif"), // INFO_ORIGINAL_WITH_SUBTITLE
    };

    mInfoMsgArr = new String[] {
      mLocalizer.msg("blackAndWhite", "Black and white"),     // INFO_VISION_BLACK_AND_WHITE
      mLocalizer.msg("4to3", "4:3"),                          // INFO_VISION_4_TO_3
      mLocalizer.msg("16to9", "16:9"),                        // INFO_VISION_16_TO_9
      mLocalizer.msg("mono", "Mono"),                         // INFO_AUDIO_MONO
      mLocalizer.msg("stereo", "Stereo"),                     // INFO_AUDIO_STEREO
      mLocalizer.msg("dolbySurround", "Dolby surround"),      // INFO_AUDIO_DOLBY_SURROUND
      mLocalizer.msg("dolbyDigital5.1", "Dolby digital 5.1"), // INFO_AUDIO_DOLBY_DIGITAL_5_1
      mLocalizer.msg("twoChannelTone", "Two channel tone"),   // INFO_AUDIO_TWO_CHANNEL_TONE
      mLocalizer.msg("subtitleForAurallyHandicapped", "Subtitle for aurally handicapped"), // INFO_SUBTITLE_FOR_AURALLY_HANDICAPPED
      mLocalizer.msg("live", "Live"),                         // INFO_LIVE
      mLocalizer.msg("originalWithSubtitle", "Original with subtitle"), // INFO_ORIGINAL_WITH_SUBTITLE
    };
  }
  
  
  private Icon createIcon(String fileName) {
    return ImageUtilities.createImageIconFromJar("programinfo/" + fileName, getClass());
  }


  public String getContextMenuItemText() {
    return mLocalizer.msg("contextMenuText", "Program information");
  }


  public PluginInfo getInfo() {
    String name = mLocalizer.msg("pluginName", "Program information");
    String desc =
      mLocalizer.msg("description", "Show information about a program");
    String author = "Martin Oberhauser";
    return new PluginInfo(name, desc, author, new Version(1, 6));
  }


  public String getButtonText() {
    return null;
  }


  public void execute(Program program) {
    ProgramInfoDialog dlg = new ProgramInfoDialog(getParentFrame(), program, mInfoBitArr,
                                                  mInfoIconArr, mInfoMsgArr);
    dlg.pack();
    dlg.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentMoved(ComponentEvent e) {
        e.getComponent().getLocation(location);
      }

      public void componentResized(ComponentEvent e) {
        e.getComponent().getSize(size);
      }
    });

    if (size != null) {
      dlg.setSize(size);
    }
    if (location != null) {
      dlg.setLocation(location);
      dlg.show();
    } else {
      UiUtilities.centerAndShow(dlg);
      size = dlg.getSize();
      location = dlg.getLocation();
    }
  }


  public String getMarkIconName() {
    return "programinfo/Information16.gif";
  }


  public String getButtonIconName() {
    return null;
  }


  /**
   * Gets the description text for the program table icons provided by this
   * Plugin.
   * <p>
   * If the plugin does not provide such icons <code>null</code> will be returned.
   * 
   * @return The description text for the program table icons.
   * @see #getProgramTableIcons(Program)
   */
  public String getProgramTableIconText() {
    return mLocalizer.msg("programTableIconText", "Movie format");
  }


  /**
   * Gets the icons this Plugin provides for the given program. These icons will
   * be shown in the program table.
   * <p>
   * If the plugin does not provide such icons <code>null</code> will be returned.
   * 
   * @param program The programs to get the icons for.
   * @return The icons for the given program or <code>null</code>.
   */
  public Icon[] getProgramTableIcons(Program program) {
    int info = program.getInfo();
    if ((info == -1) || (info == 0)) {
      return null;
    }

    // Put the icons for this program into a list    
    ArrayList iconList = null;
    for (int i = 0; i < mInfoBitArr.length; i++) {
      if (bitSet(info, mInfoBitArr[i]) && (mInfoIconArr[i] != null)) {
        // Create the list if it doesn't already exist
        if (iconList == null) {
          iconList = new ArrayList();
        }
        
        // Add the icon to the list
        iconList.add(mInfoIconArr[i]);
      }
    }

    // Convert the list into an array and return it    
    if (iconList == null) {
      return null;
    } else {
      Icon[] iconArr = new Icon[iconList.size()];
      iconList.toArray(iconArr);
      
      return iconArr;
    }
  }


  /**
   * Returns whether a bit (or combination of bits) is set in the specified
   * number.
   */
  static boolean bitSet(int num, int pattern) {
    return (num & pattern) == pattern;
  }

}