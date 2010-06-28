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
package tvbrowser.core.plugin;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

/**
 * A ActionProxy handles Erros throwns by Plugins and enables the User to
 * deactivate those Plugins
 * 
 * @author bodum
 *
 */
public class ActionProxy implements Action {
  /** Action to use*/
  private Action mAction;
  /** PluginProxy to use */
  private PluginProxy mPluginProxy;
  
  /**
   * Create the Proxy
   * 
   * @param pluginProxy PluginProxy to use
   * @param action Action that gets a better Error-Handling
   */
  public ActionProxy(PluginProxy pluginProxy, Action action) {
    mPluginProxy = pluginProxy;
    mAction = action;
  }

  public Object getValue(String key) {
    return mAction.getValue(key);
  }

  public void putValue(String key, Object value) {
    mAction.putValue(key, value);
  }

  public void setEnabled(boolean b) {
    mAction.setEnabled(b);
  }

  public boolean isEnabled() {
    return mAction.isEnabled();
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    mAction.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    mAction.removePropertyChangeListener(listener);
  }

  public void actionPerformed(ActionEvent e) {
    try {
      mAction.actionPerformed(e);
    } catch (RuntimeException ex) {
      mPluginProxy.handlePluginException(ex);
      
    }
  }

}
