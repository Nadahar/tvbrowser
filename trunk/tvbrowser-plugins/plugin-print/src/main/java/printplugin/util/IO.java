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
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */

package printplugin.util;

import java.awt.Font;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import printplugin.settings.MutableProgramIconSettings;
import printplugin.settings.PrinterProgramIconSettings;
import printplugin.settings.ProgramIconSettings;
import devplugin.ProgramFieldType;


public class IO {

  public static ProgramIconSettings readProgramIconSettings(ObjectInputStream in) throws IOException, ClassNotFoundException {
    int version = in.readInt(); // version
    Font textFont = readFont(in);
    Font titleFont = readFont(in);
    boolean showPluginMarks = false;
    if (version > 1) {
      showPluginMarks = in.readBoolean();
    }
    int fieldCnt = in.readInt();
    ProgramFieldType[] fields = new ProgramFieldType[fieldCnt];
    for (int i=0; i<fields.length; i++) {
      fields[i] = ProgramFieldType.getTypeForId(in.readInt());
    }

    MutableProgramIconSettings result = new MutableProgramIconSettings(PrinterProgramIconSettings.create());
    result.setProgramInfoFields(fields);
    result.setTextFont(textFont);
    result.setTimeFont(titleFont);
    result.setTitleFont(titleFont);
    result.setPaintPluginMarks(showPluginMarks);
    return result;
  }

  public static void writeProgramIconSettings(ProgramIconSettings settings, ObjectOutputStream out) throws IOException {
    out.writeInt(2); // version
    writeFont(settings.getTextFont(), out);
    writeFont(settings.getTitleFont(), out);
    out.writeBoolean(settings.getPaintPluginMarks());

    ProgramFieldType[] fields = settings.getProgramInfoFields();
    out.writeInt(fields.length);
    for (ProgramFieldType field : fields) {
      out.writeInt(field.getTypeId());
    }
  }


  public static Font readFont(ObjectInputStream in) throws IOException, ClassNotFoundException {
    String name = (String)in.readObject();
    int size = in.readInt();
    int style = in.readInt();

    return new Font(name, style, size);
  }

  public static void writeFont(Font f, ObjectOutputStream out) throws IOException {
    out.writeObject(f.getName());
    out.writeInt(f.getSize());
    out.writeInt(f.getStyle());
  }
}
