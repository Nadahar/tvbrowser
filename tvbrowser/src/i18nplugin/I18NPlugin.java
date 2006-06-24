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

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;

import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.ThemeIcon;
import devplugin.Version;

/**
 * This Plugin should help a User to create Translations for the TV-Browser 
 * @author bodum
 */
public class I18NPlugin extends Plugin {
  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(I18NPlugin.class);

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
      dialog = new TranslationDialog((JDialog)wnd);
    } else {
      dialog = new TranslationDialog((JFrame)wnd);
    }
    
    UiUtilities.centerAndShow(dialog);
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