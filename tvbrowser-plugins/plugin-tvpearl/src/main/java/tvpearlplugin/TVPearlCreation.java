/*
 * TV-Pearl improvement by René Mach
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
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvpearlplugin;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import devplugin.Plugin;
import devplugin.Program;

public class TVPearlCreation {
  private Program mProgram;
  private AbstractPluginProgramFormating mFormating;
  private String mComment;
  private boolean mIsValid;
  
  public static TVPearlCreation readData(ObjectInputStream in, int version) throws IOException, ClassNotFoundException {    
    String uniqueID = in.readUTF();
    String comment = in.readUTF();
    AbstractPluginProgramFormating formating = AbstractPluginProgramFormating.readData(in);
    
    Program prog = Plugin.getPluginManager().getProgram(uniqueID);
    
    TVPearlCreation pearl = new TVPearlCreation(prog, formating);
    
    pearl.mComment = comment;
    
    return pearl;
  }
  
  public TVPearlCreation(Program prog, AbstractPluginProgramFormating formating) {
    mProgram = prog;
    mFormating = formating;
    mIsValid = prog != null && formating.isValid();
  }
  
  public boolean isValid() {
    return mIsValid;
  }
  
  public boolean equals(Object o) {
    if(o instanceof Program) {
      return mProgram.equals(o);
    }
    
    return o == this; 
  }
  
  public void setFormating(AbstractPluginProgramFormating formating) {
    mFormating = formating;
  }
  
  public String getFormatedText() {
    final ParamParser parser = new ParamParser();
    
    return parser.analyse(mFormating.getContentValue(), mProgram);
  }
  
  public AbstractPluginProgramFormating getFormating() {
    return mFormating;
  }
  
  public Program getProgram() {
    return mProgram;
  }
  
  public void setComment(String comment) {
    mComment = comment;
  }
  
  public String getComment() {
    return mComment == null ? "" : mComment;
  }
  
  public void writeData(ObjectOutputStream out) throws IOException {
    out.writeUTF(mProgram.getUniqueID());
    out.writeUTF(getComment());
    mFormating.writeData(out);
  }
}
