/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
 *     $Date: 2010-08-16 08:23:45 +0200 (Mo, 16. Aug 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6704 $
 */
package devplugin;

import java.util.ArrayList;

/**
 * PluginCommunication is the superclass for all classes that
 * are granting access to a Plugin. This class should only contain public
 * methods, that are at best self describing in name to use them with
 * reflection from other Plugins.
 * <p>
 * You are free to provide any access to your Plugin (not for DataSevices).
 * Provide it in that way that any Plugin using this class to connect to your
 * Plugin can be sure not to run in any unexpected situation.
 * <p>
 * If you are using this class to connect to another Plugin take nothing
 * for granted, every new Plugin version of the Plugin you want to connect to
 * can make changes to the class or even remove support for it (you can take that
 * personally because it's really bad style to just remove functions without prior
 * warning)
 * 
 * @author René Mach
 * @since 3.3.4
 */
public abstract class PluginCommunication {
  /**
   * Always increase the version number if you make changes
   * to this communication class. Otherwise another Plugin may
   * be expecting methods that are not present anymore.
   * <p>
   * @return The version number of this communication class.
   */
  public abstract int getVersion();
  
  /**
   * Always provide information about discountinued methods.
   * You make it safer to use your communication class if you tell
   * the using Plugin, what methods that were previously included were removed
   * (all of them not just the ones from the last version).
   * <p>
   * @return An array with the names of the discountinued methods or
   * <code>null</code> if there are no discontinued methods.
   */
  public String[] getDiscontinuedMethodNames() {
    return null;
  }
  
  /**
   * You may want to make changes to the parameter of a method. In that case always
   * discontinue the old method and inform an accessing Plugin what method with which
   * parameters was discontinued. Also use this if you remove a method completely.
   * <p>
   * @param methodName The name of the method to get the parameter information.
   * @return A list of arrays with the parameters of the discontinued methods.
   */
  public ArrayList<Class<?>[]> getMethodParametersOfDiscontinuedMethod(String methodName) {
    return null;
  }
  
  /**
   * You may want to make changes to the return value of a method. In that case always
   * discontinue the old method and inform an accessing Plugin what method with which
   * return value was discontinued. Also use this if you remove a method completely.
   * <p>
   * @param methodName The name of the method to get the return value type information.
   * @param parameter An array with the parameters of the discontinued method to get the return value for.
   * @return A Class of the return value type.
   */
  public Class<?> getReturnValueOfDiscontinuedMethod(String methodName, Class<?>[] parameter) {
    return null;
  }
}
