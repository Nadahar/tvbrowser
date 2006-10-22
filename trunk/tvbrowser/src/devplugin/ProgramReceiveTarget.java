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
 */
package devplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Is a target for receiving of program from other plugins.
 * 
 * @author René Mach
 * @since 2.5
 */
public final class ProgramReceiveTarget {
  private String mReceiveIfId;
  private String mTargetId;
  private String mTargetName;

  /**
   * Creates the default target for a ProgramReceiveIf.
   * 
   * @param receiveIf The ProgramReceiveIf to create the null target for.
   * @return The default target for ProgramReceiveIf.
   */
  public static ProgramReceiveTarget[] createNullTargetArrayForProgramReceiveIf(ProgramReceiveIf receiveIf) {
    return new ProgramReceiveTarget[] {new ProgramReceiveTarget(receiveIf, "Standardziel", "NULL")};
  }


  /**
   * Creates the default target for an id od a ProgramReceiveIf.
   * 
   * @param receiveIfId The id of the ProgramReceiveIf to create for.
   * @return The default target for the id of the ProgramReceiveIf.
   */
  public static ProgramReceiveTarget createDefaultTargetForProgramReceiveIfId(String receiveIfId) {
    return new ProgramReceiveTarget(receiveIfId, "Standardziel", "NULL");
  }
  /**
   * Creates an instance of the ProgramReceiveTarget.
   * Use this to create a target from the plugin for the other plugins to read.
   * 
   * @param receiveIfId The ProgramReceiveIf id (your Plugin) to create for.
   * @param name The name of the target.
   * @param targetId The unique id of the target. 
   */
  private ProgramReceiveTarget(String receiveIfId, String name, String targetId) {
    mReceiveIfId = receiveIfId;
    mTargetName = name;
    mTargetId = targetId;
  }

  /**
   * Creates an instance of the ProgramReceiveTarget.
   * Use this to create a target from the plugin for the other plugins to read.
   * 
   * @param receiveIf The ProgramReceiveIf (your Plugin) to create for.
   * @param name The name of the target.
   * @param targetId The unique id of the target.
   */
  public ProgramReceiveTarget(ProgramReceiveIf receiveIf, String name, String targetId) {
    mReceiveIfId = receiveIf.getId();
    mTargetName = name;
    mTargetId = targetId;
  }
  
  /**
   * Creates an instance of the ProgramReceiveTarget.
   * Use this to load an target previously saved with {@link #writeData(ObjectOutputStream)}.
   * 
   * @param in The input stream.
   * @throws IOException
   * @throws ClassNotFoundException
   */
  public ProgramReceiveTarget(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt(); // version
    mReceiveIfId = in.readUTF();
    mTargetId = in.readUTF();
    mTargetName = in.readUTF();
  }
  
  /**
   * Use this to save a target.
   * 
   * @param out
   * @throws IOException
   */
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeInt(1); //version
    out.writeUTF(mReceiveIfId);
    out.writeUTF(mTargetId);
    out.writeUTF(mTargetName);
  }
  
  public String toString() {
    return mTargetName;
  }
  
  public String getReceiveIfId() {
    return mReceiveIfId;
  }

  public String getTargetId() {
      return mTargetId;
  }

  public boolean equals(Object o) {
    if(o == null)
      return false;
    else if(o instanceof ProgramReceiveTarget)
      return ((ProgramReceiveTarget)o).mReceiveIfId.compareTo(mReceiveIfId) == 0 && ((ProgramReceiveTarget)o).mTargetId.compareTo(mTargetId) == 0;

    return false;
  }
  
  public ProgramReceiveIf getReceifeIdOfTarget() {
    return Plugin.getPluginManager().getReceiceIfForId(mReceiveIfId);
  }
}
