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


package printplugin.printer;

import java.util.ArrayList;

import devplugin.Program;


public abstract class AbstractColumnModel implements ColumnModel {
  
  private ArrayList<Program> mPrograms;


  public AbstractColumnModel(Program[] progs) {
    mPrograms = new ArrayList<Program>();
    setPrograms(progs);
  }

  public AbstractColumnModel() {
    this(new Program[]{});
  }

  public void setPrograms(Program[] progs) {
    for (Program prog : progs) {
      mPrograms.add(prog);
    }
  }

  public Program getProgramAt(int inx) {
    if (inx>=0 && inx<mPrograms.size()) {
      return mPrograms.get(inx);
    }
    return null;
  }

  public void addProgram(Program prog) {
    mPrograms.add(prog);
  }


  public int getProgramCount() {
    return mPrograms.size();
  }

  
}