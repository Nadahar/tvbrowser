/*
 * RememberMe Plugin
 * Copyright (C) 2013 René Mach (rene@tvbrowser.org)
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
package rememberme;

import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import util.ui.Localizer;

import devplugin.Date;
import devplugin.Program;
import devplugin.ProgramFieldType;

public class RememberedProgram implements Comparable<RememberedProgram> {
  private Program mProgram;
  private Date mProgramDate;
  private String mProgramTitle;
  private String mEpisodeTitle;
  private String mProgramId;
  private String mTag;
  
  public RememberedProgram(Program prog, String tag) {
    mProgram = prog;
    mProgramId = prog.getUniqueID();
    mProgramDate = prog.getDate();
    mProgramTitle = prog.getTitle();
    mEpisodeTitle = prog.getTextField(ProgramFieldType.EPISODE_TYPE);
    mTag = tag;
  }
  
  public static RememberedProgram readData(ObjectInputStream in, int version, RememberMe rMe) throws IOException, ClassNotFoundException {
    RememberedProgram load = new RememberedProgram(in, version, rMe);
    
    return load;
  }
  
  private RememberedProgram(ObjectInputStream in, int version, RememberMe rMe) throws IOException, ClassNotFoundException {
    mProgramId = in.readUTF();
    mProgramTitle = in.readUTF();
    mProgramDate = Date.readData(in);
    
    if(in.readBoolean()) {
      mEpisodeTitle = in.readUTF();
    }
    
    mProgram = RememberMe.getPluginManager().getProgram(mProgramId);
    
    if(mProgram != null) {
      mProgram.mark(rMe);
      rMe.getRootNode().addProgram(mProgram);
    }
    
    if(version > 1) {
      mTag = in.readUTF();
    }
    else {
      mTag = "";
    }
  }
  
  public void save(ObjectOutputStream out, int version) throws IOException {
    out.writeUTF(mProgramId);
    out.writeUTF(mProgramTitle);
    mProgramDate.writeData((DataOutput)out);
    
    out.writeBoolean(mEpisodeTitle != null);
    
    if(mEpisodeTitle != null) {
      out.writeUTF(mEpisodeTitle);
    }
    
    out.writeUTF(mTag);
  }
  
  public boolean hasProgram() {
    return mProgram != null;
  }
  
  public String getTitle() {
    return mProgramTitle;
  }
  
  public String getEpisodeTitle() {
    return mEpisodeTitle;
  }
  
  public String getTag() {
    return mTag;
  }
  
  public String toString() {
    StringBuilder builder = new StringBuilder("<html><b>");
    
    String date = mProgramDate.toString();
    
    if(mProgramDate.compareTo(Date.getCurrentDate()) == 0) {
      date = Localizer.getLocalization(Localizer.I18N_TODAY);
    }
    else if(mProgramDate.compareTo(Date.getCurrentDate().addDays(-1)) == 0) {
      date = Localizer.getLocalization(Localizer.I18N_YESTERDAY);
    }
    
    builder.append(date).append(" - ");
    builder.append(mProgramTitle);
    
    if(mTag != null && mTag.trim().length() > 0) {
      builder.append(": \"");
      builder.append(mTag);
      builder.append("\"");
    }
    
    builder.append("</b>");
    
    if(mEpisodeTitle != null) {
      builder.append("<br><i>");
      builder.append(mEpisodeTitle);
      builder.append("</i>");
    }
    
    builder.append("</html>");
    
    return builder.toString();
  }

  @Override
  public int compareTo(RememberedProgram otherProgram) {
    int value = mProgramDate.compareTo(otherProgram.mProgramDate);
    
    if(value == 0) {
      value = mProgramTitle.compareToIgnoreCase(otherProgram.mProgramTitle);
    }
    
    return value;
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof Program) {
      if(mProgram != null) {
        return mProgram.equals(obj);
      }
      else {
        return mProgramId.equals(((Program)obj).getUniqueID());
      }
    }
    else if(obj instanceof RememberedProgram) {
      return mProgramId.equals(((RememberedProgram)obj).mProgramId) && mTag.equals(((RememberedProgram)obj).mTag);
    }
    
    return this == obj;
  }
  
  public Date getDate() {
    return mProgramDate;
  }
  
  public String getUniqueID() {
    return mProgramId;
  }
  
  public boolean isOnAir() {
    return mProgram != null && mProgram.isOnAir();
  }
  
  public boolean isExpired() {
    return mProgram == null || mProgram.isExpired();
  }
  
  public boolean isValid() {
    return mProgramDate.compareTo(Date.getCurrentDate().addDays(-14)) >= 0 && !(mProgram == null && mProgramDate.compareTo(Date.getCurrentDate()) >= 0);
  }
  
  public void unmark(RememberMe rMe) {
    if(mProgram != null) {
      mProgram.unmark(rMe);
      rMe.getRootNode().removeProgram(mProgram);
      rMe.getRootNode().update();
    }
  }
}
