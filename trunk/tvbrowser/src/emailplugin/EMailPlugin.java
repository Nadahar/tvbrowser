/*
 * EMailPlugin by Bodo Tasche
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
package emailplugin;

import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;

import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.SettingsTab;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * This Plugin makes it possible to send an email with short info's about the
 * Program
 * 
 * @author bodum
 */
public class EMailPlugin extends Plugin {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(EMailPlugin.class);

  /** Properties */
  private Properties mSettings;

  /** The Default-Parameters */
  public static final String DEFAULT_PARAMETER = "{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n";
  
  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#getInfo()
   */
  public PluginInfo getInfo() {
    String name = mLocalizer.msg("pluginName", "EMail export");
    String desc = mLocalizer.msg("description", "Send a EMail with an external Program");
    String author = "Bodo Tasche";
    return new PluginInfo(name, desc, author, new Version(0, 5));
  }

  /*
   * (non-Javadoc)
   * @see devplugin.Plugin#getMarkIconFromTheme()
   */
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("actions", "mail-message-new", 16);
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#getContextMenuActions(devplugin.Program)
   */
  public ActionMenu getContextMenuActions(final Program program) {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        Program[] programArr = { program };
        new MailCreator(EMailPlugin.this, mSettings).createMail(getParentFrame(), programArr);
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("contextMenuText", "Send via EMail"));
    action.putValue(Action.SMALL_ICON, createImageIcon("actions", "mail-message-new", 16)); 
    return new ActionMenu(action);
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#canReceivePrograms()
   */
  public boolean canReceivePrograms() {
    return true;
  }

  /**
   * This method is invoked for multiple program execution.
   * 
   * @see #canReceivePrograms()
   */
  public void receivePrograms(Program[] programArr) {
    new MailCreator(this, mSettings).createMail(getParentFrame(), programArr);
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#getSettingsTab()
   */
  public SettingsTab getSettingsTab() {

    EMailSettingsTab tab = new EMailSettingsTab(this, mSettings);

    return tab;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#loadSettings(java.util.Properties)
   */
  public void loadSettings(Properties settings) {
    mSettings = settings;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#storeSettings()
   */
  public Properties storeSettings() {
    return mSettings;
  }
}