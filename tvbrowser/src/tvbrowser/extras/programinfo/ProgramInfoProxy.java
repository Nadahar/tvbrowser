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
 *     $Date: 2006-06-13 12:02:39 +0200 (Di, 13 Jun 2006) $
 *   $Author: ds10 $
 * $Revision: 2495 $
 */
package tvbrowser.extras.programinfo;

import javax.swing.Icon;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.common.AbstractInternalPluginProxy;
import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Program;
import devplugin.SettingsItem;
import devplugin.SettingsTab;

/**
 * Encapsulates the ProgramInfo and manages the access to it.
 *
 * @author René Mach
 */
public class ProgramInfoProxy extends AbstractInternalPluginProxy implements ContextMenuIf {

  private static ProgramInfoProxy mInstance;

  private ProgramInfoProxy() {
    mInstance = this;
  }

  /**
   * @return The instance of the ProgramInfoProxy
   */
  public static synchronized ProgramInfoProxy getInstance() {
    if(mInstance == null) {
      new ProgramInfoProxy();
    }

    return mInstance;
  }

  public ActionMenu getContextMenuActions(Program program) {
    return ProgramInfo.getInstance().getContextMenuActions(program);
  }

  public String getId() {
    return ProgramInfo.getProgramInfoPluginId();
  }

  public String toString() {
    return ProgramInfo.getName();
  }

  public String getButtonActionDescription() {
    return ProgramInfo.mLocalizer.msg("description","Shows details about a program.");
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("actions", "edit-find", 16);
  }

  public String getName() {
    return toString();
  }

  public String getSettingsId() {
    return SettingsItem.PROGRAMINFO;
  }

  public SettingsTab getSettingsTab() {
    return new ProgramInfoSettingsTab();
  }

  @Override
  public void handleTvDataUpdateFinished() {
    // nothing to do
  }
}
