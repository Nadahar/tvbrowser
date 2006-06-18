/*
 * SimpleMarkerPlugin by René Mach
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
 *   $Author: ds10 $
 * $Revision: 2537 $
 */
package simplemarkerplugin;

import java.util.Vector;

import devplugin.Program;

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
   * Constructor of this class.
   */
  public MarkListsVector() {
    addElement(new MarkList("Default"));
  }
  /**
   * @param p
   *          The Program to find.
   * @return True if a list contains the Program.
   */
  public boolean contains(Program p) {
    for (int i = 0; i < size(); i++) {
      if (elementAt(i).contains(p))
        return true;
    }
    return false;
  }

  /**
   * @param p
   *          The Program to find
   * @return The array of the names of the lists that containing p.
   */
  public String[] getNamesOfListsContainingProgram(Program p) {
    Vector<String> vec = new Vector<String>();

    for (int i = 0; i < size(); i++)
      if (elementAt(i).contains(p))
        vec.addElement(elementAt(i).getName());
    
    return vec.toArray(new String[vec.size()]);
  }

  /**
   * @param name
   *          The name of the requested list.
   * @return The list for the name.
   */
  public MarkList getListForName(String name) {
    for (int i = 0; i < size(); i++)
      if (elementAt(i).getName().equals(name))
        return elementAt(i);

    return null;
  }

  /**
   * @param name The name of the list to remove.
   */
  public void removeListForName(String name) {
    MarkList list = null;
    
    for(int i = 0; i < size(); i++)
      if(elementAt(i).getName().compareTo(name) == 0) {
        list = remove(i);
        break;
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
  public MarkList getListAt(int index) {
    return elementAt(index);
  }
  
  /**
   * 
   * @return The names of the MarkLists.
   */
  public String[] getMarkListNames() {    
    String[] names = new String[size()];
    
    for(int i = 0; i < names.length; i++)
      names[i] = elementAt(i).getName();
    
    return names;
  }
}
