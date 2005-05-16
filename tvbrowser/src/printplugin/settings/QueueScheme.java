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
    Font titleFont = settings.getTitleFont();
    Font descFont = settings.getDescriptionFont();

    out.writeInt(1);  // Version
    out.writeBoolean(emptyQueuAfterPrinting);
    writeFont(titleFont, out);
    writeFont(descFont, out);

  }




  public void read(ObjectInputStream in) throws IOException, ClassNotFoundException {
    in.readInt();  // version

    boolean emptyQueueAfterPrinting = in.readBoolean();
    Font titleFont = readFont(in);
    Font descFont = readFont(in);

    QueuePrinterSettings settings = new QueuePrinterSettings(emptyQueueAfterPrinting, titleFont, descFont);
    setSettings(settings);
  }


  private Font readFont(ObjectInputStream in) throws IOException, ClassNotFoundException {
    String name = (String)in.readObject();
    int size = in.readInt();
    int style = in.readInt();

    return new Font(name, style, size);
  }

  private void writeFont(Font f, ObjectOutputStream out) throws IOException {
    out.writeObject(f.getName());
    out.writeInt(f.getSize());
    out.writeInt(f.getStyle());
  }
}
