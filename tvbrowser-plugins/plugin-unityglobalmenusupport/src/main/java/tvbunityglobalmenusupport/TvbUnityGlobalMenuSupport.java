/*
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
package tvbunityglobalmenusupport;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.java.ayatana.ApplicationMenu;
import org.java.ayatana.AyatanaDesktop;

import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Version;

/**
 * A plugin that moves the TV-Browser menu to the
 * global menu of the Unity desktop.
 * <p>
 * @author René Mach
 */
public class TvbUnityGlobalMenuSupport extends Plugin {  
  public static Version getVersion() {
    return new Version(0,12,false);
  }
  
  public PluginInfo getInfo() {
    return new PluginInfo(TvbUnityGlobalMenuSupport.class, "Unity Global Menu Support", "Moves menu of TV-Browser to the top panel.", "Ren\u00e9 Mach", "GPL");
  }
  
  public void handleTvBrowserStartFinished() {
    SwingUtilities.invokeLater(new Thread() {
      @Override
      public void run() {
    	if(AyatanaDesktop.isSupported()) {
          ApplicationMenu.tryInstall((JFrame)getParentFrame());
    	}
        else {
          System.out.println("GLOBAL MENU NOT SUPPORTED");
        }
      }
    });
  }
}
