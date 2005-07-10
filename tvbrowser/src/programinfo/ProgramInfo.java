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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.*;

import util.ui.UiUtilities;
import devplugin.*;

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
  
  
  private Point mLocation = null;
  private Dimension mSize = null;
  
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

  public ActionMenu getContextMenuActions(final Program program) {
    ContextMenuAction action = new ContextMenuAction();
    action.setText(mLocalizer.msg("contextMenuText", "Program information"));
    action.setSmallIcon(createImageIcon("programinfo/Information16.gif"));
    action.setActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent event) {
        showProgramInformation(program);
      }
    });

    return new ActionMenu(action);
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
    return new PluginInfo(name, desc, author, new Version(1, 10));
  }





  public devplugin.SettingsTab getSettingsTab() {
      return new ProgramInfoSettingsTab(mSettings);
  }

  public Properties storeSettings() {
      
    if (mLocation != null) {
        mSettings.setProperty("DialogLocation.X", Integer.toString(mLocation.x));
        mSettings.setProperty("DialogLocation.Y", Integer.toString(mLocation.y));
    }
    
    if (mSize != null) {
        mSettings.setProperty("DialogSize.Width", Integer.toString(mSize.width));
        mSettings.setProperty("DialogSize.Height", Integer.toString(mSize.height));
    }

    return mSettings;
  }
  
  
  
  public void loadSettings(Properties settings) {
    if (settings == null ) {
      settings = new Properties();
    }
    
    String width = settings.getProperty("DialogSize.Width");
    String height = settings.getProperty("DialogSize.Height");
    
    if ((width != null) && (height != null)) {
        int w = parseNumber(width);
        int h = parseNumber(height);
        mSize = new Dimension(w, h);
    }

    String x = settings.getProperty("DialogLocation.X");
    String y = settings.getProperty("DialogLocation.Y");
    
    if ((x != null) && (y != null)) {
        int xv = parseNumber(x);
        int yv = parseNumber(y);
        mLocation = new Point(xv, yv);
    }
    
    mSettings = settings;    
  }
  
  /**
   * Parses a Number from a String.
   * @param str Number in String to Parse
   * @return Number if successfull. Default is 0
   */
  public int parseNumber(String str) {
      
      try {
          int i = Integer.parseInt(str);
          return i;
      } catch (Exception e) {
          
      }
      
      return 0;
  }
  
  private void showProgramInformation(Program program) {
    
    String styleSheet = mSettings.getProperty("stylesheet_v1",DEFAULT_STYLE_SHEET);
    
    ProgramInfoDialog dlg = new ProgramInfoDialog(getParentFrame(), styleSheet, program);
    dlg.pack();
    dlg.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentMoved(ComponentEvent e) {
        e.getComponent().getLocation(mLocation);
      }

      public void componentResized(ComponentEvent e) {
        mSize = e.getComponent().getSize(mSize);
      }
    });

    if (mSize != null) {
      dlg.setSize(mSize);
    }
    if (mLocation != null) {
      dlg.setLocation(mLocation);
      dlg.setVisible(true);
    } else {
      UiUtilities.centerAndShow(dlg);
      mSize = dlg.getSize();
      mLocation = dlg.getLocation();
    }
  }


  public String getMarkIconName() {
    return "programinfo/Information16.gif";
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