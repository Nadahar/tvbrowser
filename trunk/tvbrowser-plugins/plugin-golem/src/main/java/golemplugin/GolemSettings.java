/*
 * Golem.de guckt - Plugin for TV-Browser
 * Copyright (C) 2010 Bodo Tasche
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
 *     $Date: 2010-02-20 13:09:24 +0100 (Sa, 20. Feb 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6530 $
 */
package golemplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import util.program.ProgramUtilities;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;

public class GolemSettings {
  private ArrayList<Program> mProgramList = new ArrayList<Program>();
  private boolean mMarkEnabled = true;
  private int mMarkPriority = Program.MAX_MARK_PRIORITY;
  private ArrayList<ProgramReceiveTarget> mReceiveTargets = new ArrayList<ProgramReceiveTarget>();

  public GolemSettings() {

  }

  public GolemSettings(ObjectInputStream in) throws IOException, ClassNotFoundException {
    readData(in);
  }

  public void addProgram(Program program) {
    if (!mProgramList.contains(program)) {
      mProgramList.add(program);
      GolemPlugin.getInstance().getRootNode().addProgram(program);
      if (isMarkEnabled()) {
        program.mark(GolemPlugin.getInstance());
      }

      for (ProgramReceiveTarget receiveTarget : mReceiveTargets) {
        receiveTarget.receivePrograms(new Program[]{program});
      }
    }
  }

  public Collection<Program> getProgramList() {
    Collections.sort(mProgramList, ProgramUtilities.getProgramComparator());
    return mProgramList;
  }

  private void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt(); // version, currently unused

    mMarkEnabled = in.readBoolean();
    mMarkPriority = in.readInt();

    int size = in.readInt();

    for (int i = 0; i < size; i++) {
      Date programDate = Date.readData(in);
      String progId = (String) in.readObject();
      Program program = Plugin.getPluginManager().getProgram(programDate, progId);
      if (program != null) {
        addProgram(program);
      }
    }

    size = in.readInt();
    for (int i = 0; i < size; i++) {
      ProgramReceiveTarget receiveTarget = new ProgramReceiveTarget(in);
      mReceiveTargets.add(receiveTarget);
    }
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version

    out.writeBoolean(mMarkEnabled);
    out.writeInt(mMarkPriority);

    out.writeInt(mProgramList.size());

    for (Program program : mProgramList) {
      program.getDate().writeData((java.io.DataOutput) out);
      out.writeObject(program.getID());
    }

    out.writeInt(mReceiveTargets.size());

    for (ProgramReceiveTarget receiveTarget : mReceiveTargets) {
      receiveTarget.writeData(out);
    }
  }

  public boolean isMarkEnabled() {
    return mMarkEnabled;
  }

  public void setMarkEnabled(boolean mark) {
    mMarkEnabled = mark;
  }

  public int getMarkPriority() {
    return mMarkPriority;
  }

  public void setMarkPriority(int prio) {
    mMarkPriority = prio;
  }

  public ProgramReceiveTarget[] getReceiveTargets() {
    return mReceiveTargets.toArray(new ProgramReceiveTarget[mReceiveTargets.size()]);
  }

  public void setReceiveTargets(ProgramReceiveTarget[] targets) {
    mReceiveTargets = new ArrayList<ProgramReceiveTarget>(Arrays.asList(targets));
  }

  public void resetPrograms() {
    mProgramList = new ArrayList<Program>();
  }
}
