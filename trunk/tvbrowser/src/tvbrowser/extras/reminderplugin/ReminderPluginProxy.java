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
package tvbrowser.extras.reminderplugin;

import javax.swing.Icon;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.ButtonActionIf;
import tvbrowser.extras.common.AbstractInternalPluginProxy;
import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Marker;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsItem;
import devplugin.SettingsTab;

/**
 * Encapsulates the Reminder and manages the access to it.
 *
 * @author René Mach
 * @since 2.5
 */
public class ReminderPluginProxy extends AbstractInternalPluginProxy implements ButtonActionIf, ContextMenuIf, Marker {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(ReminderPluginProxy.class);

  private static final String PROGRAM_TARGET_REMIND = "target_remind";
  private static ReminderPluginProxy mInstance;
  private Icon mMarkIcon;

  private ReminderPluginProxy() {
    mInstance = this;
  }

  /**
   * @return The instance of the ReminderPluginProxy
   */
  public static ReminderPluginProxy getInstance() {
    if(mInstance == null) {
      new ReminderPluginProxy();
    }

    return mInstance;
  }

  public ActionMenu getContextMenuActions(Program program) {
    return getReminderInstance().getContextMenuActions(program);
  }

  public String getId() {
    return ReminderPlugin.getReminderPluginId();
  }

  public String toString() {
    return ReminderPlugin.getName();
  }

  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
    getReminderInstance().addPrograms(programArr);
    return true;
  }

  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return new ProgramReceiveTarget[] { new ProgramReceiveTarget(this,
        mLocalizer.msg("programTarget", "Remind"), PROGRAM_TARGET_REMIND) };
  }

  public Icon getMarkIcon() {
    if(mMarkIcon == null) {
      mMarkIcon = IconLoader.getInstance().getIconFromTheme("apps", "appointment", 16);
    }

    return mMarkIcon;
  }

  public Icon[] getMarkIcons(Program p) {
    return new Icon[] {getMarkIcon()};
  }

  public int getMarkPriorityForProgram(Program p) {
    return getReminderInstance().getMarkPriority();
  }

  public String getButtonActionDescription() {
    return ReminderPlugin.mLocalizer.msg("description","The reminder function of TV-Browser.");
  }

  public Icon getIcon() {
    return getMarkIcon();
  }

  public String getName() {
    return toString();
  }

  public SettingsTab getSettingsTab() {
    return new ReminderSettingsTab();
  }

  public String getSettingsId() {
    return SettingsItem.REMINDER;
  }

  public ActionMenu getButtonAction() {
    return ReminderPlugin.getButtonAction();
  }

  public boolean receiveValues(String[] values,
      ProgramReceiveTarget receiveTarget) {
    return false;
  }

  @Override
  public void handleTvDataUpdateFinished() {
    getReminderInstance().handleTvDataUpdateFinished();
  }

  private static ReminderPlugin getReminderInstance() {
    return ReminderPlugin.getInstance();
  }
}
