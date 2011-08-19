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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.ConfigurationHandler;
import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.programtable.ProgramTable;
import util.exc.ErrorHandler;
import util.settings.PluginPictureSettings;
import util.ui.Localizer;
import util.ui.UIThreadRunner;
import util.ui.UiUtilities;

import com.l2fprod.common.swing.plaf.LookAndFeelAddons;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.Program;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ProgramInfo {

  static Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ProgramInfo.class);

  private static final String DATAFILE_PREFIX = "programinfo.ProgramInfo";

  private Dimension mLeftSplit = null;

  private ProgramInfoSettings mSettings;

  private ConfigurationHandler mConfigurationHandler;

  private static ProgramInfo mInstance;

  private Object[] mOrder;

  private static boolean mIsShowing = false;

  private Thread mInitThread;

  private ArrayList<Program> mHistory = new ArrayList<Program>();
  private int mHistoryIndex = 0;

  private ProgramInfo() {
    mInstance = this;
    mConfigurationHandler = new ConfigurationHandler(DATAFILE_PREFIX);
    loadSettings();
    LookAndFeelAddons.setTrackingLookAndFeelChanges(true);
  }

  /**
   * Initializes the ProgramInfoDialog.
   */
  public void handleTvBrowserStartFinished() {
    mInitThread = new Thread("Program Info init thread") {
      public void run() {
        try {
          UIThreadRunner.invokeAndWait(new Runnable() {

            @Override
            public void run() {
              ProgramInfoDialog.getInstance(Plugin.getPluginManager().getExampleProgram(), mLeftSplit, true);
            }
          });
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    };
    mInitThread.setPriority(Thread.NORM_PRIORITY);
    mInitThread.start();
  }

  protected ActionMenu getContextMenuActions(final Program program) {
    ContextMenuAction action = new ContextMenuAction();
    action.setText(mLocalizer.msg("contextMenuText", "Program information"));
    action.setSmallIcon(IconLoader.getInstance().getIconFromTheme("actions",
        "edit-find", 16));
    action.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent event) {
        try {
          UIThreadRunner.invokeAndWait(new Runnable() {

            @Override
            public void run() {
              setLook();
              showProgramInformation(program, true);
            }
          });
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
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
  public ProgramInfoSettings getSettings() {
    return mSettings;
  }

  private void loadSettings() {

    try {
      mSettings = new ProgramInfoSettings(mConfigurationHandler.loadSettings());
    } catch (IOException e) {
      ErrorHandler.handle("Could not load programinfo settings.", e);
    }

    final int splitWidht = mSettings.getWidth();
    final int splitHeigt = mSettings.getHeight();

    if ((splitWidht > 0) && (splitHeigt > 0)) {
      mLeftSplit = new Dimension(splitWidht, splitHeigt);
    }

  }

  /**
   * Save settings.
   */
  public void store() {
    if (mLeftSplit != null) {
      mSettings.setWidth(mLeftSplit.width);
      mSettings.setHeight(mLeftSplit.height);
    }

    try {
      mSettings.storeSettings(mConfigurationHandler);
    } catch (IOException e) {
      ErrorHandler.handle("Could not store settings for programinfo.", e);
    }

  }

  /**
   * Parses a Number from a String.
   *
   * @param str
   *          Number in String to Parse
   * @return Number if successful. Default is 0
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
    if (program.equals(Plugin.getPluginManager().getExampleProgram()) && showSettings) {
      return;
    }
    // remember program for history
    if (mHistory.isEmpty() || !mHistory.get(mHistory.size() - 1).equals(program)) {
      mHistory.add(program);
      mHistoryIndex = mHistory.size() - 1;
    }

    synchronized (mInitThread) {
      if (mInitThread != null && mInitThread.isAlive()) {
        try {
          mInitThread.join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    
    if ((mIsShowing || ProgramInfoDialog.isShowing()) && showSettings) {
      if (!ProgramInfoDialog.closeDialog()) {
        return;
      }
    }
    mIsShowing = true;

    synchronized (this) {
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
  }

  protected void setSettings(Dimension d) {
    if (mSettings.getShowFunctions()) {
      mLeftSplit = d;
    }
  }

  protected Object[] getOrder() {
    if(mOrder == null) {
      mOrder = mSettings.getFieldOrder();
    }

    return mOrder;
  }

  protected void setOrder() {
    mOrder = mSettings.getFieldOrder();
  }

  protected void setLook() {
    try {
      if (mIsShowing) {
        return;
      }
      String lf = mSettings.getLook();

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

  @Override
  public String toString() {
    return getName();
  }

  static String getName() {
    return mLocalizer.msg("pluginName","Program details");
  }

  protected PluginPictureSettings getPictureSettings() {
    return new PluginPictureSettings(mSettings.getPictureSettings());
  }

  /**
   * return whether the program info is currently shown or is to be shown immediately
   * @return true if the program info is currently shown or is to be shown immediately
   */
  public static boolean isShowing() {
    return ProgramInfoDialog.isShowing() || mIsShowing;
  }

  public void showProgramInformation(final Program program) {
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

  public void historyBack() {
    history(-1);
  }

  private void history(final int delta) {
    mHistoryIndex += delta;
    if (mHistoryIndex < 0) {
      mHistoryIndex = 0;
    }
    if (mHistoryIndex >= mHistory.size()) {
      mHistoryIndex = mHistory.size() - 1;
    }
    if (mHistoryIndex >= 0) {
      ProgramInfoDialog.getInstance(mHistory.get(mHistoryIndex), mLeftSplit, true);
    }
  }

  public void historyForward() {
    history(+1);
  }

  public boolean canNavigateBack() {
    return mHistoryIndex > 0;
  }

  public boolean canNavigateForward() {
    return mHistoryIndex < mHistory.size() - 1;
  }

  public String navigationBackwardText() {
    if (!canNavigateBack()) {
      return null;
    }
    return mHistory.get(mHistoryIndex - 1).getTitle();
  }

  public String navigationForwardText() {
    if (!canNavigateForward()) {
      return null;
    }
    return mHistory.get(mHistoryIndex + 1).getTitle();
  }

  public static void resetLocalizer() {
    mLocalizer = Localizer.getLocalizerFor(ProgramInfo.class);
  }

}