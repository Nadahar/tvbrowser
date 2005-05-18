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

package printplugin.settings;

import devplugin.ProgramFieldType;

import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.awt.*;


public class QueueScheme extends Scheme {

  public QueueScheme(String name) {
    super(name);
  }

  public void store(ObjectOutputStream out) throws IOException {
    QueuePrinterSettings settings = (QueuePrinterSettings)getSettings();

    boolean emptyQueuAfterPrinting = settings.emptyQueueAfterPrinting();
    int columnsPerPage = settings.getColumnsPerPage();
    
    out.writeInt(1);  // Version
    out.writeBoolean(emptyQueuAfterPrinting);
    out.writeInt(columnsPerPage);

    writeProgramIconSettings(settings.getProgramIconSettings(), out);

  }




  public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt();  // version

    boolean emptyQueueAfterPrinting = in.readBoolean();
    int columnsPerPage = in.readInt();
    ProgramIconSettings programIconSettings = readProgramIconSettings(in);

    QueuePrinterSettings settings = new QueuePrinterSettings(emptyQueueAfterPrinting, columnsPerPage, programIconSettings);
    setSettings(settings);
  }


  private ProgramIconSettings readProgramIconSettings(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt(); // version
    Font textFont = readFont(in);
    Font titleFont = readFont(in);
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
    return result;
  }

  private void writeProgramIconSettings(ProgramIconSettings settings, ObjectOutputStream out) throws IOException {
    out.writeInt(1); // version
    writeFont(settings.getTextFont(), out);
    writeFont(settings.getTitleFont(), out);

    ProgramFieldType[] fields = settings.getProgramInfoFields();
    out.writeInt(fields.length);
    for (int i=0; i<fields.length; i++) {
      out.writeInt(fields[i].getTypeId());
    }
  }

  private static Font readFont(ObjectInputStream in) throws IOException, ClassNotFoundException {
      String name = (String)in.readObject();
      int size = in.readInt();
      int style = in.readInt();

    return new Font(name, style, size);
  }

    private static void writeFont(Font f, ObjectOutputStream out) throws IOException {
      out.writeObject(f.getName());
      out.writeInt(f.getSize());
      out.writeInt(f.getStyle());
    }

}
