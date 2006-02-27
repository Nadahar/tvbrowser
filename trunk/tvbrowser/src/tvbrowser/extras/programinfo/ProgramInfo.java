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

package tvbrowser.extras.programinfo;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Properties;
import java.io.*;

import javax.swing.*;

import com.l2fprod.common.swing.plaf.LookAndFeelAddons;

import util.ui.UiUtilities;
import util.exc.ErrorHandler;
import devplugin.*;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.ConfigurationHandler;
import tvbrowser.ui.mainframe.MainFrame;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class ProgramInfo implements ContextMenuIf {

  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ProgramInfo.class);

  private static String DATAFILE_PREFIX = "programinfo.ProgramInfo";

  // private static java.util.logging.Logger mLog
  // = java.util.logging.Logger.getLogger(ProgramInfo.class.getName());

  private Point mLocation = null;

  private Dimension mSize = null;
  
  private Dimension mLeftSplit = null;

  private Properties mSettings;

  private ConfigurationHandler mConfigurationHandler;

  private static ProgramInfo mInstance;

  private ProgramInfo() {
    mInstance = this;
    mConfigurationHandler = new ConfigurationHandler(DATAFILE_PREFIX);
    loadSettings();
    LookAndFeelAddons.setTrackingLookAndFeelChanges(true);
  }

  public ActionMenu getContextMenuActions(final Program program) {
    ContextMenuAction action = new ContextMenuAction();
    action.setText(mLocalizer.msg("contextMenuText", "Program information"));
    action.setSmallIcon(IconLoader.getInstance().getIconFromTheme("actions",
        "edit-find", 16));
    action.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        setLook();
        showProgramInformation(program, true);
      }
    });

    return new ActionMenu(action);
  }

  public static synchronized ProgramInfo getInstance() {
    if (mInstance == null)
      new ProgramInfo();
    return mInstance;
  }

  public Properties getSettings() {
    return mSettings;
  }

  private void loadSettings() {

    try {
      mSettings = mConfigurationHandler.loadSettings();
    } catch (IOException e) {
      ErrorHandler.handle("Could not load programinfo settings.", e);
    }

    String width = mSettings.getProperty("DialogSize.Width");
    String height = mSettings.getProperty("DialogSize.Height");

    if ((width != null) && (height != null)) {
      int w = parseNumber(width);
      int h = parseNumber(height);
      mSize = new Dimension(w, h);
    }

    String x = mSettings.getProperty("DialogLocation.X");
    String y = mSettings.getProperty("DialogLocation.Y");

    if ((x != null) && (y != null)) {
      int xv = parseNumber(x);
      int yv = parseNumber(y);
      mLocation = new Point(xv, yv);
    }
    
    String splitWidht = mSettings.getProperty("LeftSplit.Width");
    String splitHeigt = mSettings.getProperty("LeftSplit.Height");

    if ((x != null) && (y != null)) {
      int sw = parseNumber(splitWidht);
      int sh = parseNumber(splitHeigt);
      mLeftSplit = new Dimension(sw, sh);
    }

  }

  public void store() {
    if (mLocation != null) {
      mSettings.setProperty("DialogLocation.X", Integer.toString(mLocation.x));
      mSettings.setProperty("DialogLocation.Y", Integer.toString(mLocation.y));
    }

    if (mSize != null) {
      mSettings.setProperty("DialogSize.Width", Integer.toString(mSize.width));
      mSettings
          .setProperty("DialogSize.Height", Integer.toString(mSize.height));
    }
    
    if(mLeftSplit != null) {
      mSettings.setProperty("LeftSplit.Width", Integer.toString(mLeftSplit.width));
      mSettings.setProperty("LeftSplit.Height", Integer.toString(mLeftSplit.height));
    }

    try {
      mConfigurationHandler.storeSettings(mSettings);
    } catch (IOException e) {
      ErrorHandler.handle("Could not store settings for programinfo.", e);
    }

  }

  /*
   * public Properties storeSettings() { if (mLocation != null) {
   * mSettings.setProperty("DialogLocation.X", Integer.toString(mLocation.x));
   * mSettings.setProperty("DialogLocation.Y", Integer.toString(mLocation.y)); }
   * 
   * if (mSize != null) { mSettings.setProperty("DialogSize.Width",
   * Integer.toString(mSize.width)); mSettings.setProperty("DialogSize.Height",
   * Integer.toString(mSize.height)); }
   * 
   * return mSettings; }
   */

  /*
   * public void loadSettings(Properties settings) { if (settings == null ) {
   * settings = new Properties(); }
   * 
   * String width = settings.getProperty("DialogSize.Width"); String height =
   * settings.getProperty("DialogSize.Height");
   * 
   * if ((width != null) && (height != null)) { int w = parseNumber(width); int
   * h = parseNumber(height); mSize = new Dimension(w, h); }
   * 
   * String x = settings.getProperty("DialogLocation.X"); String y =
   * settings.getProperty("DialogLocation.Y");
   * 
   * if ((x != null) && (y != null)) { int xv = parseNumber(x); int yv =
   * parseNumber(y); mLocation = new Point(xv, yv); }
   * 
   * mSettings = settings; }
   */

  /**
   * Parses a Number from a String.
   * 
   * @param str
   *          Number in String to Parse
   * @return Number if successfull. Default is 0
   */
  public int parseNumber(String str) {

    try {
      return Integer.parseInt(str);
    } catch (Exception e) {
      // ignore
    }

    return 0;
  }

  protected void showProgramInformation(Program program, boolean showSettings) {
    Window parent = UiUtilities.getBestDialogParent(MainFrame.getInstance());
    ProgramInfoDialog dlg;
    
    if (parent instanceof Dialog) {
      dlg = new ProgramInfoDialog((Dialog) parent, program, mLeftSplit, showSettings);
    } else {
      dlg = new ProgramInfoDialog((Frame) parent, program, mLeftSplit, showSettings);
    }

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
    } else
      UiUtilities.centerAndShow(dlg);
  }

  /**
   * Gets the description text for the program table icons provided by this
   * Plugin.
   * <p>
   * If the plugin does not provide such icons <code>null</code> will be
   * returned.
   * 
   * @return The description text for the program table icons.
   * @see #getProgramTableIcons(Program)
   */
/*  public String getProgramTableIconText() {
    return mLocalizer.msg("programTableIconText", "Movie format");
  }

  /**
   * Gets the icons this Plugin provides for the given program. These icons will
   * be shown in the program table.
   * <p>
   * If the plugin does not provide such icons <code>null</code> will be
   * returned.
   * 
   * @param program
   *          The programs to get the icons for.
   * @return The icons for the given program or <code>null</code>.
   */
 /* public Icon[] getProgramTableIcons(Program program) {
    int info = program.getInfo();
    if ((info == -1) || (info == 0)) {
      return null;
    }

    // Put the icons for this program into a list
    ArrayList iconList = null;
    for (int i = 0; i < ProgramInfoHelper.mInfoBitArr.length; i++) {
      if (bitSet(info, ProgramInfoHelper.mInfoBitArr[i])
          && (ProgramInfoHelper.mInfoIconArr[i] != null)) {
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
 /* static boolean bitSet(int num, int pattern) {
    return (num & pattern) == pattern;
  }*/
  
  protected void setSettings(JDialog dialog, Dimension d) {
    mSize = dialog.getSize();
    mLocation = dialog.getLocation();
    mLeftSplit = d;
  }
  
  protected boolean getExpanded(String key) {
    return (mSettings.getProperty(key,"true").compareTo("true") == 0);
  }
  
  protected void setExpanded(String key, boolean value) {
    mSettings.setProperty(key,String.valueOf(value));
  }
  
  protected String getProperty(String key, String def) {
    return mSettings.getProperty(key,def);
  }
  
  protected String getUserfont(String value, String def) {
    String tvalue = mSettings.getProperty(value);
    
    boolean userfont = mSettings.getProperty("userfont","false").equals("true");    
    return tvalue != null && tvalue.trim().length() > 0 && userfont ? tvalue : def;
  }
  
  protected void setLook() {
    int n = Integer.parseInt(mSettings.getProperty("look","4"));
    
    String[] lnf = {"com.l2fprod.common.swing.plaf.aqua.AquaLookAndFeelAddons",
                    "com.l2fprod.common.swing.plaf.basic.BasicLookAndFeelAddons",
        "com.l2fprod.common.swing.plaf.metal.MetalLookAndFeelAddons",
        "com.l2fprod.common.swing.plaf.motif.MotifLookAndFeelAddons",
        "com.l2fprod.common.swing.plaf.windows.WindowsLookAndFeelAddons",
        "com.l2fprod.common.swing.plaf.windows.WindowsClassicLookAndFeelAddons"};
    
    try{      
      LookAndFeelAddons.setAddon(lnf[n]);
    }catch(Exception e){}
  }

  public String getId() {
    return DATAFILE_PREFIX;
  }
}