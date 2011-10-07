/*
 * SimpleMarkerPlugin by René Mach
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * SVN information:
 *     $Date: 2011-03-26 21:21:11 +0100 (Sa, 26 Mrz 2011) $
 *   $Author: bananeweizen $
 * $Revision: 6974 $
 */
package simplemarkerplugin;

import java.util.Vector;

import devplugin.Program;
import devplugin.ProgramReceiveTarget;

/**
 * SimpleMarkerPlugin 1.4 Plugin for TV-Browser since version 2.3
 * to only mark programs and add them to the Plugin tree.
 * 
 * (Formerly known as Just_Mark ;-))
 * 
 * A class that contains the MarkLists.
 * 
 * @author René Mach
 * 
 */
public class MarkListsVector extends Vector<MarkList> {

  private static final long serialVersionUID = 1L;

  /**
   * @param p
   *          The Program to find.
   * @return True if a list contains the Program.
   */
  protected boolean contains(Program p) {
    for (int i = 0; i < size(); i++) {
      if (elementAt(i).contains(p)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param p
   *          The Program to find
   * @return The array of the names of the lists that containing p.
   */
  protected String[] getNamesOfListsContainingProgram(Program p) {
    Vector<String> vec = new Vector<String>();

    for (int i = 0; i < size(); i++) {
      if (elementAt(i).contains(p)) {
        vec.addElement(elementAt(i).getName());
      }
    }
    
    return vec.toArray(new String[vec.size()]);
  }

  /**
   * @param name
   *          The name of the requested list.
   * @return The list for the name.
   */
  protected MarkList getListForName(String name) {
    for (int i = 0; i < size(); i++) {
      if (elementAt(i).getName().equals(name)) {
        return elementAt(i);
      }
    }

    return null;
  }

  /**
   * @param name The name of the list to remove.
   */
  protected void removeListForName(String name) {
    MarkList list = null;
    
    for(int i = 0; i < size(); i++) {
      if(elementAt(i).getName().compareTo(name) == 0) {
        list = remove(i);
        break;
      }
    }
    
    if (list != null) {
      Program[] programs = list.toArray(new Program[list.size()]);
      
      list.removeAllElements();
      
      SimpleMarkerPlugin.getInstance().revalidate(programs);
    }
  }
  
  /**
   * 
   * @param index The index of the list.
   * @return The list at the index.
   */
  protected MarkList getListAt(int index) {
    return elementAt(index);
  }
  
  /**
   * 
   * @return The names of the MarkLists.
   */
  public String[] getMarkListNames() {
    String[] names = new String[size()];
    
    for(int i = 0; i < names.length; i++) {
      names[i] = elementAt(i).getName();
    }
    
    return names;
  }
  
  /**
   * Gets the available ProgramReceiveTargets
   * 
   * @return Thr available ProgramReceiveTargets
   */
  public ProgramReceiveTarget[] getReceiveTargets() {
    ProgramReceiveTarget[] targets = new ProgramReceiveTarget[size()];
      
    for(int i = 0; i < size(); i++) {
      targets[i] = get(i).getReceiveTarget();
    }
      
    return targets;
  }
  
  /**
   * Gets the the MarkList of the given taget.
   * 
   * @param target The target to get the MarkList for.
   * @return The wanted MarkList or <code>null</code> if the
   *         list for the target was not found.
   */
  protected MarkList getMarkListForTarget(ProgramReceiveTarget target) {
    for(int i = 0; i < size(); i++) {
      if(get(i).getReceiveTarget().equals(target)) {
        return get(i);
      }
    }
    
    return null;
  }
  
  /**
   * @param id
   *          The id of the requested list.
   * @return The list for the name.
   */
  protected MarkList getListForId(String id) {
    for (int i = 0; i < size(); i++) {
      if (elementAt(i).getId().equals(id)) {
        return elementAt(i);
      }
    }

    return null;
  }
}
