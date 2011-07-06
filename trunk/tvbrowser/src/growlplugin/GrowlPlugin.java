/*
 * GrowlPlugin by Bodo Tasche
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
package growlplugin;

import java.util.Properties;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import util.ui.ImageUtilities;
import util.ui.Localizer;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * This is the Growl-Plugin
 * 
 * It sends Growl-Notifications for each Program it receives.
 *
 * For more Details look at http://growl.info
 * 
 * @author bodum
 */
public class GrowlPlugin extends Plugin {
  private static final String GROWL_TARGET_ID = "growlnotify";

  private static final Version mVersion = new Version(3,0);
  
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(GrowlPlugin.class);
  /** The Growl-Container */
  private GrowlContainer mContainer;

  private GrowlSettings mSettings;

  private final ImageIcon mIcon = new ImageIcon(ImageUtilities
      .createImageFromJar("growlplugin/growlclaw.png", GrowlSettingsTab.class));

  /**
   * Checks the OS and initializes the System accordingly.
   * 
   */
  public GrowlPlugin() {
  }
  
  public PluginInfo getInfo() {
    final String name = mLocalizer.msg("pluginName", "Growl Notification");
    final String desc = mLocalizer.msg("description",
              "Sends all received Programs to Growl.");
    final String author = "Bodo Tasche";
      return new PluginInfo(this.getClass(), name, desc, author);
  }

  public static Version getVersion() {
    return mVersion;
  }


  @Override
  public boolean canReceiveProgramsWithTarget() {
      return true;
  }

  @Override
  public boolean receivePrograms(final Program[] programArr,
      final ProgramReceiveTarget receiveTarget) {
    if (receiveTarget.getTargetId().equals(GROWL_TARGET_ID)) {
      for (Program program : programArr) {
        mContainer.notifyGrowl(mSettings, program);
      }
      return true;
    }
    return false;
  }

  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return new ProgramReceiveTarget[] { new ProgramReceiveTarget(this,
        mLocalizer.msg("pluginName", "Growl Notification"), GROWL_TARGET_ID) };
  }

  public SettingsTab getSettingsTab() {
    return new GrowlSettingsTab(this, mSettings);
  }
  
  public void loadSettings(final Properties settings) {
    mSettings = new GrowlSettings(settings);
  }
  
  /**
   * Return the GrowlContainer that is currently used
   * @return GrowlContainer that is used
   */
  public GrowlContainer getContainer() {
    return mContainer;
  }
  
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }

  public Icon[] getMarkIconsForProgram(final Program program) {
    return new Icon[] {mIcon};
  }

  protected Icon getPluginIcon() {
    return mIcon;
  }

  @Override
  public void onActivation() {
    mContainer = new GrowlContainer();
    mContainer.registerApplication();
  }

  @Override
  public void onDeactivation() {
    mContainer = null;
  }
  
  public String getPluginCategory() {
    return Plugin.REMOTE_CONTROL_SOFTWARE_CATEGORY;
  }

}