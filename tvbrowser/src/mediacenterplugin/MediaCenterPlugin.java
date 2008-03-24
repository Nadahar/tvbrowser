/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package mediacenterplugin;

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
import devplugin.Version;


/**
 * The MediaCenter Plugin
 * 
 * @author bodum
 */
public class MediaCenterPlugin extends Plugin {

  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(MediaCenterPlugin.class);

  /** Properties */
  private Properties mProperties;

  /** Is the MediaCenter visible at the moment ? */
  private boolean mMediaCenterShowing = false;
  
  /**
   * Create the Plugin
   */
  public MediaCenterPlugin() {
    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#getInfo()
   */
  public PluginInfo getInfo() {
    String name = mLocalizer.msg("pluginName", "MediaCenter");
    String desc = mLocalizer.msg("description", "Show a EPG-Styled Window");
    String author = "Bodo Tasche";
    return new PluginInfo(name, desc, author, new Version(0, 1));
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#getButtonAction()
   */
  public ActionMenu getButtonAction() {

    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        showMediaCenter();
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("contextMenuText", "Show MediaCenter"));
    action.putValue(Action.SMALL_ICON, new ImageIcon(ImageUtilities.createImageFromJar("mediacenterplugin/images/krdc16.png",
        MediaCenterPlugin.class)));
    action.putValue(BIG_ICON, new ImageIcon(ImageUtilities.createImageFromJar("mediacenterplugin/images/krdc24.png",
        MediaCenterPlugin.class)));

    return new ActionMenu(action);
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#loadSettings(java.util.Properties)
   */
  public void loadSettings(Properties settings) {
    mProperties = settings;
  }

  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#storeSettings()
   */
  public Properties storeSettings() {
    return mProperties;
  }

  /**
   * If the MediaPanel is not visible, it will be shown in a separate Thread
   */
  private void showMediaCenter() {
    if (!mMediaCenterShowing) {
      mMediaCenterShowing = true;
      
      new Thread(
      new Runnable() {

        public void run() {
          @SuppressWarnings("unused")
          MediaCenterFrame frame = new MediaCenterFrame(MediaCenterPlugin.this, 800, 600, false);
          mMediaCenterShowing = false;
        }
        
      }).start();
    }
  }
  
}