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
package listviewplugin;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import util.ui.ImageUtilities;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * This Plugin shows a List of current running Programs
 * 
 * @author bodo
 */
public class ListViewPlugin extends Plugin {

    public static final int PROGRAMTABLEWIDTH = 200;
  
    /** Translator */
    private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(ListViewPlugin.class);

    /** Location of Dialog */
    private Point mLocation = null;
    /** Size of Dialog */
    private Dimension mSize = null;
    /** Settings */
    private Properties mSettings;
    
    /** Show at Startup */
    private boolean mShowAtStartup = false;
    
    /**
     * Creates the Plugin
     */
    public ListViewPlugin() {
    }

    /**
     * Returns Informations about this Plugin
     */
    public PluginInfo getInfo() {
        String name = mLocalizer.msg("pluginName", "View List Plugin");
        String desc = mLocalizer.msg("description", "Shows a List of current running Programs");
        String author = "Bodo Tasche";
        return new PluginInfo(name, desc, author, new Version(1, 41));
    }

    /**
     * Creates the Dialog
     */
    public void showDialog() {
        final ListViewDialog dlg = new ListViewDialog(getParentFrame(), this, mSettings);

        dlg.pack();
        dlg.addComponentListener(new java.awt.event.ComponentAdapter() {

            public void componentResized(ComponentEvent e) {
                mSize = e.getComponent().getSize();
            }

            public void componentMoved(ComponentEvent e) {
                e.getComponent().getLocation(mLocation);
            }
        });

        if ((mLocation != null) && (mSize != null)) {
            dlg.setLocation(mLocation);
            dlg.setSize(mSize);
            dlg.setVisible(true);
        } else {
            dlg.setSize(600, 600);
            UiUtilities.centerAndShow(dlg);
            mLocation = dlg.getLocation();
            mSize = dlg.getSize();
        }

    }

    /**
     * Icon to show for a marked program
     */
    public String getMarkIconName() {
        return "listviewplugin/listview16.gif";
    }


    /*
     *  (non-Javadoc)
     * @see devplugin.Plugin#getButtonAction()
     */
    public ActionMenu getButtonAction() {
        AbstractAction action = new AbstractAction() {

            public void actionPerformed(ActionEvent evt) {
                showDialog();
            }
        };
        action.putValue(Action.NAME, mLocalizer.msg("buttonName", "View Liste"));
        action.putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities.createImageFromJar("listviewplugin/listview16.gif", ListViewPlugin.class)));
        action.putValue(BIG_ICON, new ImageIcon(ImageUtilities.createImageFromJar("listviewplugin/listview24.gif", ListViewPlugin.class)));
        
        
        return new ActionMenu(action);
    }
    
    /**
     * Load the Settings
     */
    public void loadSettings(Properties settings) {
      
      if (settings == null ) {
        settings = new Properties();
      }
      
      String width = settings.getProperty("DialogSize.Width");
      String height = settings.getProperty("DialogSize.Height");
      
      if ((width != null) && (height != null)) {
          int w = parseNumber(width);
          int h = parseNumber(height);
          mSize = new Dimension(w, h);
      }

      String x = settings.getProperty("DialogLocation.X");
      String y = settings.getProperty("DialogLocation.Y");
      
      if ((x != null) && (y != null)) {
          int xv = parseNumber(x);
          int yv = parseNumber(y);
          mLocation = new Point(xv, yv);
      }
      
      mSettings = settings;

      mShowAtStartup = mSettings.getProperty("showAtStartup", "false").equals("true");
      
      if (mShowAtStartup) {
        waitForTVBrowserAndShowDialog();
      }
      
    }
    
    /**
     * Waits for the Main-Dialog and then shows the List
     */
    private void waitForTVBrowserAndShowDialog() {
      
      new Thread() {
        public void run() {
          while (getParentFrame() == null){
            try {
              sleep(200); 
            } catch (Exception e) {
              // TODO: handle exception
            }
          }
          
          SwingUtilities.invokeLater(new Runnable(){
            public void run() {
              showDialog();
            }
          });
        };
      }.start();
      
      
    }
    
    /**
     * Store the Settings
     */
    public Properties storeSettings() {
      
      if (mLocation != null) {
          mSettings.setProperty("DialogLocation.X", Integer.toString(mLocation.x));
          mSettings.setProperty("DialogLocation.Y", Integer.toString(mLocation.y));
      }
      
      if (mSize != null) {
          mSettings.setProperty("DialogSize.Width", Integer.toString(mSize.width));
          mSettings.setProperty("DialogSize.Height", Integer.toString(mSize.height));
      }

      return mSettings;
    }
    
    /**
     * Parses a Number from a String.
     * @param str Number in String to Parse
     * @return Number if successfull. Default is 0
     */
    public int parseNumber(String str) {
        
        try {
            int i = Integer.parseInt(str);
            return i;
        } catch (Exception e) {
            
        }
        
        return 0;
    }    
    
    public SettingsTab getSettingsTab() {
      return new ListViewSettings(mSettings);
    }
}