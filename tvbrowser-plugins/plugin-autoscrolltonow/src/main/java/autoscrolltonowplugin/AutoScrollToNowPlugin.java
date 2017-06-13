/*
 * AutoScrollToNowPlugin
 * Copyright (C) 2012 René Mach (rene@tvbrowser.org)
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
 */
package autoscrolltonowplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;

import util.io.IOUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import compat.PluginCompat;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * A plugin that scrolls the program table automatically
 * repeatedly to now after a given time range.
 * <p>
 * @author René Mach
 */
public class AutoScrollToNowPlugin extends Plugin {
  private Properties mSettings;
  private Timer mTimer;
  
  public AutoScrollToNowPlugin() {
    mSettings = new Properties();
  }
  
  public static Version getVersion() {
    return new Version(0, 99, 1, false);
  }
  
  public PluginInfo getInfo() {
    return new PluginInfo(AutoScrollToNowPlugin.class, "Auto Scroll To Now", "Automatically scrolls to now after a given time range.", "Ren\u00e9 Mach", "GPL");
  }
  
  public void loadSettings(Properties settings) {
    mSettings = settings;
  }
  
  public Properties storeSettings() {
    return mSettings;
  }
  
  public SettingsTab getSettingsTab() {
    return new SettingsTab() {
      private JSpinner mTimeSpiner;
      
      @Override
      public void saveSettings() {
        mSettings.put("time", String.valueOf(mTimeSpiner.getValue()));
        mTimer.setDelay(Integer.parseInt(mSettings.getProperty("time", "30"))*60000);
      }
      
      @Override
      public String getTitle() {
        return getInfo().getName();
      }
      
      @Override
      public Icon getIcon() {
        return null;
      }
      
      @Override
      public JPanel createSettingsPanel() {
        mTimeSpiner = new JSpinner(new SpinnerNumberModel(Integer.parseInt(mSettings.getProperty("time", "30")), 10, 240, 10));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default,3dlu,default,3dlu,default","5dlu,default"));
        CellConstraints cc = new CellConstraints();
        
        pb.addLabel("Automatically scroll to now after", cc.xy(2,2));
        pb.add(mTimeSpiner, cc.xy(4,2));
        pb.addLabel("minutes.", cc.xy(6, 2));
        
        return pb.getPanel();
      }
    };
  }
  
  public void onActivation() {
    mTimer = new Timer(Integer.parseInt(mSettings.getProperty("time", "30"))*60000, new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        getPluginManager().goToDate(Date.getCurrentDate());
        
        try {
          Method scrollToTime = getPluginManager().getClass().getMethod("scrollToTime", new Class[] {int.class,boolean.class});
          scrollToTime.invoke(getPluginManager(), new Object[] {IOUtilities.getMinutesAfterMidnight(),false});
        } catch (Exception e1) {
          getPluginManager().scrollToTime(IOUtilities.getMinutesAfterMidnight());
        }
      }
    });
    mTimer.start();
  }
  
  public void onDeactivation() {
    mTimer.stop();
  }
  
  public String getPluginCategory() {
    return PluginCompat.CATEGORY_OTHER;
  }
}
