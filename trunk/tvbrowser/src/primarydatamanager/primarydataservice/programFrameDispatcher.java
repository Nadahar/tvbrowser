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
package primarydatamanager.primarydataservice;

import tvbrowserdataservice.file.*;
import devplugin.*;

import java.io.*;

public class ProgramFrameDispatcher {
  
  private String mDirectory;
  private DayProgramFile mCurFile=null;
  private String mCurFilename;
  private int mCurID=0;
  
  
  
  public ProgramFrameDispatcher(String directory) {
    mDirectory=directory;    
    Channel c;
  }
  
  public void dispatch(ProgramFrame frame, Date date, Channel channel) throws IOException, FileFormatException {
    String country=channel.getCountry();
    String ch=channel.getId();
    
    if (mCurFile==null) {
      mCurID=0;
      mCurFile=new DayProgramFile();
      mCurFilename=DayProgramFile.getProgramFileName(date,country,ch);
      frame.setId(mCurID);
      mCurFile.addProgramFrame(frame);      
    }
    else {
      frame.setId(mCurID);
      String fName=DayProgramFile.getProgramFileName(date,country,ch);
      if (fName.equals(mCurFilename)) {
        mCurFile.addProgramFrame(frame);
      }
      else {
        flush();
        mCurFile=new DayProgramFile();
        mCurFilename=DayProgramFile.getProgramFileName(date,country,ch);
        mCurFile.addProgramFrame(frame);
      }
      
      
    }
    mCurID++;
    
  }
  
  public void flush() throws IOException, FileFormatException {
    mCurFile.writeToFile(new File(mDirectory,mCurFilename));    
  }
  
}

