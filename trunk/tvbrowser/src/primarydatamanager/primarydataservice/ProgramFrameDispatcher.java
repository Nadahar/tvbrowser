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

import java.util.*;
import java.io.*;

public class ProgramFrameDispatcher {
  
  private String mDirectory;
  private DayProgramFile mCurFile=null;
  private String mCurFilename;
  private int mCurID=0;
  private Channel mChannel;
  
  private HashMap mDayPrograms;
  
  /**
     * @deprecated
     */  
  public ProgramFrameDispatcher(String directory) {
    mDirectory=directory;    
  }
  
  public ProgramFrameDispatcher(Channel channel) {
    mChannel=channel;
    mDayPrograms=new HashMap();
  }
  
  
  
  public void dispatchProgramFrame(ProgramFrame frame, devplugin.Date date)  {
    
    if (frame==null) {
      return;
    }
    DayProgramFile file=(DayProgramFile)mDayPrograms.get(date);
    if (file==null) {
      file=new DayProgramFile(date,mChannel);
      mDayPrograms.put(date,file);
    }
    file.addProgramFrame(frame);   
    //dumpFrame(frame);    
    
  }
  
  private void dumpFrame(ProgramFrame progFrame) {
    ProgramField timeField=progFrame.getProgramFieldOfType(ProgramFieldType.START_TIME_TYPE);
    ProgramField titleField=progFrame.getProgramFieldOfType(ProgramFieldType.TITLE_TYPE);
    int time=timeField.getTimeData();
    System.out.println((time/60)+":"+(time%60)+": "+titleField.getTextData());
  }
  
  public void store(String directory) throws FileFormatException, IOException {
    Iterator it=mDayPrograms.values().iterator();
    while (it.hasNext()) {
      DayProgramFile f=(DayProgramFile)it.next();
      int cnt=f.getProgramFrameCount();
      for (int i=0;i<cnt;i++) {
        ProgramFrame frame=f.getProgramFrameAt(i);
        frame.setId(i);
        //dumpFrame(frame);
      }
      
      
      f.writeToFile(new File(directory,f.getProgramFileName()));      
    }
  }
  
  
  /**
     * @deprecated
     */  
  
  public void dispatch(ProgramFrame frame, devplugin.Date date, Channel channel) throws IOException, FileFormatException {
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
  
  /**
     * @deprecated
     */  
  public void flush() throws IOException, FileFormatException {
    mCurFile.writeToFile(new File(mDirectory,mCurFilename));    
  }
  
}

