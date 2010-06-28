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
 *     $Date: 2006-06-05 21:02:43 +0200 (Mo, 05 Jun 2006) $
 *   $Author: darras $
 * $Revision: 2466 $
 */
package util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class contains some utility functions for handling zip files
 * 
 * @author bodum
 * 
 */
public class ZipUtil {

  /**
   * Creates a zip file and stores all files in a directory recursively
   * 
   * @param zipfile
   *          compress to this file
   * @param directory
   *          compress this directory
   * @throws IOException
   */
  public void zipDirectory(File zipfile, File directory) throws IOException {
    zipfile.delete();

    if (!directory.exists() || !directory.isDirectory()) {
      return;
    }

    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));

    zipDirFiles(out, directory, directory.getAbsolutePath().length()+1);

    out.close();
  }

  /**
   * Zip all Files
   * @param out use this outputstream
   * @param directory zip this directory and all it's childs
   * @param parentlength length of parent-directory name. This is stripped from the entries in the zip file
   * @throws IOException
   */
  private void zipDirFiles(ZipOutputStream out, File directory, int parentlength) throws IOException {
    File[] files = directory.listFiles();
    System.out.println(">" + directory.getAbsolutePath());
    // Compress the files
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          zipDirFiles(out, file, parentlength);
        } else {
          System.out.println(">" + file.getAbsolutePath());
          byte[] buf = new byte[1024];
  
          FileInputStream in = new FileInputStream(file);
  
          // Add ZIP entry to output stream.
          out.putNextEntry(new ZipEntry(file.getAbsolutePath().substring(parentlength)));
  
          // Transfer bytes from the file to the ZIP file
          int len;
          while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
          }
  
          // Complete the entry
          out.closeEntry();
          in.close();
        }
      }
    }
  }

}