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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package i18nplugin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;

import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * This Plugin should help a User to create Translations for the TV-Browser
 * 
 * Attention:   This Plugin uses some Core-Stuff, but "normal" Plugins are not allowed
 *              to do this !
 * 
 * @author bodum
 */
public class I18NPlugin extends Plugin {
  private static final Version mVersion = new Version(3,0);
  
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(I18NPlugin.class);

  /** Instance of this Plugin */
  private static I18NPlugin mInstance;
  
  private PluginInfo mPluginInfo;

  private I18NSettings mSettings;
  
  /**
   * Constructor, stores current instance in static field
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
  
  public static Version getVersion() {
    return mVersion;
  }
  
  @Override
  public PluginInfo getInfo() {
    if(mPluginInfo == null) {
      String name = mLocalizer.msg("pluginName", "I18NPlugin");
      String desc = mLocalizer.msg("description", "Tool for Translators");
      String author = "Bodo Tasche";
      mPluginInfo = new PluginInfo(I18NPlugin.class, name, desc, author);
    }
    
    return mPluginInfo;
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
    
    Window parent = UiUtilities.getLastModalChildOf(getParentFrame());
    dialog = new TranslationDialog(parent, mSettings.getDivider());
    
    layoutWindow("i18nDlg", dialog, new Dimension(800,750));
    
    dialog.setVisible(true);
    mSettings.setDivider(dialog.getDividerLocation());
  }

  @Override
  public void loadSettings(final Properties properties) {
    mSettings = new I18NSettings(properties);
  }
  
  @Override
  public Properties storeSettings() {
    return mSettings.storeSettings();
  }
  
  @Override
  public ThemeIcon getMarkIconFromTheme() {
    return new ThemeIcon("apps", "preferences-desktop-locale", 16);
  }
  
  protected Color getTranslationColor(int state) {
    if (state == LanguageNodeIf.STATE_MISSING_TRANSLATION) {
      return Color.RED;
    }
    else if (state >= LanguageNodeIf.STATE_NON_WELLFORMED && state != LanguageNodeIf.STATE_OK) {
      return Color.ORANGE.darker();
    }
    else {
      return Color.BLACK;
    }
    
  }

}