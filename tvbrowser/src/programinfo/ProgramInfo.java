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

package programinfo;

import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.Icon;

import util.ui.UiUtilities;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramInfoHelper;
import devplugin.Version;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ProgramInfo extends devplugin.Plugin {

  private static final util.ui.Localizer mLocalizer =
    util.ui.Localizer.getLocalizerFor(ProgramInfo.class);
  
  private static java.util.logging.Logger mLog
      = java.util.logging.Logger.getLogger(ProgramInfo.class.getName());
  
  
  private java.awt.Point location = null;
  private java.awt.Dimension size = null;

  private Properties mSettings;

  private static devplugin.Plugin mInstance;
  
  public static final String DEFAULT_STYLE_SHEET = "#title {\n" +
    "  font-size:15px;\n" +
    "  font-family:Dialog;\n" +
    "  text-align:center;\n" +
    "  font-weight:bold;\n" +
    "}\n\n" +
    "#time {\n" +
      "  font-size:9px;\n" +
      "  font-family:Dialog;\n" +
      "  text-align:center;\n" +
      "}\n\n" +
    "#maininfo {\n" +
    "  font-size:9px;\n" +
    "  font-family:Dialog;\n" +
    "  text-align:right;\n" +
    "  font-weight:bold;\n" +
    "}\n\n" +  
    "#info {\n" +
    "  font-size:9px;\n" +
    "  font-family:Dialog;\n" +
    "}\n\n" +
    "#text {\n" +
    "  font-size:11px;\n" +
    "  font-family:Dialog;\n" +
    "}\n\n" +
    "#small {\n" +
    "  font-size:9px;\n" +
    "  font-family:Dialog;\n" +
    "}\n";

  public ProgramInfo() {
    mInstance = this;
  }
  
  
  public String getContextMenuItemText() {
    return mLocalizer.msg("contextMenuText", "Program information");
  }

  public static devplugin.Plugin getInstance() {
    if (mInstance == null) {
      // this should never happen
      mLog.severe("mInstance is null");
      mInstance = new ProgramInfo();
    }
    return mInstance;
  }

  public PluginInfo getInfo() {
    String name = mLocalizer.msg("pluginName", "Program information");
    String desc =
      mLocalizer.msg("description", "Show information about a program");
    String author = "Martin Oberhauser";
    return new PluginInfo(name, desc, author, new Version(1, 7));
  }


  public String getButtonText() {
    return null;
  }


  public devplugin.SettingsTab getSettingsTab() {
      return new ProgramInfoSettingsTab(mSettings);
  }

  public Properties storeSettings() {
    return mSettings;
  }
  
  
  
  public void loadSettings(Properties settings) {
    if (settings == null ) {
      settings = new Properties();
    }    
    mSettings = settings;    
  }
  

  public void execute(Program program) {
    
    String styleSheet = mSettings.getProperty("stylesheet_v1",DEFAULT_STYLE_SHEET);
    
    ProgramInfoDialog dlg = new ProgramInfoDialog(getParentFrame(), styleSheet, program, ProgramInfoHelper.mInfoBitArr,
            ProgramInfoHelper.mInfoIconArr, ProgramInfoHelper.mInfoMsgArr);
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
    for (int i = 0; i < ProgramInfoHelper.mInfoBitArr.length; i++) {
      if (bitSet(info, ProgramInfoHelper.mInfoBitArr[i]) && (ProgramInfoHelper.mInfoIconArr[i] != null)) {
        // Create the list if it doesn't already exist
        if (iconList == null) {
          iconList = new ArrayList();
        }
        
        // Add the icon to the list
        iconList.add(ProgramInfoHelper.mInfoIconArr[i]);
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