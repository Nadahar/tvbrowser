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
 *     $Date: 2006-06-05 21:02:43 +0200 (Mo, 05 Jun 2006) $
 *   $Author: darras $
 * $Revision: 2466 $
 */
package i18nplugin;

import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.ThemeIcon;
import devplugin.Version;
import util.ui.Localizer;
import util.ui.UiUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.util.Properties;

/**
 * This Plugin should help a User to create Translations for the TV-Browser 
 * 
 * Attention:   This Plugin uses some Core-Stuff, but "normal" Plugins are not allowed
 *              to do this !
 *              
 * @author bodum
 */
public class I18NPlugin extends Plugin {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(I18NPlugin.class);

  /** Instance of this Plugin */
  private static I18NPlugin mInstance;

  private Point mLocation = null;

  private Dimension mSize = null;
  
  private int mDevider = -1;
  
  /**
   * Contructor, stores current instance in static field
   */
  public I18NPlugin() {
    mInstance = this;
  }

  /**
   * @return Instance of this Plugin.
   */
  public static I18NPlugin getInstance() {
    return mInstance;
  }
  
  @Override
  public PluginInfo getInfo() {
    String name = mLocalizer.msg("pluginName", "I18NPlugin");
    String desc = mLocalizer.msg("description", "Tool for Translators");
    String author = "Bodo Tasche";
    return new PluginInfo(name, desc, author, new Version(0, 1));
  }

  @Override
  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {
      public void actionPerformed(ActionEvent evt) {
        openTranslationTool();
      }
    };
    action.putValue(Action.NAME, mLocalizer.msg("buttonName", "Open Translation Tool"));
    action.putValue(Action.SMALL_ICON, createImageIcon("apps", "preferences-desktop-locale", 16));
    action.putValue(BIG_ICON, createImageIcon("apps", "preferences-desktop-locale", 22));
    return new ActionMenu(action);
  }

  private void openTranslationTool() {
    TranslationDialog dialog;
    
    Window wnd = UiUtilities.getLastModalChildOf(getParentFrame());
    
    if (wnd instanceof JDialog) {
      dialog = new TranslationDialog((JDialog)wnd, mDevider);
    } else {
      dialog = new TranslationDialog((JFrame)wnd, mDevider);
    }
    
    dialog.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentMoved(ComponentEvent e) {
        mLocation = e.getComponent().getLocation(mLocation);
      }

      public void componentResized(ComponentEvent e) {
        mSize = e.getComponent().getSize(mSize);
      }
    });

    if (mSize != null) {
      dialog.setSize(mSize);
    }
    if (mLocation != null) {
      dialog.setLocation(mLocation);
      dialog.setVisible(true);
    } else
      UiUtilities.centerAndShow(dialog);
    
    mDevider = dialog.getDeviderLocation();
  }

  @Override
  public void loadSettings(Properties settings) {
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
    
    String devider = settings.getProperty("DialogDevider.Location");
    if (devider != null) {
        mDevider = parseNumber(devider);
    }
  }
  
  /**
   * Parses a Number from a String.
   * 
   * @param str
   *          Number in String to Parse
   * @return Number if successfull. Default is 0
   */
  public int parseNumber(String str) {

    try {
      return Integer.parseInt(str);
    } catch (Exception e) {
      // ignore
    }

    return 0;
  }
  
  @Override
  public Properties storeSettings() {
    Properties prop = new Properties();

    if (mLocation != null) {
      prop.setProperty("DialogLocation.X", Integer.toString(mLocation.x));
      prop.setProperty("DialogLocation.Y", Integer.toString(mLocation.y));
    }

    if (mSize != null) {
      prop.setProperty("DialogSize.Width", Integer.toString(mSize.width));
      prop.setProperty("DialogSize.Height", Integer.toString(mSize.height));
    }

    if (mDevider > 0) {
      prop.setProperty("DialogDevider.Location", Integer.toString(mDevider));
    }
    
    return prop;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see devplugin.Plugin#getMarkIconFromTheme()
   */
  @Override
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("apps", "preferences-desktop-locale", 16);
  }

}