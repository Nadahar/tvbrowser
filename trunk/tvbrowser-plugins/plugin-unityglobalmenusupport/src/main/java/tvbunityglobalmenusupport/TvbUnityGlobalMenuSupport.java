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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import util.io.IOUtilities;

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
  private File mJarFile;
  
  public TvbUnityGlobalMenuSupport() {
	File dir = new File(getPluginManager().getTvBrowserSettings().getTvBrowserUserHome(),"GlobalMenuSupport");
	   
    if(!dir.isDirectory()) {
      dir.mkdirs();
    }
   
    /*mJarFile = new File(dir,"jayatana-1.2.3.jar");
   
    try {
     byte[] jcomDll = IOUtilities.loadFileFromJar("/tvbunityglobalmenusupport/jayatana-1.2.3.jar", getClass());
     FileOutputStream out = new FileOutputStream(mJarFile);
     out.getChannel().truncate(0);
     out.write(jcomDll);
     out.close();
    } catch (IOException e) {
      e.printStackTrace();
      // ignore
    }*/
  }
  
  public static Version getVersion() {
    return new Version(0,11,false);
  }
  
  public PluginInfo getInfo() {
    return new PluginInfo(TvbUnityGlobalMenuSupport.class, "Unity Global Menu Support", "Moves menu of TV-Browser to the top panel.", "Ren\u00e9 Mach"."GPL");
  }
  
  public void handleTvBrowserStartFinished() {
    SwingUtilities.invokeLater(new Thread() {
      
      @Override
      public void run() {
        try {
          URL[] urls = new URL[] { mJarFile.toURI().toURL() };
            URLClassLoader classLoader = URLClassLoader.newInstance(urls, ClassLoader.getSystemClassLoader());
            
            Class<?> ayatanaDesktop = classLoader.loadClass("org.java.ayatana.AyatanaDesktop");
            Method isSupported = ayatanaDesktop.getMethod("isSupported", new Class<?>[0]);
            
            Object answer = isSupported.invoke(ayatanaDesktop, new Object[0]);
            
            if(answer instanceof Boolean) {
            if(((Boolean)answer).booleanValue()) {
                Class<?> applicationMenu = classLoader.loadClass("org.java.ayatana.ApplicationMenu");
                Method tryInstall = applicationMenu.getMethod("tryInstall", new Class<?>[] {JFrame.class});
                tryInstall.invoke(applicationMenu, (JFrame)getParentFrame());
            }
            else {
              System.out.println("GLOBAL MENU NOT SUPPORTED");
            }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
      }
    });
  }
}
