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


 /**
  * TV-Browser
  * @author Martin Oberhauser
  */

package tvbrowser.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


class DateSelection implements java.io.Serializable {

    private HashMap map;
    public DateSelection() {
        map=new HashMap();
    }
    public void addProgram(devplugin.Program prog, devplugin.Plugin plugin) {
        Selection sel=(Selection)map.get(prog.getID());
        if (sel==null) {
            sel=new Selection(prog);
            sel.select(plugin);
            map.put(prog.getID(),sel);
        }else{
            sel.select(plugin);
        }
    }

    public Selection getSelection(devplugin.Program prog) {
        return (Selection)map.get(prog.getID());

    }

    public Object[] getSelections() {
        return map.values().toArray();
    }
}

public class ProgramSelection implements java.io.Serializable {

    private HashMap map;

    public ProgramSelection() {
        map=new HashMap();
    }

    public void addProgram(devplugin.Program prog, devplugin.Plugin plugin) {
        DateSelection dateSel=(DateSelection)map.get(prog.getDate().toString());
        if (dateSel==null) {
            dateSel=new DateSelection();
            dateSel.addProgram(prog,plugin);
            map.put(prog.getDate().toString(),dateSel);
        }else{
            dateSel.addProgram(prog,plugin);
        }
    }

    /**
     * returns Selections on the given day
     */
    public Object[] getSelections(devplugin.Date date) {
        if (date==null) return null;
        DateSelection dateSel=(DateSelection)map.get(date.toString());
        if (dateSel==null) {
            return null;
        }
        return dateSel.getSelections();
    }


}
