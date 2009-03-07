/*
 * Copyright Michael Keppler
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package timelistplugin;

import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import util.ui.ImageUtilities;
import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * @author bananeweizen
 *
 */
public final class TimeListPlugin extends Plugin {

  /**
   * plugin version
   */
  private static final Version PLUGIN_VERSION = new Version(2, 70, false);

  /**
   * localizer for this class
   */
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(TimeListPlugin.class);

  /**
   * option key for showing description of programs
   */
  private static final String SHOW_DESCRIPTION = "showDescription";

  /**
   * option key for showing expired programs
   */
  private static final String SHOW_EXPIRED = "showExpired";

  /**
   * current instance of the plugin
   */
  private static TimeListPlugin instance = null;

  /**
   * properties of the plugin
   */
  private Properties mSettings;

  public static Version getVersion() {
    return PLUGIN_VERSION;
  }

  public TimeListPlugin() {
    super();
    instance = this;
  }

  @Override
  public PluginInfo getInfo() {
    final String name = mLocalizer.msg("pluginName", "Time list");
    final String desc = mLocalizer.msg("pluginDescription",
        "Shows the available programs by time only");
    return new PluginInfo(TimeListPlugin.class, name, desc, "Michael Keppler",
        "GPL 3");
  }

  @Override
  public ActionMenu getButtonAction() {
    final AbstractAction action = new AbstractAction() {

      public void actionPerformed(final ActionEvent e) {
        showDialog();
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("buttonAction", "Time list"));
    action.putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities.createImageFromJar("timelistplugin/timelist-16.png", TimeListPlugin.class)));
    action.putValue(BIG_ICON, new ImageIcon(ImageUtilities.createImageFromJar("timelistplugin/timelist-22.png", TimeListPlugin.class)));
    action.putValue(Action.SHORT_DESCRIPTION, getInfo().getDescription());

    return new ActionMenu(action);
  }

  private void showDialog() {
    final ProgramListDialog dialog = new ProgramListDialog(getParentFrame());
    
    layoutWindow("programListDlg", dialog);
    
    dialog.setVisible(true);
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new TimeListSettingsTab(this);
  }

  /**
   * get option value: show program descriptions
   */
  protected boolean isShowDescriptions() {
    return Boolean.valueOf(mSettings.getProperty(SHOW_DESCRIPTION, "false"));
  }

  @Override
  public void loadSettings(final Properties settings) {
    mSettings = settings;
    if (mSettings == null) {
      mSettings = new Properties();
    }
  }

  @Override
  public Properties storeSettings() {
    return mSettings;
  }

  public void saveSettings(final boolean showDescription,
      final boolean showExpired) {
    mSettings.setProperty(SHOW_DESCRIPTION, String.valueOf(showDescription));
    mSettings.setProperty(SHOW_EXPIRED, String.valueOf(showExpired));
  }

  public static TimeListPlugin getInstance() {
    return instance;
  }

  /**
   * get option value: show expired programs
   */
  protected boolean isShowExpired() {
    return Boolean.valueOf(mSettings.getProperty(SHOW_EXPIRED, "false"));
  }

}
