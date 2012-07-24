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
 *     $Date: 2010-06-28 19:33:48 +0200 (Mo, 28 Jun 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6662 $
 */
package primarydatamanager.primarydataservice;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import tvbrowserdataservice.file.DayProgramFile;
import tvbrowserdataservice.file.ProgramField;
import tvbrowserdataservice.file.ProgramFrame;
import util.io.FileFormatException;
import devplugin.Channel;
import devplugin.Date;
import devplugin.ProgramFieldType;

public class ProgramFrameDispatcher {
  
  private String mDirectory;
  private DayProgramFile mCurFile=null;
  private String mCurFilename;
  private int mCurID=0;
  private Channel mChannel;
  
  private HashMap<Date, DayProgramFile> mDayPrograms;
  
  /**
     * @deprecated
     */
  @Deprecated
  public ProgramFrameDispatcher(String directory) {
    mDirectory=directory;
  }
  
  public ProgramFrameDispatcher(Channel channel) {
    mChannel=channel;
    mDayPrograms=new HashMap<Date, DayProgramFile>();
  }
  
  
  /**
   * Gets the channel this dispatcher is responsible for.
   * 
   * @return The channel.
   */
  public Channel getChannel() {
    return mChannel;
  }
  
  
  public void dispatchProgramFrame(ProgramFrame frame, devplugin.Date date)  {
    if (frame == null) {
      throw new NullPointerException("frame is null");
    }
    if (date == null) {
      throw new NullPointerException("date is null");
    }

    DayProgramFile file=mDayPrograms.get(date);
    if (file==null) {
      file=new DayProgramFile(date,mChannel);
      mDayPrograms.put(date,file);
    }
    file.addDistinctProgramFrame(frame);
    //dumpFrame(frame);
    
  }
  
  private void dumpFrame(ProgramFrame progFrame) {
    ProgramField timeField=progFrame.getProgramFieldOfType(ProgramFieldType.START_TIME_TYPE);
    ProgramField titleField=progFrame.getProgramFieldOfType(ProgramFieldType.TITLE_TYPE);
    int time=timeField.getTimeData();
    System.out.println((time/60)+":"+(time%60)+": "+titleField.getTextData());
  }
  
  
  /**
   * Gets an iterator over all {@link DayProgramFile}s of the dispatcher.
   * 
   * @return An iterator over all DayProgramFiles.
   */
  public Iterator<DayProgramFile> getDayProgramFiles() {
    return mDayPrograms.values().iterator();
  }
  
  
  public void store(String directory) throws FileFormatException, IOException {
    Iterator<DayProgramFile> it = getDayProgramFiles();
    while (it.hasNext()) {
      DayProgramFile f=it.next();
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
   * @throws IOException
   * @throws FileFormatException
   * @deprecated Use {@link #dispatchProgramFrame(ProgramFrame, devplugin.Date)}
   *             instead.
   */
  @Deprecated
  public void dispatch(ProgramFrame frame, devplugin.Date date, Channel channel) throws IOException, FileFormatException {
    String country=channel.getCountry();
    String ch=channel.getId();
    
    if (mCurFile==null) {
      mCurID=0;
      mCurFile=new DayProgramFile();
      mCurFilename=DayProgramFile.getProgramFileName(date,country,ch);
      frame.setId(mCurID);
      mCurFile.addDistinctProgramFrame(frame);
    }
    else {
      frame.setId(mCurID);
      String fName=DayProgramFile.getProgramFileName(date,country,ch);
      if (fName.equals(mCurFilename)) {
        mCurFile.addDistinctProgramFrame(frame);
      }
      else {
        flush();
        mCurFile=new DayProgramFile();
        mCurFilename=DayProgramFile.getProgramFileName(date,country,ch);
        mCurFile.addDistinctProgramFrame(frame);
      }
      
      
    }
    mCurID++;
    
  }
  
  /**
   * @throws IOException
   * @throws FileFormatException
   * @deprecated
     */
  @Deprecated
  public void flush() throws IOException, FileFormatException {
    mCurFile.writeToFile(new File(mDirectory,mCurFilename));
  }
  
}

