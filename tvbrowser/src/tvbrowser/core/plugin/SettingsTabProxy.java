/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
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

package tvbrowser.core.plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JPanel;

import util.ui.FixedSizeIcon;
import devplugin.CancelableSettingsTab;
import devplugin.SettingsTab;


/**
 * This is a proxy class that catches exceptions originating in SettingsTab implementations.
 * 
 */
public class SettingsTabProxy {

  private static final Logger mLog = java.util.logging.Logger
       .getLogger(SettingsTabProxy.class.getName());


  private SettingsTab mSettingsTab;

  public SettingsTabProxy(SettingsTab tab) {
    mSettingsTab = tab;
  }

  public JPanel createSettingsPanel() {
    try {
      return mSettingsTab.createSettingsPanel();
    }catch(Throwable t) {
      mLog.log(Level.WARNING, "Could not get settings panel", t);
      return null;
    }
  }

  public void saveSettings() {
    try {
      mSettingsTab.saveSettings();
    }catch(Throwable t) {
      mLog.log(Level.WARNING, "Could not save settings", t);
    }
  }

  public Icon getIcon() {
    try {
      Icon icon = mSettingsTab.getIcon();
      if (icon != null) {
        return new FixedSizeIcon(16, 16, icon);
      }
      return icon;
    }catch(Throwable t) {
      mLog.log(Level.WARNING, "Could not get settings icon", t);
      return null;
    }
  }

  public String getTitle() {
    try {
      return mSettingsTab.getTitle();
    }catch(Throwable t) {
      mLog.log(Level.WARNING, "Could not get settings panel title", t);
      return "";
    }
  }
  
  public void cancel() {
    try {
      if(mSettingsTab instanceof CancelableSettingsTab) {
        ((CancelableSettingsTab)mSettingsTab).cancel();
      }
    }catch(Throwable t) {
      mLog.log(Level.WARNING, "Could not inform about closing", t);
    }
  }
}
