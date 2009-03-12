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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Properties;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.ConfigurationHandler;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.programtable.ProgramTable;
import util.exc.ErrorHandler;
import util.program.CompoundedProgramFieldType;
import util.program.ProgramTextCreator;
import util.settings.PluginPictureSettings;
import util.ui.UiUtilities;

import com.l2fprod.common.swing.plaf.LookAndFeelAddons;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * TV-Browser
 * 
 * @author Martin Oberhauser
 */
public class ProgramInfo {

  protected static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ProgramInfo.class);

  private static final String DATAFILE_PREFIX = "programinfo.ProgramInfo";

  private Dimension mLeftSplit = null;

  private Properties mSettings;

  private ConfigurationHandler mConfigurationHandler;

  private static ProgramInfo mInstance;
  
  private Object[] mOrder;
  
  private boolean mShowFunctions, mShowTextSearchButton;
  
  private static boolean mIsShowing = false;
  
  private Thread mInitThread;

  private ProgramInfo() {
    mInstance = this;
    mConfigurationHandler = new ConfigurationHandler(DATAFILE_PREFIX);
    loadSettings();
    LookAndFeelAddons.setTrackingLookAndFeelChanges(true);
  }
  
  /**
   * Inits the ProgramInfoDialog.
   */
  public void handleTvBrowserStartFinished() {
    mInitThread = new Thread("Program Info init thread") {
      public void run() {
        ProgramInfoDialog.getInstance(Plugin.getPluginManager().getExampleProgram(), mLeftSplit, true);
      }
    };
    mInitThread.setPriority(Thread.MIN_PRIORITY);
    mInitThread.start();
  }

  protected ActionMenu getContextMenuActions(final Program program) {
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

  /**
   * @return An instance of this class
   */
  public static synchronized ProgramInfo getInstance() {
    if (mInstance == null) {
      new ProgramInfo();
    }
    return mInstance;
  }

  /**
   * @return Settings
   */
  public Properties getSettings() {
    return mSettings;
  }

  private void loadSettings() {

    try {
      mSettings = mConfigurationHandler.loadSettings();
    } catch (IOException e) {
      ErrorHandler.handle("Could not load programinfo settings.", e);
    }

    String splitWidht = mSettings.getProperty("LeftSplit.Width");
    String splitHeigt = mSettings.getProperty("LeftSplit.Height");

    if ((splitWidht != null) && (splitHeigt != null)) {
      int sw = parseNumber(splitWidht);
      int sh = parseNumber(splitHeigt);
      mLeftSplit = new Dimension(sw, sh);
    }
    
    mShowFunctions = mSettings.getProperty("showFunctions","true").compareTo("true") == 0;
    mShowTextSearchButton = mSettings.getProperty("showTextSearchButton","true").compareTo("true") == 0;
  }

  /**
   * Save settings.
   */
  public void store() {
    if (mLeftSplit != null) {
      mSettings.setProperty("LeftSplit.Width", Integer
          .toString(mLeftSplit.width));
      mSettings.setProperty("LeftSplit.Height", Integer
          .toString(mLeftSplit.height));
    }

    try {
      mConfigurationHandler.storeSettings(mSettings);
    } catch (IOException e) {
      ErrorHandler.handle("Could not store settings for programinfo.", e);
    }

  }

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

  protected synchronized void showProgramInformation(Program program, boolean showSettings) {
    if(program.equals(Plugin.getPluginManager().getExampleProgram()) && showSettings) {
      return;
    }
    
    if(mInitThread != null && mInitThread.isAlive()) {
      try {
        mInitThread.join();
      }catch(InterruptedException e) {
        e.printStackTrace();
      }
    }
    
    if (mIsShowing || ProgramInfoDialog.isShowing()) {
      if (!ProgramInfoDialog.closeDialog()) {
        return;
      }
    }
    mIsShowing = true;
    Window window = UiUtilities.getLastModalChildOf(MainFrame.getInstance());
    // show busy cursor
    ProgramTable programTable = MainFrame.getInstance().getProgramTableScrollPane().getProgramTable();
    window.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    programTable.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    // open dialog
    ProgramInfoDialog.getInstance(program, mLeftSplit, showSettings).show();
    window.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    programTable.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    mIsShowing = false;
  }

  protected void setSettings(Dimension d) {
    if(mShowFunctions) {
      mLeftSplit = d;
    }
  }

  protected boolean getExpanded(String key) {
    return (mSettings.getProperty(key, "true").compareTo("true") == 0);
  }

  protected void setExpanded(String key, boolean value) {
    mSettings.setProperty(key, String.valueOf(value));
  }

  protected String getProperty(String key, String def) {
    return mSettings.getProperty(key, def);
  }

  protected String getUserfont(String value, String def) {
    String tvalue = mSettings.getProperty(value);
    boolean userfont = mSettings.getProperty("userfont", "false")
        .equals("true");
    return tvalue != null && tvalue.trim().length() > 0 && userfont ? tvalue
        : def;
  }

  protected Object[] getOrder() {
    if(mOrder == null) {
      setOrder();
    }
    
    return mOrder;
  }
  
  protected void setOrder() {
    if(mSettings.getProperty("setupwasdone","false").compareTo("false") == 0) {
      mOrder = ProgramTextCreator.getDefaultOrder();
    } else {
      String[] id = mSettings.getProperty("order", "").trim().split(";");
      mOrder = new Object[id.length];
      for (int i = 0; i < mOrder.length; i++) {
        int parsedId = Integer.parseInt(id[i]);
        
        if(parsedId == ProgramFieldType.UNKOWN_FORMAT) {
          mOrder[i] = ProgramTextCreator.getDurationTypeString();
        }
        else if(parsedId >= 0) {
          mOrder[i] = ProgramFieldType.getTypeForId(parsedId);
        }
        else {
          mOrder[i] = CompoundedProgramFieldType.getCompoundedProgramFieldTypeForId(parsedId);
        }
      }
    }
  }

  protected void setLook() {
    try {
      if (mIsShowing) {
        return;
      }
      String lf = mSettings.getProperty("look", LookAndFeelAddons.getBestMatchAddonClassName());
      
      if (lf.length() > 0) {
        LookAndFeelAddons.setAddon(lf);
      } else {
        LookAndFeelAddons.setAddon(LookAndFeelAddons
            .getBestMatchAddonClassName());
      }
      ProgramInfoDialog.resetFunctionGroup();
    } catch (Exception e) {
      // ignore
    }
  }
  
  protected void setShowFunctions(boolean value) {
    mShowFunctions = value;
    mSettings.setProperty("showFunctions", String.valueOf(value));
  }
  
  protected boolean isShowFunctions() {
    return mShowFunctions;
  }
  
  protected void setShowTextSearchButton(boolean value) {
    mShowTextSearchButton = value;
    mSettings.setProperty("showTextSearchButton", String.valueOf(value));    
  }
  
  protected boolean isShowTextSearchButton() {
    return mShowTextSearchButton;
  }

  @Override
  public String toString() {
    return mLocalizer.msg("pluginName","Program details");
  }
  
  protected String getId() {
    return getProgramInfoPluginId();
  }
  
  protected PluginPictureSettings getPictureSettings() {
    return new PluginPictureSettings(Integer.parseInt(mSettings.getProperty("pictureSettings",String.valueOf(PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE))));
  }
    
  /**
   * return whether the program info is currently shown or is to be shown immediately
   * @return
   */
  public static boolean isShowing() {
    return ProgramInfoDialog.isShowing() || mIsShowing;
  }

  public void showProgramInformation(Program program) {
    showProgramInformation(program, true);
  }

  /**
   * get the plugin id without loading the plugin
   * 
   * @return the plugin id
   * @since 3.0
   */
  public static String getProgramInfoPluginId() {
    return DATAFILE_PREFIX;
  }
}