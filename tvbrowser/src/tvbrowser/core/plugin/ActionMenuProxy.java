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

import javax.swing.Action;

import devplugin.ActionMenu;

/**
 * This Class is a Proxy for ActionMenu's.
 * 
 * It capsulates Actions into ActionProxies. These ActionProxies will
 * show an Error Dialog that enables the User to deactive a malfuncioning Plugin
 * 
 * 
 * @author bodum
 *
 */
public class ActionMenuProxy extends ActionMenu {
  /** PluginProxy */
  private PluginProxy mPluginProxy;
  
  /**
   * Creates the Proxy
   * @param pluginProxy PluginProxy to use
   * @param actionMenu ActionMenu that is going to be encapsulated
   */
  public ActionMenuProxy(PluginProxy pluginProxy, ActionMenu actionMenu) {
    super(actionMenu);
    mPluginProxy = pluginProxy;
  }
  
  /**
   * Returns an ActionProxy containing the Action
   * @return Action
   */
  public Action getAction() {
    return new ActionProxy(mPluginProxy, super.getAction());
  }
  
  /**
   * Returns SubItems that are encapsulated by ActionMenuProxies
   */
  public ActionMenu[] getSubItems() {
    ActionMenu[] items =super.getSubItems();
    
    ActionMenu[] result = new ActionMenuProxy[items.length];
    
    for (int i=0;i<items.length;i++) {
      result[i] = new ActionMenuProxy(mPluginProxy, items[i]);
    }
    
    return result;
  }
}