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

import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class GolemSettings {
  private ArrayList<Program> programList = new ArrayList<Program>();
  private boolean markEnabled = true;
  private int markPriority = Program.MAX_MARK_PRIORITY;
  private ArrayList<ProgramReceiveTarget> receiveTargets = new ArrayList<ProgramReceiveTarget>();

  public GolemSettings() {

  }

  public GolemSettings(ObjectInputStream in) throws IOException, ClassNotFoundException {
    readData(in);
  }

  public void addProgram(Program p) {
    if (!programList.contains(p)) {
      programList.add(p);
      GolemPlugin.getInstance().getRootNode().addProgram(p);
      if (isMarkEnabled()) {
        p.mark(GolemPlugin.getInstance());
      }

      for (ProgramReceiveTarget t : receiveTargets) {
        t.receivePrograms(new Program[]{p});
      }
    }
  }

  public Collection<Program> getProgramList() {
    return programList;
  }

  private void readData(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt();

    markEnabled = in.readBoolean();
    markPriority = in.readInt();

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
      ProgramReceiveTarget r = new ProgramReceiveTarget(in);
      receiveTargets.add(r);
    }
  }

  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version

    out.writeBoolean(markEnabled);
    out.writeInt(markPriority);

    out.writeInt(programList.size());

    for (Program p : programList) {
      p.getDate().writeData((java.io.DataOutput) out);
      out.writeObject(p.getID());
    }

    out.writeInt(receiveTargets.size());

    for (ProgramReceiveTarget t : receiveTargets) {
      t.writeData(out);
    }
  }

  public boolean isMarkEnabled() {
    return markEnabled;
  }

  public void setMarkEnabled(boolean mark) {
    markEnabled = mark;
  }

  public int getMarkPriority() {
    return markPriority;
  }

  public void setMarkPriority(int prio) {
    markPriority = prio;
  }

  public ProgramReceiveTarget[] getReceiveTargets() {
    return receiveTargets.toArray(new ProgramReceiveTarget[receiveTargets.size()]);
  }

  public void setReceiveTargets(ProgramReceiveTarget[] targets) {
    receiveTargets = new ArrayList<ProgramReceiveTarget>(Arrays.asList(targets));
  }
}
