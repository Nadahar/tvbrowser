/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package tvbrowserdataservice.file;

import java.io.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Holds the common code of the file classes.
 * 
 * @author Til Schneider, www.murfman.de
 */
public abstract class AbstractFile {


  public void readFromFile(File file) throws IOException, FileFormatException {
    BufferedInputStream stream = null;
    try {
      stream = new BufferedInputStream(new FileInputStream(file), 0x4000);
      
      readFromStream(stream);
    }
    finally {
      if (stream != null) {
        try { stream.close(); } catch (IOException exc) {}
      }
    }
  }


  public abstract void readFromStream(InputStream stream)
    throws IOException, FileFormatException;


  public void writeToFile(File file) throws IOException, FileFormatException {
    // NOTE: We need two try blocks to ensure that the file is closed in the
    //       outer block.
    
    try {
      FileOutputStream stream = null;
      try {
        stream = new FileOutputStream(file);
        
        writeToStream(stream);
      }
      finally {
        // Close the file in every case
        if (stream != null) {
          try { stream.close(); } catch (IOException exc) {}
        }
      }
    }
    catch (IOException exc) {
      file.delete();
      throw exc;
    }
    catch (FileFormatException exc) {
      file.delete();
      throw new FileFormatException("Writing file failed "
        + file.getAbsolutePath(), exc);
    }
  }


  public abstract void writeToStream(OutputStream stream)
    throws IOException, FileFormatException;

}
