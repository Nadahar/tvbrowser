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
import java.awt.event.ComponentEvent;
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
import devplugin.PluginTreeNode;
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

  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {

      public void actionPerformed(ActionEvent evt) {
        showDialog();
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("pluginName", "Clipboard"));
    action.putValue(Action.SMALL_ICON, createImageIcon("actions", "edit-paste", 16));
    action.putValue(BIG_ICON, createImageIcon("actions", "edit-paste", 22));
    action.putValue(Action.SHORT_DESCRIPTION, mLocalizer.msg("pluginName", "Clipboard"));

    return new ActionMenu(action);
  }

  public ActionMenu getContextMenuActions(final Program program) {
    final PluginTreeNode node = getRootNode();
    final boolean inList = node.contains(program);

    ImageIcon img = createImageIcon("actions", "edit-paste", 16);

    AbstractAction addRemoveAction = new AbstractAction() {

      public void actionPerformed(ActionEvent evt) {
        if (inList) {
          node.removeProgram(program);
        } else {
          node.addProgram(program);
        }
        node.update();
      }
    };

    if (inList) {
      addRemoveAction.putValue(Action.NAME, mLocalizer.msg("contextMenuRemoveText", "Remove from Clipboard"));
    } else {
      addRemoveAction.putValue(Action.NAME, mLocalizer.msg("contextMenuAddText", "Add to Clipboard"));
    }

    AbstractAction copyToSystem = new AbstractAction(mLocalizer.msg("copyToSystem", "Copy to System-Clipboard")) {
      public void actionPerformed(ActionEvent evt) {
        Program[] list = { program };
        copyProgramsToSystem(list);
      }
    };
    copyToSystem.putValue(Action.SMALL_ICON, img);

    addRemoveAction.putValue(Action.SMALL_ICON, img);

    Action action = new devplugin.ContextMenuAction(mLocalizer.msg("contextMenu", "Clipboard"));
    action.putValue(Action.SMALL_ICON, img);

    Action[] actionList = { addRemoveAction, copyToSystem };

    return new ActionMenu(action, actionList);
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#getInfo()
   */
  public PluginInfo getInfo() {
    String name = mLocalizer.msg("pluginName", "Clipboard");
    String desc = mLocalizer.msg("description",
        "A internal Clipboard for receiving and sending Programs from/to other Plugins.");
    String author = "Bodo Tasche";
    return new PluginInfo(name, desc, author, new Version(0, 20));
  }

  /**
   * Creates the Dialog
   */
  public void showDialog() {

    PluginTreeNode node = getRootNode();
    if (node.isEmpty()) {
      JOptionPane.showMessageDialog(getParentFrame(), mLocalizer.msg("empty", "The Clipboard is empty."));
      return;
    }

    ClipboardDialog dlg = new ClipboardDialog(getParentFrame(), this, mSettings, node);

    dlg.pack();
    dlg.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        mDimensionListDialog = e.getComponent().getSize();
      }

      public void componentMoved(ComponentEvent e) {
        e.getComponent().getLocation(mLocationListDialog);
      }
    });

    if ((mLocationListDialog != null) && (mDimensionListDialog != null)) {
      dlg.setLocation(mLocationListDialog);
      dlg.setSize(mDimensionListDialog);
      dlg.setVisible(true);
    } else {
      dlg.setSize(400, 300);
      UiUtilities.centerAndShow(dlg);
      mLocationListDialog = dlg.getLocation();
      mDimensionListDialog = dlg.getSize();
    }

  }

  public boolean canReceivePrograms() {
    return true;
  }

  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("actions", "edit-paste", 16);
  }

  public void receivePrograms(Program[] programArr) {
    PluginTreeNode node = getRootNode();
    for (int i = 0; i < programArr.length; i++) {
      if (!node.contains(programArr[i])) {
        node.addProgram(programArr[i]);
      }
    }
    node.update();
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

  public boolean canUseProgramTree() {
    return true;
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

}