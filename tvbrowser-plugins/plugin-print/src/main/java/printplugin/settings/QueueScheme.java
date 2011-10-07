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
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mrz 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */

package printplugin.settings;

import java.awt.Font;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import printplugin.util.IO;


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
    IO.writeProgramIconSettings(settings.getProgramIconSettings(), out);
    IO.writeFont(settings.getDateFont(), out);
  }




  public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt();  // version

    boolean emptyQueueAfterPrinting = in.readBoolean();
    int columnsPerPage = in.readInt();
    ProgramIconSettings programIconSettings = IO.readProgramIconSettings(in);
    Font dateFont = IO.readFont(in);
    QueuePrinterSettings settings = new QueuePrinterSettings(emptyQueueAfterPrinting, columnsPerPage, programIconSettings, dateFont);
    setSettings(settings);
  }






}
