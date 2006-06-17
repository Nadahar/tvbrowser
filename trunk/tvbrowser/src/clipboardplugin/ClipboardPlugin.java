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
package clipboardplugin;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import util.paramhandler.ParamParser;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * This Plugin is an internal Clipboard.
 * 
 * @author bodo
 */
public class ClipboardPlugin extends Plugin {

  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ClipboardPlugin.class);

  /** Needed for Position */
  private Point mLocationListDialog = null;

  /** Needed for Position */
  private Dimension mDimensionListDialog = null;

  /** Settings for this Plugin */
  private Properties mSettings;

  /** The Default-Parameters */
  public static final String DEFAULT_PARAM = "{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n";

  public ActionMenu getContextMenuActions(final Program program) {
    ImageIcon img = createImageIcon("actions", "edit-paste", 16);

    AbstractAction copyToSystem = new AbstractAction(mLocalizer.msg("copyToSystem", "Copy to System-Clipboard")) {
      public void actionPerformed(ActionEvent evt) {
        Program[] list = { program };
        copyProgramsToSystem(list);
      }
    };
    copyToSystem.putValue(Action.SMALL_ICON, img);

    return new ActionMenu(copyToSystem);
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#getInfo()
   */
  public PluginInfo getInfo() {
    String name = mLocalizer.msg("pluginName", "Clipboard");
    String desc = mLocalizer.msg("description",
        "Copy programs to the Clipboard");
    String author = "Bodo Tasche";
    return new PluginInfo(name, desc, author, new Version(0, 30));
  }

 
  public boolean canReceivePrograms() {
    return true;
  }

  public void receivePrograms(Program[] programArr) {
    copyProgramsToSystem(programArr);
  }

  public void loadSettings(Properties settings) {
    mSettings = settings;
  }

  public Properties storeSettings() {
    return mSettings;
  }

  public SettingsTab getSettingsTab() {
    return new ClipboardSettingsTab(this, mSettings);
  }

  /**
   * Copy Programs to System-Clipboard
   * 
   * @param programs Programs to Copy
   */
  public void copyProgramsToSystem(Program[] programs) {
    String param = mSettings.getProperty("ParamToUse", DEFAULT_PARAM);

    StringBuffer result = new StringBuffer();
    ParamParser parser = new ParamParser();

    int i = 0;

    while (!parser.hasErrors() && (i < programs.length)) {
      String prgResult = parser.analyse(param, (Program) programs[i]);
      result.append(prgResult);
      i++;
    }

    if (parser.hasErrors()) {
      JOptionPane.showMessageDialog(UiUtilities.getLastModalChildOf(getParentFrame()), parser.getErrorString(),
          "Error", JOptionPane.ERROR_MESSAGE);
    } else {
      Clipboard clip = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
      clip.setContents(new StringSelection(result.toString()), null);
    }
  }

  /*
   * (non-Javadoc)
   * @see devplugin.Plugin#getMarkIconFromTheme()
   */
  @Override
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("actions", "edit-paste", 16);
  }
}